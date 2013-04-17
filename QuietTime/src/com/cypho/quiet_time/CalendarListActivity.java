package com.cypho.quiet_time;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;

public class CalendarListActivity extends ListActivity {
	Context ctx;
	String group;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		ctx = this;
		setContentView(R.layout.calendars);
		

		Bundle extras = getIntent().getExtras();
        group =  (extras != null) ? extras.getString("group") : "null";  
        DbGroupsAdapter mDbHelper = new DbGroupsAdapter(this);
		mDbHelper.open();
		Cursor cur = mDbHelper.getGroupData(group);
		String cals = "";
		if (cur.moveToNext()) {
			cals = cur.getString(cur.getColumnIndex("calendars"));
		}
		Cursor mCursor = getCalendars();
		ListAdapter adapter = new CalendarCursorAdapter(this, mCursor, group, cals);
		setListAdapter(adapter);
		mDbHelper.close();
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
            CheckBox c = (CheckBox) v.findViewById(R.id.CheckBox);
        	c.setChecked(!c.isChecked());
		}

	private Cursor getCalendars() {

        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        String[] projection = new String[] {
               CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
               CalendarContract.Calendars.ACCOUNT_NAME,
               CalendarContract.Calendars._ID
        };

        ContentResolver cr = getContentResolver();
        return cr.query(uri, projection, null, null, null);
        
	}
    
	public void refreshEvents(){
		AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, PendingIntent.getBroadcast(ctx, 5, new Intent(ctx, CheckForEvents.class), 0));
	}

}