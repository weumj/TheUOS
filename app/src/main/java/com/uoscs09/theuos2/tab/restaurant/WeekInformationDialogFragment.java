package com.uoscs09.theuos2.tab.restaurant;


import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsAnimDialogFragment;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.ArrayList;

import rx.Subscription;

public class WeekInformationDialogFragment extends AbsAnimDialogFragment {
    private static final String TAG = "WeekInformationDialogFragment";

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mProgressLayout;
    private ProgressWheel mProgressWheel;
    private View mFailView, mEmptyView;

    private RestWeekAdapter mRestWeekAdapter = new RestWeekAdapter(new ArrayList<>());
    private int mCurrentSelectionId;
    private RestWeekItem restWeekItem;
    private Subscription mSubscription;

    public static void fetchDataAndShow(final Fragment fragment, final int stringId, View v) {
        MaterialDialog d = AppUtil.getProgressDialog(fragment.getActivity(), false, null);// (dialog, which) -> subscription.unsubscribe());

        Subscription subscription = AppRequests.Restaurants.readWeekInfo(getCode(stringId))
                .subscribe(
                        result -> showWeekDialog(fragment, stringId, v, result),
                        e -> {
                            AppUtil.showErrorToast(fragment.getActivity(), e, true);
                            d.dismiss();
                        },
                        d::dismiss
                );

        d.show();
    }

    private static void showWeekDialog(final Fragment fragment, final int stringId, View v, RestWeekItem restWeekItem) {
        WeekInformationDialogFragment dialogFragment = new WeekInformationDialogFragment();
        dialogFragment.setSelection(stringId);
        dialogFragment.setRestWeekItem(restWeekItem);
        dialogFragment.showFromView(fragment.getFragmentManager(), "week", v);
    }

    public void setSelection(int stringId) {
        this.mCurrentSelectionId = stringId;
    }

    private void setRestWeekItem(RestWeekItem restWeekItem) {
        this.restWeekItem = restWeekItem;
        mRestWeekAdapter.restItemArrayList.clear();
        mRestWeekAdapter.restItemArrayList.addAll(restWeekItem.weekList);
        mRestWeekAdapter.notifyDataSetChanged();
    }

    @Override
    protected View createView() {
        View rootView = View.inflate(getActivity(), R.layout.dialog_tab_rest_week, null);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mCurrentSelectionId != 0) {
            mToolbar.setTitle(mCurrentSelectionId);
            mToolbar.setSubtitle(restWeekItem.getPeriodString());
            sendTrackerEvent("view", getString(mCurrentSelectionId));
        } else {
            dismiss();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.tab_rest_week_swipe_layout);
        mSwipeRefreshLayout.setColorSchemeColors(ResourceUtil.getAttrColor(getActivity(), R.attr.colorPrimaryDark));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            sendTrackerEvent("swipe", "SwipeRefreshView");
            execute(true);
        });

        RecyclerView recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.tab_rest_week_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mRestWeekAdapter);

        mProgressLayout = rootView.findViewById(R.id.progress_layout);
        mProgressWheel = (ProgressWheel) mProgressLayout.findViewById(R.id.progress_wheel);

        return rootView;
    }

    private void showReloadView() {
        if (mFailView == null) {
            mFailView = ((ViewStub) ((View) mSwipeRefreshLayout.getParent()).findViewById(R.id.tab_rest_week_stub_reload)).inflate();
            mFailView.findViewById(android.R.id.content).setOnClickListener(v -> {
                sendClickEvent("fail view");
                execute(true);
            });
        }
    }

    private void showEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = ((ViewStub) ((View) mSwipeRefreshLayout.getParent()).findViewById(R.id.tab_rest_week_stub_empty_info)).inflate();
            mEmptyView.findViewById(android.R.id.content).setOnClickListener(v -> dismiss());
        }
    }


    private void execute(final boolean shouldUpdateUsingInternet) {
        if (mProgressWheel != null)
            mProgressLayout.setVisibility(View.VISIBLE);
        if (mProgressWheel != null)
            mProgressWheel.spin();
        if (mFailView != null)
            mFailView.setVisibility(View.GONE);


        mSubscription = AppRequests.Restaurants.readWeekInfo(getCode(mCurrentSelectionId))
                .subscribe(result -> {
                            ArrayList<RestItem> weekList = result.weekList;
                            setRestWeekItem(result);

                            if (weekList.isEmpty()) {
                                showEmptyView();
                            } else {
                                mToolbar.setSubtitle(result.getPeriodString());
                            }
                        },
                        e -> {
                            postExecute();
                            AppUtil.showErrorToast(getActivity(), e, true);
                            showReloadView();
                        },
                        this::postExecute
                );
    }

    private void postExecute() {
        if (mProgressWheel != null)
            mProgressWheel.stopSpinning();
        if (mProgressLayout != null)
            mProgressLayout.setVisibility(View.INVISIBLE);
        if (mFailView != null)
            mFailView.setVisibility(View.VISIBLE);

        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);

        mSubscription = null;
    }

    static String getCode(int selection) {
        switch (selection) {
            case R.string.tab_rest_students_hall: // 학생회관
                return "020";
            case R.string.tab_rest_anekan: // 양식당
                return "030";
            case R.string.tab_rest_nature_science: // 자연과학관
                return "040";
            case R.string.tab_rest_main_8th: // 본관 8층
                return "010";
            default:
            case R.string.tab_rest_dormitory: // 생활관
                return "050";
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mSubscription != null)
            mSubscription.unsubscribe();
        mSubscription = null;

    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return TAG;
    }


    private static class RestWeekAdapter extends RecyclerView.Adapter<ViewHolder> {
        final ArrayList<RestItem> restItemArrayList;

        RestWeekAdapter(ArrayList<RestItem> arrayList) {
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
