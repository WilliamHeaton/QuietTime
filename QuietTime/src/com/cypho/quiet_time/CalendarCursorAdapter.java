package com.cypho.quiet_time;

import java.util.Arrays;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CalendarCursorAdapter extends CursorAdapter {
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, null, parent);
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.calendarrow, parent, false);
		bindView(v, context, cursor);
		return v;
	}
	Context ctx;
	String group;
	String[] calendars;
	public CalendarCursorAdapter(Context context, Cursor c, String g, String cals) {
		super(context, c);
		this.ctx = context;
		calendars = cals.split(",");
		group = g;
	}
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		TextView calName = (TextView) view.findViewById(R.id.calName);
		calName.setText(cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)));
		
		TextView accountName = (TextView) view.findViewById(R.id.accountName);
		accountName.setText(cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)));
		
		CheckBox check = (CheckBox) view.findViewById(R.id.CheckBox);
		check.setChecked(Arrays.asList(calendars).contains(cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars._ID))));
		
		check.setTag(cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars._ID)));
		check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String calID = (String) buttonView.getTag();
				String cals;
				
				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				Cursor cur = mDbHelper.getGroupData(group);
				cur.moveToNext();
				cals = cur.getString(cur.getColumnIndex("calendars"));
				
				if(isChecked){
					cals = cals+","+calID;
				}else{
					cals = cals.replace(","+calID, "");
				}
				mDbHelper.updateGroup(group, "calendars", cals);
				mDbHelper.close();
				ctx.sendBroadcast(new Intent(ctx, CheckForEvents.class));
			}
        });
	}
}
