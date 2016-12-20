package com.uoscs09.theuos2.tab.subject;


import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsAnimDialogFragment;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mj.android.utils.task.Task;

public class CoursePlanDialogFragment extends AbsAnimDialogFragment implements Toolbar.OnMenuItemClickListener {

    private final static String TAG = "CoursePlanDialogFragment";
    private final static String INFO = "info";
    private static final int REQUEST_PERMISSION_TXT = 40;
    private static final int REQUEST_PERMISSION_IMAGE = 41;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private View mCourseTitle;
    private TextView mCourseName, mCourseCode, mCourseProf, mCourseProfTel, mCourseEval, mCourseBook, mCourseLocation;

    @BindView(R.id.fragment_course_plan_listview)
    ListView mListView;
    private ArrayAdapter<CoursePlan> mAdapter;

    private ArrayList<CoursePlan> infoList = new ArrayList<>();

    private Subject mSubject;

    public static void fetchCoursePlanAndShow(final Fragment fragment, Subject subject, View v) {
        Task<List<CoursePlan>> task = AppRequests.Subjects.requestCoursePlan(subject);
        Dialog d = AppUtil.getProgressDialog(fragment.getActivity(), false, (dialog, which) -> task.cancel());

        d.show();
        task.getAsync(coursePlanItems -> {
                    d.dismiss();

                    if (!coursePlanItems.isEmpty()) {
                        CoursePlanDialogFragment f = new CoursePlanDialogFragment();
                        f.initValues(subject, coursePlanItems);
                        f.showFromView(fragment.getFragmentManager(), "course", v);
                    } else {
                        AppUtil.showToast(fragment.getActivity(), R.string.tab_course_plan_result_empty);
                    }
                },
                throwable -> {
                    AppUtil.showErrorToast(fragment.getActivity(), throwable, true);
                    d.dismiss();
                }
        );
    }


    public void initValues(Subject item, List<CoursePlan> infoList) {
        this.mSubject = item;
        this.infoList.clear();
        this.infoList.addAll(infoList);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INFO, infoList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            infoList = savedInstanceState.getParcelableArrayList(INFO);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mSubject == null) {
            dismiss();
        }
    }

    @Override
    protected View createView() {
        View v = View.inflate(getActivity(), R.layout.dialog_course_plan, null);
        ButterKnife.bind(this, v);

        mToolbar.setTitle(R.string.tab_course_plan_title);

        mToolbar.inflateMenu(R.menu.dialog_courseplan);
        mToolbar.setOnMenuItemClickListener(this);

        mCourseTitle = LayoutInflater.from(getActivity()).inflate(R.layout.view_course_plan_header, mListView, false);
        mCourseName = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_subject_name);
        mCourseCode = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_subject_code);
        mCourseProf = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_prof_name);
        mCourseProfTel = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_prof_tel);
        mCourseLocation = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_location);
        mCourseEval = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_eval);
        mCourseBook = ButterKnife.findById(mCourseTitle, R.id.fragment_course_plan_book);

        mListView.addHeaderView(mCourseTitle);

        mAdapter = new CoursePlanAdapter(getActivity(), infoList);
        AnimationAdapter aAdapter = new AlphaInAnimationAdapter(mAdapter);
        aAdapter.setAbsListView(mListView);

        mListView.setAdapter(aAdapter);

        setCourseTitle(infoList.get(0));
        mAdapter.notifyDataSetChanged();

        aAdapter.reset();
        aAdapter.notifyDataSetChanged();

        return v;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_text:
                saveCoursePlanToText();
                return true;

            case R.id.action_save_image:
                saveCoursePlanToImage();
                return true;

            default:
                return false;
        }
    }

    private void setCourseTitle(CoursePlan course) {
        mCourseName.setText(course.subject_nm);
        mCourseCode.setText(course.subject_no);
        mCourseProf.setText(course.prof_nm);
        mCourseProfTel.setText(course.tel_no);
        mCourseEval.setText(course.score_eval_rate);
        mCourseBook.setText(course.book_nm);

        if (mSubject != null)
            mCourseLocation.setText(mSubject.getClassRoomTimeInformation());

        mToolbar.setTitle(course.subject_nm);
        mToolbar.setSubtitle(course.prof_nm);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mToolbar.setTitle(R.string.tab_course_plan_title);
        mToolbar.setSubtitle(null);
    }


    void onError(Dialog d, Throwable e) {
        d.dismiss();
        AppUtil.showErrorToast(getActivity(), e, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_TXT:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, getString(R.string.tab_course_plan_permission_reject))) {
                    saveCoursePlanToText();
                }
                break;

            case REQUEST_PERMISSION_IMAGE:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, getString(R.string.tab_course_plan_permission_reject))) {
                    saveCoursePlanToImage();
                }
                break;

            default:
                break;
        }
    }

    void saveCoursePlanToImage() {
        sendClickEvent("save course plan to image");

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_IMAGE);
            return;
        }


        final Task<String> task = AppRequests.Subjects.saveCoursePlanToImage(mListView, mAdapter, mCourseTitle, mSubject);
        final Dialog d = AppUtil.getProgressDialog(getActivity(), false, (dialog, which) -> task.cancel());
        d.show();
        task.getAsync(result -> {
                    d.dismiss();

                    String pictureDir = result.replace(result.substring(result.lastIndexOf('/')), "");
                    pictureDir = pictureDir.substring(pictureDir.lastIndexOf('/') + 1);
                    Snackbar.make(mListView, getString(R.string.tab_course_plan_action_save_image_completed, pictureDir), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, v -> {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                // fixme api25
                                intent.setDataAndType(Uri.parse("file://" + result), "image/*");

                                try {
                                    AnimUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                } catch (ActivityNotFoundException e) {
                                    //e.printStackTrace();
                                    AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                } catch (Exception e) {
                                    AppUtil.showErrorToast(getActivity(), e, true);
                                }
                                sendClickEvent("show course plan image");
                            })
                            .show();
                },
                throwable -> onError(d, throwable)
        );
    }

    void saveCoursePlanToText() {
        sendClickEvent("save course plan to text");

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_TXT);
            return;
        }

        final Task<String> task = AppRequests.Subjects.saveCoursePlanToTextFile(infoList, mSubject);
        final Dialog d = AppUtil.getProgressDialog(getActivity(), false, (dialog, which) -> task.cancel());
        d.show();
        task.getAsync(result -> {
                    d.dismiss();

                    String docDir = result.replace(result.substring(result.lastIndexOf('/')), "");
                    docDir = docDir.substring(docDir.lastIndexOf('/') + 1);
                    Snackbar.make(mListView, getString(R.string.tab_course_plan_action_save_text_completed, docDir), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, v -> {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                // fixme api25
                                intent.setDataAndType(Uri.parse("file://" + result), "text/*");

                                try {
                                    AnimUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                } catch (ActivityNotFoundException e) {
                                    //e.printStackTrace();
                                    AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                } catch (Exception e) {
                                    AppUtil.showErrorToast(getActivity(), e, true);
                                }

                                sendClickEvent("show course plan text");
                            })
                            .show();
                },
                throwable -> onError(d, throwable)
        );
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return TAG;
    }

    static class CoursePlanAdapter extends AbsArrayAdapter<CoursePlan, CoursePlanAdapter.ViewHolder> {
        CoursePlanAdapter(Context context, List<CoursePlan> list) {
            super(context, R.layout.list_layout_course_plan, list);
        }

        @Override
        public void onBindViewHolder(int position, ViewHolder holder) {
            holder.setView(getItem(position));
        }

        @Override
        public ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new ViewHolder(convertView);
        }

        static class ViewHolder extends AbsArrayAdapter.ViewHolder {
            @BindView(R.id.course_plan_week)
            TextView week;
            @BindView(R.id.course_plan_content)
            TextView content;
            @BindView(R.id.course_plan_meth)
            TextView meth;
            @BindView(R.id.course_plan_book)
            TextView book;
            @BindView(R.id.course_plan_etc)
            TextView etc;

            public ViewHolder(View view) {
                super(view);
            }

            void setView(CoursePlan item) {
                if (item != null) {
                    week.setText(String.valueOf(item.week));
                    content.setText(item.class_cont);
                    meth.setText(item.class_meth);
                    book.setText(item.week_book);
                    etc.setText(item.prjt_etc);
                } else {
                    week.setText("");
                    content.setText("");
                    meth.setText("");
                    book.setText("");
                    etc.setText("");
                }
            }
        }
    }

}
