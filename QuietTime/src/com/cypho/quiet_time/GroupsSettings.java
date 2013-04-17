package com.cypho.quiet_time;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GroupsSettings extends Activity {
	int maxbefore = 20;
	int maxafter = 20;
	int maxvolume = 0;
	String ringtone = null;
	String group;
	String gname,title,description,location;
	EditText input;
	Context ctx;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.groupssettings);
		ctx = this;
		
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		maxvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		
		Bundle extras = getIntent().getExtras();
        group =  (extras != null) ? extras.getString("group") : "null";  
		
		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(this);
		mDbHelper.open();
		Cursor cur = mDbHelper.getGroupData(group);
		cur.moveToNext();
		
		refreshAdvanced();

////////////////// Alert Dialog Text Entry /////////////////////		
		

		setupAlertDialog(R.id.groupHolder,		R.id.groupName,		"groupname",	cur.getString(cur.getColumnIndex("groupname")),    getString(R.string.groupDialog), 	 "" );
		setupAlertDialog(R.id.titleHolder,		R.id.title,	 		"title",    	cur.getString(cur.getColumnIndex("title")),        getString(R.string.match_title),		 getString(R.string.match_title));
		setupAlertDialog(R.id.descriptionHolder,R.id.description,	"description",  cur.getString(cur.getColumnIndex("description")),  getString(R.string.match_description),getString(R.string.match_description));
		setupAlertDialog(R.id.locationHolder,	R.id.location,	 	"location",    	cur.getString(cur.getColumnIndex("location")),     getString(R.string.match_location),	 getString(R.string.match_location));

		
////////////////// Calendars List View /////////////////////			

		LinearLayout calHolder = (LinearLayout) findViewById(R.id.calHolder);
		calHolder.setBackgroundResource(R.drawable.list_selector_holo_dark);
		
        String cals = cur.getString(cur.getColumnIndex("calendars"));
		String[] calls = cals.split(",");
		
		TextView selectCalendars = (TextView) findViewById(R.id.selectCalendars);
        selectCalendars.setText((calls.length -  1) + getString(R.string.calendarsSelected));
        calHolder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	            Intent intent = new Intent( ctx, CalendarListActivity.class);
	            intent.putExtra("group", group);
	            startActivityForResult( intent, 2);
			}
          });
        
///////////////// Ringtone Selector /////////////////

        ringtone = cur.getString(cur.getColumnIndex("ringtone"));
        if(ringtone != null && ringtone.length()>3){
	        Uri ringuri = Uri.parse(ringtone);
			if (ringuri != null) {
				Ringtone ringtoner = RingtoneManager.getRingtone(this, ringuri);
				ringtone = ringtoner.getTitle(this);
				TextView ringtoneName = (TextView) findViewById(R.id.selectRingtone);
		        ringtoneName.setText(ringtone);
			}
        }
        
        LinearLayout ringtoneTable = (LinearLayout) findViewById(R.id.ringtoneTable);
        ringtoneTable.setBackgroundResource(R.drawable.list_selector_holo_dark);
        ringtoneTable.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TextView ringtoneName = (TextView) findViewById(R.id.selectRingtone);
		        ringtoneName.requestFocus();
		        
				String uri = ringtone.length()>1?ringtone:null;
	            Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
	            
	            
	            if( uri != null)
	                 intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,Uri.parse( uri));
	            else
	                 intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri)null);
	            
	            startActivityForResult( intent, 1);
			}
        });
                
        
        

////////////////// Spinners /////////////////////	        

//All Day        
        
		LinearLayout alldayHolder = (LinearLayout) findViewById(R.id.alldayHolder);
		alldayHolder.setBackgroundResource(R.drawable.list_selector_holo_dark);
		alldayHolder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Spinner spin = (Spinner) findViewById(R.id.select_allday);
				spin.performClick();
			}
          });
		Spinner allday = (Spinner) findViewById(R.id.select_allday);
		ArrayAdapter<CharSequence> spinadapter = ArrayAdapter.createFromResource(ctx, R.array.allday_array, R.layout.spinner_layout);
		spinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    allday.setAdapter(spinadapter);
		allday.setSelection(cur.getInt(cur.getColumnIndex("allday")));
		allday.setOnItemSelectedListener(new OnItemSelectedListener(){
			 public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "allday", Integer.toString(pos));
				mDbHelper.close();

				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
		    }
		    public void onNothingSelected(AdapterView<?>  parent) {
		    }
		});

// Busy

		LinearLayout busyHolder = (LinearLayout) findViewById(R.id.busyHolder);
		busyHolder.setBackgroundResource(R.drawable.list_selector_holo_dark);
		busyHolder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Spinner spin = (Spinner) findViewById(R.id.select_busy);
				spin.performClick();
			}
          });
		Spinner busy = (Spinner) findViewById(R.id.select_busy);
		ArrayAdapter<CharSequence> busyadapter = ArrayAdapter.createFromResource(ctx, R.array.busy_array, R.layout.spinner_layout);
		busyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		busy.setAdapter(busyadapter);
		busy.setSelection(cur.getInt(cur.getColumnIndex("busy")));
		busy.setOnItemSelectedListener(new OnItemSelectedListener(){
			 public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "busy", Integer.toString(pos));
				mDbHelper.close();

				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
		    }
		    public void onNothingSelected(AdapterView<?>  parent) {
		    }
		});

// Mode		
		LinearLayout modeHolder = (LinearLayout) findViewById(R.id.modeHolder);
		modeHolder.setBackgroundResource(R.drawable.list_selector_holo_dark);
		modeHolder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Spinner spin = (Spinner) findViewById(R.id.select_mode);
				spin.performClick();
			}
          });
		
		Spinner ringmode = (Spinner) findViewById(R.id.select_mode);
		ArrayAdapter<CharSequence> modeadapter = ArrayAdapter.createFromResource(ctx, R.array.ringMode_array, R.layout.spinner_layout);
		modeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ringmode.setAdapter(modeadapter);
		
		int rmode = cur.getInt(cur.getColumnIndex("ringmode"));
		ringmode.setSelection(rmode);
		hideshowvolume(rmode);
		ringmode.setOnItemSelectedListener(new OnItemSelectedListener(){
        	 public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        		hideshowvolume(pos);

        		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "ringmode", Integer.toString(pos));
				mDbHelper.close();

				SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
				Editor mEditor = prefs.edit();
				mEditor.putInt("event", -1);
				mEditor.commit();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
				
        		 
		    }
		    public void onNothingSelected(AdapterView<?>  parent) {
		      // Do nothing.
		    	return;
		    }
		});

		
///////////////// SeekBars ////////////////
		
// Volume		
        TextView volumeText = (TextView) findViewById(R.id.volumeText);
        int vol = cur.getInt(cur.getColumnIndex("volume"));
        volumeText.setText(getString(R.string.volume) + vol );
        SeekBar volume = (SeekBar) findViewById(R.id.volume);
        vol = (int) ((float) vol/(float) maxvolume*100);
        volume.setProgress( vol );
        volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			int vol = (int) Math.floor(maxvolume*progress/100);
    	        TextView volumeText = (TextView) findViewById(R.id.volumeText);
    	        volumeText.setText(getString(R.string.volume) + vol );
    		}
    		public void onStartTrackingTouch(SeekBar seekBar) {
    		}
    		public void onStopTrackingTouch(SeekBar seekBar) {
    			int tim = (int) Math.floor(maxvolume*seekBar.getProgress()/100);
				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "volume", Integer.toString(tim));
				mDbHelper.close();
				
				SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
				Editor mEditor = prefs.edit();
				mEditor.putInt("event", -1);
				mEditor.commit();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
    		}
    	});
		
        
// Before        
        
        TextView beforeEventText = (TextView) findViewById(R.id.beforeEventText);
        int before = cur.getInt(cur.getColumnIndex("before"));
		beforeEventText.setText(getString(R.string.before1) + before + getString(R.string.before2));
        SeekBar beforeEvent = (SeekBar) findViewById(R.id.beforeEvent);
        before = (int) ((float) before/(float) maxbefore*100);
        beforeEvent.setProgress( before );
        beforeEvent.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			int tim = (int) Math.floor(maxbefore*progress/100);
    			TextView beforeEventText = (TextView) findViewById(R.id.beforeEventText);
    			beforeEventText.setText(getString(R.string.before1) + tim + getString(R.string.before2));
    		}
    		public void onStartTrackingTouch(SeekBar seekBar) {
    		}
    		public void onStopTrackingTouch(SeekBar seekBar) {
    			int tim = (int) Math.floor(maxbefore*seekBar.getProgress()/100);
				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "before", Integer.toString(tim));
				mDbHelper.close();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
    		}
    	});
        
        TextView afterEventText = (TextView) findViewById(R.id.afterEventText);
        int after = cur.getInt(cur.getColumnIndex("after"));
        afterEventText.setText(getString(R.string.after1) + after + getString(R.string.after2));
        SeekBar afterEvent = (SeekBar) findViewById(R.id.afterEvent);
        after = (int) ((float) after/(float) maxafter*100);
        afterEvent.setProgress( after );
        afterEvent.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			int tim = (int) Math.floor(maxafter*progress/100);
    			TextView afterEventText = (TextView) findViewById(R.id.afterEventText);
    			afterEventText.setText(getString(R.string.after1) + tim + getString(R.string.after2));
    		}
    		public void onStartTrackingTouch(SeekBar seekBar) {
    		}
    		public void onStopTrackingTouch(SeekBar seekBar) {
    			int tim = (int) Math.floor(maxafter*seekBar.getProgress()/100);
				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.updateGroup(group, "after", Integer.toString(tim));
				mDbHelper.close();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
    		}
    	});
	        
			
		mDbHelper.close();
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);

		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
		mDbHelper.open();
		
		if (resultCode == RESULT_OK) {
			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			if (uri != null) {
				Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
				String name = ringtone.getTitle(this);
				TextView ringtoneName = (TextView) findViewById(R.id.selectRingtone);
		        ringtoneName.setText(name);
		        ringtoneName.requestFocus();
		        
				mDbHelper.updateGroup(group, "ringtone", uri.toString());
				
				SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
				Editor mEditor = prefs.edit();
				mEditor.putInt("event", -1);
				mEditor.commit();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
			}
            
		}

		Cursor cur = mDbHelper.getGroupData(group);
		cur.moveToNext();
        String cals = cur.getString(cur.getColumnIndex("calendars"));
		String[] calls = cals.split(",");
		TextView selectedCalendars = (TextView) findViewById(R.id.selectCalendars);
		selectedCalendars.setText((calls.length -  1) + getString(R.string.calendarsSelected));
		mDbHelper.close();
	}
	public void hideshowvolume(int pos){
		if(pos == 0 || pos==1){
			 findViewById(R.id.volumeTable).setVisibility(8);
			 findViewById(R.id.ringtoneTable).setVisibility(8);
		 }else if(pos==2){
			 findViewById(R.id.volumeTable).setVisibility(0);
			 findViewById(R.id.ringtoneTable).setVisibility(8);
		 }
		 else if(pos==3){
			 findViewById(R.id.volumeTable).setVisibility(8);
			 findViewById(R.id.ringtoneTable).setVisibility(0);
		 }else{
			 findViewById(R.id.volumeTable).setVisibility(0);
			 findViewById(R.id.ringtoneTable).setVisibility(0);
		 }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu1, menu);
		return true;
	}
	// If you update this don't forget to update this section of CalendarListActivity
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.RefreshEvents:
			ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
			break;
		case R.id.AdvancedSettings:
			Intent i = new Intent(ctx, AdvancedSettings.class);
			startActivity(i);
			this.finish();
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		refreshAdvanced();
		
	}
	
	@Override
	public void onBackPressed() {
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		if(prefs.getBoolean("advanced", false)){

			Intent i = new Intent(ctx, GroupsListActivity.class);
			startActivity(i);
			
		}
		this.finish();
	}
	
	public void refreshAdvanced(){

		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		
		
		((TextView)     findViewById(R.id.groupLabel)       ).setVisibility(prefs.getBoolean("advanced",    false)? View.VISIBLE: View.GONE);
		((LinearLayout) findViewById(R.id.groupHolder)      ).setVisibility(prefs.getBoolean("advanced",    false)? View.VISIBLE: View.GONE);
		((View)         findViewById(R.id.titleLine)        ).setVisibility(prefs.getBoolean("title",       false)? View.VISIBLE: View.GONE);
		((LinearLayout) findViewById(R.id.titleHolder)      ).setVisibility(prefs.getBoolean("title",       false)? View.VISIBLE: View.GONE);
		((View)         findViewById(R.id.descriptionLine)  ).setVisibility(prefs.getBoolean("description", false)? View.VISIBLE: View.GONE);
		((LinearLayout) findViewById(R.id.descriptionHolder)).setVisibility(prefs.getBoolean("description", false)? View.VISIBLE: View.GONE);
		((View)         findViewById(R.id.locationLine)     ).setVisibility(prefs.getBoolean("location",    false)? View.VISIBLE: View.GONE);
		((LinearLayout) findViewById(R.id.locationHolder)   ).setVisibility(prefs.getBoolean("location",    false)? View.VISIBLE: View.GONE);
		
	}
	public void setupAlertDialog(int holder,int text,String field, String value, String dialog, String extra ){
		
		TextView textView = (TextView) findViewById(text);
		textView.setText(extra + value);
		textView.setTag(value);
		LinearLayout holderView = (LinearLayout) findViewById(holder);
		holderView.setBackgroundResource(R.drawable.list_selector_holo_dark);
		
		String[] tag =  {field, Integer.toString(text),dialog,extra};
		holderView.setTag(tag);
		
		holderView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				String[] tag =  (String[]) v.getTag();
				
				String val =  (String) ((TextView) findViewById(Integer.parseInt(tag[1]))).getTag();
		
				input = new EditText(ctx);
				input.setText(val);
				input.setTag(tag);
				
				AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
				alert.setTitle(tag[2]);
				alert.setView(input);
				
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					String[] tag =  (String[]) input.getTag();
					
					String value = input.getText().toString();
					
					DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
					mDbHelper.open();
					mDbHelper.updateGroup(group, tag[0], value);
					mDbHelper.close();
					TextView tv = (TextView) findViewById(Integer.parseInt(tag[1]));
					tv.setText(tag[3]+value);
					tv.setTag(value);

					ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
				  }
				});
				alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});
				alert.show();
			}
          });
		
	}
}
