package com.uoscs09.theuos2.tab.timetable;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.TimeTableHttpRequest;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.IOException;
import java.util.Map;

import butterknife.Bind;

public class TabTimeTableFragment2 extends AbsProgressFragment<TimeTable>
        implements View.OnClickListener, Request.ResultListener<TimeTable>, Request.ErrorListener, Processor<TimeTable, TimeTable> {
    private AlertDialog mLoginDialog;
    View rootView;
    protected EditText mWiseIdView, mWisePasswdView;
    private Spinner mWiseTermSpinner, mWiseYearSpinner;
    private AlertDialog mDeleteDialog;
    @Bind(R.id.time_table_listView1)
    ListView mTimetableListView;
    @Bind(R.id.tab_timetable_empty)
    View emptyView;
    private Dialog mProgressDialog;

    @AsyncData
    private TimeTable mTimeTable;
    private TimeTableAdapter2 mTimeTableAdapter2;

    private final SerializableArrayMap<String, Integer> colorTable = new SerializableArrayMap<>();


    private static final ParseTimeTable2 TIME_TABLE_PARSER = new ParseTimeTable2();


    private final SubjectDetailDialogFragment mSubjectDetailDialog = new SubjectDetailDialogFragment();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Map<String, Integer> colorMap = (Map<String, Integer>) savedInstanceState.getSerializable("color");
            if (colorMap != null)
                colorTable.putAll(colorMap);
            mTimeTable = savedInstanceState.getParcelable(IOUtil.FILE_TIMETABLE);

        } else {
            mTimeTable = new TimeTable();
        }

        mSubjectDetailDialog.setColorTable(colorTable);
        mSubjectDetailDialog.setColorSelectedListener(i -> mTimeTableAdapter2.notifyDataSetChanged());

        initDialog();

        super.onCreate(savedInstanceState);

        if (mTimeTable.semesterCode != null)
            setTermTextViewText(mTimeTable);


        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, getToolbarParent(), false);

        mTimeTableAdapter2 = new TimeTableAdapter2(getActivity(), mTimeTable, colorTable);
        mTimeTableAdapter2.setOnItemClickListener((vh, v, subject) -> {
            if (subject.isEqualsTo(Subject.EMPTY))
                return;

            mSubjectDetailDialog.setSubject(subject);
            mSubjectDetailDialog.setTimeTable(mTimeTable);

            if (!mSubjectDetailDialog.isAdded()) {
                mSubjectDetailDialog.show(getFragmentManager(), "subject");
                sendClickEvent("detail subject");
            }

        });

        registerTabParentView(mTabParent);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(IOUtil.FILE_TIMETABLE, mTimeTable);
        outState.putSerializable("color", colorTable);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected int getLayout() {
        return R.layout.tab_timetable;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        emptyView.findViewById(R.id.tab_timetable_empty_text).setOnClickListener(this);

        mTimetableListView.setEmptyView(emptyView);
        mTimetableListView.setAdapter(mTimeTableAdapter2);

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        if (savedInstanceState == null)
            readTimetableFromFileOnFragmentCreated();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_timetable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wise:
                if (isTaskRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);

                } else {
                    sendClickEvent("wise login");
                    mLoginDialog.show();
                }
                return true;

            case R.id.action_delete:
                if (mDeleteDialog == null) {
                    initDeleteDialog();
                }

                sendClickEvent("delete timetable");
                mDeleteDialog.show();
                return true;

            case R.id.action_save:

                sendClickEvent("save timetable image");
                saveTimetableImage();
                return true;

            default:
                return false;
        }
    }

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
        mProgressDialog.setOnCancelListener(null);
    }

    private void saveTimetableImage() {
        if (mTimeTableAdapter2.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_not_exist, true);
            return;
        }

        if (mProgressDialog == null)
            mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, getText(R.string.progress_ongoing), null);

        mTimeTableAdapter2.changeLayout(true);

        final String picturePath = PrefUtil.getPicturePath(getActivity());
        String savedPath = picturePath + "/timetable_" + mTimeTable.year + '_' + mTimeTable.semesterCode + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

        final AsyncTask<Void, ?, String> task = new ImageUtil.ListViewBitmapRequest.Builder(mTimetableListView, mTimeTableAdapter2)
                .setHeaderView(getTabParentView())
                .build()
                .wrap(new ImageUtil.ImageWriteProcessor(savedPath))
                .getAsync(
                        result -> {
                            dismissProgressDialog();
                            mTimeTableAdapter2.changeLayout(false);

                            String pictureDir = picturePath.substring(picturePath.lastIndexOf('/') + 1);
                            Snackbar.make(rootView, getString(R.string.tab_timetable_saved, pictureDir), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_open, v -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.parse("file://" + result), "image/*");

                                        try {
                                            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                            sendClickEvent("show timetable image");
                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();
                                            AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            AppUtil.showErrorToast(getActivity(), e, true);
                                        }
                                    })
                                    .show();
                        },
                        e -> {
                            dismissProgressDialog();
                            mTimeTableAdapter2.changeLayout(false);

                            AppUtil.showErrorToast(getActivity(), e, true);
                        }
                );
        mProgressDialog.setOnCancelListener(dialog -> AsyncUtil.cancelTask(task));
        mProgressDialog.show();

    }

    private void setTermTextViewText(@NonNull TimeTable timeTable) {
        setSubtitleWhenVisible(timeTable.getYearAndSemester());
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        emptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        clearPassWd();
    }

    void execute() {
        Semester semester = Semester.values()[mWiseTermSpinner.getSelectedItemPosition()];
        String mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();

        execute(true, TimeTableHttpRequest.newRequest(mWiseIdView.getText(), mWisePasswdView.getText(), semester, mTimeTableYear)
                .wrap(new XmlParserWrapper<>(TIME_TABLE_PARSER))
                .wrap(IOUtil.<TimeTable>newFileWriteProcessor(getActivity(), IOUtil.FILE_TIMETABLE))
                .wrap(this), this, this, true);
    }

    @Override
    public TimeTable process(TimeTable timeTable) throws Exception {
        // 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
        // 시간표의 과목과 과목의 색을 Mapping 한다.
        if (timeTable != null && !timeTable.isEmpty()) {

            Context context = getActivity();
            SerializableArrayMap<String, Integer> newColorTable = TimetableUtil.makeColorTable(timeTable);
            TimetableUtil.saveColorTable(context, newColorTable);

            colorTable.clear();
            colorTable.putAll((SimpleArrayMap<String, Integer>) newColorTable);

            timeTable.getClassTimeInformationTable();
            //TimetableUtil.writeTimetable(context, result);

        }
        return timeTable;
    }

    @Override
    public void onResult(TimeTable result) {
        if (result == null || result.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());

            if (mTimeTableAdapter2.isEmpty())
                emptyView.setVisibility(View.VISIBLE);

            return;
        }

        mTimeTable.copyFrom(result);
        mTimeTableAdapter2.notifyDataSetChanged();

        setTermTextViewText(mTimeTable);
    }

    @Override
    public void onError(Exception e) {
        if (mTimeTableAdapter2.isEmpty())
            emptyView.setVisibility(View.VISIBLE);

        if (e instanceof IOException || e instanceof NullPointerException) {
            e.printStackTrace();
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
        } else {
            simpleErrorRespond(e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_empty_text:
                sendEmptyViewClickEvent();
                mLoginDialog.show();
                break;

            default:
                break;
        }
    }

    private void readTimetableFromFileOnFragmentCreated() {
        AsyncUtil.newRequest(() -> {
                    TimeTable timeTable = TimetableUtil.readTimetable(getActivity());
                    if (timeTable != null) {
                        colorTable.clear();
                        SimpleArrayMap<String, Integer> map = TimetableUtil.readColorTableFromFile(getActivity());
                        if (map != null)
                            colorTable.putAll(map);
                        timeTable.getClassTimeInformationTable();
                    }

                    return timeTable;
                }
        ).getAsync(result -> {
                    if (result == null || result.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);

                    } else {
                        mTimeTable.copyFrom(result);
                        mTimeTableAdapter2.notifyDataSetChanged();

                        setTermTextViewText(mTimeTable);
                    }
                },
                e -> Log.e("TimeTable", "cannot read timetable from file.", e)
        );
    }

    private void initDialog() {
        Context context = getActivity();
        View wiseDialogLayout = View.inflate(context, R.layout.dialog_timetable_wise_login, null);

        mWiseYearSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_year);
        mWiseYearSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, OApiUtil.getYears()));
        mWiseYearSpinner.setSelection(2);

        mWiseIdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_id_input);
        mWisePasswdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_passwd_input);
        mWiseTermSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_term);

        DialogInterface.OnClickListener l = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    loginToWise();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    clearPassWd();
                    break;

                default:
                    break;
            }
        };

        mLoginDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.tab_timetable_wise_login_title)
                .setView(wiseDialogLayout)
                .setPositiveButton(R.string.confirm, l)
                .setNegativeButton(R.string.cancel, l)
                .create();
    }

    private void loginToWise() {
        String id = mWiseIdView.getText().toString();
        Context context = getActivity();

        if (id.equals("123456789") && mWisePasswdView.length() < 1) {
            if (AppUtil.test) {
                AppUtil.test = false;
            } else {
                AppUtil.test = true;
                AppUtil.showToast(context, "test", isVisible());
            }

            PrefUtil.getInstance(context).put("test", AppUtil.test);
            clearText();
            return;
        }

        if (mWisePasswdView.length() < 1 || StringUtil.NULL.equals(id)) {
            AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_null, true);
            clearText();
        } else {
            execute();
        }
    }


    private void clearText() {
        clearId();
        clearPassWd();
    }

    private void clearId() {
        if (mWiseIdView != null && mWiseIdView.length() > 0) {
            TextKeyListener.clear(mWiseIdView.getText());
        }
    }

    private void clearPassWd() {
        if (mWisePasswdView != null && mWisePasswdView.length() > 0) {
            TextKeyListener.clear(mWisePasswdView.getText());
        }
    }


    private void initDeleteDialog() {
        mDeleteDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    deleteTimetable();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    void deleteTimetable() {
        mDeleteDialog.show();
        AsyncUtil.newRequest(
                () -> TimetableUtil.deleteTimetable(getActivity()))
                .getAsync(
                        result -> {
                            mDeleteDialog.dismiss();
                            if (result) {
                                mTimeTableAdapter2.clear();

                                mTimeTable.copyFrom(new TimeTable());
                                mTimeTableAdapter2.notifyDataSetChanged();

                                AppUtil.showToast(getActivity(), R.string.execute_delete, isVisible());
                                setSubtitleWhenVisible(null);
                            } else {
                                AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                            }
                        },
                        e -> {
                            mDeleteDialog.dismiss();
                            AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                        }
                );

    }


    @Override
    protected CharSequence getSubtitle() {
        if (mTimeTable != null)
            return mTimeTable.getYearAndSemester();
        else
            return null;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabTimeTableFragment2";
    }

}
