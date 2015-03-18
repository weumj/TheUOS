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
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseSchedule;

public class TabScheduleFragment extends AbsDrawableProgressFragment<ScheduleItemWrapper> {
    TextView mTextView;

    private static final String URL = OApiUtil.URL_API_MAIN_DB + '?'
            + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;

    @Override
    public ScheduleItemWrapper call() throws Exception {
        return new ParseSchedule(HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR)).parse().get(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setMenuRefresh(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_schedule, container, false);
        CalendarView calendarView = (CalendarView) v
                .findViewById(R.id.tab_schedule_calendarView);
        calendarView.setShowWeekNumber(false);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            calendarView.setWeekSeparatorLineColor(Color.TRANSPARENT);
        }
        // calendarView.

        mTextView = new TextView(getActivity());
        ScrollView s = (ScrollView) v.findViewById(R.id.tab_schedule_scroll);
        s.addView(mTextView);
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
                excute();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected MenuItem getLoadingMenuItem(Menu menu) {
        return menu.findItem(R.id.action_refresh);
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
