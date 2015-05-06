package com.uoscs09.theuos2.tab.timetable;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.OApiUtil;

/**
 * 시간표 알림 이벤트를 받아 알림을 띄우는 BroadcastReceiver
 */
public class TimeTableNotiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {

            case TimetableAlarmUtil.ACTION_SET_ALARM:
                long[] vibrate = new long[]{200, 200, 500, 300};

                int code = intent.getIntExtra(TimetableAlarmUtil.INTENT_CODE, 0);
                if (code < 1)
                    return;

                int timeSelection = intent.getIntExtra(TimetableAlarmUtil.INTENT_TIME, 0);
                if (timeSelection < 1) {
                    return;
                }

                String subjectName = intent.getStringExtra(OApiUtil.SUBJECT_NAME);
                String when = context.getResources().getStringArray(R.array.tab_timetable_alarm_time_array)[timeSelection];
                String content = context.getString(R.string.tab_timetable_notify_subject, subjectName, when);

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                long time = System.currentTimeMillis();
                Notification notify = new NotificationCompat.Builder(context)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentTitle(context.getText(R.string.tab_timetable_noti))
                        .setContentText(content)
                        .setTicker(content)
                        .setLights(0xff00ff00, 1000, 3000)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setVibrate(vibrate)
                        .setWhen(time)
                        .build();

                nm.notify(code * 100, notify);
                break;
        }


    }
}
