package com.cypho.quiet_time;

import android.database.sqlite.SQLiteDatabase;

	public class DbGroupsTable {

		// Database creation SQL statement
		private static final String DATABASE_CREATE = 
				"create table groups (" +
				
				// Group				
					"_id integer primary key autoincrement, " +
					"groupname text null, " +
				
				// Calendar Settings
					"calendars string  null, " +
					"allday integer  null, " +
					"busy integer  null, " +
					"title string  null, " +
					"location string  null, " +
					"description string  null, " +
					
				// When
					"before integer  null, " +
					"after integer  null, " +
					
				// Ringtone Settings
					"ringmode integer  null, " +
					"volume integer  null, " +
					"ringtone string  null" +
					
				");";

		public static void onCreate(SQLiteDatabase database) {
			database.execSQL(DATABASE_CREATE);
			database.execSQL("insert into groups(groupname,calendars,allday,busy,title,location,description,ringmode,volume,before,after,ringmode,volume,ringtone) values ('Default QuietTime Group','',0,0,'','','',0,0,0,0,0,0,'')");
		}

		public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			database.execSQL("DROP TABLE IF EXISTS groups");
			onCreate(database);
		}
	}
