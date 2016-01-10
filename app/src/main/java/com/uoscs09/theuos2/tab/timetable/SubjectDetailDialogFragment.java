package com.uoscs09.theuos2.tab.timetable;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.gc.materialdesign.widgets.ColorSelector;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.tab.map.GoogleMapActivity;
import com.uoscs09.theuos2.tab.subject.CoursePlanDialogFragment;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.util.AppResources;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SubjectDetailDialogFragment extends BaseDialogFragment implements ColorSelector.OnColorSelectedListener {
    private static final String TAG = "SubjectDetailDialogFragment";

    @Bind(R.id.dialog_timetable_title)
    TextView mTimeTableDialogTitle;
    private Dialog mProgress;
    private Dialog mClassDivSelectDialog;
    @Bind(R.id.timetable_callback_alarm_spinner)
    Spinner mAlarmTimeSelectSpinner;

    private ArrayAdapter<SubjectInfoItem> mClassDivSelectAdapter;
    private Subject mSubject;
    private TimeTable mTimeTable;

    private final CoursePlanDialogFragment mCoursePlanDialogFragment = new CoursePlanDialogFragment();

    private final PieProgressDrawable pieProgressDrawable = new PieProgressDrawable();

    @Nullable
    private ColorSelector.OnColorSelectedListener mColorSelectedListener;

    public SubjectDetailDialogFragment() {
        pieProgressDrawable.setLevel(100);
    }

    public void setTimeTable(TimeTable timeTable) {
        mTimeTable = timeTable;
    }

    public void setSubject(@NonNull Subject subject) {
        mSubject = subject;
    }


    public void setColorSelectedListener(@Nullable ColorSelector.OnColorSelectedListener colorSelectedListener) {
        this.mColorSelectedListener = colorSelectedListener;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSubject != null) {
            int color = getActivity().getResources().getColor(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));

            if (mTimeTable.getColorTable().size() > 0) {
                Integer idx = mTimeTable.getColorTable().get(mSubject.subjectName);
                if (idx != null)
                    color = TimetableUtil.getTimeTableColor(getActivity(), idx);
            }

            pieProgressDrawable.setColor(color);
            mTimeTableDialogTitle.setText(mSubject.getSubjectNameLocal());
            mTimeTableDialogTitle.invalidateDrawable(pieProgressDrawable);

            if (mAlarmTimeSelectSpinner != null) {
                mAlarmTimeSelectSpinner.setTag(TAG);
                mAlarmTimeSelectSpinner.setSelection(TimetableAlarmUtil.readTimeSelection(getActivity(), mSubject.period, mSubject.day));
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(createView())
                .create();
    }

    private View createView() {
        mProgress = AppUtil.getProgressDialog(getActivity(), false, null);

        View view = View.inflate(getActivity(), R.layout.dialog_timetable_subject, null);
        ButterKnife.bind(this, view);

        int size = getResources().getDimensionPixelSize(R.dimen.timetable_color_icon_size);
        pieProgressDrawable.setBounds(0, 0, size, size);

        mTimeTableDialogTitle.setCompoundDrawables(pieProgressDrawable, null, null, null);
        mTimeTableDialogTitle.setCompoundDrawablePadding(40);

        //View alarmButton = view.findViewById(R.id.dialog_timetable_button_alarm);

        mAlarmTimeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mAlarmTimeSelectSpinner.getTag() != null) {
                    mAlarmTimeSelectSpinner.setTag(null);
                    return;
                }

                sendTrackerEvent("timetable alarm", "period : " + mSubject.period + " / day : " + mSubject.day);
                setOrCancelAlarm(mSubject, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (mSubject != null) {
            mAlarmTimeSelectSpinner.setTag(TAG);
            mAlarmTimeSelectSpinner.setSelection(TimetableAlarmUtil.readTimeSelection(getActivity(), mSubject.period, mSubject.day));
        }


        /*
        if (AppUtil.test) {
            spinner.setVisibility(View.VISIBLE);
            alarmButton.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
            alarmButton.setVisibility(View.GONE);
        }
*/
        initSelectDialog();

        return view;
    }


    @Override
    public void onColorSelected(int i) {

        if (mSubject != null && mTimeTable.getColorTable() != null) {
            Integer idx = mTimeTable.getColorTable().get(mSubject.subjectName);
            if (idx != null) {
                TimetableUtil.putTimeTableColor(getActivity(), idx, i);

                dismiss();
                if (mColorSelectedListener != null)
                    mColorSelectedListener.onColorSelected(i);
            }
        }
    }

    @OnClick(R.id.dialog_timetable_button_color)
    void showColorSelector() {
        if (getActivity() == null)
            return;

        int color;

        if (mSubject != null && mTimeTable.getColorTable() != null) {
            Integer idx = mTimeTable.getColorTable().get(mSubject.subjectName);
            color = idx != null ? TimetableUtil.getTimeTableColor(getActivity(), idx) : Color.BLACK;
        } else {
            color = Color.BLACK;
        }

        sendClickEvent("color table");
        ColorSelector colorSelector = new ColorSelector(getActivity(), color, this);
        colorSelector.show();
    }

    @OnClick(R.id.dialog_timetable_button_map)
    void showMap(View v) {
        if (getActivity() == null)
            return;

        if (mSubject != null && mSubject.univBuilding.code > 0) {
            Intent intent = new Intent(v.getContext(), GoogleMapActivity.class);
            intent.putExtra("building", mSubject.univBuilding.code);

            sendClickEvent("map");
            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
            dismiss();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }

    @OnClick(R.id.dialog_timetable_button_info)
    void showSubjectInfo() {

        if (getActivity() == null)
            return;

        if (mSubject != null && !mSubject.subjectName.equals(StringUtil.NULL)) {

            sendClickEvent("course plan");

            AppResources.Subjects.requestSubjectInfo(getActivity(), mSubject.subjectName, mTimeTable.year, mTimeTable.semesterCode.code)
                    .getAsync(
                            result -> {
                                mProgress.dismiss();

                                int size;
                                if (result == null || (size = result.size()) == 0) {
                                    AppUtil.showToast(getActivity(), R.string.tab_timetable_error_on_search_subject, true);
                                    dismiss();

                                } else if (size == 1) {
                                    showCoursePlan(result.get(0).toSubjectItem(mTimeTable, mSubject));
                                    dismiss();

                                } else {
                                    mClassDivSelectAdapter.clear();
                                    mClassDivSelectAdapter.addAll(result);
                                    mClassDivSelectAdapter.notifyDataSetChanged();

                                    mClassDivSelectDialog.show();
                                }

                            },
                            this::onError
                    );

            mProgress.show();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }

    }

    public void onError(Exception e) {
        mProgress.dismiss();

        e.printStackTrace();
        AppUtil.showErrorToast(getActivity(), e, isVisible());
    }

    private void setOrCancelAlarm(Subject subject, int spinnerSelection) {
        TimetableAlarmUtil.setOrCancelAlarm(getActivity(), subject, spinnerSelection);
    }


    private void initSelectDialog() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_timecallback, null);
        mClassDivSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mSubject.getSubjectNameLocal())
                .setView(dialogView)
                .setOnDismissListener(dialog -> dismiss())
                .create();

        mClassDivSelectAdapter = new ClassDivAdapter(getActivity(), new ArrayList<>());

        ListView divListView = (ListView) dialogView.findViewById(R.id.dialog_timetable_callback_listview_div);
        divListView.setAdapter(mClassDivSelectAdapter);
        divListView.setOnItemClickListener((adapter, arg1, pos, arg3) -> {
            showCoursePlan(mClassDivSelectAdapter.getItem(pos).toSubjectItem(mTimeTable, mSubject));

            mClassDivSelectDialog.dismiss();
        });
    }

    private void showCoursePlan(SubjectItem2 subject) {
        if (!mCoursePlanDialogFragment.isAdded()) {
            mCoursePlanDialogFragment.setSubjectItem(subject);
            mCoursePlanDialogFragment.show(getFragmentManager(), "course");
        }
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return TAG;
    }

    private static class ClassDivAdapter extends AbsArrayAdapter<SubjectInfoItem, ViewHolder> {

        public ClassDivAdapter(Context context, List<SubjectInfoItem> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public void onBindViewHolder(int position, SubjectDetailDialogFragment.ViewHolder holder) {
            holder.textView.setText(getItem(position).class_div);
        }

        @Override
        public SubjectDetailDialogFragment.ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new SubjectDetailDialogFragment.ViewHolder(convertView);
        }
    }

    private static class ViewHolder implements AbsArrayAdapter.IViewHolder {
        public final TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

}
