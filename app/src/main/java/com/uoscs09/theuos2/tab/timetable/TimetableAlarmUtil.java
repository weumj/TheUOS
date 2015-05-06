package com.uoscs09.theuos2.tab.timetable;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

public class TimetableAlarmUtil {
    private static final String TAG = "TimetableAlarmUtil";

    static final String FILE_PREFIX_SUBJECT = "timetable_alarm_subject_";
    static final String FILE_PREFIX_TIME = "timetable_alarm_time_";

    static final String ACTION_SET_ALARM = "com.uoscs09.theuos2.tab.timetable.set_alarm";
    static final String INTENT_TIME = "com.uoscs09.theuos2.tab.timetable.INTENT_TIME";
    static final String INTENT_CODE = "com.uoscs09.theuos2.tab.timetable.INTENT_CODE";
    static final String EXTRA_SUBJECT = "extra_subject";
    static final String EXTRA_ALARM_TIME_INT = "extra_alarm_time_int";

    // ************ SERVICE ************

    public static ComponentName startService(Context context) {
        return startService(context, new Intent(context, TimeTableAlarmService.class));
    }

    static ComponentName startService(Context context, Subject subject, int timeSelection) {
        Intent service = new Intent(context, TimeTableAlarmService.class);
        service.putExtra(TimetableAlarmUtil.EXTRA_SUBJECT, (Parcelable) subject);
        service.putExtra(TimetableAlarmUtil.EXTRA_ALARM_TIME_INT, timeSelection);

        return startService(context, service);
    }

    public static boolean stopService(Context context) {
        Log.i(TAG, "Timetable Alarm Service stop...");

        PrefUtil.getInstance(context).put(PrefUtil.KEY_CHECK_TIMETABLE_NOTIFY_SERVICE, false);

        return context.stopService(new Intent(context, TimeTableAlarmService.class));
    }

    private static ComponentName startService(Context context, Intent intent) {
        ComponentName name = context.startService(intent);

        if (name != null) {
            Log.i(TAG, "Timetable Alarm Service start...");
            Log.i(TAG, name.toString());

            PrefUtil.getInstance(context).put(PrefUtil.KEY_CHECK_TIMETABLE_NOTIFY_SERVICE, true);
        }

        return name;
    }

    // ************ ALARM **************

    /**
     * 현재 설정된 시간표 알림을 모두 취소한다.
     */
    public static void clearAllAlarm(Context context) {
        final Context appContext = context.getApplicationContext();

        AsyncLoader.excuteFor(new Runnable() {
            @Override
            public void run() {
                clearAllAlarmInner(appContext);
            }
        });

    }

    /**
     * 현재 설정된 시간표 알림을 모두 취소한다.
     */
    public static void clearAllAlarmWithResult(Context context, AsyncLoader.OnTaskFinishedListener<Boolean> finishedListener) {
        final Context appContext = context.getApplicationContext();

        AsyncLoader.excute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return clearAllAlarmInner(appContext);
            }
        }, finishedListener);

    }

    private static boolean clearAllAlarmInner(Context appContext) {
        boolean stopped = stopService(appContext);

        AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(appContext.getApplicationContext(), TimeTableNotiReceiver.class)
                .setAction(ACTION_SET_ALARM)
                .putExtra(INTENT_CODE, 0);

        int day;
        for (int period = 0; period < 15; period++) {
            for (day = 1; day < 7; day++) {
                int code = getAlarmCode(period, day);

                PendingIntent pi = PendingIntent.getBroadcast(appContext, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.cancel(pi);

                deleteTimeSelection(appContext, period, day);
                deleteSubject(appContext, period, day);
            }
        }
        return stopped;
    }

    static void cancelAlarm(Context context, int period, int day) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TimeTableNotiReceiver.class)
                .setAction(TimetableAlarmUtil.ACTION_SET_ALARM)
                .putExtra(TimetableAlarmUtil.INTENT_CODE, 0);

        PendingIntent pi = PendingIntent.getBroadcast(context, getAlarmCode(period, day), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        Log.i(TAG, "cancel alarm : " + " [period : " + period + " / day : " + day + "]");
    }

    static void setAlarm(Context context, Subject subject) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int period = subject.period, day = subject.day;
        int code = getAlarmCode(period, day);

        Intent intent = new Intent(context, TimeTableNotiReceiver.class)
                .setAction(TimetableAlarmUtil.ACTION_SET_ALARM)
                .putExtra(TimetableAlarmUtil.INTENT_CODE, code)
                .putExtra(OApiUtil.SUBJECT_NAME, subject.subjectName)
                .putExtra(TimetableAlarmUtil.INTENT_TIME, readTimeSelection(context, period, day));

        long notiTime = getNotificationTime(context, period, day);
        PendingIntent pi = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notiTime == -1) {
            am.cancel(pi);

        } else {
            Log.i(TAG, "set alarm : " + subject.subjectName + " [period : " + period + " / day : " + day + " / time : " + TimeUtil.sFormat_yMd_kms.format(new Date(notiTime)) + "]");
            am.setRepeating(AlarmManager.RTC_WAKEUP, notiTime, AlarmManager.INTERVAL_DAY * 7, pi);

        }
    }


    // *********** TIME ****************

    static class PeriodTime {
        int hour, minute;
    }

    static PeriodTime getPeriodTime(int period) {
        if (period < 0 || period > 14)
            return null;

        PeriodTime outTime = new PeriodTime();
        switch (period) {
            case 10:
                outTime.hour = 18;
                outTime.minute = 45;
                break;
            case 11:
                outTime.hour = 19;
                outTime.minute = 35;
                break;
            case 12:
                outTime.hour = 20;
                outTime.minute = 20;
                break;
            case 13:
                outTime.hour = 21;
                outTime.minute = 10;
                break;
            case 14:
                outTime.hour = 21;
                outTime.minute = 55;
                break;
            default:
                outTime.hour = period + 9;
                outTime.minute = 0;
                break;
        }

        return outTime;
    }

    /**
     * 선택된 시간의 알림 시간을 반환한다.
     */
    static long getNotificationTime(Context context, int period, int day) {
        int hour, minute;
        PeriodTime time = getPeriodTime(period);

        if (time == null)
            return -1;

        hour = time.hour;
        minute = time.minute;

        int beforeMinute = getTimeSelectionMinute(context, period, day);
        if (beforeMinute == -1)
            return -1;

        minute -= beforeMinute;
        if (minute < 0) {
            hour -= 1;
            minute += 60;
        }

        Calendar alarmTime = Calendar.getInstance(Locale.KOREA);
        alarmTime. set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);

        //FIXME

        return alarmTime.getTimeInMillis();
    }

    /**
     * '몇 분전' 이 선택된 값에 따라 몇 분전의 값을 반환한다. -1의 경우에는 에러
     */
    static int getTimeSelectionMinute(Context context, int period, int day) {
        switch (readTimeSelection(context, period, day)) {

            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 20;
            case 4:
                return 30;
            case 5:
                return 45;
            case 6:
                return 60;

            default:
                return -1;
        }
    }

    /**
     * 현재 시간이 어느 교시인지 반환함.
     */
    static int getCurrentPeriod() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY), min = c.get(Calendar.MINUTE);
        if (hour < 9)
            return 0;

        if (hour < 18)
            return hour - 9;

        switch (hour) {
            case 18:
                if (min < 45)
                    return 9;
                else
                    return 10;

            case 19:
                if (min < 35)
                    return 10;
                else
                    return 11;

            case 20:
                if (min < 20)
                    return 11;
                else
                    return 12;

            case 21:
                if (min < 5)
                    return 12;
                else if (min < 55)
                    return 13;
                else
                    return 14;

            case 22:
            default:
                return 14;
        }

    }

    /**
     * 오늘의 날짜를 반환함. 월 : 0 , 토 : 5, 일 6
     */
    static int getToday() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return today < 2 ? 6 : today - 2;
    }


    /**
     * 현재 시간과 다음 알림 등록시간 (새벽 1시) 까지의 남은 시간을 비교함.
     */
    static long calculateWaitTimeUntilNextDay() {
        Calendar alarmTime = Calendar.getInstance(Locale.KOREA);


        if (alarmTime.get(Calendar.HOUR_OF_DAY) > 0) {
            alarmTime.add(Calendar.DATE, 1);
        }

        alarmTime.set(Calendar.HOUR_OF_DAY, 1);
        alarmTime.set(Calendar.MINUTE, 0);
        alarmTime.set(Calendar.SECOND, 0);

        Calendar thisTime = Calendar.getInstance();

        return alarmTime.getTimeInMillis() - thisTime.getTimeInMillis();
    }


    //**** etc *********

    static int getAlarmCode(int period, int day) {
        return period * 10 + day;
    }

    static String getAlarmCodeString(int period, int day) {
        return Integer.toString(getAlarmCode(period, day));
    }


    //*************** I / O ****************

    static void writeSubject(Context context, int period, int day, Subject subject) {
        IOUtil.writeObjectToFileSuppressed(context, FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day), subject);
    }

    static Subject readSubject(Context context, int period, int day) {
        return IOUtil.readFromFileSuppressed(context, FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day));
    }

    static void deleteSubject(Context context, int period, int day) {
        try {
            context.deleteFile(FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void writeTimeSelection(Context context, int period, int day, int selection) {
        IOUtil.writeObjectToFileSuppressed(context, FILE_PREFIX_TIME + getAlarmCodeString(period, day), selection);
    }

    static int readTimeSelection(Context context, int period, int day) {
        Object obj = IOUtil.readFromFileSuppressed(context, FILE_PREFIX_TIME + getAlarmCodeString(period, day));

        if (obj == null)
            return 0;

        int selection = (int) obj;
        return selection > 0 ? selection : 0;
    }

    static void deleteTimeSelection(Context context, int period, int day) {
        try {
            context.deleteFile(FILE_PREFIX_TIME + getAlarmCodeString(period, day));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
