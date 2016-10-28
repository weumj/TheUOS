package com.uoscs09.theuos2.tab.timetable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.TaskUtil;

import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class TabTimeTableFragment extends AbsProgressFragment<Timetable2> {

    @Override
    protected int layoutRes() {
        return R.layout.tab_timetable;
    }

    private static final String TAG = "TabTimeTableFragment";
    private static final int REQUEST_PERMISSION_SAVE_IMAGE = 10;

    View rootView;

    @BindView(R.id.time_table_listView1)
    ListView mTimetableListView;
    @BindView(R.id.tab_timetable_empty)
    View emptyView;

    @AsyncData
    private Timetable2 mTimeTable;
    private TimeTableAdapter mTimeTableAdapter;

    private final SubjectDetailDialogFragment mSubjectDetailDialog = new SubjectDetailDialogFragment();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mTimeTable = savedInstanceState.getParcelable(IOUtil.FILE_TIMETABLE);
        }

        mSubjectDetailDialog.setColorSelectedListener(i -> mTimeTableAdapter.notifyDataSetChanged());

        super.onCreate(savedInstanceState);

        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, getToolbarParent(), false);

        mTimeTableAdapter = new TimeTableAdapter(getActivity());
        mTimeTableAdapter.setTimeTable(mTimeTable);
        mTimeTableAdapter.setOnItemClickListener((vh, v, subject) -> {
            if (subject == null)
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        mTimetableListView.setEmptyView(emptyView);
        mTimetableListView.setAdapter(mTimeTableAdapter);

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        if (savedInstanceState == null)
            readTimetableFromFile();
    }

    @OnClick(R.id.tab_timetable_empty_text)
    void emptyViewClick() {
        sendEmptyViewClickEvent();
        showLoginDialog();
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
                if (taskQueue().exist(TAG))
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                else {
                    sendClickEvent("wise login");
                    showLoginDialog();
                }
                return true;

            case R.id.action_delete:

                sendClickEvent("delete timetable");
                deleteDialog().show();
                return true;

            case R.id.action_save:

                sendClickEvent("save timetable image");
                saveTimetableImage();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_SAVE_IMAGE:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, R.string.tab_timetable_permission_image_reject)) {
                    saveTimetableImage();
                }
                break;

            default:
                break;
        }
    }

    private void saveTimetableImage() {
        if (mTimeTableAdapter.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_not_exist, true);
            return;
        }

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SAVE_IMAGE);
            return;
        }


        final Dialog progressDialog = AppUtil.getProgressDialog(getActivity(), false, getText(R.string.progress_ongoing), null);

        mTimeTableAdapter.changeLayout(true);

        final Task<String> task = TimetableUtil.saveTimetableToImage(mTimeTable, mTimetableListView, mTimeTableAdapter, getTabParentView()).getAsync(result -> {
                    progressDialog.dismiss();
                    progressDialog.setOnCancelListener(null);
                    mTimeTableAdapter.changeLayout(false);

                    String pictureDir = result.substring(result.lastIndexOf('/') + 1);
                    Snackbar.make(rootView, getString(R.string.tab_timetable_saved, pictureDir), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, v -> {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + result), "image/*");

                                try {
                                    AnimUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                    sendClickEvent("show timetable image");
                                } catch (ActivityNotFoundException e) {
                                    //e.printStackTrace();
                                    AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                } catch (Exception e) {
                                    AppUtil.showErrorToast(getActivity(), e, true);
                                }
                            })
                            .show();
                },
                e -> {
                    progressDialog.dismiss();
                    progressDialog.setOnCancelListener(null);
                    mTimeTableAdapter.changeLayout(false);

                    AppUtil.showErrorToast(getActivity(), e, true);
                }
        );
        progressDialog.setOnCancelListener(dialog -> TaskUtil.cancel(task));
        progressDialog.show();

    }


    void execute(Semester semester, String year, String id, String passwd) {
        emptyView.setVisibility(View.INVISIBLE);

        // dummy : AppRequests.TimeTables.dummyRequest(id, passwd, semester, year)

        executeWithQueue(TAG, AppRequests.TimeTables.request(id, passwd, semester, year),
                r -> {
                    setTimetable(r);
                    if (r == null)
                        AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
                },
                t -> {
                    emptyView.setVisibility(View.VISIBLE);

                    if (t instanceof IOException || t instanceof NullPointerException) {
                        t.printStackTrace();
                        AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
                    } else {
                        simpleErrorRespond(t);
                    }
                }
        );
    }

    private void readTimetableFromFile() {
        AppRequests.TimeTables.readFile().getAsync(
                this::setTimetable,
                e -> Log.e(TAG, "cannot read timetable from file.", e)
        );
    }

    @SuppressLint("SwitchIntDef")
    private void showLoginDialog() {
        Context context = getActivity();
        View wiseDialogLayout = View.inflate(context, R.layout.dialog_timetable_wise_login, null);

        Spinner wiseYearSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_year);
        wiseYearSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, OApiUtil.getYears()));
        wiseYearSpinner.setSelection(2);

        EditText wiseIdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_id_input);
        EditText wisePasswdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_passwd_input);
        Spinner wiseTermSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_term);

        Calendar c = Calendar.getInstance();
        switch (c.get(Calendar.MONTH)) {
            default:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
            case Calendar.APRIL:
            case Calendar.MAY:
                wiseTermSpinner.setSelection(Semester.SPRING.ordinal());
                break;

            case Calendar.JUNE:
            case Calendar.JULY:
                wiseTermSpinner.setSelection(Semester.SUMMER.ordinal());
                break;

            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
                wiseTermSpinner.setSelection(Semester.AUTUMN.ordinal());
                break;

            case Calendar.DECEMBER:
            case Calendar.JANUARY:
                wiseTermSpinner.setSelection(Semester.WINTER.ordinal());
                break;
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.tab_timetable_wise_login_title)
                .setView(wiseDialogLayout)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String id = wiseIdView.getText().toString().trim();

                    if (wisePasswdView.length() < 1 || TextUtils.isEmpty(id)) {
                        AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_null, true);
                    } else {
                        execute(
                                Semester.values()[wiseTermSpinner.getSelectedItemPosition()],
                                wiseYearSpinner.getSelectedItem().toString(),
                                wiseIdView.getText().toString().trim(),
                                wisePasswdView.getText().toString().trim()
                        );
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setTimetable(Timetable2 timeTable) {
        if (timeTable == null) {
            emptyView.setVisibility(View.VISIBLE);
            setSubtitleWhenVisible(null);
        } else {
            this.mTimeTable = timeTable;
            setSubtitleWhenVisible(timeTable.getYearAndSemester());
        }
        mTimeTableAdapter.setTimeTable(timeTable);
    }


    private AlertDialog deleteDialog() {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    deleteTimetable();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    void deleteTimetable() {
        AlertDialog dialog = deleteDialog();
        Tasks.newTask(() -> TimetableUtil.deleteTimetable(getActivity())).getAsync(
                result -> {
                    dialog.dismiss();
                    if (result) {
                        AppUtil.showToast(getActivity(), R.string.execute_delete, isVisible());
                        setTimetable(null);
                    } else {
                        AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                    }
                },
                e -> {
                    dialog.dismiss();
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
        return "TabTimeTableFragment";
    }

}
