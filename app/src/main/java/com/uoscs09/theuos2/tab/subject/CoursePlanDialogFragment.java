package com.uoscs09.theuos2.tab.subject;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
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
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CoursePlanDialogFragment extends BaseDialogFragment implements Request.ErrorListener,  Toolbar.OnMenuItemClickListener {
    private final static String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiCoursePlanView.oapi";
    private final static String INFO = "info";

    private final static String TAG = "CoursePlanDialogFragment";

    private final ArrayMap<String, String> mOApiParams;

    private Toolbar mToolbar;
    private View mCourseTitle;
    private TextView mCourseName, mCourseCode, mCourseProf, mCourseProfTel, mCourseEval, mCourseBook, mCourseLocation;

    private ListView mListView;
    private ArrayAdapter<CoursePlanItem> mAdapter;

    private ArrayList<CoursePlanItem> infoList;
    private AnimationAdapter aAdapter;
    private Dialog mProgressDialog;

    private boolean isDataInvalid = false;
    private SubjectItem2 mSubject;

    private static final XmlParserWrapper<ArrayList<CoursePlanItem>> COURSE_PLAN_PARSER = OApiUtil.getParser(CoursePlanItem.class);

    private AsyncTask<Void, ?, ArrayList<CoursePlanItem>> mAsyncTask;

    public void setSubjectItem(SubjectItem2 item) {

        mSubject = item;
        isDataInvalid = true;

        mOApiParams.put(OApiUtil.TERM, item.term);
        mOApiParams.put(OApiUtil.SUBJECT_NO, item.subject_no);
        mOApiParams.put(OApiUtil.CLASS_DIV, item.class_div);
        mOApiParams.put(OApiUtil.YEAR, item.year);
    }

    public CoursePlanDialogFragment() {
        mOApiParams = new ArrayMap<>(5);
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

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.tab_course_plan_title);

        mToolbar.inflateMenu(R.menu.dialog_courseplan);
        mToolbar.setOnMenuItemClickListener(this);

        mListView = (ListView) v.findViewById(R.id.fragment_course_plan_listview);

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

        mProgressDialog.setOnCancelListener(mJobCanceler);
        mProgressDialog.show();

        mAsyncTask = AsyncUtil.newRequest(
                new Callable<ArrayList<CoursePlanItem>>() {
                    @Override
                    public ArrayList<CoursePlanItem> call() throws Exception {
                        if (mSubject.classInformationList.isEmpty())
                            mSubject.afterParsing();

                        return HttpRequest.Builder.newConnectionRequestBuilder(URL)
                                .setParams(mOApiParams)
                                .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                                .build()
                                .checkNetworkState(getActivity())
                                .wrap(COURSE_PLAN_PARSER)
                                .get();
                    }
                })
                .getAsync(
                        new Request.ResultListener<ArrayList<CoursePlanItem>>() {
                            @Override
                            public void onResult(ArrayList<CoursePlanItem> result) {
                                dismissProgressDialog();

                                if (result.isEmpty())
                                    setCourseTitle(new CoursePlanItem());
                                else
                                    setCourseTitle(result.get(0));

                                infoList.clear();
                                infoList.addAll(result);

                                mAdapter.notifyDataSetChanged();

                                aAdapter.reset();
                                aAdapter.notifyDataSetChanged();
                                isDataInvalid = false;
                            }
                        },
                        this
                );
    }

    private final DialogInterface.OnCancelListener mJobCanceler = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            AsyncUtil.cancelTask(mAsyncTask);
            dismiss();
        }
    };

    private void dismissProgressDialog() {
        if (mAsyncTask != null)
            mAsyncTask = null;

        mProgressDialog.dismiss();
        mProgressDialog.setOnCancelListener(null);
    }

    @Override
    public void onError(Exception e) {
        dismissProgressDialog();

        AppUtil.showErrorToast(getActivity(), e, true);
    }

    void saveCoursePlanToImage() {
        sendClickEvent("save course plan to image");

        String dir = PrefUtil.getPicturePath(getActivity()) + "/" + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".jpeg";
        final AsyncTask<Void, ?, String> task = new ImageUtil.ListViewBitmapRequest.Builder(mListView, mAdapter)
                .setHeaderView(mCourseTitle)
                .build()
                .wrap(new ImageUtil.ImageWriteProcessor(dir))
                .getAsync(
                        new Request.ResultListener<String>() {
                            @Override
                            public void onResult(final String result) {
                                dismissProgressDialog();

                                Snackbar.make(mListView, getText(R.string.tab_course_plan_action_save_image_completed), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_open, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                                                AppUtil.startActivityWithScaleUp(getActivity(), intent, v);

                                                sendClickEvent("show course plan image");
                                            }
                                        })
                                        .show();
                            }
                        },
                        this
                );
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncUtil.cancelTask(task);
            }
        });
        mProgressDialog.show();

    }

    void saveCoursePlanToText() {
        sendClickEvent("save course plan to text");

        final String fileName = PrefUtil.getDocumentPath(getActivity()) + "/" + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".txt";

        final AsyncTask<Void, ?, String> task = AsyncUtil.newRequest(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        StringBuilder sb = new StringBuilder();
                        writeHeader(sb);

                        int size = infoList.size();
                        for (int i = 0; i < size; i++) {
                            writeWeek(sb, infoList.get(i));
                        }
                        return sb.toString();
                    }
                })
                .wrap(IOUtil.<String>newFileWriteProcessor(null, fileName))
                .getAsync(
                        new Request.ResultListener<String>() {
                            @Override
                            public void onResult(final String result) {
                                dismissProgressDialog();
                                Snackbar.make(mListView, getText(R.string.tab_course_plan_action_save_text_completed), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_open, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.parse("file://" + fileName), "text/*");
                                                AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                                sendClickEvent("show course plan text");
                                            }
                                        })
                                        .show();
                            }
                        },
                        this
                );
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncUtil.cancelTask(task);
            }
        });
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
        public ViewHolder onCreateViewHolder(View convertView, int viewType) {
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
            }
        }
    }

}
