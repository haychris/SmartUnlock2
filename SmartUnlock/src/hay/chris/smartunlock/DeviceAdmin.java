package hay.chris.smartunlock;

import hay.chris.smartunlock.MainActivity.TimerCondition;


import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class DeviceAdmin extends DeviceAdminReceiver {

	
	public DeviceAdmin() {
	}
	
	@Override
	public void onEnabled(Context context, Intent intent){
		super.onEnabled(context, intent);
	}

	@Override
	public void onDisabled(Context context, Intent intent){
		super.onDisabled(context, intent);
	}
	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		Log.e("test", "password entered successfully");
		SharedPreferences prefs = context.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
		Set<String> activeTimers = prefs.getStringSet("all_timers_active", new HashSet<String>());
		int[] timesInMillis = new int[activeTimers.size()];
		Log.e("test", "" + activeTimers.size());
		int count = 0;
		// go through every active timer and check if it responds to device login success
		// if so, check if all other conditions in its set are active
		// finally, get the shortest active timer and relock device after that amount of time
		for (String conditionName : activeTimers) {
			String data = prefs.getString(conditionName, "");
			TimerCondition timer = new TimerCondition(conditionName, data);
			if (timer.radio == 0 || timer.radio == 2 || timer.radio == 3) {
				// valid timer if radio = 0 and all other conditions are valid
				// relies on format of data where splitData[1] represents isValid boolean
				boolean validTimer = true;
				Set<String> activeConditionsInSet = prefs.getStringSet(timer.setName, new HashSet<String>());
				for (String otherConditionName : activeConditionsInSet) {
					String otherData = prefs.getString(otherConditionName, "");
					String[] splitData = otherData.split(";");
					if (Integer.parseInt(splitData[1]) == 0) validTimer = false;
				}
				if (validTimer) {
					switch(timer.radio){
						case 0:
							timesInMillis[count] = timer.time;
							switch(timer.timeType){
								case 0: timesInMillis[count] *= 60;
								case 1: timesInMillis[count] *= 60;
								case 2: timesInMillis[count] *= 1000;
								break; 
							}
							break;
							// sets to always unlock; priority over locking in future
						case 2: timesInMillis[count] = -1;
						break;
							// sets to never unlock; priority over locking in future and always unlocking
						case 3: timesInMillis[count] = -2;
						break;
					}
					count++;
				}
			}
		}
		if (count > 0) {
			int minTime = timesInMillis[0];
			for (int i = 1; i < count; i++){
				if (timesInMillis[i] < minTime) minTime = timesInMillis[i];
			}
			if (minTime > 0)
				resetPasswordAfterTime(context, minTime);
			if (minTime == -1) 
				getManager(context).resetPassword("", 0);
			if (minTime == -2) {
				Intent lockIntent = new Intent(context, LockUnReceiver.class);
				intent.putExtra("toLock", true);
				context.sendBroadcast(lockIntent);
			}
		}
	}
	@SuppressLint("NewApi")
	private void resetPasswordAfterTime(Context context, int timeInMillis) {
		getManager(context).resetPassword("", 0);
		//creating alarm with intent
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LockUnReceiver.class);
        intent.putExtra("toLock", true);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //TODO check if needs to wakeup
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        	alarmMgr.setWindow(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeInMillis, timeInMillis/10, alarmIntent);
        else alarmMgr.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + timeInMillis, alarmIntent);
	}
	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		
	}
	public void onReceive(Context context, Intent intent) {
		onPasswordSucceeded(context, intent);
	}
}
