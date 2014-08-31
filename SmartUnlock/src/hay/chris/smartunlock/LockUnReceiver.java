package hay.chris.smartunlock;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class LockUnReceiver extends BroadcastReceiver {
	private DevicePolicyManager mDPM;
	public LockUnReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Boolean toLock = bundle.getBoolean("toLock");
    	mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (toLock) {
			lockDevice(context);
		}
		else {
			unlockDevice();
		}
	}
	private void lockDevice(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String password = settings.getString("stored_password", "");
		mDPM.resetPassword(password, 0);
	}
	private void unlockDevice(){
		mDPM.resetPassword("", 0);
	}
}
