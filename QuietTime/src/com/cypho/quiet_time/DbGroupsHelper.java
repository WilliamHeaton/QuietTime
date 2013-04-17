package com.cypho.quiet_time;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbGroupsHelper  extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "groupsettings";

	private static final int DATABASE_VERSION = 1;

	public DbGroupsHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		DbGroupsTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		DbGroupsTable.onUpgrade(database, oldVersion, newVersion);
	}
}
