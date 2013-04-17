package com.cypho.quiet_time;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbGroupsAdapter {

		// Database fields
		private Context context;
		private SQLiteDatabase db;
		private DbGroupsHelper dbHelper;

		public DbGroupsAdapter(Context context) {
			this.context = context;
		}

		public DbGroupsAdapter open() throws SQLException {
			dbHelper = new DbGroupsHelper(context);
			db = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
		}

		public void newGroup() {
			 db.execSQL("insert into groups(groupname,calendars,allday,busy,title,location,description,ringmode,volume,before,after,ringmode,volume,ringtone) values ('New QuietTime Group','',0,0,'','','',0,0,0,0,0,0,'')");
		}

		public void updateGroup(String group, String key, String value) {
			
			ContentValues values = new ContentValues();
			values.put(key, value);			
			db.update("groups", values, "_id=?", new String[]{group});
		}

		public boolean deleteGroup(long rowId) {
			return db.delete("groups", "_id" + "=" + rowId, null) > 0;
		}

		public Cursor getGroups() {
			return db.query("groups", new String[] { "_id", "groupname"},          null, null, null, null, "_id", null);
		}
		public Cursor getGroupData(String group) {
			return db.query("groups", null, "_id=?", new String[]{group}, null, null, "_id", null);
		}
		public Cursor getGroupsData(String order) {
			return db.query("groups", null, null,null, null, null, "_id "+order, null);
		}
	}