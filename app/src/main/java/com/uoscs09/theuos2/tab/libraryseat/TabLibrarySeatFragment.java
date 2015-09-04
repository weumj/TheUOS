package com.uoscs09.theuos2.tab.libraryseat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.base.OnItemClickListener;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

/**
 * 도서관 좌석 정보 현황을 보여주는 페이지
 */
public class TabLibrarySeatFragment extends AbsProgressFragment<SeatInfo>
        implements OnItemClickListener<SeatListAdapter.ViewHolder>, Request.ResultListener<SeatInfo>, Request.ErrorListener, Processor<SeatInfo, SeatInfo> {

    /**
     * 중앙 도서관 좌석 정보 확인 페이지
     */
    private final static String URL = "http://203.249.102.34:8080/seat/domian5.asp";
    /**
     * bundle 에서 동기화 시간 정보 String을 가리킨다.
     */
    private final static String COMMIT_TIME = "COMMIT_TIME";

    private final static String INFO_LIST = "InfoList";
    /**
     * {@code SubSeatWebActivity}에 전달할 SeatItem을 가리킨다.
     */
    final static String ITEM = "item";

    private static final ParseSeat LIBRARY_SEAR_PARSER = new ParseSeat();
    /*
    private SeatListAdapter mSeatAdapter;
    private StaggeredGridLayoutManager mLayoutManager;
    */

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mSeatListView;
    private SeatDismissDialogFragment mSeatDismissDialogFragment;

    private SeatInfo mSeatInfo;
    private boolean isAdapterItemClicked = false;

    /**
     * 상단 액션바에 설정되는 timeTextView 에 설정될 Text.<br>
     * <p/>
     * {@code onSaveInstanceState()} 에서 "COMMIT_TIME"라는 이름으로 저장된다.
     */
    private String mSearchTime = StringUtil.NULL;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSearchTime = savedInstanceState.getString(COMMIT_TIME);
            mSeatInfo = savedInstanceState.getParcelable(INFO_LIST);

        } else {
            mSeatInfo = new SeatInfo();
            mSeatInfo.seatItemList = new ArrayList<>();
            mSeatInfo.seatDismissInfoList = new ArrayList<>();
        }

        mSeatDismissDialogFragment = new SeatDismissDialogFragment();
        mSeatDismissDialogFragment.setSeatInfo(mSeatInfo);
        mSeatDismissDialogFragment.setCancelable(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_libraryseat, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                AppUtil.getAttrValue(getActivity(), R.attr.color_actionbar_title),
                AppUtil.getAttrValue(getActivity(), R.attr.colorAccent)
        );
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendTrackerEvent("swipe", "SwipeRefreshView");
                execute();
            }
        });

        SeatListAdapter mSeatAdapter = new SeatListAdapter(getActivity(), mSeatInfo.seatItemList);
        mSeatAdapter.setOnItemClickListener(this);

        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        mSeatListView = (RecyclerView) rootView.findViewById(R.id.tab_library_list_seat);
        mSeatListView.setAdapter(mSeatAdapter);
        mSeatListView.setLayoutManager(mLayoutManager);
        mSeatListView.setItemAnimator(new SlideInDownAnimator());

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        if (mSeatInfo.seatItemList.isEmpty()) {
            execute();
        }

        return rootView;
    }

    @Override
    public void onItemClick(SeatListAdapter.ViewHolder viewHolder, View v) {
        if (isAdapterItemClicked)
            return;

        isAdapterItemClicked = true;
        Intent intent = new Intent(getActivity(), SubSeatWebActivity.class);
        intent.putExtra(TabLibrarySeatFragment.ITEM, (Parcelable) viewHolder.getItem());

        // tracking 은 SubSeatWebActivity 에서 함.
        AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
        isAdapterItemClicked = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(COMMIT_TIME, mSearchTime);
        outState.putParcelable(INFO_LIST, mSeatInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_library_seat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                sendClickEvent("refresh");
                mSwipeRefreshLayout.setRefreshing(true);
                execute();
                return true;

            case R.id.action_info:
                if (isTaskRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_while_loading, true);
                    return true;
                }

                sendClickEvent("dismiss info");
                if (!mSeatDismissDialogFragment.isAdded())
                    mSeatDismissDialogFragment.show(getFragmentManager(), "SeatDismissInfo");

                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mSeatListView.getAdapter().notifyItemRangeRemoved(0, mSeatInfo.seatItemList.size());
        mSeatInfo.seatItemList.clear();
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }


    private void execute() {
        execute(true, requestSeatInfo(getActivity()).wrap(this), this, this, true);
    }

    @Override
    public SeatInfo process(SeatInfo info) throws Exception {
        ArrayList<SeatItem> callSeatList = info.seatItemList;

        // 이용률이 50%가 넘는 스터디룸은 보여주지 않음
        if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_SEAT, false)) {
            filterSeatList(callSeatList);
        }
        return info;
    }

    @Override
    public void onResult(SeatInfo result) {
        updateTimeView();

        mSeatInfo.clearAndAddAll(result);

        mSeatListView.getAdapter().notifyItemRangeInserted(0, result.seatItemList.size());
        mSeatDismissDialogFragment.notifyDataSetChanged();
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mSwipeRefreshLayout != null && isTaskRunning())
            mSwipeRefreshLayout.setRefreshing(true);
    }

    public static Request<SeatInfo> requestSeatInfo(Context context) {
        return HttpRequest.Builder.newStringRequestBuilder(URL)
                .setResultEncoding(StringUtil.ENCODE_EUC_KR)
                .build()
                .checkNetworkState(context)
                .wrap(LIBRARY_SEAR_PARSER);
    }

    private static void filterSeatList(ArrayList<SeatItem> originalList) {
        SeatItem item;

        // 스터디룸 인덱스
        final int[] filterArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
        final int size = filterArr.length;
        for (int i = size - 1; i > -1; i--) {
            item = originalList.get(filterArr[i]);

            if (item.utilizationRate >= 50)
                originalList.remove(item);

        }
    }

    private void updateTimeView() {
        mSearchTime = TimeUtil.getFormat_am_hms().format(new Date());
        setSubtitleWhenVisible(mSearchTime);
    }

    @Override
    protected CharSequence getSubtitle() {
        return mSearchTime;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabLibrarySeatFragment";
    }

}
