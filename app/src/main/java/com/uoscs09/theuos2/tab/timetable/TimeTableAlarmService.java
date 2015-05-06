package com.uoscs09.theuos2.tab.timetable;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.uoscs09.theuos2.util.TimeUtil;

import java.util.Date;


public class TimeTableAlarmService extends Service {
    private static final String TAG = "TimeTableAlarmService";

    private AlarmThread mAlarmThread;

    private class AlarmThread extends Thread {

        @Override
        public void run() {
            Log.i(TAG, "thread started");

            while (true) {
                try {
                    Date currentDate = new Date();
                    Log.i(TAG, "registering alarm at : " + TimeUtil.sFormat_yMd_kms.format(currentDate));

                    // period 의 범위는 0 ~ 14
                    // day 의 범위는 0 (월) ~ 5 (토) , 6 (일)
                    int period = TimetableAlarmUtil.getCurrentPeriod(), day = TimetableAlarmUtil.getToday();

                    // 일요일이 아닌 경우 기록된 알림을 읽어서 등록함.
                    if (day < 6)
                        registerTodayAlarm(period, day);

                    // 다음날의 알림 등록시간까지 기다림
                    long waitTime = TimetableAlarmUtil.calculateWaitTimeUntilNextDay();
                    currentDate.setTime(currentDate.getTime() + waitTime);
                    Log.i(TAG, "thread wait to : " + TimeUtil.sFormat_yMd_kms.format(currentDate));

                    synchronized (this) {
                        wait(waitTime);
                    }

                } catch (InterruptedException e) {
                    break;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            Log.i(TAG, "thread finished");

        }

        private void registerTodayAlarm(int startPeriod, int today) {
            for (int i = startPeriod; i < 15; i++) {
                registerAlarm(i, today);
            }
        }

        private void registerAlarm(int period, int day) {
            Subject subject = TimetableAlarmUtil.readSubject(getApplicationContext(), period, day);
            int alarmTimeSelection = TimetableAlarmUtil.readTimeSelection(getApplicationContext(), period, day);

            // 알림 없음
            if (subject == null || alarmTimeSelection == 0) {
                TimetableAlarmUtil.cancelAlarm(getApplicationContext(), period, day);
            } else {
                TimetableAlarmUtil.setAlarm(getApplicationContext(), subject);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 알람 시간 기록
        if (intent != null) {
            Subject subject = intent.getParcelableExtra(TimetableAlarmUtil.EXTRA_SUBJECT);
            if (subject != null) {
                int alarmTime = intent.getIntExtra(TimetableAlarmUtil.EXTRA_ALARM_TIME_INT, 0);
                registerAlarmTime(subject, alarmTime);

                if (TimetableAlarmUtil.getToday() == subject.day)
                    wakeAlarmThread();

            }

            return super.onStartCommand(intent, flags, startId);
        }

        wakeAlarmThread();


        return super.onStartCommand(intent, flags, startId);
    }

    private void wakeAlarmThread() {
        // 알람 등록 스레드 실행
        if (mAlarmThread == null) {
            mAlarmThread = new AlarmThread();
            mAlarmThread.start();

        } else {
            synchronized (mAlarmThread) {
                mAlarmThread.notify();
            }

        }
    }

    private void registerAlarmTime(Subject subject, int alarmTime) {
        // 알림 없음
        int period = subject.period, day = subject.day;

        if (alarmTime == 0) {
            TimetableAlarmUtil.deleteSubject(this, period, day);
            TimetableAlarmUtil.deleteTimeSelection(this, period, day);
        } else {
            TimetableAlarmUtil.writeSubject(this, subject.period, subject.day, subject);
            TimetableAlarmUtil.writeTimeSelection(this, subject.period, subject.day, alarmTime);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAlarmThread != null) {
            synchronized (mAlarmThread) {
                mAlarmThread.interrupt();
            }
            mAlarmThread = null;
        }

    }


}
