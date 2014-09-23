package hay.chris.smartunlock;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends Activity { 

	private ConditionAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
       
        //inflate list view
        LayoutInflater mInflater = getLayoutInflater();
        ViewGroup view =  (ViewGroup) mInflater.inflate(R.layout.activity_main, (ViewGroup) findViewById(android.R.id.content), false); 
		ListView list = (ListView) view.findViewById(R.id.conditions_listview);
		
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
		int setStorageSize = prefs.getInt("total_set_storage_size", 0);
		if (mAdapter == null) { 
			ArrayList<ConditionSet> data = new ArrayList<ConditionSet>();
			int nullCount = 0;
			for (int i = 0; i < setStorageSize + nullCount; i++) {
				String setName = "set" + i;
				Set<String> activeConditions = prefs.getStringSet(setName +"_active", new HashSet<String>());
				Set<String> inActiveConditions = prefs.getStringSet(setName +"_inactive", new HashSet<String>());
				if (activeConditions.isEmpty() && inActiveConditions.isEmpty()) nullCount++;
				else {
					Log.e(setName, ((Integer)i).toString());
					data.add(new ConditionSet(setName, i));
				}
			}
			 mAdapter = new ConditionAdapter(this, R.layout.condition_card, data);
			 mAdapter.setNotifyOnChange(true);
		} else {
	    	refreshSets();
	    	Log.e("test", "refresh attempt");
	    }
		if (list != null) {
	    	list.setAdapter(mAdapter);
	    }
	    setContentView(view);
		  
	    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
	    mSwipeRefreshLayout.setColorSchemeColors(Color.GREEN,
	    		Color.BLUE, Color.YELLOW, Color.RED);
	    mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
	        @Override
	        public void onRefresh() {
	            Log.e(getClass().getSimpleName(), "refresh");
	            new Refresher().execute();
	        }
	    });
    }


    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_delete_all){
        	removeAllSets();
        }
        return super.onOptionsItemSelected(item);
    }
    public void onResume(){
		super.onResume();
		refreshSets();
				
	}
    public void refreshSets() {
		Log.e("test", "refreshing");
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
		int setStorageSize = prefs.getInt("total_set_storage_size", 0);
		if (mAdapter != null && mAdapter.getCount() != setStorageSize){
			ArrayList<ConditionSet> newData = new ArrayList<ConditionSet>();
			int nullCount = 0;
			for (int i = mAdapter.getCount(); i < setStorageSize + nullCount; i++) {
				String setName = "set" + i;
				Set<String> activeConditions = prefs.getStringSet(setName +"_active", new HashSet<String>());
				Set<String> inActiveConditions = prefs.getStringSet(setName +"_inactive", new HashSet<String>());
				if (activeConditions.isEmpty() && inActiveConditions.isEmpty()) nullCount++;
				else {
					Log.e(setName, ((Integer) i).toString());
					newData.add(new ConditionSet(setName, i));
				}
			}
			mAdapter.addAll(newData);
			((ListView) findViewById(R.id.conditions_listview)).invalidateViews();

		}
    }

	public void removeCondition(int position){
		
	}
	public void removeAllConditions() {
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
		SharedPreferences.Editor ed = prefs.edit();
	    int currentConditionSize = prefs.getInt("total_condition_storage_size", 0);
	    for (int i = 0; i < currentConditionSize; i++)
	    	ed.remove("condition" + i);
	    ed.putInt("total_condition_storage_size", 0);
	    ed.commit();
	}
	public void removeAllSets() {
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
		SharedPreferences.Editor ed = prefs.edit();
		Set<String> activeSets = new HashSet<String>(prefs.getStringSet("all_sets_active", new HashSet<String>()));
		Set<String> inActiveSets = new HashSet<String>(prefs.getStringSet("all_sets_inactive", new HashSet<String>()));

		for (String setName : activeSets) {
			Set<String> activeConditions = new HashSet<String>(prefs.getStringSet(setName + "_active", new HashSet<String>()));
			Set<String> inActiveConditions = new HashSet<String>(prefs.getStringSet(setName + "_inactive", new HashSet<String>()));
			for (String conditionName : activeConditions) {
				ed.remove(conditionName);
			}
			for (String conditionName : inActiveConditions) {
				ed.remove(conditionName);
			}
			ed.remove(setName + "_active");
			ed.remove(setName + "_inactive");
		}
		for (String setName : inActiveSets) {
			Set<String> activeConditions = new HashSet<String>(prefs.getStringSet(setName + "_active", new HashSet<String>()));
			Set<String> inActiveConditions = new HashSet<String>(prefs.getStringSet(setName + "_inactive", new HashSet<String>()));
			for (String conditionName : activeConditions) {
				ed.remove(conditionName);
			}
			for (String conditionName : inActiveConditions) {
				ed.remove(conditionName);
			}
			ed.remove(setName + "_active");
			ed.remove(setName + "_inactive");
		}
	    ed.putInt("total_condition_storage_size", 0);
	    ed.putInt("total_set_storage_size", 0);
	    ed.remove("all_sets_active");
	    ed.remove("all_sets_inactive");
	    ed.remove("all_timers_active");
	    ed.remove("all_bluetooth_active");
	    ed.remove("all_wifi_active");
	    ed.remove("all_locations_active");
	    ed.commit();

	}
    
    public int getConditionType(String data) {
    	String[] split = data.split(";");
    	return Integer.parseInt(split[0]);
    }
    
    
    
    /* 
     * 	handle xml on click calls
     */
    
    // add new condition
    public void launchCreateCondition(View view) {
 	   Intent intent = new Intent(this, CreateConditionActivity.class);
 	   startActivity(intent);
    }
    
    
    
    
    
    
    
    public class Refresher extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
        	Log.e("test", "refreshing");
    		SharedPreferences prefs = MainActivity.this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
    		int setStorageSize = prefs.getInt("total_set_storage_size", 0);
    		if (mAdapter != null && mAdapter.getCount() != setStorageSize){
    			ArrayList<ConditionSet> newData = new ArrayList<ConditionSet>();
    			int nullCount = 0;
    			for (int i = mAdapter.getCount(); i < setStorageSize + nullCount; i++) {
    				String setName = "set" + i;
    				Set<String> activeConditions = prefs.getStringSet(setName +"_active", new HashSet<String>());
    				Set<String> inActiveConditions = prefs.getStringSet(setName +"_inactive", new HashSet<String>());
    				if (activeConditions.isEmpty() && inActiveConditions.isEmpty()) nullCount++;
    				else {
    					newData.add(new ConditionSet(setName, i));
    				}
    			}
    			mAdapter.addAll(newData);
    		}
    		//invalidate view to force redraw
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
             //Here you can update the view
    		((ListView) findViewById(R.id.conditions_listview)).invalidateViews();
            // Notify swipeRefreshLayout that the refresh has finished
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }
    
    
    
    //creates views for listView
    private class ConditionAdapter extends ArrayAdapter<ConditionSet> {

		private Context mContext;
		public ConditionAdapter(Context context, int resource, int textViewResourceId, ArrayList<ConditionSet> data) {
			super(context, resource, textViewResourceId, data);
			mContext = context;
		}
		public ConditionAdapter(Context context, int resource, ArrayList<ConditionSet> data) {
			super(context, resource, data);
			mContext = context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.default_set, parent, false);
			//}
			final int pos = position;
			ConditionSet set = getItem(position);
			TextView title = (TextView) ((ViewGroup) convertView).getChildAt(0);
			TextView description = (TextView) ((ViewGroup) convertView).getChildAt(1);
			Switch setSwitch = (Switch)((ViewGroup) convertView).getChildAt(2);
			LinearLayout timerHolder = (LinearLayout)((ViewGroup) convertView).getChildAt(3);
			LinearLayout conditionHolder = (LinearLayout)((ViewGroup) convertView).getChildAt(5);
			
			SharedPreferences prefs = mContext.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			Log.e("test", "" + set.orderedConditions.size());
			for (String conditionName : set.orderedConditions) {
				String data = prefs.getString(conditionName, "");
				ConditionObject condition;
				final String conName = conditionName;
				LinearLayout conditionViewHolder;
				View conditionView;
				TextView conditionTitle;
				TextView conditionDescription;  
				ImageView image;
				switch(data.charAt(0)){
				case '0':  
					TimerCondition timer = new TimerCondition(conditionName, data);
					conditionView = getLayoutInflater().inflate(R.layout.condition_card,
										  			timerHolder, false);
					conditionTitle = (TextView) ((ViewGroup) conditionView).getChildAt(0);
					conditionDescription = (TextView) ((ViewGroup) conditionView).getChildAt(1);
					image = (ImageView) ((ViewGroup) conditionView).getChildAt(2);
					final boolean initState =  timer.isActive;
					if (timer.isActive) image.setImageResource(R.drawable.ic_timer_active); 
					else image.setImageResource(R.drawable.ic_timer_inactive); 
					image.setOnClickListener(new OnClickListener() {  
												private boolean enabled = initState;
												public void onClick(View v) {
													if (enabled) {
														((ImageView) v).setImageResource(R.drawable.ic_timer_inactive);
														enabled = false;
														disableCondition(conName);
													} else {
														((ImageView) v).setImageResource(R.drawable.ic_timer_active);
														enabled = true;
														activateCondition(conName);
													}
												}
											});
					conditionTitle.setText(R.string.title_timer);
					switch(timer.radio) {
						case 0: 
							String timeTypeName;
							switch(timer.timeType){
								case 0: timeTypeName = " hours "; break;
								case 1: timeTypeName = " minutes "; break;
								case 2: timeTypeName = " seconds "; break;
								default: timeTypeName = ""; break;
							}
							conditionDescription.setText("Disable lock for " + timer.time + timeTypeName + "after successful login.");
						break;
						case 1: //TODO handle this case
						break;
						case 2: 
							conditionDescription.setText("Always disable lock.");
						break;
						case 3:
							conditionDescription.setText("Never disable lock.");
						break;
					}
					timerHolder.addView(conditionView);
				break;
				case '1':
					BluetoothCondition bluetooth = new BluetoothCondition(conditionName, data);
					conditionView = getLayoutInflater().inflate(R.layout.condition_card,
										  			conditionHolder, false);
					conditionTitle = (TextView) ((ViewGroup) conditionView).getChildAt(0);
					conditionDescription = (TextView) ((ViewGroup) conditionView).getChildAt(1);
					image = (ImageView) ((ViewGroup) conditionView).getChildAt(2);
					final boolean initStateB =  bluetooth.isActive;
					if (bluetooth.isActive) image.setImageResource(R.drawable.ic_bluetooth_active); 
					else image.setImageResource(R.drawable.ic_bluetooth_inactive); 
					image.setOnClickListener(new OnClickListener() {  
												private boolean enabled = initStateB;
												public void onClick(View v) {
													if (enabled) {
														((ImageView) v).setImageResource(R.drawable.ic_bluetooth_inactive);
														enabled = false;
														disableCondition(conName);
													} else {
														((ImageView) v).setImageResource(R.drawable.ic_bluetooth_active);
														enabled = true;
														activateCondition(conName);
													}
												}
											});
					conditionTitle.setText(R.string.title_bluetooth);
					StringBuilder descript = new StringBuilder("Disable lock when ");
					switch(bluetooth.radio) {
						case 1: descript.append("not ");
						case 0: descript.append("connected to: ");
						break;
						case 2: descript.append("near: ");
						break;
					}
					Log.e("bluetooth", "about to add devices to description");
					if (bluetooth.devices.size() > 0) {
						Log.e("bluetooth", "adding devices to description");
						for (String nameMac : bluetooth.devices){
							String[] names = nameMac.split("\n");
							descript.append(names[0] + ", ");
						}
						//removes trailing comma
						descript.deleteCharAt(descript.length() - 2);
					}
					conditionDescription.setText(descript);
					conditionHolder.addView(conditionView);
					break;
				case '2':
					//TODO implement these other cases
					condition = new ConditionObject(conditionName);
					conditionViewHolder = (LinearLayout) getLayoutInflater().inflate(R.layout.condition_card,
										  			(ViewGroup)findViewById(R.id.set_timer_condition), true);
					conditionView = conditionViewHolder.getChildAt(0);
					conditionTitle = (TextView) ((ViewGroup) conditionView).getChildAt(0);
					conditionDescription = (TextView) ((ViewGroup) conditionView).getChildAt(1);
					image = (ImageView) ((ViewGroup) conditionView).getChildAt(2);
					image.setImageResource(R.drawable.ic_wifi_active);
					conditionTitle.setText(R.string.title_wifi);
					break;
				case '3':
					//TODO implement these other cases
					condition = new ConditionObject(conditionName);
					conditionViewHolder = (LinearLayout) getLayoutInflater().inflate(R.layout.condition_card,
										  			(ViewGroup)findViewById(R.id.set_timer_condition), true);
					conditionView = conditionViewHolder.getChildAt(0);
					conditionTitle = (TextView) ((ViewGroup) conditionView).getChildAt(0);
					conditionDescription = (TextView) ((ViewGroup) conditionView).getChildAt(1);
					image = (ImageView) ((ViewGroup) conditionView).getChildAt(2);
					image.setImageResource(R.drawable.ic_location_active);
					conditionTitle.setText(R.string.title_location);
					break;
				}
			}
			
			return convertView;
		}
		protected void disableCondition(String name) {
			SharedPreferences prefs = mContext.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			String data = prefs.getString(name, "");
			switch(data.charAt(0)){
				case '0':
					TimerCondition timer = new TimerCondition(name, data);
					timer.disable(mContext);
					break;
				case '1': 
					BluetoothCondition bluetooth = new BluetoothCondition(name, data);
					bluetooth.disable(mContext);
					break;
				case '2': break;
				case '3': break;
			}
		}
		
		protected void activateCondition(String name) {
			//TODO fix
			SharedPreferences prefs = mContext.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			String data = prefs.getString(name, "");
			switch(data.charAt(0)){
				case '0':
					TimerCondition timer = new TimerCondition(name, data);
					timer.activate(mContext);
					break;
				case '1': 
					BluetoothCondition bluetooth = new BluetoothCondition(name, data);
					bluetooth.activate(mContext);
					break;
				case '2': break;
				case '3': break;
			}
		}		
	}
    class ConditionSet{
    	String name;
    	String title;
    	String description;
    	TimerCondition timer;
    	Set<String> activeConditions;
    	Set<String> inactiveConditions;
    	Set<String> orderedConditions;
    	public ConditionSet(String setName, int setNumber){
    		this.name = setName;
    		SharedPreferences prefs = MainActivity.this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
    		activeConditions = new HashSet<String>(prefs.getStringSet("set" + setNumber + "_active", new HashSet<String>()));
    		inactiveConditions = new HashSet<String>(prefs.getStringSet("set" + setNumber + "_inactive", new HashSet<String>()));
    		orderedConditions = new HashSet<String>();
    		int[] order = new int[activeConditions.size() + inactiveConditions.size()];
    		int count = 0;
    		for (String conditionName : activeConditions) {
    			Log.e("test", conditionName);
    			String conNum = conditionName.substring(9);
    			order[count] = Integer.parseInt(conNum);
    			count++;
    		}
    		for (String conditionName : inactiveConditions) {
    			order[count] = Integer.parseInt(conditionName.substring(9));
    			count++;
    		}
    		Arrays.sort(order);
    		for (int num : order) {
    				orderedConditions.add("condition" + num);
    		}
    	}
    	
    }
    //parent for all conditions
    static class ConditionObject{
    	protected int type;
    	protected String name;
    	protected String setName;
    	protected boolean isValid;
    	protected boolean isActive;
    	
    	//for saving
    	public ConditionObject(int type, String name, String setName){
    		this.type = type;
    		this.name = name;
    		this.setName = setName;
    		this.isValid = false;
    	}
    	//for loading
    	public ConditionObject(String name){
    		this.name = name;
    	}
    	public int getType() {
    		return type;
    	}
    	public String getName() {
    		return name;
    	}
    	public String getSetName() {
    		return setName;
    	}
    	public void setSetName(String setName) {
    		this.setName = setName;
    	}
    	public boolean isValid() {
    		return isValid;
    	}
    	public void setValid(boolean validity) {
    		this.isValid = validity;
    	}
    	public void activate(Context context){
    		String typeName;
    		switch(type){
	    		case 0: typeName = "timers"; break;
	    		case 1: typeName = "bluetooth"; break;
	    		case 2: typeName = "wifi"; break;
	    		case 3: typeName = "locations"; break;
	    		default: typeName = "";
    		}
    		isActive = true;
    		SharedPreferences prefs = context.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			SharedPreferences.Editor ed = prefs.edit();
    		Set<String> activeConditions = new HashSet<String>(
					prefs.getStringSet(setName + "_active", new HashSet<String>()));
			Set<String> inactiveConditions = new HashSet<String>(
					prefs.getStringSet(setName + "_inactive", new HashSet<String>()));
			Set<String> activeSets = new HashSet<String>(
					prefs.getStringSet("all_sets_active", new HashSet<String>()));
			Set<String> inactiveSets = new HashSet<String>(
					prefs.getStringSet("all_sets_inactive", new HashSet<String>()));
			Set<String> activeTimers = new HashSet<String>(
					prefs.getStringSet("all_" + typeName + "_active", new HashSet<String>()));
			activeTimers.add(name);
			inactiveConditions.remove(name);
			activeConditions.add(name);
			inactiveSets.remove(setName);
			activeSets.add(setName);
			ed.putString(name, toString());
			ed.putStringSet("all_" + typeName + "_active", activeTimers);
			ed.putStringSet(setName + "_active", activeConditions);
			ed.putStringSet(setName + "_inactive", inactiveConditions);
			ed.putStringSet("all_sets_active", activeSets);
			ed.putStringSet("all_sets_inactive", inactiveSets);
			Log.e("enabling " + name, toString());
			ed.commit(); 
    	}
    	public void disable(Context context){
    		String typeName;
    		switch(type){
	    		case 0: typeName = "timers"; break;
	    		case 1: typeName = "bluetooth"; break;
	    		case 2: typeName = "wifi"; break;
	    		case 3: typeName = "locations"; break;
	    		default: typeName = "";
    		}
    		isActive = false;
    		SharedPreferences prefs = context.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			SharedPreferences.Editor ed = prefs.edit();
    		Set<String> activeConditions = new HashSet<String>(
					prefs.getStringSet(setName + "_active", new HashSet<String>()));
			Set<String> inactiveConditions = new HashSet<String>(
					prefs.getStringSet(setName + "_inactive", new HashSet<String>()));
			Set<String> activeSets = new HashSet<String>(
					prefs.getStringSet("all_sets_active", new HashSet<String>()));
			Set<String> inactiveSets = new HashSet<String>(
					prefs.getStringSet("all_sets_inactive", new HashSet<String>()));
			Set<String> activeTimers = new HashSet<String>(
					prefs.getStringSet("all_" + typeName + "_active", new HashSet<String>()));
			activeTimers.remove(name);
			inactiveConditions.add(name);
			activeConditions.remove(name);
			if (activeConditions.isEmpty()) {
				inactiveSets.add(setName);
				activeSets.remove(setName);
			}
			Log.e("disabling " + name, toString());
			ed.putString(name, toString());
			ed.putStringSet("all_" + typeName + "_active", activeTimers);
			ed.putStringSet(setName + "_active", activeConditions);
			ed.putStringSet(setName + "_inactive", inactiveConditions);
			ed.putStringSet("all_sets_active", activeSets);
			ed.putStringSet("all_sets_inactive", inactiveSets);
			ed.commit();
    	}
    }
    static class TimerCondition extends ConditionObject {
    	int radio;
    	int time;
    	int timeType;
    	int startTime;
    	int endTime;
    	 
    	//for saving
    	public TimerCondition(String name, String setName, int radio){
    		super(0, name, setName);
    		this.radio = radio;
    	}
    	//for loading
    	public TimerCondition(String name, String data) {
    		super(name);
    		if (data.equals("")) return;
    		String[] splitData = data.split(";");
    		
    		type = Integer.parseInt(splitData[0]);
    		if (Integer.parseInt(splitData[1]) == 1) isValid = true;
    		else isValid = false;
    		setName = splitData[2];
    		radio = Integer.parseInt(splitData[3]);
    		switch(radio){
	    		case 0:
	    		case 1: setTime(Integer.parseInt(splitData[4]), Integer.parseInt(splitData[5]));
	    				if (Integer.parseInt(splitData[6]) == 1) isActive = true;
	    				else isActive = false;		
	    				break;
	    		case 2:
	    		case 3: if (Integer.parseInt(splitData[4]) == 1) isActive = true;
	    				else isActive = false;
	    				isValid = true;
	    				break;
    		}
    	}
    	public void setTime(int time1, int time2) {
    		if (radio == 0) {
    			time = time1;
    			timeType = time2;
    		} else if (radio == 1) {
    			startTime = time1;
    			endTime = time2;
    		}
    	}
    	//used to store all relevant data
    	public String toString() {
    		String validity;
    		String active;
    		if (isValid()) validity = "1";
    		else validity = "0";
    		if (isActive) active = "1";
    		else active = "0";
    		String data = "" + getType() + ";" + validity + ";" + getSetName() + ";" + radio + ";";
    		switch (radio) {
	    		case 0: return data + time + ";" + timeType + ";" + active + ";";
	    		case 1: return data + startTime + ";" + endTime + ";"+ active + ";";
	    		case 2: 
	    		case 3: return data + ";"+ active + ";";
	    		default: return data;
    		}
    	}    	
    	
    }
    static class BluetoothCondition extends ConditionObject {
    	int radio;
    	ArrayList<String> devices;
    	public BluetoothCondition(String name, String setName, int radio) {
    		super(1, name, setName);
    		this.radio = radio;
    		devices = new ArrayList<String>();
    	}
    	public BluetoothCondition(String name, String data){
    		super(name);
    		if (data.equals("")) return;
    		
    		Log.e(name, data);
    		String[] splitData = data.split(";");
    		
    		type = Integer.parseInt(splitData[0]);
    		if (Integer.parseInt(splitData[1]) == 1) isValid = true;
    		else isValid = false;
    		setName = splitData[2];
    		radio = Integer.parseInt(splitData[3]);    		
    		int numDevices = splitData.length - 5;
    		devices = new ArrayList<String>();
    		for (int i = 4; i < numDevices + 4; i++){
    			Log.e("device", splitData[i]);
    			devices.add(splitData[i]);
    		}
    		isActive = Integer.parseInt(splitData[splitData.length - 1]) > 0;
    	}
    	public void addDevice(String name, String mac) {
    		devices.add(name + "\n" + mac);
    	}
    	public void addDevice(String data){
    		devices.add(data);
    		Log.e("device list size", "" + devices.size());
    	}
    	public void removeDevice(String data){
    		devices.remove(data);
    	}
    	public String toString() {
    		String validity;
    		String active;
    		if (isValid()) validity = "1";
    		else validity = "0";
    		if (isActive) active = "1";
    		else active = "0";
    		StringBuilder deviceList = new StringBuilder();
    		for (String name : devices)
    			deviceList.append(name + ";");
    		String data = "" + getType() + ";" + validity + ";" + getSetName() + ";" + radio + ";" + deviceList;
    		Log.e("saving bluetooth", data + active + ";");	
    		return data + active + ";";
    	} 
    }
    static class WifiCondition extends ConditionObject {
    	public WifiCondition(String name, String setName) {
    		super(2, name, setName);
    	}
    }
    static class LocationCondition extends ConditionObject {
    	public LocationCondition(String name, String setName) {
    		super(3, name, setName);
    	}
    }
}
