	package com.cypho.quiet_time;

	import android.content.Context;
	import android.database.Cursor;
	import android.view.LayoutInflater;
	import android.view.View;
	import android.view.ViewGroup;
	import android.widget.CursorAdapter;
	import android.widget.TextView;

	public class GroupsCursorAdapter extends CursorAdapter {
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.groupsrow, parent, false);
			bindView(v, context, cursor);
			return v;
		}
		Context ctx;
		public GroupsCursorAdapter(Context context, Cursor c) {
			super(context, c);
			this.ctx = context;
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			TextView groupName = (TextView) view.findViewById(R.id.groupName);
			groupName.setText(cursor.getString(cursor.getColumnIndex("groupname")));
				
		}
	}

