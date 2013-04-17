package com.cypho.quiet_time;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class GroupsListActivity extends ListActivity {
	Context ctx;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		ctx = this;

		setContentView(R.layout.groups);

		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		
		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(this);
		mDbHelper.open();
		
		Cursor mCursor = mDbHelper.getGroups();
		ListAdapter adapter = new GroupsCursorAdapter(this, mCursor);
		setListAdapter(adapter);
		mDbHelper.close();

        Button newgroup = (Button) findViewById(R.id.newgroup);
        newgroup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
				mDbHelper.open();
				mDbHelper.newGroup();
				
				Cursor mCursor = mDbHelper.getGroups();
				ListAdapter adapter = new GroupsCursorAdapter(ctx, mCursor);
				setListAdapter(adapter);
				mDbHelper.close();
			}
          });

		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService("alarm");
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, prefs.getInt("frequency", 3600000), PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, CheckForEvents.class), 0));
		
		if(!prefs.getBoolean("advanced", false)){
			Intent i = new Intent(ctx, GroupsSettings.class);
			i.putExtra("group", "1");
			startActivity(i);
			this.finish();
			//return;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		
		DbGroupsAdapter mDbHelper = new DbGroupsAdapter(ctx);
		mDbHelper.open();
		Cursor mCursor = mDbHelper.getGroups();
		ListAdapter adapter = new GroupsCursorAdapter(ctx, mCursor);
		setListAdapter(adapter);
		mDbHelper.close();
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);

			Intent i = new Intent(ctx, GroupsSettings.class);
			i.putExtra("group", Long.toString( id));
			startActivity(i);
			this.finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu1, menu);
		return true;
	}

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

}