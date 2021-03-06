package com.uoscs09.theuos2.appwidget.timetable;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsListRemoteViewsFactory;
import com.uoscs09.theuos2.tab.timetable.Timetable2;
import com.uoscs09.theuos2.tab.timetable.TimetableUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.Calendar;

public abstract class WidgetTimeTableListService2 extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return getListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    protected abstract ListRemoteViewsFactory getListRemoteViewsFactory(Context context, Intent intent);

    protected static abstract class ListRemoteViewsFactory extends AbsListRemoteViewsFactory<Timetable2.Period> {
        @Nullable
        private Timetable2 mTimeTable;
        private final int[] viewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_frame,
                R.id.widget_time_table_list_tue_frame,
                R.id.widget_time_table_list_wed_frame,
                R.id.widget_time_table_list_thr_frame,
                R.id.widget_time_table_list_fri_frame
        };
        private final int[] textViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon,
                R.id.widget_time_table_list_tue,
                R.id.widget_time_table_list_wed,
                R.id.widget_time_table_list_thr,
                R.id.widget_time_table_list_fri
        };
        private final int[] subViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_sub,
                R.id.widget_time_table_list_tue_sub,
                R.id.widget_time_table_list_wed_sub,
                R.id.widget_time_table_list_thr_sub,
                R.id.widget_time_table_list_fri_sub
        };

        private final String[] periodTimeArray;

        ListRemoteViewsFactory(Context context, Intent intent) {
            super(context, intent);
            periodTimeArray = context.getResources().getStringArray(R.array.tab_timetable_timelist_only_time);

            getData();
        }

        @Override
        public int getCount() {
            int defaultCount = super.getCount();
            if (mTimeTable != null && PrefHelper.TimeTables.isShowingLastEmptyPeriod()) {
                int maxCount = mTimeTable.maxPeriod();

                return maxCount > defaultCount ? defaultCount : maxCount;
            }
            return defaultCount;
        }


        protected abstract boolean isBigSize();

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(getContext().getPackageName(), isBigSize() ? R.layout.list_layout_widget_timetable_5x4 : R.layout.list_layout_widget_timetable_4x4);

            // guard
            if(position >= getCount())
                return views;

            views.setTextViewText(viewIds[0], periodTimeArray[position]);

            if (mTimeTable == null) {
                getData();
            }

            Timetable2.Period period = getItem(position);
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

            //월요일 (1) ~ 금요일 (5)
            for (int weekIndex = 1; weekIndex < viewIds.length; weekIndex++) {
                int subjectIndex = weekIndex - 1;
                int id = textViewIds[weekIndex];
                int subId = subViewIds[weekIndex];

                if (today == weekIndex) {
                    // 오늘 날짜의 과목의 글자색을 검은색으로
                    views.setTextColor(textViewIds[today], Color.BLACK);
                    views.setTextColor(subViewIds[today], Color.BLACK);
                } else {
                    views.setTextColor(id, Color.WHITE);
                    views.setTextColor(subId, Color.WHITE);
                }

                Timetable2.SubjectInfo subject = period.getSubjectInfo(subjectIndex);
                // 현재 표시하려는 과목과 한 단계 위의 과목의 이름이 같으면
                // 내용을 표시하지 않음
                if (subject == null || subject.isEqualPrior()) {
                    views.setTextViewText(id, "");
                    views.setTextViewText(subId, "");
                } else {
                    OApiUtil.UnivBuilding building = subject.building();
                    views.setTextViewText(id, StringUtil.emptyStringIfNull(subject.name()));
                    views.setTextViewText(subId, String.format("%s\n%s\n%s",
                            StringUtil.emptyStringIfNull(subject.professor()),
                            StringUtil.emptyStringIfNull(subject.location()),
                            building == null ? "" : StringUtil.emptyStringIfNull(building.getLocaleName()))
                    );
                }

                // 과목 배경색 설정
                int idx = mTimeTable.color(subject);
                views.setInt(viewIds[weekIndex], "setBackgroundColor", idx != -1 ? TimetableUtil.getTimeTableColor(getContext(), idx) : 0);
            }

            return views;
        }


        @Override
        public void onDataSetChanged() {
            getData();
        }

        @Override
        public void onDestroy() {
            mTimeTable = null;
        }

        private void getData() {
            AppRequests.TimeTables.readFile()
                    .subscribe(result -> this.mTimeTable = result,
                            Throwable::printStackTrace,
                            () -> {
                                clear();
                                if (mTimeTable != null) {
                                    addAll(mTimeTable.periods());
                                }
                            });
        }

    }
}
