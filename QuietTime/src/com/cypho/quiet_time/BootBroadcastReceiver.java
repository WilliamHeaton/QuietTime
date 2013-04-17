package com.cypho.quiet_time;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.SharedPreferences;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		SharedPreferences prefs = ctx.getSharedPreferences("Cals",0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService("alarm");
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, prefs.getInt("frequency", 3600000), PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, CheckForEvents.class), 0));
	}
}
