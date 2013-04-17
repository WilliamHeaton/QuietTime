package com.cypho.quiet_time;

import java.util.Arrays;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.widget.Toast;

public class SetSilent extends BroadcastReceiver {
	Context ctx;
	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		Editor mEditor = prefs.edit();
		AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
		Toast toast;
		Calendar cal = Calendar.getInstance();
		ContentResolver cr = ctx.getContentResolver();
		Uri uri = CalendarContract.Instances.CONTENT_URI;
		Builder builder = uri.buildUpon();
		Cursor CALcur;

		int after,before,avail,allday,ringmode,volume,group,eventId;
		String title,location,description,ringtone;
		String[] calendars;
		Boolean on = false;
		
		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
		mDbHelper.open();
		
// Loop though Groups to check for an ongoing event
// If there is one or more ongoing events switch the ringtone to the proper settings
// Otherwise it is time to end silent time, go back to the default settings
		
		// Get group info, highest priority first
		
		Cursor DBcur;
		if(prefs.getBoolean("advanced", false))
			DBcur = mDbHelper.getGroupsData("ASC");
		else
			DBcur = mDbHelper.getGroupData("1");
		
		groupLoop: while (DBcur.moveToNext()) {
			// get data from database for this group
			after = 		DBcur.getInt(DBcur.getColumnIndex("after"));
			before = 		DBcur.getInt(DBcur.getColumnIndex("before"));
			allday = 		DBcur.getInt(DBcur.getColumnIndex("allday"));
			avail = 		DBcur.getInt(DBcur.getColumnIndex("busy"));
			title = 		DBcur.getString(DBcur.getColumnIndex("title"));
			location = 		DBcur.getString(DBcur.getColumnIndex("location"));
			description = 	DBcur.getString(DBcur.getColumnIndex("description"));
			ringmode = 		DBcur.getInt(DBcur.getColumnIndex("ringmode"));
			volume = 		DBcur.getInt(DBcur.getColumnIndex("volume"));
			ringtone = 		DBcur.getString(DBcur.getColumnIndex("ringtone"));
			group = 		DBcur.getInt(DBcur.getColumnIndex("_id"));
			calendars = 	DBcur.getString(DBcur.getColumnIndex("calendars")).split(",");

			//Query calendar events DB for events going on right now that are a fit for this group
			
			// must be going on right now
			String selection = "(" + CalendarContract.Instances.END   + " >  " + ( cal.getTimeInMillis() - ((after *60*1000) ) )  + " ) ";
			selection +=  " AND (" + CalendarContract.Instances.BEGIN + " <= " + ( cal.getTimeInMillis() + ((before*60*1000) ) )  + " ) ";

			// check allday status against group setting
			if(allday == 0)
				selection +=  " AND ( NOT " + CalendarContract.Instances.ALL_DAY  + " ) ";
			else if(allday == 2)
				selection +=  " AND ( " + CalendarContract.Instances.ALL_DAY  + " ) ";
			
			// check availability setting against group setting
			if(avail == 0)
				selection +=  " AND (" + CalendarContract.Instances.AVAILABILITY + " = " + CalendarContract.Instances.AVAILABILITY_BUSY  + " ) ";
			else if(avail == 1)
				selection +=  " AND (" + CalendarContract.Instances.AVAILABILITY + " = " + CalendarContract.Instances.AVAILABILITY_FREE  + " ) ";

			// check title, location, and description against group setting

			if(title.length()>0 && prefs.getBoolean("title", false))
				selection +=  " AND (" + CalendarContract.Instances.TITLE + " LIKE '%" + title.replace("'","\'")  + "%' ) ";
			
			if(location.length()>0 && prefs.getBoolean("location", false))
				selection +=  " AND (" + CalendarContract.Instances.EVENT_LOCATION + " LIKE '%" + location.replace("'","\'")  + "%' ) ";
			
			if(description.length()>0 && prefs.getBoolean("description", false))
				selection +=  " AND (" + CalendarContract.Instances.DESCRIPTION + " LIKE '%" + description.replace("'","\'")  + "%' ) ";
			
			
			// send the query
			selection = "( " + selection + " )";
			

			builder = uri.buildUpon();
			ContentUris.appendId(builder, cal.getTimeInMillis() - ((after *60*1000)));
			ContentUris.appendId(builder, cal.getTimeInMillis() + ((before*60*1000)));

			
			
			CALcur = cr.query(builder.build(), new String[] {CalendarContract.Instances._ID,CalendarContract.Instances.CALENDAR_ID,CalendarContract.Instances.TITLE, CalendarContract.Instances.END,CalendarContract.Instances.BEGIN}, selection, null, null);
			while (CALcur.moveToNext()) {
				
				// if the event is not in one of this groups calendars, move on
				//TODO would be nice if this was pre-query
				if(!Arrays.asList(calendars).contains(CALcur.getString( CALcur.getColumnIndex(CalendarContract.Instances.CALENDAR_ID))))
					continue;
				

				// There is an ongoing event, we are not going to want to end silent time now
				// setting a flag so we will remember this later
				on = true;
				
				eventId = 	CALcur.getInt( CALcur.getColumnIndex(CalendarContract.Instances._ID));
				
				// check that we are not currently in silent time for this event and in this group.
				// if we are, nothing more needs to be done
				if(prefs.getInt("group", 10000)==group && prefs.getInt("event", -1) == eventId)
			        break groupLoop;
				
				// If there are no ongoing events, save the current ringtone settings so we can restore them at the end
				if( prefs.getInt("group", 10000) == 10000 ){
			        mEditor.putInt("volume", audioManager.getStreamVolume(AudioManager.STREAM_RING));
			        mEditor.putInt("ringmode", audioManager.getRingerMode());
			        mEditor.putString("ringtone",RingtoneManager.getActualDefaultRingtoneUri(ctx,RingtoneManager.TYPE_RINGTONE).toString());
			        mEditor.commit();
                }

				// Let everyone know that quiet time is beginning
				if(prefs.getBoolean("toasts",false)){
					toast = Toast.makeText(ctx, "Silent Time Starting", Toast.LENGTH_SHORT);
					toast.show();
				}
				if(prefs.getBoolean("vibrateStart", false)){
					Vibrator vib = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE); 
					vib.vibrate(300);
				}
				
				// Since we are beginning silent time, save info about the event
				mEditor.putInt("group", group);
				mEditor.putInt("event", eventId);
		        mEditor.commit();
				
		        
		        // Change the ringer to this groups silent time settings
		        if(ringmode == 0){
					audioManager.setRingerMode(0);
					
					mEditor.putInt("qtringmode", 0);
			        mEditor.commit();
		        }else if(ringmode == 1){
					audioManager.setRingerMode(1);
					
					mEditor.putInt("qtringmode", 1);
			        mEditor.commit();
		        }else if(ringmode == 2){
					audioManager.setRingerMode(2);
		        	audioManager.setStreamVolume(AudioManager.STREAM_RING, volume,0);
		        	
		        	mEditor.putInt("qtringmode", 2);
					mEditor.putInt("qtvolume", volume);
			        mEditor.commit();
		        }else if(ringmode == 3){
					audioManager.setRingerMode(2);
					setRingtone(ringtone);
					
					mEditor.putInt("qtringmode", 2);
					mEditor.putString("qtringtone", ringtone);
			        mEditor.commit();
				}else{
					audioManager.setRingerMode(2);
		        	audioManager.setStreamVolume(AudioManager.STREAM_RING, volume,0);
					setRingtone(ringtone);
					
					mEditor.putInt("qtringmode", 2);
					mEditor.putInt("qtvolume", volume);
					mEditor.putString("qtringtone", ringtone);
			        mEditor.commit();
		        }
		        break groupLoop;
			}
		}
		mDbHelper.close();
		
		// if we are currently in silent time, but we did not find any ongoing events
		// then it is time to go back to default ringtone settings
		if(!on && prefs.getInt("group", 10000) != 10000){
			
			// let everyone know we are turning it off
			if(prefs.getBoolean("toasts",false)){
				toast = Toast.makeText(ctx, "Quiet Time Ending", Toast.LENGTH_SHORT);
				toast.show();
			}
			if(prefs.getBoolean("vibrateEnd", false)){
				Vibrator vib = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE); 
				vib.vibrate(300);
			}
			
			// set the ringmode to default
			if(audioManager.getRingerMode() == prefs.getInt("qtringmode",2) )
				audioManager.setRingerMode(prefs.getInt("ringmode",2));
			
			// set the volume to default
			if( audioManager.getStreamVolume(AudioManager.STREAM_RING) == prefs.getInt("qtvolume", 0) )
				audioManager.setStreamVolume(AudioManager.STREAM_RING, prefs.getInt("volume",audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)),0);
			
			// set the ringtone to default
			setRingtone(prefs.getString("ringtone", ""));
			
			
			// it is no longer quiet time
			mEditor.putInt("group", 10000);
			mEditor.putInt("event", -1);
			mEditor.commit();
			
		}
	}
	private void setRingtone(String ringtone){
		
    	if(ringtone.length()>1){
			Uri ringuri = Uri.parse(ringtone);
			RingtoneManager.setActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_RINGTONE, ringuri);
    	}
	} 
}
