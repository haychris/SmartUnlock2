package hay.chris.smartunlock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothReceiver extends BroadcastReceiver {
	public BluetoothReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Boolean isApp = intent.getBooleanExtra("key", false);
		String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (isApp){
        	Log.e("BluetoothReceiver", "isApp");
    		BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }
        
        if (action != null){
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            //Device found
	        	Log.e("BluetoothReceiver", "device found");
	        	Toast.makeText(context, "device found", Toast.LENGTH_SHORT).show();
	        }
	        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
	            //Device is now connected
	        	Log.e("BluetoothReceiver", "device connected");
	        	Toast.makeText(context, "device connected", Toast.LENGTH_SHORT).show();
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
	            //Device is about to disconnect
	        	Log.e("BluetoothReceiver", "device disconnect request");
	        }
	        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
	            //Device has disconnected
	        	Log.e("BluetoothReceiver", "device disconnected");
	        	Toast.makeText(context, "device disconnected", Toast.LENGTH_SHORT).show();
	        }   
        }
	}
	
	private void enableBootReceiver(Context context){
		ComponentName receiver = new ComponentName(context, RebootReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}
	private void disableBootReceiver(Context context) {
		ComponentName receiver = new ComponentName(context, RebootReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}
	public void startTimer(Context context) {
		//cancels existing alarms
		cancelTimer(context);
		//creating alarm with intent
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, BluetoothReceiver.class);
		intent.putExtra("key", true);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		//setting timer length based on setting
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int timerPreference = Integer.parseInt(preferences.getString("bluetooth_scan_frequency", "-1")) * 1000;
		//TODO remove
		timerPreference = 30000;
		if (timerPreference > 0){
			enableBootReceiver(context);
			alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
										 SystemClock.elapsedRealtime() + timerPreference,
										 timerPreference, alarmIntent);
			//alarmMgr.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), timerPreference, alarmIntent);
		}
		else {
			disableBootReceiver(context);
		}
		Log.e("Timer", Integer.toString(timerPreference));
	}
	
	public void cancelTimer(Context context) {
		//creating alarm with intent
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, BluetoothReceiver.class);
		intent.putExtra("key", true);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		//cancels alarms with same intent filter
		alarmMgr.cancel(alarmIntent);
	}

	public class RebootReceiver extends BroadcastReceiver {
		public RebootReceiver() {
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
				// TODO: Set the alarm here.
				BluetoothReceiver br = new BluetoothReceiver();
				br.startTimer(context);
			}
		}
	}	
}
