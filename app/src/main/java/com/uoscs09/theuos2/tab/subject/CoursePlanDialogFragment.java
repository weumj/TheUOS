package com.uoscs09.theuos2.tab.subject;


import android.Manifest;
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
import android.support.v7.app.AlertDialog;
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
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.util.AppResources;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CoursePlanDialogFragment extends BaseDialogFragment implements Toolbar.OnMenuItemClickListener {

    private final static String TAG = "CoursePlanDialogFragment";
    private final static String INFO = "info";
    private static final int REQUEST_PERMISSION_TXT = 40;
    private static final int REQUEST_PERMISSION_IMAGE = 41;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    private View mCourseTitle;
    private TextView mCourseName, mCourseCode, mCourseProf, mCourseProfTel, mCourseEval, mCourseBook, mCourseLocation;

    @Bind(R.id.fragment_course_plan_listview)
    ListView mListView;
    private ArrayAdapter<CoursePlanItem> mAdapter;

    private ArrayList<CoursePlanItem> infoList;
    private AnimationAdapter aAdapter;
    private Dialog mProgressDialog;

    private boolean isDataInvalid = false;
    private SubjectItem2 mSubject;


    private AsyncTask<Void, ?, ArrayList<CoursePlanItem>> mAsyncTask;

    public void setSubjectItem(SubjectItem2 item) {
        mSubject = item;
        isDataInvalid = true;

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
        } else {
            infoList = new ArrayList<>();
        }

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, null);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //dialog.getTitleFrame().setBackgroundResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
        return new AlertDialog.Builder(getActivity())
                //.titleColorAttr(R.attr.color_actionbar_title)
                .setView(createView())
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSubject == null) {
            dismiss();
            return;
        }

        if (infoList.isEmpty() || isDataInvalid) {
            execute();
        }
    }

    private View createView() {
        View v = View.inflate(getActivity(), R.layout.dialog_course_plan, null);
        ButterKnife.bind(this, v);

        mToolbar.setTitle(R.string.tab_course_plan_title);

        mToolbar.inflateMenu(R.menu.dialog_courseplan);
        mToolbar.setOnMenuItemClickListener(this);

        mCourseTitle = LayoutInflater.from(getActivity()).inflate(R.layout.view_course_plan_header, mListView, false);
        mCourseName = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_subject_name);
        mCourseCode = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_subject_code);
        mCourseProf = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_prof_name);
        mCourseProfTel = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_prof_tel);
        mCourseLocation = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_location);
        mCourseEval = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_eval);
        mCourseBook = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_book);

        mListView.addHeaderView(mCourseTitle);

        mAdapter = new CoursePlanAdapter(getActivity(), infoList);
        aAdapter = new AlphaInAnimationAdapter(mAdapter);
        aAdapter.setAbsListView(mListView);

        mListView.setAdapter(aAdapter);


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

    private void setCourseTitle(CoursePlanItem course) {
        mCourseName.setText(course.subject_nm);
        mCourseCode.setText(course.subject_no);
        mCourseProf.setText(course.prof_nm);
        mCourseProfTel.setText(course.tel_no);
        mCourseEval.setText(course.score_eval_rate);
        mCourseBook.setText(course.book_nm);

        mCourseLocation.setText(mSubject.getClassRoomInformation(getActivity()));

        mToolbar.setTitle(course.subject_nm);
        mToolbar.setSubtitle(course.prof_nm);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mToolbar.setTitle(R.string.tab_course_plan_title);
        mToolbar.setSubtitle(null);
    }

    void execute() {
        if (mAsyncTask != null && mAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
            mAsyncTask.cancel(true);

        mProgressDialog.setOnCancelListener(dialog -> {
            AsyncUtil.cancelTask(mAsyncTask);
            dismiss();
        });
        mProgressDialog.show();

        mAsyncTask = AppResources.Subjects.requestCoursePlan(getActivity(), mSubject)
                .getAsync(
                        result -> {
                            dismissProgressDialog();

                            if (getActivity() == null) {
                                dismiss();
                                return;
                            }

                            if (result.isEmpty()) {
                                AppUtil.showToast(getActivity(), R.string.tab_course_plan_result_empty);
                                dismiss();
                                return;
                            } else {
                                setCourseTitle(result.get(0));
                            }

                            infoList.clear();
                            infoList.addAll(result);

                            mAdapter.notifyDataSetChanged();

                            aAdapter.reset();
                            aAdapter.notifyDataSetChanged();
                            isDataInvalid = false;
                        },
                        this::onError
                );
    }

    private void dismissProgressDialog() {
        if (mAsyncTask != null)
            mAsyncTask = null;

        mProgressDialog.dismiss();
        mProgressDialog.setOnCancelListener(null);
    }

    public void onError(Exception e) {
        dismissProgressDialog();

        AppUtil.showErrorToast(getActivity(), e, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_PERMISSION_TXT:
                if(checkPermissionResultAndShowToastIfFailed(permissions, grantResults, getString(R.string.tab_course_plan_permission_reject))){
                    saveCoursePlanToText();
                }
                break;

            case REQUEST_PERMISSION_IMAGE:
                if(checkPermissionResultAndShowToastIfFailed(permissions, grantResults, getString(R.string.tab_course_plan_permission_reject))){
                    saveCoursePlanToImage();
                }
                break;
        }
    }

    void saveCoursePlanToImage() {
        sendClickEvent("save course plan to image");

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_IMAGE);
            return;
        }

        final String picturePath = PrefUtil.getPicturePath(getActivity());
        String dir = picturePath + "/" + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".jpeg";
        final AsyncTask<Void, ?, String> task = new ImageUtil.ListViewBitmapRequest.Builder(mListView, mAdapter)
                .setHeaderView(mCourseTitle)
                .build()
                .wrap(new ImageUtil.ImageWriteProcessor(dir))
                .getAsync(
                        result -> {
                            dismissProgressDialog();

                            String pictureDir = picturePath.substring(picturePath.lastIndexOf('/') + 1);
                            Snackbar.make(mListView, getString(R.string.tab_course_plan_action_save_image_completed, pictureDir), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_open, v -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.parse("file://" + result), "image/*");

                                        try {
                                            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();
                                            AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            AppUtil.showErrorToast(getActivity(), e, true);
                                        }
                                        sendClickEvent("show course plan image");
                                    })
                                    .show();
                        },
                        this::onError
                );
        mProgressDialog.setOnCancelListener(dialog -> AsyncUtil.cancelTask(task));
        mProgressDialog.show();

    }

    void saveCoursePlanToText() {
        sendClickEvent("save course plan to text");

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_TXT);
            return;
        }

        final String docPath = PrefUtil.getDocumentPath(getActivity());
        final String fileName = docPath + "/" + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".txt";

        final AsyncTask<Void, ?, String> task = AsyncUtil.newRequest(
                () -> {
                    StringBuilder sb = new StringBuilder();
                    writeHeader(sb);

                    int size = infoList.size();
                    for (int i = 0; i < size; i++) {
                        writeWeek(sb, infoList.get(i));
                    }
                    return sb.toString();
                })
                .wrap(IOUtil.<String>newExternalFileWriteProcessor(fileName))
                .getAsync(result -> {
                            dismissProgressDialog();

                            String docDir = docPath.substring(docPath.lastIndexOf('/') + 1);
                            Snackbar.make(mListView, getString(R.string.tab_course_plan_action_save_text_completed, docDir), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_open, v -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.parse("file://" + fileName), "text/*");

                                        try {
                                            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();
                                            AppUtil.showToast(getActivity(), R.string.error_no_activity_found_to_handle_file);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            AppUtil.showErrorToast(getActivity(), e, true);
                                        }

                                        sendClickEvent("show course plan text");
                                    })
                                    .show();
                        },
                        this::onError
                );
        mProgressDialog.setOnCancelListener(dialog -> AsyncUtil.cancelTask(task));
        mProgressDialog.show();

    }


    private void writeHeader(StringBuilder sb) {
        CoursePlanItem course = infoList.get(0);

        sb.append(course.subject_nm);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(course.subject_no);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(getString(R.string.tab_course_plan_prof));
        sb.append(" : ");
        sb.append(course.prof_nm);
        sb.append(StringUtil.NEW_LINE);

        sb.append(getString(R.string.tab_course_plan_location));
        sb.append(" : ");
        sb.append(mSubject.getClassRoomInformation(getActivity()));
        sb.append(StringUtil.NEW_LINE);

        sb.append(getString(R.string.tab_course_plan_prof_tel));
        sb.append(" : ");
        sb.append(course.tel_no);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(getString(R.string.tab_course_plan_eval));
        sb.append(" : ");
        sb.append(StringUtil.NEW_LINE);
        sb.append(course.score_eval_rate);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(getString(R.string.tab_course_plan_book));
        sb.append(" : ");
        sb.append(StringUtil.NEW_LINE);
        sb.append(course.book_nm);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);
    }

    private void writeWeek(StringBuilder sb, CoursePlanItem item) {
        sb.append(item.week);
        sb.append(getString(R.string.tab_course_week));
        sb.append("  ----------------------");
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(" + ");
        sb.append(getString(R.string.tab_course_week_class_cont));
        sb.append(StringUtil.NEW_LINE);
        sb.append(item.class_cont);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(" + ");
        sb.append(getString(R.string.tab_course_week_class_meth));
        sb.append(StringUtil.NEW_LINE);
        sb.append(item.class_meth);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(" + ");
        sb.append(getString(R.string.tab_course_week_book));
        sb.append(StringUtil.NEW_LINE);
        sb.append(item.week_book);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);

        sb.append(" + ");
        sb.append(getString(R.string.tab_course_week_prjt_etc));
        sb.append(StringUtil.NEW_LINE);
        sb.append(item.prjt_etc);
        sb.append(StringUtil.NEW_LINE);
        sb.append(StringUtil.NEW_LINE);
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return TAG;
    }

    static class CoursePlanAdapter extends AbsArrayAdapter<CoursePlanItem, CoursePlanAdapter.ViewHolder> {
        public CoursePlanAdapter(Context context, List<CoursePlanItem> list) {
            super(context, R.layout.list_layout_course_plan, list);
        }

        @Override
        public void onBindViewHolder(int position, ViewHolder holder) {
            CoursePlanItem item = getItem(position);

            holder.week.setText(String.valueOf(item.week));
            holder.content.setText(item.class_cont);
            holder.meth.setText(item.class_meth);
            holder.book.setText(item.week_book);
            holder.etc.setText(item.prjt_etc);
        }

        @Override
        public ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new ViewHolder(convertView);
        }

        static class ViewHolder extends AbsArrayAdapter.ViewHolder {
            @Bind(R.id.course_plan_week)
            public TextView week;
            @Bind(R.id.course_plan_content)
            public TextView content;
            @Bind(R.id.course_plan_meth)
            public TextView meth;
            @Bind(R.id.course_plan_book)
            public TextView book;
            @Bind(R.id.course_plan_etc)
            public TextView etc;

            public ViewHolder(View view) {
                super(view);
            }
        }
    }

}
