package com.uoscs09.theuos2.widget.timetable;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.tab.timetable.TabTimeTableFragment;
import com.uoscs09.theuos2.tab.timetable.TimeTableItem;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

@Deprecated
public abstract class WidgetTimetableListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return getListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    protected abstract ListRemoteViewsFactory getListRemoteViewsFactory(Context context, Intent intent);

    protected static abstract class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private ArrayList<TimeTableItem> mWidgetItems;
        private final Context mContext;
        private final int mAppWidgetId;
        private int maxTime;
        private Hashtable<String, Integer> colorTable;
        private final int[] viewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_frame,
                R.id.widget_time_table_list_tue_frame,
                R.id.widget_time_table_list_wed_frame,
                R.id.widget_time_table_list_thr_frame,
                R.id.widget_time_table_list_fri_frame};
        private final int[] textViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon,
                R.id.widget_time_table_list_tue,
                R.id.widget_time_table_list_wed,
                R.id.widget_time_table_list_thr,
                R.id.widget_time_table_list_fri};
        private final int[] subViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_sub,
                R.id.widget_time_table_list_tue_sub,
                R.id.widget_time_table_list_wed_sub,
                R.id.widget_time_table_list_thr_sub,
                R.id.widget_time_table_list_fri_sub};

        public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
            this.mContext = applicationContext;
            this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public int getCount() {
            return PrefUtil.getInstance(mContext).get(
                    PrefUtil.KEY_TIMETABLE_LIMIT, false) ? maxTime
                    : mWidgetItems.size();
        }

        private void calculateMaxTime() {
            int size = mWidgetItems.size() - 1;

            // 마지막 수업시간 판별, 14부터 시작
            while (size > 0 && mWidgetItems.get(size).isTimeTableEmpty()) {
                size--;
            }

            maxTime = size + 1;
            if (maxTime < 0)
                maxTime = mWidgetItems.size() == 0 ? 0 : mWidgetItems.size() - 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        protected abstract boolean isBigSize();

        @Override
        public RemoteViews getViewAt(int position) {
            TimeTableItem item = mWidgetItems.get(position);
            RemoteViews views = new RemoteViews(mContext.getPackageName(), isBigSize() ? R.layout.list_layout_widget_timetable_5x4 : R.layout.list_layout_widget_timetable_4x4);


            String[] arr = {item.time, item.mon, item.tue, item.wed, item.thr, item.fri};

            views.setTextViewText(viewIds[0], arr[0].split("\n\n")[0]);

            if (colorTable == null) {
                getData();
            }
            Integer idx;
            TimeTableItem upperItem;
            String[] upperArray = null;
            if (position != 0) {
                upperItem = mWidgetItems.get(position - 1);
                upperArray = new String[]{null, upperItem.mon, upperItem.tue, upperItem.wed, upperItem.thr, upperItem.fri};
            }

            int id, subId;
            for (int i = 1; i < viewIds.length; i++) {
                id = textViewIds[i];
                subId = subViewIds[i];
                views.setTextColor(id, Color.WHITE);
                views.setTextColor(subId, Color.WHITE);

                // 현재 표시하려는 과목과 리스트뷰의 한 단계 위의 과목의 이름이 같으면
                // 내용을 표시하지 않음
                if (upperArray != null && OApiUtil.getSubjectName(upperArray[i]).equals(OApiUtil.getSubjectName(arr[i]))) {
                    views.setTextViewText(id, StringUtil.NULL);
                    views.setTextViewText(subId, StringUtil.NULL);

                } else {
                    String[] contents = removeTimetableProf(arr[i]);
                    if (contents != null) {
                        views.setTextViewText(id, contents[0]);
                        views.setTextViewText(subId, contents[1]);

                    } else {
                        views.setTextViewText(id, arr[i]);
                        views.setTextViewText(subId, StringUtil.NULL);
                    }
                }

                idx = colorTable.get(OApiUtil.getSubjectName(arr[i]));
                views.setInt(viewIds[i], "setBackgroundResource", idx != null ? AppUtil.getTimetableColor(idx) : 0);

            }

            // 오늘 날짜의 과목의 글자색을 검은색으로 줌
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            if (day > 0 && day < textViewIds.length) {
                views.setTextColor(textViewIds[day], Color.BLACK);
                views.setTextColor(subViewIds[day], Color.BLACK);
            }

            // 위젯 날짜가 시스템 날짜와 다르면 위젯을 업데이트 하라고 broadcast함
            PrefUtil pref = PrefUtil.getInstance(mContext);
            if (pref.get(TimeTableWidget.WIDGET_TIMETABLE_DAY, 0) != day) {
                pref.put(TimeTableWidget.WIDGET_TIMETABLE_DAY, day);
                mContext.sendBroadcast(new Intent(
                        AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
            }
            return views;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            getData();
        }

        @Override
        public void onDestroy() {
        }

        private void getData() {
            //mWidgetItems = TabTimeTableFragment.readTimetable(mContext);
            colorTable = TabTimeTableFragment.getColorTable(mWidgetItems, mContext);
            calculateMaxTime();
        }

        private String[] removeTimetableProf(String timetable) {
            String[] arr = timetable.trim().split(StringUtil.NEW_LINE);
            if (arr.length > 3) {
                return new String[]{
                        arr[0].trim(),
                        arr[2].trim() + StringUtil.NEW_LINE + arr[3].trim()
                };
            } else {
                return null;
            }
        }
    }
}
