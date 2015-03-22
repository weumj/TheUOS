package com.uoscs09.theuos2.tab.timetable;


import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.widgets.ColorSelector;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.common.UOSApplication;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseSubjectList2;
import com.uoscs09.theuos2.tab.map.SubMapActivity;
import com.uoscs09.theuos2.tab.subject.CoursePlanDialogFragment;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;


public class SubjectDetailDialogFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, Callable<ArrayList<SubjectInfoItem>>, ColorSelector.OnColorSelectedListener {
    private final Hashtable<String, String> params;
    private final ParseSubjectList2 mParser = new ParseSubjectList2();

    private TextView mTimeTableDialogTitle;
    private Dialog mProgress;
    private Dialog mClassDivSelectDialog;
    private ArrayAdapter<SubjectInfoItem> mClassDivSelectAdapter;
    private Subject mSubject;
    private TimeTable mTimeTable;

    private final CoursePlanDialogFragment mCoursePlanDialogFragment = new CoursePlanDialogFragment();

    private final PieProgressDrawable pieProgressDrawable = new PieProgressDrawable();

    @Nullable
    private ColorSelector.OnColorSelectedListener mColorSelectedListener;
    private Hashtable<String, Integer> colorTable;

    public SubjectDetailDialogFragment() {
        params = new Hashtable<>();
        params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);

        pieProgressDrawable.setLevel(100);
    }

    public void setColorTable(Hashtable<String, Integer> colorTable) {
        this.colorTable = colorTable;
    }

    public void setTimeTable(TimeTable timeTable) {
        mTimeTable = timeTable;
    }

    public void setSubject(Subject subject) {
        if (mSubject == null || !mSubject.isEqualsTo(subject)) {
            mSubject = subject;
        }
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
                    color = AppUtil.getTimeTableColor(getActivity(), idx);
            }

            pieProgressDrawable.setColor(color);
            mTimeTableDialogTitle.setText(mSubject.getSubjectNameLocal());
            mTimeTableDialogTitle.invalidateDrawable(pieProgressDrawable);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .customView(createView(), true)
                .build();
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

        View alarmButton = view.findViewById(R.id.dialog_timetable_button_alarm);
        Spinner spinner = (Spinner) view.findViewById(R.id.timetable_callback_alarm_spinner);

        spinner.setOnItemSelectedListener(this);


        if (AppUtil.test) {
            spinner.setVisibility(View.VISIBLE);
            alarmButton.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
            alarmButton.setVisibility(View.GONE);
        }

        initSelectDialog();

        return view;
    }


    @Override
    public void onColorSelected(int i) {
        // AppUtil.showToast(getActivity(), "" + i);

        if (mSubject != null && colorTable != null) {
            Integer idx = colorTable.get(mSubject.subjectName);
            if (idx != null) {
                AppUtil.putTimeTableColor(getActivity(), idx, i);

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
            color = idx != null ? AppUtil.getTimeTableColor(getActivity(), idx) : Color.BLACK;
        } else {
            color = Color.BLACK;
        }

        sendClickEvent("color table");
        ColorSelector colorSelector = new ColorSelector(getActivity(), color, this);
        colorSelector.show();
    }

    private void showMap(View v) {

        if (mSubject != null && mSubject.univBuilding.code > 0) {
            Intent intent = new Intent(getActivity(), SubMapActivity.class);
            intent.putExtra("building", mSubject.univBuilding.code);
            dismiss();

            sendClickEvent("map");
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                getActivity().startActivity(intent, ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
            } else{
                startActivity(intent);
            }


        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }

    private void showSubjectInfo() {

        if (mSubject != null && !mSubject.subjectName.equals(StringUtil.NULL)) {

            sendClickEvent("course plan");
            AsyncLoader.excute(this, CALLBACK);
            mProgress.show();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }

    void initSelectDialog() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_timecallback, null);
        mClassDivSelectDialog = new MaterialDialog.Builder(getActivity())
                .title(mSubject.getSubjectNameLocal())
                .customView(dialogView, false)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dismiss();
                    }
                })
                .build();

        mClassDivSelectAdapter = new ClassDivAdapter(getActivity(), new ArrayList<SubjectInfoItem>());

        ListView divListView = (ListView) dialogView.findViewById(R.id.dialog_timetable_callback_listview_div);
        divListView.setAdapter(mClassDivSelectAdapter);
        divListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int pos, long arg3) {
                showCoursePlan(mClassDivSelectAdapter.getItem(pos).toSubjectItem(mTimeTable));

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

    private final AsyncCallback<ArrayList<SubjectInfoItem>> CALLBACK = new AsyncCallback<ArrayList<SubjectInfoItem>>() {
        @Override
        public void onResult(ArrayList<SubjectInfoItem> result) {
            int size;
            if (result == null || (size = result.size()) == 0) {
                AppUtil.showToast(getActivity(), R.string.tab_timetable_error_on_search_subject, true);
                dismiss();

            } else if (size == 1) {
                showCoursePlan(result.get(0).toSubjectItem(mTimeTable));
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
        public void cancelled() {
        }

        @Override
        public void onPostExcute() {
            mProgress.dismiss();
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // PrefUtil.getInstance(context).put(PrefUtil.KEY_TIMETABLE_NOTIFY_TIME + pos + "-" + day, arg2);
        //setOrCancelAlarm(pos, day, subjectName, arg2, arg2 > 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private static final String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiSubjectList.oapi";

    @Override
    public ArrayList<SubjectInfoItem> call() throws Exception {
        params.put(OApiUtil.SUBJECT_NAME, mSubject.subjectName);
        params.put(OApiUtil.YEAR, Integer.toString(mTimeTable.year));
        params.put(OApiUtil.TERM, mTimeTable.semesterCode.code);

        HttpURLConnection connection = HttpRequest.getConnection(URL, StringUtil.ENCODE_EUC_KR, params);

        try {
            return mParser.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

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
        public SubjectDetailDialogFragment.ViewHolder getViewHolder(View convertView) {
            return new SubjectDetailDialogFragment.ViewHolder(convertView);
        }
    }

    private static class ViewHolder implements AbsArrayAdapter.ViewHolderable {
        public final TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    Tracker getTracker(UOSApplication.TrackerName name) {
        return ((UOSApplication) getActivity().getApplication()).getTracker(name);
    }

    Tracker getTracker() {
        return getTracker(UOSApplication.TrackerName.APP_TRACKER);
    }

    void sendTrackerEvent(String action, String label) {
        getTracker().send(new HitBuilders.EventBuilder()
                .setCategory("SubjectDetailDialogFragment")
                .setAction(action)
                .setLabel(label)
                .build());
    }

    void sendTrackerEvent(String action, String label, long value) {
        getTracker().send(new HitBuilders.EventBuilder()
                .setCategory("SubjectDetailDialogFragment")
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    void sendClickEvent(String label) {
        sendTrackerEvent("click", label);
    }

    protected void sendClickEvent(String label, long value) {
        sendTrackerEvent("click", label, value);
    }

    protected void sendEmptyViewClickEvent() {
        sendClickEvent("emptyView");
    }
}
