package hay.chris.smartunlock;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity{
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
    protected static final int REQUEST_CODE_ENABLE_ADMIN=1;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);

		//reset Password listener and sets summary
		findPreference("enable_device_admin").setOnPreferenceChangeListener(sActionableValueChange);
		Preference tempPreference = findPreference("password_type");
		tempPreference.setOnPreferenceChangeListener(sActionableValueChange);
		setSummaries(tempPreference, PreferenceManager.getDefaultSharedPreferences(
				tempPreference.getContext()).getString(tempPreference.getKey(),
				""));
		
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		//bindPreferenceSummaryToValue(findPreference("password_type"));
		bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}
	public void onActivityResult(int requestcode, int resultcode, Intent data){
		Log.e("result", "true");
		if (requestcode == 1){
			if (resultcode == Activity.RESULT_OK){
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor preferences = settings.edit();
				preferences.putBoolean("enable_device_admin", true);
				preferences.commit();
				((CheckBoxPreference)findPreference("enable_device_admin")).setChecked(true);
			} else {
				((CheckBoxPreference)findPreference("enable_device_admin")).setChecked(false);
			}
		}
		
	}
	public void resetUI(Preference preference){
		
	}
	private Preference.OnPreferenceChangeListener sActionableValueChange = new Preference.OnPreferenceChangeListener() {
	
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {			
        	if (preference.getKey().equals("enable_device_admin")) {

				boolean boolValue = (Boolean) newValue;
				Log.e("newValue", boolString(boolValue));
	
	
				ComponentName deviceAdmin = new ComponentName(SettingsActivity.this, DeviceAdmin.class);
	        	DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
	            Log.e("isActiveBeforeSet", boolString(mDPM.isAdminActive(deviceAdmin)));
	
	        	//if (preference.getKey().equals("enable_device_admin")) {
        		Log.e("keyMatch", "true");
                if (boolValue != mDPM.isAdminActive(deviceAdmin)) {
                	Log.e("startChange", "true");
                    if (boolValue) {
                        // Launch the activity to have the user enable our admin.
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Needed for app to run");
                        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                        // return false - don't update checkbox until we're really active
                        return false;
                    } else {
                        mDPM.removeActiveAdmin(deviceAdmin);
        				((CheckBoxPreference)findPreference("enable_device_admin")).setChecked(false);
        				return false;
                        //enableDeviceCapabilitiesArea(false);
                    }
                }
            } else if (preference.getKey().equals("password_type")){
            	int type = Integer.parseInt((String) newValue);
            	Log.e("newValue", ((Integer)type).toString());
            	if(type != 0) {
            		storeDialog(false, type);
            	} else {
            		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            		SharedPreferences.Editor preferences = settings.edit();
      			  	preferences.putString("stored_password", "");
      			  	preferences.commit();
            	}
            	setSummaries(preference, newValue);
            	return true;
            }
            //Log.e("isActive", boolString(mDPM.isAdminActive(deviceAdmin)));
            return false;
		}
	};
	private void storeDialog(boolean failed, final int type){
		AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		alert.setTitle("Set Password");
		TextView setPassword = new TextView(this);
		setPassword.setText("Enter Password");
		TextView confirmPassword = new TextView(this);
		confirmPassword.setText("Confirm Password");

		
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		final EditText confirmInput = new EditText(this);
		if (type == 1) {
			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			confirmInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		} else if (type == -1) {
			input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			confirmInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		} 
		layout.addView(setPassword);
		layout.addView(input);
		layout.addView(confirmPassword);
		layout.addView(confirmInput);

		if (failed) {
			TextView failedMessage = new TextView(this);
			failedMessage.setText("Password did not match. Please enter again.");
			failedMessage.setTextColor(Color.RED);
			layout.addView(failedMessage);
		}
		alert.setView(layout);
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  String confirmValue = confirmInput.getText().toString();
		  if (!value.equals(confirmValue)){
			  storeDialog(true, type);
		  } else {
			  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
			  SharedPreferences.Editor preferences = settings.edit();
			  preferences.putString("stored_password", value);
			  preferences.commit();
			  setSummaries(findPreference("password_type"), ((Integer)type).toString());
		  }
		  
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
//			  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
//			  if (settings.getString("stored_password", "").equals("")) {
//				  SharedPreferences.Editor preferences = settings.edit();
//				  preferences.putString("password_type", "0");
//				  preferences.commit();
//				  sActionableValueChange.onPreferenceChange(findPreference("password_type"), "0");
//			  }
			  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
			  if (settings.getString("stored_password", "").equals("")) {
				  setSummaries(findPreference("password_type"), "0");
			  } else {
				  setSummaries(findPreference("password_type"), "1");
			  }			  
		  }
		});

		alert.show();
	}
	private String boolString(boolean bool){
		if(bool){
			return "true";
		} else {
			return "false";
		}
	}
	private void setSummaries(Preference preference, Object newValue){
//		ListPreference listPreference = (ListPreference) preference;
//		int index = listPreference.findIndexOfValue((String) newValue);
//
//		// Set the summary to reflect the new value.
//		preference
//				.setSummary(index >= 0 ? listPreference.getEntries()[index]
//						: null);
		if (Integer.parseInt((String)newValue) != 0) {
			preference.setSummary("Password Set");
		} else {
			preference.setSummary("No Password");
		}
	}
	private void setValues(Preference preference, Object newValue) {
		ListPreference listPreference = (ListPreference) preference;
		int index = listPreference.findIndexOfValue((String) newValue);

		// Set the summary to reflect the new value.
		listPreference
				.setValue((String) (index >= 0 ? listPreference.getEntryValues()[index]
						: null));
	}
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		
		
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}
				
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("example_text"));
			bindPreferenceSummaryToValue(findPreference("example_list"));
		}
	}

	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
	}
}
