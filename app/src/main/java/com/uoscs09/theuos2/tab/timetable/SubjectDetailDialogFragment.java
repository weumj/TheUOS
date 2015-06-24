package com.uoscs09.theuos2.tab.timetable;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.gc.materialdesign.widgets.ColorSelector;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.parse.ParseUtil;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.tab.map.SubMapActivity;
import com.uoscs09.theuos2.tab.subject.CoursePlanDialogFragment;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.util.ArrayList;
import java.util.List;


public class SubjectDetailDialogFragment extends DialogFragment implements View.OnClickListener, ColorSelector.OnColorSelectedListener {
    private static final String TAG = "SubjectDetailDialogFragment";
    private static final String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiSubjectList.oapi";

    private final ArrayMap<String, String> params;
    private static final XmlParser<ArrayList<SubjectInfoItem>> SUBJECT_INFO_PARSER = OApiUtil.getParser(SubjectInfoItem.class);

    private TextView mTimeTableDialogTitle;
    private Dialog mProgress;
    private Dialog mClassDivSelectDialog;
    private Spinner mAlarmTimeSelectSpinner;

    private ArrayAdapter<SubjectInfoItem> mClassDivSelectAdapter;
    private Subject mSubject;
    private TimeTable mTimeTable;

    private final CoursePlanDialogFragment mCoursePlanDialogFragment = new CoursePlanDialogFragment();

    private final PieProgressDrawable pieProgressDrawable = new PieProgressDrawable();

    @Nullable
    private ColorSelector.OnColorSelectedListener mColorSelectedListener;
    private SerializableArrayMap<String, Integer> colorTable;

    public SubjectDetailDialogFragment() {
        params = new ArrayMap<>();
        params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);

        pieProgressDrawable.setLevel(100);
    }

    public void setColorTable(SerializableArrayMap<String, Integer> colorTable) {
        this.colorTable = colorTable;
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
    public void onResume() {
        super.onResume();

        if (mSubject != null) {
            int color = getActivity().getResources().getColor(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
            if (colorTable != null) {
                Integer idx = colorTable.get(mSubject.subjectName);
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

        view.findViewById(R.id.dialog_timetable_button_map).setOnClickListener(this);
        view.findViewById(R.id.dialog_timetable_button_info).setOnClickListener(this);
        view.findViewById(R.id.dialog_timetable_button_color).setOnClickListener(this);

        int size = getResources().getDimensionPixelSize(R.dimen.timetable_color_icon_size);
        pieProgressDrawable.setBounds(0, 0, size, size);

        mTimeTableDialogTitle = (TextView) view.findViewById(R.id.dialog_timetable_title);
        mTimeTableDialogTitle.setCompoundDrawables(pieProgressDrawable, null, null, null);
        mTimeTableDialogTitle.setCompoundDrawablePadding(40);

        //View alarmButton = view.findViewById(R.id.dialog_timetable_button_alarm);
        mAlarmTimeSelectSpinner = (Spinner) view.findViewById(R.id.timetable_callback_alarm_spinner);

        mAlarmTimeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mAlarmTimeSelectSpinner.getTag() != null) {
                    mAlarmTimeSelectSpinner.setTag(null);
                    return;
                }

                TrackerUtil.getInstance(SubjectDetailDialogFragment.this).sendEvent(TAG, "timetable alarm", "period : " + mSubject.period + " / day : " + mSubject.day);
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

        if (mSubject != null && colorTable != null) {
            Integer idx = colorTable.get(mSubject.subjectName);
            if (idx != null) {
                TimetableUtil.putTimeTableColor(getActivity(), idx, i);

                dismiss();
                if (mColorSelectedListener != null)
                    mColorSelectedListener.onColorSelected(i);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_timetable_button_info:
                showSubjectInfo();
                break;

            case R.id.dialog_timetable_button_map:
                showMap(v);
                break;

            case R.id.dialog_timetable_button_color:
                showColorSelector();
                break;

        }
    }

    private void showColorSelector() {
        int color;

        if (mSubject != null && colorTable != null) {
            Integer idx = colorTable.get(mSubject.subjectName);
            color = idx != null ? TimetableUtil.getTimeTableColor(getActivity(), idx) : Color.BLACK;
        } else {
            color = Color.BLACK;
        }

        TrackerUtil.getInstance(this).sendClickEvent(TAG, "color table");
        ColorSelector colorSelector = new ColorSelector(getActivity(), color, this);
        colorSelector.show();
    }

    private void showMap(View v) {

        if (mSubject != null && mSubject.univBuilding.code > 0) {
            Intent intent = new Intent(getActivity(), SubMapActivity.class);
            intent.putExtra("building", mSubject.univBuilding.code);

            TrackerUtil.getInstance(this).sendClickEvent(TAG, "map");
            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
            dismiss();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }

    private void showSubjectInfo() {

        if (mSubject != null && !mSubject.subjectName.equals(StringUtil.NULL)) {

            TrackerUtil.getInstance(this).sendClickEvent(TAG, "course plan");
            AsyncUtil.execute(JOB);
            mProgress.show();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }


    void setOrCancelAlarm(Subject subject, int spinnerSelection) {
        TimetableAlarmUtil.setOrCancelAlarm(getActivity(), subject, spinnerSelection);
    }


    void initSelectDialog() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_timecallback, null);
        mClassDivSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mSubject.getSubjectNameLocal())
                .setView(dialogView)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dismiss();
                    }
                })
                .create();

        mClassDivSelectAdapter = new ClassDivAdapter(getActivity(), new ArrayList<SubjectInfoItem>());

        ListView divListView = (ListView) dialogView.findViewById(R.id.dialog_timetable_callback_listview_div);
        divListView.setAdapter(mClassDivSelectAdapter);
        divListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int pos, long arg3) {
                showCoursePlan(mClassDivSelectAdapter.getItem(pos).toSubjectItem(mTimeTable, mSubject));

                mClassDivSelectDialog.dismiss();
            }
        });
    }

    void showCoursePlan(SubjectItem2 subject) {
        if (!mCoursePlanDialogFragment.isAdded()) {
            mCoursePlanDialogFragment.setSubjectItem(subject);
            mCoursePlanDialogFragment.show(getFragmentManager(), "course");
        }
    }

    private final AsyncJob.Base<ArrayList<SubjectInfoItem>> JOB = new AsyncJob.Base<ArrayList<SubjectInfoItem>>() {

        @Override
        public ArrayList<SubjectInfoItem> call() throws Exception {
            params.put(OApiUtil.SUBJECT_NAME, mSubject.subjectName);
            params.put(OApiUtil.YEAR, Integer.toString(mTimeTable.year));
            params.put(OApiUtil.TERM, mTimeTable.semesterCode.code);

            return ParseUtil.parseXml(getActivity(), SUBJECT_INFO_PARSER, URL, params);

        }

        @Override
        public void onResult(ArrayList<SubjectInfoItem> result) {
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

        }

        @Override
        public void exceptionOccured(Exception e) {
            e.printStackTrace();
            AppUtil.showErrorToast(getActivity(), e, isVisible());
        }

        @Override
        public void onPostExcute() {
            mProgress.dismiss();
        }
    };


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

    private static class ViewHolder implements AbsArrayAdapter.ViewHoldable {
        public final TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(android.R.id.text1);
        }
    }


}
