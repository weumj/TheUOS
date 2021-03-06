package com.uoscs09.theuos2.tab.buildings;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsAnimDialogFragment;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class ClassroomTimetableDialogFragment extends AbsAnimDialogFragment {
    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "ClassroomTimetableDialogFragment";
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.tab_time_bar_parent)
    ViewGroup timeBarParent;

    ClassRoomTimetable classRoomTimetable;
    OApiUtil.Semester semester;

    public static void showTimetableDialog(Fragment f, ClassRoomTimetable classRoomTimetable, OApiUtil.Semester semester, View fromView) {
        ClassroomTimetableDialogFragment fragment = new ClassroomTimetableDialogFragment();
        fragment.classRoomTimetable = classRoomTimetable;
        fragment.semester = semester;
        if (classRoomTimetable.timetables().isEmpty()) {
            AppUtil.showToast(f.getActivity(), R.string.tab_building_classroom_timetable_result_empty);
        } else
            fragment.showFromView(f.getChildFragmentManager(), "timetable", fromView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (classRoomTimetable != null)
            outState.putParcelable("timetable", classRoomTimetable);
        if (semester != null)
            outState.putInt("semester", semester.ordinal());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            classRoomTimetable = savedInstanceState.getParcelable("timetable");
            semester = OApiUtil.Semester.values()[savedInstanceState.getInt("semester")];
        }

    }

    @Override
    protected View createView() {
        View view = View.inflate(getActivity(), R.layout.dialog_building_room_timetable, null);
        ButterKnife.bind(this, view);

        if (classRoomTimetable.timetables().isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_building_classroom_timetable_result_empty);
            dismiss();
        } else {
            ClassRoomTimetable.Timetable timetable = classRoomTimetable.timetables().get(0);

            toolbar.setTitle(R.string.tab_building_classroom_timetable);
            toolbar.setSubtitle(String.format("%s / %s", timetable.roomNo(), semester.nameByLocale()));

            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek > 0) // '시간' 부분 제외
                timeBarParent.getChildAt(dayOfWeek).setBackgroundColor(ResourceUtil.getAttrColor(getActivity(), R.attr.color_actionbar_title));

            listView.setAdapter(new TimetableListAdapter(getActivity(), classRoomTimetable.timetables()));
        }
        return view;
    }


    class TimetableListAdapter extends AbsArrayAdapter<ClassRoomTimetable.Timetable, TimetableListViewHolder> {

        public TimetableListAdapter(Context context, List<ClassRoomTimetable.Timetable> list) {
            super(context, R.layout.list_layout_building_classroom_timetable, list);
        }

        @Override
        public void onBindViewHolder(int position, TimetableListViewHolder holder) {
            holder.setView(position, getItem(position));
        }

        @Override
        public TimetableListViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new TimetableListViewHolder(convertView);
        }
    }

    ArrayMap<Integer, Integer> colorMap = new ArrayMap<>();
    int colorIndex = 0;

    class TimetableListViewHolder extends AbsArrayAdapter.ViewHolder {
        @BindViews({
                R.id.tab_timetable_list_text_period,
                R.id.tab_timetable_list_text_mon,
                R.id.tab_timetable_list_text_tue,
                R.id.tab_timetable_list_text_wed,
                R.id.tab_timetable_list_text_thr,
                R.id.tab_timetable_list_text_fri,
                R.id.tab_timetable_list_text_sat
        })
        public TextView[] views;
        ClassRoomTimetable.Timetable timetable;

        public TimetableListViewHolder(View itemView) {
            super(itemView);

            TextView textView = views[0];
            textView.setTextSize(16);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        void setView(int position, ClassRoomTimetable.Timetable timetable) {
            this.timetable = timetable;

            // period
            views[0].setText(String.valueOf(position + 1));

            for (int i = 1; i < 7; i++)
                setViewColor(views[i], timetable.dateInfo(i));
        }

        void setViewColor(TextView v, String s) {
            int hash = s.hashCode();
            if (hash == 0)
                v.setBackgroundResource(0);
            else {
                if (!colorMap.containsKey(hash)) {
                    colorMap.put(hash, colorIndex++);
                }

                v.setBackgroundResource(ResourceUtil.getOrderedColorRes(colorMap.get(hash)));

            }

            v.setText(s);
        }

/*
        @OnClick({
                R.id.tab_timetable_list_text_mon,
                R.id.tab_timetable_list_text_tue,
                R.id.tab_timetable_list_text_wed,
                R.id.tab_timetable_list_text_thr,
                R.id.tab_timetable_list_text_fri,
                R.id.tab_timetable_list_text_sat
        })

        void subjectClicked(View v) {
            if (timetable == null)
                return;

            switch (v.getId()) {
                case R.id.tab_timetable_list_text_mon:
                case R.id.tab_timetable_list_text_tue:
                case R.id.tab_timetable_list_text_wed:
                case R.id.tab_timetable_list_text_thr:
                case R.id.tab_timetable_list_text_fri:
                case R.id.tab_timetable_list_text_sat:
            }
        }
*/

    }

}
