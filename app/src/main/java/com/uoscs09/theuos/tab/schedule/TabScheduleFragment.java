package com.uoscs09.theuos.tab.schedule;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.base.AbsProgressFragment;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseSchedule;
import com.uoscs09.theuos.util.OApiUtil;
import com.uoscs09.theuos.util.StringUtil;

public class TabScheduleFragment extends AbsProgressFragment<ScheduleItemWrapper> {
    private TextView mTextView;
    private final ParseSchedule mParser = new ParseSchedule();
    private static final String URL = OApiUtil.URL_API_MAIN_DB + '?' + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;

    @Override
    public ScheduleItemWrapper call() throws Exception {
        return mParser.parse(HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,   Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_schedule, container, false);
        CalendarView calendarView = (CalendarView) v.findViewById(R.id.tab_schedule_calendarView);
        calendarView.setShowWeekNumber(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            calendarView.setWeekSeparatorLineColor(Color.TRANSPARENT);
        }
        // calendarView.

        mTextView = new TextView(getActivity());
        ScrollView s = (ScrollView) v.findViewById(R.id.tab_schedule_scroll);
        s.addView(mTextView);

        registerProgressView(v.findViewById(R.id.progress_layout));

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_restaurant, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                execute();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTransactResult(ScheduleItemWrapper result) {
        StringBuilder sb = new StringBuilder();
        for (ScheduleItem item : result.scheduleList) {
            for (String s : item.stringArray) {
                sb.append(s).append(StringUtil.NEW_LINE);
            }
            sb.append(StringUtil.NEW_LINE);
            sb.append("-------------------------------\n");
        }
        sb.append("-------------------------------\n");
        sb.append(StringUtil.NEW_LINE);
        for (BoardItem item : result.boardList) {
            for (String s : item.stringArray) {
                sb.append(s).append(StringUtil.NEW_LINE);
            }
            sb.append(StringUtil.NEW_LINE);
            sb.append("-------------------------------\n");
        }

        mTextView.setText(sb.toString());
    }
}
