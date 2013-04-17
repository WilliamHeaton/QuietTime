package com.cypho.quiet_time;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AdvancedSettings extends Activity {

	Context ctx;
	String group;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		ctx = this;
		setContentView(R.layout.advanced);
		setupFreq();
		setupCheckbox(R.id.A_advancedHolder,R.id.A_advancedBox,"advanced");
		setupCheckbox(R.id.A_titleHolder,R.id.A_titleBox,"title");
		setupCheckbox(R.id.A_locationHolder,R.id.A_locationBox,"location");
		setupCheckbox(R.id.A_descriptionHolder,R.id.A_descriptionBox,"description");
		setupCheckbox(R.id.A_toastHolder,R.id.A_toastBox,"toasts");
		setupCheckbox(R.id.A_vibrateStartHolder,R.id.A_vibrateStartBox,"vibrateStart");
		setupCheckbox(R.id.A_vibrateEndHolder,R.id.A_vibrateEndBox,"vibrateEnd");
		
    }
    OnClickListener viewClicked = new OnClickListener() {
		public void onClick(View view) {
			CheckBox cb = (CheckBox) findViewById((Integer) view.getTag());
			cb.performClick();
		}
	};
    OnClickListener freqClicked = new OnClickListener() {
		public void onClick(View view) {
			
			
		}
	};
	OnCheckedChangeListener checkClicked = new OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton view, boolean isChecked) {
			SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
			Editor mEditor = prefs.edit();
			mEditor.putBoolean((String) view.getTag(), isChecked);
			mEditor.commit();
		}
    };
    private void setupFreq(){
    	
    	LinearLayout hold = (LinearLayout) findViewById(R.id.A_freqHolder);
		hold.setBackgroundResource(R.drawable.list_selector_holo_dark);

		
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		int freq = prefs.getInt("frequency", 3600000);
		int v;
    	switch(freq){
    		case 900000:
    			v=0;
    			break;
    		case 1800000:
    			v=1;
    			break;
    		case 3600000:
    		default:
    			v=2;
    			break;
    		case 43200000:
    			v=3;
    			break;
    		case 86400000:
    			v=4;
    			break;
    	}
		hold.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Spinner spin = (Spinner) findViewById(R.id.select_freq);
				spin.performClick();
			}
          });
		Spinner spin = (Spinner) findViewById(R.id.select_freq);
		ArrayAdapter<CharSequence> spinadapter = ArrayAdapter.createFromResource(ctx, R.array.frequency_array, R.layout.spinner_layout);
		spinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(spinadapter);
	    spin.setSelection(v);
	    spin.setOnItemSelectedListener(new OnItemSelectedListener(){
			 public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				int v = 3600000;
				switch(pos){
					case 0:
						v=900000;
						break;
					case 1:
						v=1800000;
						break;
					case 2:
					default:
						v=3600000;
						break;
					case 3:
						v=43200000;
						break;
					case 4:
						v=86400000;
						break;
				}
				 
				SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
				Editor mEditor = prefs.edit();
				mEditor.putInt("frequency", v);
				mEditor.commit();
				
				AlarmManager alarmManager = (AlarmManager) ctx.getSystemService("alarm");
				alarmManager.cancel(PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, CheckForEvents.class), 0));
				alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, prefs.getInt("frequency", 3600000), PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, CheckForEvents.class), 0));
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
		    }
		    public void onNothingSelected(AdapterView<?>  parent) {
		    }
		});
    	
    	
    }
    private void setupCheckbox(int holder, int view, String pref){

		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);

		LinearLayout hold = (LinearLayout) findViewById(holder);
		hold.setBackgroundResource(R.drawable.list_selector_holo_dark);
		hold.setOnClickListener(viewClicked);
		hold.setTag(view);
		CheckBox cb = (CheckBox) findViewById(view);
		cb.setChecked(prefs.getBoolean(pref, false));
		cb.setTag(pref);
		cb.setOnCheckedChangeListener(checkClicked);		
    }

	@Override
	public void onBackPressed() {
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		Intent i;
		if(prefs.getBoolean("advanced", false)){
			i = new Intent(ctx, GroupsListActivity.class);
		}else{
			i = new Intent(ctx, GroupsSettings.class);
			i.putExtra("group", "1");
		}
			
		startActivity(i);
		
		this.finish();
	}
    
}