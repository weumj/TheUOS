package com.uoscs09.theuos2.tab.timetable;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.method.TextKeyListener;
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

import com.gc.materialdesign.widgets.ColorSelector;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.common.AsyncLoader.OnTaskFinishedListener;
import com.uoscs09.theuos2.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos2.http.TimeTableHttpRequest;
import com.uoscs09.theuos2.parse.ParseTimeTable2;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;

public class TabTimeTableFragment2 extends AbsProgressFragment<TimeTable> implements View.OnClickListener {
    @ReleaseWhenDestroy
    private AlertDialog mLoginDialog;
    @ReleaseWhenDestroy
    protected View rootView;
    @ReleaseWhenDestroy
    protected EditText mWiseIdView, mWisePasswdView;
    @ReleaseWhenDestroy
    private Spinner mWiseTermSpinner, mWiseYearSpinner;
    @ReleaseWhenDestroy
    private AlertDialog mDeleteDialog;
    @ReleaseWhenDestroy
    protected ListView mTimetableListView;

    private final ParseTimeTable2 mParser2 = new ParseTimeTable2();
    @AsyncData
    private TimeTable mTimeTable;
    private TimeTableAdapter2 mTimeTableAdapter2;

    private final Hashtable<String, Integer> colorTable = new Hashtable<>();
    @ReleaseWhenDestroy
    private View emptyView;


    private final SubjectDetailDialogFragment mSubjectDetailDialog = new SubjectDetailDialogFragment();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            colorTable.putAll((Map<String, Integer>) savedInstanceState.getSerializable("color"));
            mTimeTable = savedInstanceState.getParcelable(IOUtil.FILE_TIMETABLE);

        } else {
            mTimeTable = new TimeTable();

        }

        mSubjectDetailDialog.setColorTable(colorTable);
        mSubjectDetailDialog.setColorSelectedListener(new ColorSelector.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
                mTimeTableAdapter2.notifyDataSetChanged();
            }
        });

        initDialog();

        super.onCreate(savedInstanceState);

        if (mTimeTable.semesterCode != null)
            setTermTextViewText(mTimeTable);


        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, getToolbarParent(), false);

        mTimeTableAdapter2 = new TimeTableAdapter2(getActivity(), mTimeTable, colorTable);
        mTimeTableAdapter2.setOnItemClickListener(new TimeTableAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(TimeTableAdapter2.TimeTableViewHolder vh, View v, Subject subject) {
                if (subject.isEqualsTo(Subject.EMPTY))
                    return;

                mSubjectDetailDialog.setSubject(subject);
                mSubjectDetailDialog.setTimeTable(mTimeTable);

                if (!mSubjectDetailDialog.isAdded()) {
                    mSubjectDetailDialog.show(getFragmentManager(), "subject");
                    sendClickEvent("detail subject");
                }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_timetable, container, false);

        emptyView = rootView.findViewById(R.id.tab_timetable_empty);
        emptyView.findViewById(R.id.tab_timetable_empty_text).setOnClickListener(this);

        mTimetableListView = (ListView) rootView.findViewById(R.id.time_table_listView1);
        mTimetableListView.setEmptyView(emptyView);
        mTimetableListView.setAdapter(mTimeTableAdapter2);


        FloatingActionButton actionButton = (FloatingActionButton) rootView.findViewById(R.id.tab_timetable_action_btn);
        actionButton.setOnClickListener(this);

        registerProgressView(rootView.findViewById(R.id.progress_layout));


        if (savedInstanceState == null)
            readTimetableFromFileOnFragmentCreated();

        return rootView;
    }

    private void readTimetableFromFileOnFragmentCreated() {
        AsyncLoader.excute(
                new Callable<TimeTable>() {
                    @Override
                    public TimeTable call() throws Exception {
                        TimeTable timeTable = TimetableUtil.readTimetable(getActivity());
                        if (timeTable != null) {
                            colorTable.clear();
                            colorTable.putAll(TimetableUtil.readColorTableFromFile(getActivity()));
                        }
                        return timeTable;
                    }
                },
                new AsyncCallback.Base<TimeTable>() {
                    @Override
                    public void onResult(TimeTable timeTable) {
                        if (timeTable == null || timeTable.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);

                        } else {
                            mTimeTable.copyFrom(timeTable);
                            mTimeTableAdapter2.notifyDataSetChanged();

                            setTermTextViewText(mTimeTable);
                        }
                    }

                }

        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_timetable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.action_wise:
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                } else
                    loginDialog.show();
                return true;
*/
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


    private void saveTimetableImage() {
        if (mTimeTableAdapter2.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_id, true);
            return;
        }

        String dir = PrefUtil.getPictureSavedPath(getActivity()) + "timetable_" + mTimeTable.year + '_' + mTimeTable.semesterCode + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

        TimeTableImageSaveTask task = new TimeTableImageSaveTask(dir, mTimetableListView, getTabParentView());
        task.execute();
    }

    @Override
    public void onTransactResult(TimeTable result) {
        Context context = getActivity();
        if (result == null || result.isEmpty()) {
            AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
            return;
        }

        mTimeTable.copyFrom(result);
        mTimeTableAdapter2.notifyDataSetChanged();

        setTermTextViewText(mTimeTable);
    }

    private void setTermTextViewText(@NonNull TimeTable timeTable) {
        setSubtitleWhenVisible(timeTable.getYearAndSemester());
    }

    @Override
    protected void execute() {
        emptyView.setVisibility(View.INVISIBLE);

        super.execute();
    }

    @Override
    protected void onTransactPostExecute() {
        super.onTransactPostExecute();
        clearPassWd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimeTable call() throws Exception {


        // 사용자가 WISE에 시간표 정보를 요청하였을 때
        Semester semester = Semester.values()[mWiseTermSpinner.getSelectedItemPosition()];
        String mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();

        HttpURLConnection connection = TimeTableHttpRequest.getHttpConnectionPost(mWiseIdView.getText(), mWisePasswdView.getText(), semester, mTimeTableYear);

        TimeTable result;
        try {
            result = mParser2.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

        // 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
        // 시간표의 과목과 과목의 색을 Mapping한다.
        if (result != null && !result.isEmpty()) {

            Context context = getActivity();
            Hashtable<String, Integer> newColorTable = TimetableUtil.makeColorTable(result);
            TimetableUtil.saveColorTable(context, newColorTable);

            colorTable.clear();
            colorTable.putAll(newColorTable);

            TimetableUtil.writeTimetable(context, result);

        }


        return result;
    }

    @Override
    public void exceptionOccured(Exception e) {
        if (e instanceof IOException || e instanceof NullPointerException) {
            e.printStackTrace();
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
        } else {
            super.exceptionOccured(e);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_empty_text:
                sendEmptyViewClickEvent();
                mLoginDialog.show();
                break;

            case R.id.tab_timetable_action_btn:
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);

                } else {
                    sendClickEvent("actionButton");
                    mLoginDialog.show();
                }

            default:
                break;
        }
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

        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        loginToWise();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        clearPassWd();
                        break;
                }
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
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTimetable();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    void deleteTimetable() {
        AsyncLoader.excute(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return TimetableUtil.deleteTimetable(getActivity());
                    }
                },
                new OnTaskFinishedListener<Boolean>() {
                    @Override
                    public void onTaskFinished(boolean isExceptionOccurred, Boolean data, Exception e) {
                        if (!isExceptionOccurred && data) {

                            mTimeTableAdapter2.clear();

                            mTimeTable.copyFrom(new TimeTable());
                            mTimeTableAdapter2.notifyDataSetChanged();

                            AppUtil.showToast(getActivity(), R.string.execute_delete, isVisible());
                            setSubtitleWhenVisible(null);

                        } else {
                            AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                        }

                    }
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
    protected String getFragmentNameForTracker() {
        return "TabTimeTableFragment2";
    }

    private static class TimeTableImageSaveTask extends ListViewBitmapWriteTask.TitleListViewBitmapWriteTask {

        public TimeTableImageSaveTask(String fileName, ListView listView, View titleView) {
            super(fileName, listView, titleView);
        }

        @Override
        public Bitmap getBitmap() {
            try {
                Looper.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.getBitmap();

        }
    }
}
