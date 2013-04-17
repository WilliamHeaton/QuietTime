package com.cypho.quiet_time;

import java.util.Arrays;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ContentUris;
import android.database.Cursor;
import android.content.ContentResolver;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CalendarContract;
import android.widget.Toast;

public class CheckForEvents extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Toast toast;
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		if(prefs.getBoolean("toasts",false)){
			toast = Toast.makeText(ctx, "Checking Calendars", Toast.LENGTH_SHORT);
			toast.show();
		}

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService("alarm");
		Calendar cal = Calendar.getInstance();
		ContentResolver cr = ctx.getContentResolver();
		Uri uri = CalendarContract.Instances.CONTENT_URI;
		Builder builder = uri.buildUpon();
		
		Cursor CALcur;
		int after,before,freq,avail,allday,eventId;
        long dtend,dtstart;
		String title,location,description;
		String[] calendars;
		
		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
		mDbHelper.open();
		
		Cursor DBcur;
		if(prefs.getBoolean("advanced", false))
			DBcur = mDbHelper.getGroupsData("DESC");
		else
			DBcur = mDbHelper.getGroupData("1");
		
		
		while (DBcur.moveToNext()) {
			freq = prefs.getInt("frequency", 3600000);
			after = 		DBcur.getInt(DBcur.getColumnIndex("after"));
			before = 		DBcur.getInt(DBcur.getColumnIndex("before"));
			allday = 		DBcur.getInt(DBcur.getColumnIndex("allday"));
			avail = 		DBcur.getInt(DBcur.getColumnIndex("busy"));
			title = 		DBcur.getString(DBcur.getColumnIndex("title"));
			location = 		DBcur.getString(DBcur.getColumnIndex("location"));
			description = 	DBcur.getString(DBcur.getColumnIndex("description"));
			calendars = 	DBcur.getString(DBcur.getColumnIndex("calendars")).split(",");

			String selection = "(" + CalendarContract.Instances.END   + " >  " + ( cal.getTimeInMillis() - ((after *60*1000)            ) )  + " ) ";
			selection +=  " AND (" + CalendarContract.Instances.BEGIN + " <= " + ( cal.getTimeInMillis() + ((before*60*1000) + 2 * freq ) )  + " ) ";

			if(allday == 0)
				selection +=  " AND ( NOT " + CalendarContract.Instances.ALL_DAY  + " ) ";
			else if(allday == 2)
				selection +=  " AND ( " + CalendarContract.Instances.ALL_DAY  + " ) ";
			
			if(avail == 0)
				selection +=  " AND (" + CalendarContract.Instances.AVAILABILITY + " = " + CalendarContract.Instances.AVAILABILITY_BUSY  + " ) ";
			else if(avail == 1)
				selection +=  " AND (" + CalendarContract.Instances.AVAILABILITY + " = " + CalendarContract.Instances.AVAILABILITY_FREE  + " ) ";

			if(title.length()>0 && prefs.getBoolean("title", false))
				selection +=  " AND (" + CalendarContract.Instances.TITLE + " LIKE '%" + title.replace("'","\'")  + "%' ) ";
			
			if(location.length()>0 && prefs.getBoolean("location", false))
				selection +=  " AND (" + CalendarContract.Instances.EVENT_LOCATION + " LIKE '%" + location.replace("'","\'")  + "%' ) ";
			
			if(description.length()>0 && prefs.getBoolean("description", false))
				selection +=  " AND (" + CalendarContract.Instances.DESCRIPTION + " LIKE '%" + description.replace("'","\'")  + "%' ) ";

			
			selection = "( " + selection + " )";
			
			
			builder = uri.buildUpon();
			ContentUris.appendId(builder, cal.getTimeInMillis() - ((after *60*1000)));
			ContentUris.appendId(builder, cal.getTimeInMillis() + ((before*60*1000) + 2 * freq));

			
			
			CALcur = cr.query(builder.build(), new String[] {CalendarContract.Instances._ID,CalendarContract.Instances.CALENDAR_ID,CalendarContract.Instances.TITLE, CalendarContract.Instances.END,CalendarContract.Instances.BEGIN}, selection, null, null);
			while (CALcur.moveToNext()) {
				
				if(!Arrays.asList(calendars).contains(CALcur.getString( CALcur.getColumnIndex(CalendarContract.Instances.CALENDAR_ID))))
					continue;
				
				if(prefs.getBoolean("toasts",false)){
					toast = Toast.makeText(ctx, "Scheduleing Event: " + CALcur.getString( CALcur.getColumnIndex(CalendarContract.Instances.TITLE)) , Toast.LENGTH_SHORT);
					toast.show();
				}
				
				eventId = 	CALcur.getInt( CALcur.getColumnIndex(CalendarContract.Instances._ID));
	            dtend = 	CALcur.getLong(CALcur.getColumnIndex(CalendarContract.Instances.END))   + (after *60*1000);
	            dtstart = 	CALcur.getLong(CALcur.getColumnIndex(CalendarContract.Instances.BEGIN)) - (before*60*1000);
	            
	    		alarmManager.set(AlarmManager.RTC_WAKEUP, dtstart,  PendingIntent.getBroadcast(ctx,  eventId, new Intent(ctx, SetSilent.class), 0));
	    		alarmManager.set(AlarmManager.RTC_WAKEUP, dtend,    PendingIntent.getBroadcast(ctx, -eventId, new Intent(ctx, SetSilent.class), 0));
				
			}
		}
		mDbHelper.close();
		ctx.sendBroadcast(new Intent(ctx, SetSilent.class));
	}
}
