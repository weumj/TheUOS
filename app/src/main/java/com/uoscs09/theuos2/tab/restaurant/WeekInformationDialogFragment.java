package com.uoscs09.theuos2.tab.restaurant;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseRestaurantWeek;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.io.IOException;
import java.util.ArrayList;

public class WeekInformationDialogFragment extends DialogFragment {
    private static final String TAG = "WeekInformationDialogFragment";
    private static final String FILE_NAME = "FILE_REST_WEEK_ITEM";
    private static final ParseRestaurantWeek RESTAURANT_WEEK_PARSER = new ParseRestaurantWeek();

    @ReleaseWhenDestroy
    private Toolbar mToolbar;
    @ReleaseWhenDestroy
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RestWeekAdapter mRestWeekAdapter;
    private int mCurrentSelectionId;
    private AsyncTask<Void, ?, WeekRestItem> mAsyncTask;

    @ReleaseWhenDestroy
    private View mProgressLayout;
    @ReleaseWhenDestroy
    private ProgressWheel mProgressWheel;
    @ReleaseWhenDestroy
    private View mFailView;

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

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setTitle(mCurrentSelectionId);
        TrackerUtil.getInstance(this).sendEvent(TAG, "view", getString(mCurrentSelectionId));

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.tab_rest_week_swipe_layout);
        mSwipeRefreshLayout.setColorSchemeColors(AppUtil.getAttrColor(getActivity(), R.attr.colorPrimaryDark));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TrackerUtil.getInstance(WeekInformationDialogFragment.this).sendEvent(TAG, "swipe", "SwipeRefreshView");
                execute(true);
            }
        });

        RecyclerView recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.tab_rest_week_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mRestWeekAdapter = new RestWeekAdapter(new ArrayList<RestItem>()));

        mProgressLayout = rootView.findViewById(R.id.progress_layout);
        mProgressWheel = (ProgressWheel) mProgressLayout.findViewById(R.id.progress_wheel);

        execute(false);

        return rootView;
    }

    private void showReloadView() {
        if (mFailView == null) {
            mFailView = ((ViewStub) ((View) mSwipeRefreshLayout.getParent()).findViewById(R.id.tab_rest_week_stub)).inflate();
            mFailView.findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TrackerUtil.getInstance(WeekInformationDialogFragment.this).sendClickEvent(TAG, "fail view");
                    execute(true);
                }
            });
        }

    }


    private void execute(final boolean shouldUpdateUsingInternet) {
        if (mProgressWheel != null)
            mProgressLayout.setVisibility(View.VISIBLE);
        if (mProgressWheel != null)
            mProgressWheel.spin();
        if (mFailView != null)
            mFailView.setVisibility(View.GONE);

        mAsyncTask = AsyncUtil.execute(new AsyncJob.Base<WeekRestItem>() {
            @Override
            public WeekRestItem call() throws Exception {

                Context context = getActivity();
                PrefUtil pref = PrefUtil.getInstance(context);

                final int[] recodedDate = getValueFromPref(pref, mCurrentSelectionId);
                final int today = OApiUtil.getDate();

                // 이번주의 식단이 기록된 파일이 있으면, 인터넷에서 가져오지 않고 그 파일을 읽음
                if (!shouldUpdateUsingInternet && ((recodedDate[0] <= today) && (today <= recodedDate[1]))) {
                    WeekRestItem result = readFile(context, mCurrentSelectionId);

                    if (result != null)
                        return result;

                }

                return readFromInternet(context, mCurrentSelectionId);
            }

            @Override
            public void onPostExcute() {
                super.onPostExcute();

                if (mProgressWheel != null)
                    mProgressWheel.stopSpinning();
                if (mProgressLayout != null)
                    mProgressLayout.setVisibility(View.INVISIBLE);
                if (mFailView != null)
                    mFailView.setVisibility(View.VISIBLE);

                mSwipeRefreshLayout.setRefreshing(false);


                mAsyncTask = null;

            }

            @Override
            public void onResult(WeekRestItem result) {
                ArrayList<RestItem> weekList = result.weekList;
                mRestWeekAdapter.restItemArrayList.clear();
                mRestWeekAdapter.restItemArrayList.addAll(weekList);
                mRestWeekAdapter.notifyDataSetChanged();

                mToolbar.setSubtitle(result.getPeriodString());

            }

            @Override
            public void exceptionOccured(Exception e) {
                AppUtil.showErrorToast(getActivity(), e, true);
                showReloadView();
            }
        });

    }


    private static WeekRestItem readFile(Context context, int selectionId) throws IOException, ClassNotFoundException {
        return IOUtil.readFromFile(context, FILE_NAME + getCode(selectionId));
    }

    private static void writeFile(Context context, int selectionId, WeekRestItem object) throws IOException {
        IOUtil.writeObjectToFile(context, FILE_NAME + getCode(selectionId), object);
    }

    private static WeekRestItem readFromInternet(Context context, int selectionId) throws Exception {

        WeekRestItem result = HttpRequest.Builder.newStringRequestBuilder("http://www.uos.ac.kr/food/placeList.do?rstcde=" + getCode(selectionId))
                .build()
                .checkNetworkState(context)
                .wrap(RESTAURANT_WEEK_PARSER)
                .get();

        writeFile(context, selectionId, result);
        putValueIntoPref(PrefUtil.getInstance(context), selectionId, result);

        return result;
    }

    private static int[] getValueFromPref(PrefUtil prefUtil, int selectionId) {
        int today = OApiUtil.getDate();
        return new int[]{prefUtil.get(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_START_" + getCode(selectionId), today + 1),
                prefUtil.get(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_END_" + getCode(selectionId), today - 1)};
    }

    private static void putValueIntoPref(PrefUtil prefUtil, int selectionId, WeekRestItem item) {
        prefUtil.put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_START_" + getCode(selectionId), item.startDate);
        prefUtil.put(PrefUtil.KEY_REST_WEEK_FETCH_TIME + "_END_" + getCode(selectionId), item.endDate);
    }


    private static String getCode(int selection) {
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

        AsyncUtil.cancelTask(mAsyncTask);
        mAsyncTask = null;

    }


    private static class RestWeekAdapter extends RecyclerView.Adapter<ViewHolder> {
        final ArrayList<RestItem> restItemArrayList;

        public RestWeekAdapter(ArrayList<RestItem> arrayList) {
            this.restItemArrayList = arrayList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_rest_week, parent, false));
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
        final Toolbar toolbar;
        final TextView breakfastContent;
        final TextView lunchContent;
        final TextView dinnerContent;

        public ViewHolder(View itemView) {
            super(itemView);

            View contentLayout = itemView.findViewById(R.id.tab_rest_week_content_layout);

            toolbar = (Toolbar) contentLayout.findViewById(R.id.tab_rest_week_list_toolbar);
            breakfastContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_breakfast);
            lunchContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_lunch);
            dinnerContent = (TextView) contentLayout.findViewById(R.id.tab_rest_week_content_dinner);
        }
    }

}
