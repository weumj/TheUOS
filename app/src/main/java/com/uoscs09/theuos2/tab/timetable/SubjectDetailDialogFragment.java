package com.uoscs09.theuos2.tab.timetable;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
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
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SubjectDetailDialogFragment extends BaseDialogFragment implements ColorSelector.OnColorSelectedListener {
    private static final String TAG = "SubjectDetailDialogFragment";

    @BindView(R.id.dialog_timetable_title)
    TextView mTimeTableDialogTitle;
    private Dialog mProgress;
    private Dialog mClassDivSelectDialog;
    @BindView(R.id.timetable_callback_alarm_spinner)
    Spinner mAlarmTimeSelectSpinner;

    private ArrayAdapter<SubjectInfoItem> mClassDivSelectAdapter;
    private Timetable2.SubjectInfo mSubject;
    private Timetable2 mTimeTable;

    private final PieProgressDrawable pieProgressDrawable = new PieProgressDrawable();

    @Nullable
    private ColorSelector.OnColorSelectedListener mColorSelectedListener;

    public SubjectDetailDialogFragment() {
        pieProgressDrawable.setLevel(100);
    }

    public static void showDialog(Fragment fragment, Timetable2.SubjectInfo subject, Timetable2 timetable, @Nullable ColorSelector.OnColorSelectedListener colorSelectedListener) {
        SubjectDetailDialogFragment dialog = new SubjectDetailDialogFragment();
        dialog.setSubject(subject);
        dialog.setTimeTable(timetable);
        dialog.setColorSelectedListener(colorSelectedListener);
        dialog.setTargetFragment(fragment, 101);

        dialog.show(fragment.getFragmentManager(), "subject");
    }

    public void setTimeTable(Timetable2 timeTable) {
        mTimeTable = timeTable;
    }

    public void setSubject(@NonNull Timetable2.SubjectInfo subject) {
        mSubject = subject;
    }


    public void setColorSelectedListener(@Nullable ColorSelector.OnColorSelectedListener colorSelectedListener) {
        this.mColorSelectedListener = colorSelectedListener;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSubject != null) {
            int color = AppUtil.getAttrColor(getActivity(), R.attr.colorPrimary);

            if (mTimeTable.colorTable().size() > 0) {
                int i = mTimeTable.color(mSubject);
                color = TimetableUtil.getTimeTableColor(getActivity(), i);
            }

            pieProgressDrawable.setColor(color);
            mTimeTableDialogTitle.setText(mSubject.name());
            mTimeTableDialogTitle.invalidateDrawable(pieProgressDrawable);

            if (mAlarmTimeSelectSpinner != null) {
                mAlarmTimeSelectSpinner.setTag(TAG);
                // mAlarmTimeSelectSpinner.setSelection(TimetableAlarmUtil.readTimeSelection(mSubject.period, mSubject.day));
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(createView());
        return dialog;

        /*
        return new AlertDialog.Builder(getActivity())
                .setView(createView())
                .create();
                */
    }

    private View createView() {
        mProgress = AppUtil.getProgressDialog(getActivity());

        View view = View.inflate(getActivity(), R.layout.dialog_timetable_subject, null);
        ButterKnife.bind(this, view);

        int size = getResources().getDimensionPixelSize(R.dimen.timetable_color_icon_size);
        pieProgressDrawable.setBounds(0, 0, size, size);

        mTimeTableDialogTitle.setCompoundDrawables(pieProgressDrawable, null, null, null);
        mTimeTableDialogTitle.setCompoundDrawablePadding(40);

        //View alarmButton = view.findViewById(R.id.dialog_timetable_button_alarm);
/*
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
            mAlarmTimeSelectSpinner.setSelection(TimetableAlarmUtil.readTimeSelection(mSubject.period, mSubject.day));
        }
 */

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

        if (mSubject != null && mTimeTable.colorTable() != null) {
            int idx = mTimeTable.color(mSubject);
            TimetableUtil.putTimeTableColor(getActivity(), idx, i);

            dismiss();
            if (mColorSelectedListener != null)
                mColorSelectedListener.onColorSelected(i);
        }
    }

    @OnClick(R.id.dialog_timetable_button_color)
    void showColorSelector() {
        if (getActivity() == null)
            return;

        int color;

        if (mSubject != null && mTimeTable.colorTable() != null) {
            int idx = mTimeTable.color(mSubject);
            color = idx != -1 ? TimetableUtil.getTimeTableColor(getActivity(), idx) : Color.BLACK;
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

        if (mSubject != null && mSubject.building() != null && mSubject.building().code > 0) {

            Intent intent = GoogleMapActivity.startIntentWithErrorToast(getBaseActivity(), mSubject.building());

            if (intent != null) {
                sendClickEvent("map");
                AnimUtil.startActivityWithScaleUp(getActivity(), intent, v);
            } else {
                AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
            }
            dismiss();

        } else {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_no_subject);
        }
    }

    @OnClick(R.id.dialog_timetable_button_info)
    void showSubjectInfo(View v) {

        if (getActivity() == null)
            return;

        if (mSubject != null && !TextUtils.isEmpty(mSubject.nameKor())) {

            sendClickEvent("course plan");

            AppRequests.Subjects.requestSubjectInfo(mSubject.nameKor(), mTimeTable.year(), mTimeTable.semester().code).getAsync(result -> {
                        mProgress.dismiss();

                        int size;
                        if (result == null) {
                            AppUtil.showToast(getActivity(), R.string.tab_timetable_error_on_search_subject, true);
                            dismiss();

                        } else if ((size = result.size()) == 0) {
                            AppUtil.showToast(getActivity(), R.string.tab_timetable_error_on_search_subject_empty, true);
                            dismiss();

                        } else if (size == 1) {
                            showCoursePlan(result.get(0).toSubjectItem(mTimeTable, mSubject), v);
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

    public void onError(Throwable e) {
        mProgress.dismiss();

        AppUtil.showErrorToast(getActivity(), e, isVisible());
    }

    private void setOrCancelAlarm(Subject subject, int spinnerSelection) {
        TimetableAlarmUtil.setOrCancelAlarm(getActivity(), subject, spinnerSelection);
    }


    private void initSelectDialog() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_timecallback, null);
        mClassDivSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mSubject == null ? "" : mSubject.name())
                .setView(dialogView)
                .setOnDismissListener(dialog -> dismiss())
                .create();

        mClassDivSelectAdapter = new ClassDivAdapter(getActivity(), new ArrayList<>());

        ListView divListView = (ListView) dialogView.findViewById(R.id.dialog_timetable_callback_listview_div);
        divListView.setAdapter(mClassDivSelectAdapter);
        divListView.setOnItemClickListener((adapter, arg1, pos, arg3) -> {
            showCoursePlan(mClassDivSelectAdapter.getItem(pos).toSubjectItem(mTimeTable, mSubject), arg1);

            mClassDivSelectDialog.dismiss();
        });
    }

    private void showCoursePlan(SubjectItem2 subject, View v) {
        CoursePlanDialogFragment.fetchCoursePlanAndShow(getTargetFragment(), subject, v);
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
            holder.textView.setText(getItem(position).classDiv);
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
