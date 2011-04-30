package com.xinchejian.android.Robot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RobotBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent();
			i.setAction("com.xinchejian.android.Robot.RobotControlService");
			context.startService(i);

			Intent intent2 = new Intent(context, MainActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
		}
	}

}
