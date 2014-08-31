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
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity { 

	private ConditionAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeAllConditions();
        //inflate list view
        LayoutInflater mInflater = getLayoutInflater();
        ViewGroup view =  (ViewGroup) mInflater.inflate(R.layout.activity_main, (ViewGroup) findViewById(android.R.id.content), false); 
		ListView list = (ListView) view.findViewById(R.id.conditions_listview);
		
		//get conditions and add all to adapter
		//TODO: change to sets
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
	    int currentConditionSize = prefs.getInt("total_condition_storage_size", 0);
	    if (mAdapter == null){
		    ConditionObject[] data = new ConditionObject[currentConditionSize];
		    for (int i = 0; i < currentConditionSize; i++){
		    	String conditionData = prefs.getString("condition" + i, "");
		    	//TODO store int with condition type directly under "condition" + i + "_type"
		    	switch(getConditionType(conditionData)){
			    	case 0: data[i] = new TimerCondition("condition" + i, conditionData);
			    		break;
			    	case 1:
			    		break;
			    	case 2:
			    		break;
			    	case 3:
			    		break;
		    	}
		    }
		    ArrayList<ConditionObject> lst = new ArrayList<ConditionObject>();
		    lst.addAll(Arrays.asList(data));
		    mAdapter = new ConditionAdapter(this, R.layout.condition_card, lst);
		    mAdapter.setNotifyOnChange(true);
	    } else {
	    	refreshConditions();
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onResume(){
		super.onResume();
		refreshConditions();
				
	}
    
    public void refreshConditions(){
		Log.e("test", "refreshing");
		// get all conditions and add to adapter
		//TODO: change to Sets
		SharedPreferences prefs = this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
	    int currentConditionSize = prefs.getInt("total_condition_storage_size", 0);
	    // only add if there's data missing
		if (mAdapter != null && mAdapter.getCount() != currentConditionSize) {
			ConditionObject[] newData = new ConditionObject[currentConditionSize - mAdapter.getCount()];
			for (int i = mAdapter.getCount(); i < currentConditionSize; i++){
				String timerData = prefs.getString("condition" + i, "");
		    	newData[i - mAdapter.getCount()] = new TimerCondition("condition" + i, timerData);
			}
			ArrayList<ConditionObject> lst = new ArrayList<ConditionObject>();
			lst.addAll(Arrays.asList(newData));
			Log.e("test", "newData" + newData.length);
			mAdapter.addAll(lst);
		}
		//invalidate view to force redraw
		((ListView) findViewById(R.id.conditions_listview)).invalidateViews();

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
        	//removeAllConditions();
        	Log.e("test", "refreshing");
    		// get all conditions and add to adapter
    		//TODO: change to Sets
    		SharedPreferences prefs = MainActivity.this.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
    	    int currentConditionSize = prefs.getInt("total_condition_storage_size", 0);
    	    // only add if there's data missing
    		if (mAdapter != null && mAdapter.getCount() < currentConditionSize) {
    			ConditionObject[] newData = new ConditionObject[currentConditionSize - mAdapter.getCount()];
    			for (int i = mAdapter.getCount(); i < currentConditionSize; i++){
    				String timerData = prefs.getString("condition" + i, "");
    		    	newData[i - mAdapter.getCount()] = new TimerCondition("condition" + i, timerData);
    			}
    			ArrayList<ConditionObject> lst = new ArrayList<ConditionObject>();
    			lst.addAll(Arrays.asList(newData));
    			Log.e("test", "newData" + newData.length);
    			mAdapter.addAll(lst);
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
    private class ConditionAdapter extends ArrayAdapter<ConditionObject> {

		private Context mContext;
		public ConditionAdapter(Context context, int resource, int textViewResourceId, ArrayList<ConditionObject> data) {
			super(context, resource, textViewResourceId, data);
			mContext = context;
		}
		public ConditionAdapter(Context context, int resource, ArrayList<ConditionObject> data) {
			super(context, resource, data);
			mContext = context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.condition_card, parent, false);
			}
			TextView title = (TextView) ((ViewGroup) convertView).getChildAt(0);
			TextView description = (TextView) ((ViewGroup) convertView).getChildAt(1);
			ImageView image = (ImageView) ((ViewGroup) convertView).getChildAt(2);
			final int pos = position;
			
			ConditionObject condition = getItem(position);
			final String conditionName = condition.getName();
			int type = condition.getType();
			switch(type){
				case 0:  
					image.setImageResource(R.drawable.ic_timer_active); 
					image.setOnClickListener(new OnClickListener() {
												private boolean enabled = true;
												public void onClick(View v) {
													Drawable current = ((ImageView) v).getDrawable();
													if (enabled) {
														((ImageView) v).setImageResource(R.drawable.ic_timer_inactive);
														enabled = false;
														activateCondition(conditionName);
													} else {
														((ImageView) v).setImageResource(R.drawable.ic_timer_active);
														enabled = true;
														disableCondition(conditionName);
													}
												}
											});
					title.setText(R.string.title_timer);
					TimerCondition timer = (TimerCondition) condition;
					switch(timer.radio) {
						case 0: 
							String timeTypeName;
							int setTimeAmount = timer.time; //length of time in milliseconds
							switch(timer.timeType){
								case 0: timeTypeName = " hours "; break;
								case 1: timeTypeName = " minutes "; break;
								case 2: timeTypeName = " seconds "; break;
								default: timeTypeName = ""; break;
							}
							description.setText("Disable lock for " + timer.time + timeTypeName + "after successful login.");
						break;
						case 1: //TODO handle this case
						break;
						case 2: 
							description.setText("Always disable lock.");
						break;
						case 3:
							description.setText("Never disable lock.");
						break;
					}
				break;
				case 1:
					image.setImageResource(R.drawable.ic_bluetooth_active);
					title.setText(R.string.title_bluetooth);
					break;
				case 2:
					image.setImageResource(R.drawable.ic_wifi_active);
					title.setText(R.string.title_wifi);
					break;
				case 3:
					image.setImageResource(R.drawable.ic_location_active);
					title.setText(R.string.title_location);
					break;
			}
			return convertView;
		}
		protected void disableCondition(String name) {
		
		}
		
		protected void activateCondition(String name) {
			//TODO fix
			if (!name.equals("")) return;
			SharedPreferences prefs = mContext.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
			String data = prefs.getString(name, "");
			ConditionObject condition = new TimerCondition(name, data);
			String curSetName = condition.getSetName();
			int type = condition.getType();
			switch(type){
			case 0: 
				TimerCondition timer = (TimerCondition) condition;
				switch(timer.radio) {
					case 0: 
						//SharedPreferences prefs = mContext.getSharedPreferences("hay.chris.smartunlock.condition_storage", 0);
						SharedPreferences.Editor ed = prefs.edit();
						//get list of all condition sets that are active
						Set<String> activeConditionSets = prefs.getStringSet("active_condition_sets", new HashSet<String>());
						//get list of all active conditions within this set
						Set<String> curSetConditions = prefs.getStringSet(curSetName, new HashSet<String>());
						//add this condition to its set
						curSetConditions.add(name);
						//add this set to the list of active sets
						activeConditionSets.add(curSetName);
						ed.commit();
					break;
					case 1:
					break;
					case 2: 
					break;
					case 3:
					break;
				}
			break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
		}
		}
		
	}
   
    //parent for all conditions
    static class ConditionObject{
    	protected int type;
    	protected String name;
    	protected String setName;
    	protected boolean isValid;
    	
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
	    				break;
	    		default: break;
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
    		if (isValid()) validity = "1";
    		else validity = "0";
    		String data = "" + getType() + ";" + validity + ";" + getSetName() + ";" + radio + ";";
    		switch (radio) {
	    		case 0: return data + time + ";" + timeType + ";";
	    		case 1: return data + startTime + ";" + endTime + ";";
	    		case 2: return data + 1 + ";";
	    		case 3: return data + 0 + ";";
	    		default: return data;
    		}
    	}    	
    }
    static class BluetoothCondition extends ConditionObject {
    	public BluetoothCondition(String name, String setName) {
    		super(1, name, setName);
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