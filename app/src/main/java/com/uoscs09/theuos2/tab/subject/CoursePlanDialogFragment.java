package com.uoscs09.theuos2.tab.subject;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.ListViewBitmapWriteTask;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.parse.ParseCoursePlan;
import com.uoscs09.theuos2.parse.ParseUtil;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CoursePlanDialogFragment extends DialogFragment {
    private final static String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiCoursePlanView.oapi";
    private final static String INFO = "info";

    private final static String TAG = "CoursePlanDialogFragment";

    private final Hashtable<String, String> mOApiParams;

    private Toolbar mToolbar;
    private View mCourseTitle;
    private TextView mCourseName, mCourseCode, mCourseProf, mCourseProfTel, mCourseEval, mCourseBook;

    private ListView mListView;
    private ArrayAdapter<CoursePlanItem> mAdapter;

    private ArrayList<CoursePlanItem> infoList;
    private AnimationAdapter aAdapter;
    private Dialog mProgressDialog;

    private boolean isDataInvalid = false;
    private SubjectItem2 mSubject;

    private static final ParseCoursePlan COURSE_PLAN_PARSER = new ParseCoursePlan();

    private AsyncTask<Void, Void, ArrayList<CoursePlanItem>> mAsynckTask;

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

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, null);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);

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

        if (infoList.isEmpty() || isDataInvalid) {
            execute();
        }
    }

    private View createView() {
        View v = View.inflate(getActivity(), R.layout.dialog_course_plan, null);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.tab_course_plan_title);

        mToolbar.inflateMenu(R.menu.dialog_courseplan);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        });

        mListView = (ListView) v.findViewById(R.id.fragment_course_plan_listview);

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
        if (mAsynckTask != null && mAsynckTask.getStatus() != AsyncTask.Status.FINISHED)
            mAsynckTask.cancel(true);

        mProgressDialog.setOnCancelListener(mJobCanceler);
        mProgressDialog.show();
        mAsynckTask = AsyncUtil.execute(JOB);
    }

    private final DialogInterface.OnCancelListener mJobCanceler = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            AsyncUtil.cancelTask(mAsynckTask);
            dismiss();
        }
    };

    private final AsyncJob.Base<ArrayList<CoursePlanItem>> JOB = new AsyncJob.Base<ArrayList<CoursePlanItem>>() {
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
        public ArrayList<CoursePlanItem> call() throws Exception {
            return ParseUtil.parseXml(COURSE_PLAN_PARSER, URL, mOApiParams);
        }

        @Override
        public void exceptionOccured(Exception e) {
            Log.e("CoursePlanView", "", e);

            AppUtil.showErrorToast(getActivity(), e, isVisible());
        }

        @Override
        public void onPostExcute() {
            mProgressDialog.dismiss();
            mAsynckTask = null;
        }
    };

    void saveCoursePlanToImage() {
        TrackerUtil.getInstance(this).sendClickEvent(TAG, "save course plan to image");

        String dir = PrefUtil.getPicturePath(getActivity()) + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".jpeg";
        final ListViewBitmapWriteTask.TitleListViewBitmapWriteTask task = new ListViewBitmapWriteTask.TitleListViewBitmapWriteTask(mListView, mAdapter, dir, mCourseTitle, mProgressDialog) {
            @Override
            public void onResult(final String result) {
                Snackbar.make(mListView, getText(R.string.save), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_open, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                                ActivityCompat.startActivity(getActivity(), intent, ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
                            }
                        })
                        .show();

            }
        };
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                task.cancel();
            }
        });
        task.execute();

    }

    void saveCoursePlanToText() {
        TrackerUtil.getInstance(this).sendClickEvent(TAG, "save course plan to text");

        mProgressDialog.show();
        final AsyncTask<Void, Void, String> task = AsyncUtil.execute(new AsyncJob.Base<String>() {
            @Override
            public void onResult(final String result) {
                Snackbar.make(mListView, getText(R.string.save), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_open, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + result), "text/*");
                                ActivityCompat.startActivity(getActivity(), intent, ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
                            }
                        })
                        .show();

            }

            @Override
            public void exceptionOccured(Exception e) {
                AppUtil.showErrorToast(getActivity(), e, true);
            }

            @Override
            public void onPostExcute() {
                mProgressDialog.dismiss();
            }

            @Override
            public String call() throws Exception {
                StringBuilder sb = new StringBuilder();
                writeHeader(sb);

                int size = infoList.size();
                for (int i = 0; i < size; i++) {
                    writeWeek(sb, infoList.get(i));
                }

                String dir = PrefUtil.getDocumentPath(getActivity()) + getString(R.string.tab_course_plan_title) + '_' + mSubject.subject_nm + '_' + mSubject.prof_nm + '_' + mSubject.class_div + ".txt";
                IOUtil.writeObjectToExternalFile(dir, sb.toString());
                return dir;
            }


            private void writeHeader(StringBuilder sb) {
                CoursePlanItem course = infoList.get(0);

                sb.append(course.subject_nm);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append(course.subject_no);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("담당 교수 : ");
                sb.append(course.prof_nm);
                sb.append(StringUtil.NEW_LINE);

                sb.append("전화 번호 : ");
                sb.append(course.tel_no);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("평가 방법 : ");
                sb.append(StringUtil.NEW_LINE);
                sb.append(course.score_eval_rate);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("교재 : ");
                sb.append(StringUtil.NEW_LINE);
                sb.append(course.book_nm);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);
            }

            private void writeWeek(StringBuilder sb, CoursePlanItem item) {
                sb.append(item.week);
                sb.append(" 주차 ----------------------");
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("+ 내용");
                sb.append(StringUtil.NEW_LINE);
                sb.append(item.class_cont);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("+ 방법");
                sb.append(StringUtil.NEW_LINE);
                sb.append(item.class_meth);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("+ 교재");
                sb.append(StringUtil.NEW_LINE);
                sb.append(item.week_book);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);

                sb.append("+ 기타");
                sb.append(StringUtil.NEW_LINE);
                sb.append(item.prjt_etc);
                sb.append(StringUtil.NEW_LINE);
                sb.append(StringUtil.NEW_LINE);
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncUtil.cancelTask(task);
            }
        });
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


                ColorDrawable d = new ColorDrawable(r.getColor(AppUtil.getAttrValue(view.getContext(), R.attr.colorPrimaryDark)));

                d.setBounds(0, 0, size, size);

                content.setCompoundDrawables(d, null, null, null);
                meth.setCompoundDrawables(d, null, null, null);
                book.setCompoundDrawables(d, null, null, null);
                etc.setCompoundDrawables(d, null, null, null);

            }
        }
    }

}