package com.uoscs09.theuos2.tab.timetable;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mj.android.utils.task.Tasks;

public class TimetableAlarmUtil {
    private static final String TAG = "TimetableAlarmUtil";

    //TODO 파일을 일일히 기록하지 말고, Map 으로 처리하기
    private static final String FILE_PREFIX_SUBJECT = "timetable_alarm_subject_";
    private static final String FILE_PREFIX_TIME = "timetable_alarm_time_";
    private static final String FILE_ALARM_COUNT = "timetable_alarm_count_";

    static final String ACTION_SET_ALARM = "com.uoscs09.theuos2.tab.timetable.set_alarm";
    static final String INTENT_TIME = "com.uoscs09.theuos2.tab.timetable.INTENT_TIME";
    static final String INTENT_CODE = "com.uoscs09.theuos2.tab.timetable.INTENT_CODE";
    //static final String EXTRA_SUBJECT = "extra_subject";
    //static final String EXTRA_ALARM_TIME_INT = "extra_alarm_time_int";

    // ************ Component Enable / Disable **************

    private static void setNotificationReceiverEnabled(Context context, boolean enable) {
        ComponentName receiver = new ComponentName(context, TimeTableNotificationReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }


    // ************ ALARM **************

    /**
     * 주어진 과목과 알람 시간 정보로 알람을 설정하거나 취소하고 그 정보를 파일에 기록한다.
     */
    static void setOrCancelAlarm(final Context context, final TimetableSubject subject, final int alarmType) {

        // 알림 없음
        if (alarmType == 0) {
            cancelAlarm(context, subject.period, subject.day);

        } else {
            if (!PrefHelper.TimeTables.isNotifyServiceEnable()) {
                PrefHelper.TimeTables.putNotifyServiceEnable(true);
                setNotificationReceiverEnabled(context, true);
            }

            setAlarm(context, subject, alarmType);

        }

       Tasks.execute(() -> recordAlarmInfo(context, subject, alarmType));
    }


    /**
     * 알람 정보를 기록한다.
     */
    private static void recordAlarmInfo(Context context, TimetableSubject subject, int alarmType) {

        int period = subject.period, day = subject.day;
        int alarmCount = readAlarmCount();

        // 알림 없음
        if (alarmType == 0) {
            deleteSubject(period, day);
            deleteTimeSelection(period, day);

            if (alarmCount < 2) {
                deleteAlarmCount();
                setNotificationReceiverEnabled(context, false);
            } else {
                writeAlarmCount(--alarmCount);
            }

        } else {
            writeSubject(period, day, subject);
            writeTimeSelection(period, day, alarmType);

            writeAlarmCount(++alarmCount);

        }
    }


    /**
     * 기록되어 있는 모든 알람을 AlarmManager 에 등록한다.
     */
    public static void initAllAlarm(Context context) {
        if (!PrefHelper.TimeTables.isNotifyServiceEnable())
            return;

        final Context appContext = context.getApplicationContext();

        Tasks.execute(() -> {

            for (int period = 0; period < 15; period++) {
                for (int day = 0; day < 7; day++) {
                    registerAlarmFromFileOnStart(appContext, period, day);
                }
            }

        });

    }

    private static void registerAlarmFromFileOnStart(Context context, int period, int day) {
        TimetableSubject subject = TimetableAlarmUtil.readSubject(period, day);
        int alarmTimeSelection = TimetableAlarmUtil.readTimeSelection(period, day);

        // 파일이 정확히 등록되어 있는 경우만 알람을 설정함.
        if (subject != null && alarmTimeSelection != 0)
            setAlarm(context, subject, alarmTimeSelection);

        // 이 메소드는 부팅 시점에 실행되는 것 이므로
        // 등록되어있지 않은 경우, 취소할 필요는 없음.

        /*
        // 알림 없음
        if (subject == null || alarmTimeSelection == 0) {
            cancelAlarm(context, period, day);
        } else {
            setAlarm(context, subject);
        }
        */
    }

    /**
     * 현재 설정된 시간표 알림을 모두 취소한다.
     */
    public static void clearAllAlarm(Context context) {
        final Context appContext = context.getApplicationContext();

        Tasks.execute(() -> clearAllAlarmInner(appContext));

    }

    private static void clearAllAlarmInner(Context appContext) {
        AlarmManager am = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(appContext.getApplicationContext(), TimeTableNotificationReceiver.class)
                .setAction(ACTION_SET_ALARM)
                .putExtra(INTENT_CODE, 0);

        int day;
        for (int period = 0; period < 15; period++) {
            for (day = 1; day < 7; day++) {
                int code = getAlarmCode(period, day);

                PendingIntent pi = PendingIntent.getBroadcast(appContext, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                am.cancel(pi);

                deleteTimeSelection(period, day);
                deleteSubject(period, day);
            }
        }

        deleteAlarmCount();
        setNotificationReceiverEnabled(appContext, false);

     PrefHelper.TimeTables.putNotifyServiceEnable(false);
    }

    private static void cancelAlarm(Context context, int period, int day) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TimeTableNotificationReceiver.class)
                .setAction(TimetableAlarmUtil.ACTION_SET_ALARM)
                .putExtra(TimetableAlarmUtil.INTENT_CODE, 0);

        PendingIntent pi = PendingIntent.getBroadcast(context, getAlarmCode(period, day), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        Log.i(TAG, "cancel alarm : " + " [period : " + period + " / day : " + day + "]");
    }

    private static void setAlarm(Context context, TimetableSubject subject, int alarmType) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int period = subject.period, day = subject.day;
        int code = getAlarmCode(period, day);

        Intent intent = new Intent(context, TimeTableNotificationReceiver.class)
                .setAction(TimetableAlarmUtil.ACTION_SET_ALARM)
                .putExtra(TimetableAlarmUtil.INTENT_CODE, code)
                .putExtra(OApiUtil.SUBJECT_NAME, subject.subjectName)
                .putExtra(TimetableAlarmUtil.INTENT_TIME, readTimeSelection(period, day));

        long notiTime = getNotificationTime(period, day, alarmType);
        PendingIntent pi = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notiTime == -1) {
            am.cancel(pi);

        } else {
            Log.i(TAG, "set alarm : " + subject.subjectName + " [period : " + period + " / day : " + day + " / time : "
                    + new SimpleDateFormat("yyyy-MM-dd  kk:mm:ss", Locale.getDefault()).format(new Date(notiTime)) + "]");
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
    static long getNotificationTime(int period, int day, int alarmType) {
        int hour, minute;
        PeriodTime time = getPeriodTime(period);

        if (time == null)
            return -1;

        hour = time.hour;
        minute = time.minute;

        int beforeMinute = getTimeSelectionMinute(alarmType);
        if (beforeMinute == -1)
            return -1;

        minute -= beforeMinute;
        if (minute < 0) {
            hour -= 1;
            minute += 60;
        }

        int addDate = 0;

        Calendar alarmTime = Calendar.getInstance();

        int today = getToday(alarmTime);
        if (day > today) {
            addDate = day - today;
        }

        alarmTime.setTimeInMillis(System.currentTimeMillis());

        alarmTime.add(Calendar.DATE, addDate);
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);

        return alarmTime.getTimeInMillis();
    }

    /**
     * 오늘의 날짜를 반환함. 월 : 0 , 토 : 5, 일 6
     */
    private static int getToday(Calendar c) {
        int today = c.get(Calendar.DAY_OF_WEEK);
        return today < 2 ? 6 : today - 2;
    }


    /**
     * '몇 분전' 이 선택된 값에 따라 몇 분전의 값을 반환한다. -1의 경우에는 에러
     */
    static int getTimeSelectionMinute(int alarmType) {
        switch (alarmType) {

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


    //**** etc *********

    static int getAlarmCode(int period, int day) {
        return period * 10 + day;
    }

    static String getAlarmCodeString(int period, int day) {
        return Integer.toString(getAlarmCode(period, day));
    }


    //*************** I / O ****************

    static void writeSubject(int period, int day, TimetableSubject subject) {
        IOUtil.writeObjectToInternalFileSilent(FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day), subject);
    }

    static TimetableSubject readSubject(int period, int day) {
        return IOUtil.readInternalFileSilent(FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day));
    }

    static void deleteSubject(int period, int day) {
        IOUtil.deleteInternalFile(FILE_PREFIX_SUBJECT + getAlarmCodeString(period, day));
    }


    static void writeTimeSelection(int period, int day, int selection) {
        IOUtil.writeObjectToInternalFileSilent(FILE_PREFIX_TIME + getAlarmCodeString(period, day), selection);
    }

    static int readTimeSelection(int period, int day) {
        Object obj = IOUtil.readInternalFileSilent(FILE_PREFIX_TIME + getAlarmCodeString(period, day));

        if (obj == null)
            return 0;

        int selection = (int) obj;
        return selection > 0 ? selection : 0;
    }

    static void deleteTimeSelection(int period, int day) {
        IOUtil.deleteInternalFile(FILE_PREFIX_TIME + getAlarmCodeString(period, day));
    }

    static int readAlarmCount() {
        Object obj = IOUtil.readInternalFileSilent(FILE_ALARM_COUNT);

        if (obj == null)
            return 0;

        int count = (int) obj;
        return count > 0 ? count : 0;
    }

    static void writeAlarmCount(int count) {
        IOUtil.writeObjectToInternalFileSilent(FILE_ALARM_COUNT, count);
    }

    static void deleteAlarmCount() {
        IOUtil.deleteInternalFile(FILE_ALARM_COUNT);
    }

}
