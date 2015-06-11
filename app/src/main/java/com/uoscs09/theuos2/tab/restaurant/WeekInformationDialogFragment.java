package com.uoscs09.theuos2.tab.restaurant;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseRestaurantWeek;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;

public class WeekInformationDialogFragment extends DialogFragment {
    static final ParseRestaurantWeek PARSE_RESTAURANT_WEEK = new ParseRestaurantWeek();

    private RestWeekAdapter mRestWeekAdapter;
    private int mCurrentSelectionId;
    private AsyncTask<Void, Void, WeekRestItem> mAsyncTask;

    @ReleaseWhenDestroy
    private View mProgressLayout;
    @ReleaseWhenDestroy
    private ProgressWheel mProgressWheel;

    public void setSelection(int stringId) {
        this.mCurrentSelectionId = stringId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(createView())
                .create();
    }

    private View createView() {
        View rootView = View.inflate(getActivity(), R.layout.dialog_tab_rest_week, null);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(mCurrentSelectionId);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.tab_rest_week_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mRestWeekAdapter = new RestWeekAdapter(new ArrayList<RestItem>()));

        mProgressLayout = rootView.findViewById(R.id.progress_layout);
        mProgressWheel = (ProgressWheel) mProgressLayout.findViewById(R.id.progress_wheel);

        execute();

        return rootView;
    }

    private static class RestWeekAdapter extends RecyclerView.Adapter<ViewHolder> {
        ArrayList<RestItem> restItemArrayList;

        public RestWeekAdapter(ArrayList<RestItem> arrayList) {
            this.restItemArrayList = arrayList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_week_rest, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RestItem item = restItemArrayList.get(position);

            holder.toolbar.setTitle(item.title);

            holder.breakfastContent.setText(item.breakfast);
            holder.lunchContent.setText(item.lunch);
            holder.dinnerContent.setText(item.supper);
        }

        @Override
        public int getItemCount() {
            return restItemArrayList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        Toolbar toolbar;
        TextView breakfastContent, lunchContent, dinnerContent;

        public ViewHolder(View itemView) {
            super(itemView);

            View contentLayout = itemView.findViewById(R.id.tab_rest_week_content_layout);

            toolbar = (Toolbar) contentLayout.findViewById(R.id.tab_rest_week_list_toolbar);
            breakfastContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_breakfast);
            lunchContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_lunch);
            dinnerContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_dinner);
        }
    }

    private void execute() {
        if (mProgressWheel != null)
            mProgressLayout.setVisibility(View.VISIBLE);
        if (mProgressWheel != null)
            mProgressWheel.spin();

        mAsyncTask = AsyncUtil.execute(new AsyncJob.Base<WeekRestItem>(){
            @Override
            public WeekRestItem call() throws Exception {
                return PARSE_RESTAURANT_WEEK.parse(HttpRequest.getBody("http://www.uos.ac.kr/food/placeList.do?rstcde=" + getCode(mCurrentSelectionId)));
            }

            @Override
            public void onPostExcute() {
                super.onPostExcute();

                if (mProgressWheel != null)
                    mProgressWheel.stopSpinning();
                if (mProgressLayout != null)
                    mProgressLayout.setVisibility(View.INVISIBLE);

                mAsyncTask = null;

            }

            @Override
            public void onResult(WeekRestItem result) {
                mRestWeekAdapter.restItemArrayList.clear();
                mRestWeekAdapter.restItemArrayList.addAll(result.weekList);
                mRestWeekAdapter.notifyDataSetChanged();
            }

            @Override
            public void exceptionOccured(Exception e) {
                AppUtil.showErrorToast(getActivity(), e, true);
            }
        });

    }

    String getCode(int selection) {
        switch (selection) {
            case R.string.tab_rest_students_hall: // 학생회관
                return "020";
            case R.string.tab_rest_anekan: // 양식당
                return "030";
            case R.string.tab_rest_natural: // 자연과학관
                return "040";
            case R.string.tab_rest_main_8th: // 본관 8층
                return "010";
            default:
            case R.string.tab_rest_living: // 생활관
                return "050";
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mAsyncTask != null && mAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
    }
}
