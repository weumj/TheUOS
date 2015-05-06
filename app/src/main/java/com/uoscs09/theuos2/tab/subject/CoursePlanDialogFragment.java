package com.uoscs09.theuos2.tab.subject;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseCoursePlan;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;

public class CoursePlanDialogFragment extends DialogFragment implements Callable<ArrayList<CoursePlanItem>>, AsyncCallback<ArrayList<CoursePlanItem>> {
    private final static String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiCoursePlanView.oapi";
    private final static String INFO = "info";

    private final static String TAG = "CoursePlanDialogFragment";

    private final Hashtable<String, String> mOApiParams;


    private View mCourseTitle;
    private TextView mCourseName, mCourseCode, mCourseProf, mCourseProfTel, mCourseEval, mCourseBook;

    private ListView mListView;
    private ArrayAdapter<CoursePlanItem> mAdapter;

    private ArrayList<CoursePlanItem> infoList;
    private AnimationAdapter aAdapter;
    private Dialog mProgressDialog;

    private boolean isDataInvalid = false;
    private SubjectItem2 mSubject;

    private final ParseCoursePlan mParser = new ParseCoursePlan();

    private AsyncExecutor<ArrayList<CoursePlanItem>> mExecutor;

    public void setSubjectItem(SubjectItem2 item) {

        mSubject = item;
        isDataInvalid = true;

        mOApiParams.put(OApiUtil.TERM, item.term);
        mOApiParams.put(OApiUtil.SUBJECT_NO, item.subject_no);
        mOApiParams.put(OApiUtil.CLASS_DIV, item.class_div);
        mOApiParams.put(OApiUtil.YEAR, item.year);
    }

    public CoursePlanDialogFragment() {
        mOApiParams = new Hashtable<>(5);
        mOApiParams.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
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

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mExecutor != null && !mExecutor.isCancelled()) {
                            mExecutor.cancel(true);
                        }
                        dismiss();
                    }
                }
        );

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tab_course_plan_title)
                //.titleColorAttr(R.attr.color_actionbar_title)
                .setView(createView())
                .create();

        //dialog.getTitleFrame().setBackgroundResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (infoList.isEmpty() || isDataInvalid) {
            execute();
        }
    }

    private View createView() {
        View v = View.inflate(getActivity(), R.layout.fragment_course_plan, null);

        mListView = (ListView) v.findViewById(R.id.fragment_course_plan_listview);

        v.findViewById(R.id.fragment_course_plan_btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dir = PrefUtil.getPictureSavedPath(getActivity()) + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".jpeg";

                TrackerUtil.getInstance(CoursePlanDialogFragment.this).sendClickEvent(TAG, "save course plan to image");

                ListViewBitmapWriteTask.TitleListViewBitmapWriteTask task = new ListViewBitmapWriteTask.TitleListViewBitmapWriteTask(dir, mListView, mCourseTitle);
                task.execute();
            }
        });

        mCourseTitle = LayoutInflater.from(getActivity()).inflate(R.layout.view_course_plan_header, mListView, false);
        mCourseName = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_subject_name);
        mCourseCode = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_subject_code);
        mCourseProf = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_prof_name);
        mCourseProfTel = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_prof_tel);
        mCourseEval = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_eval);
        mCourseBook = (TextView) mCourseTitle.findViewById(R.id.fragment_course_plan_book);

        mListView.addHeaderView(mCourseTitle);

        mAdapter = new CoursePlanAdapter(getActivity(), infoList);
        aAdapter = new AlphaInAnimationAdapter(mAdapter);
        aAdapter.setAbsListView(mListView);

        mListView.setAdapter(aAdapter);


        return v;
    }

    private void setCourseTitle(CoursePlanItem course) {
        mCourseName.setText(course.subject_nm);
        mCourseCode.setText(course.subject_no);
        mCourseProf.setText(course.prof_nm);
        mCourseProfTel.setText(course.tel_no);
        mCourseEval.setText(course.score_eval_rate);
        mCourseBook.setText(course.book_nm);
    }

    void execute() {
        if (mExecutor != null && mExecutor.getStatus() != AsyncTask.Status.FINISHED)
            mExecutor.cancel(true);

        mProgressDialog.show();
        mExecutor = new AsyncExecutor<ArrayList<CoursePlanItem>>().setCallable(this).setCallback(this);
        mExecutor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResult(ArrayList<CoursePlanItem> coursePlanItems) {
        if (coursePlanItems.isEmpty())
            setCourseTitle(new CoursePlanItem());
        else
            setCourseTitle(coursePlanItems.get(0));

        infoList.clear();
        infoList.addAll(coursePlanItems);

        mAdapter.notifyDataSetChanged();

        aAdapter.reset();
        aAdapter.notifyDataSetChanged();
        isDataInvalid = false;
    }

    @Override
    public void exceptionOccured(Exception e) {
        Log.e("CoursePlanView", "", e);

        AppUtil.showErrorToast(getActivity(), e, isVisible());
    }

    @Override
    public void cancelled() {
    }

    @Override
    public void onPostExcute() {
        mProgressDialog.dismiss();
    }

    @Override
    public ArrayList<CoursePlanItem> call() throws Exception {
        HttpURLConnection connection = HttpRequest.getConnection(URL, StringUtil.ENCODE_EUC_KR, mOApiParams);

        try {
            return mParser.parse(connection.getInputStream());
        } finally {
            connection.disconnect();
        }

    }

    private static class CoursePlanAdapter extends AbsArrayAdapter<CoursePlanItem, CoursePlanAdapter.ViewHolder> {
        public CoursePlanAdapter(Context context, List<CoursePlanItem> list) {
            super(context, R.layout.list_layout_course_plan, list);
        }

        @Override
        public void onBindViewHolder(int position, ViewHolder holder) {
            CoursePlanItem item = getItem(position);

            holder.week.setText(Integer.toString(item.week));
            holder.content.setText(item.class_cont);
            holder.meth.setText(item.class_meth);
            holder.book.setText(item.week_book);
            holder.etc.setText(item.prjt_etc);
        }

        @Override
        public ViewHolder getViewHolder(View convertView) {
            return new ViewHolder(convertView);
        }

        static class ViewHolder extends AbsArrayAdapter.ViewHolder {
            public final TextView week;
            public final TextView content;
            public final TextView meth;
            public final TextView book;
            public final TextView etc;

            public ViewHolder(View view) {
                super(view);
                week = (TextView) view.findViewById(R.id.course_plan_week);
                content = (TextView) view.findViewById(R.id.course_plan_content);
                meth = (TextView) view.findViewById(R.id.course_plan_meth);
                book = (TextView) view.findViewById(R.id.course_plan_book);
                etc = (TextView) view.findViewById(R.id.course_plan_etc);

                Resources r = view.getResources();

                int size = r.getDimensionPixelSize(R.dimen.univ_schedule_list_drawable_size) / 2;


                ColorDrawable d = new ColorDrawable(r.getColor(AppUtil.getAttrValue(view.getContext(), R.attr.colorPrimary)));

                d.setBounds(0, 0, size, size);

                content.setCompoundDrawables(d, null, null, null);
                meth.setCompoundDrawables(d, null, null, null);
                book.setCompoundDrawables(d, null, null, null);
                etc.setCompoundDrawables(d, null, null, null);

            }
        }
    }

}
