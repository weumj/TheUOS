package com.uoscs09.theuos2.tab.timetable;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;

/** 시간표 알림 이벤트를 받아 알림을 띄우는 BroadcastReceiver */
public class TimeTableNotiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(TimeTableInfoCallback.ACTION_SET_ALARM)) {
			long[] vibrate = new long[] { 200, 200, 500, 300 };
			int code = intent
					.getIntExtra(TimeTableInfoCallback.INTENT_CODE, 11);
			String name = intent.getStringExtra(OApiUtil.SUBJECT_NAME);
			String when = IOUtil.readFromFileSuppressed(context,
					String.valueOf(code));
			if ("알림 없음".equals(when)) {
				return;
			}
			long time = System.currentTimeMillis();
			String content = "수업 (" + name + ") 시작 " + when + " 입니다.";
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notify = new NotificationCompat.Builder(context)
					.setDefaults(Notification.DEFAULT_ALL)
					.setAutoCancel(true)
					.setContentTitle(
							context.getText(R.string.tab_timetable_noti))
					.setContentText(content).setTicker(content)
					.setLights(0xff00ff00, 1000, 3000)
					.setSmallIcon(R.drawable.ic_launcher).setVibrate(vibrate)
					.setWhen(time).build();
			nm.notify(code * 100, notify);
		}
	}
}
