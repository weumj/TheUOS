package com.uoscs09.theuos2.tab.libraryseat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.base.ViewHolder;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TimeUtil;

import java.util.Date;

import butterknife.Bind;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;

/**
 * 도서관 좌석 정보 현황을 보여주는 페이지
 */
public class TabLibrarySeatFragment extends AbsProgressFragment<SeatInfo> {


    /**
     * bundle 에서 동기화 시간 정보 String을 가리킨다.
     */
    private final static String COMMIT_TIME = "COMMIT_TIME";

    private final static String INFO_LIST = "InfoList";
    /**
     * {@code SubSeatWebActivity}에 전달할 SeatItem을 가리킨다.
     */
    final static String ITEM = "item";

    /*
    private SeatListAdapter mSeatAdapter;
    private StaggeredGridLayoutManager mLayoutManager;
    */

    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.tab_library_list_seat)
    RecyclerView mSeatListView;

    private SeatDismissDialogFragment mSeatDismissDialogFragment;

    private SeatInfo mSeatInfo;
    private boolean isAdapterItemClicked = false;

    /**
     * 상단 액션바에 설정되는 timeTextView 에 설정될 Text.<br>
     * <p>
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
        }

        mSeatDismissDialogFragment = new SeatDismissDialogFragment();
        mSeatDismissDialogFragment.setSeatInfo(mSeatInfo);
        mSeatDismissDialogFragment.setCancelable(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayout() {
        return R.layout.tab_libraryseat;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setColorSchemeResources(
                AppUtil.getAttrValue(getActivity(), R.attr.color_actionbar_title),
                AppUtil.getAttrValue(getActivity(), R.attr.colorAccent)
        );
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            sendTrackerEvent("swipe", "SwipeRefreshView");
            execute();
        });

        ListRecyclerAdapter<SeatItem, SeatViewHolder> mSeatAdapter = ListRecyclerUtil.newSimpleAdapter(mSeatInfo.seatItemList, SeatViewHolder.class, R.layout.list_layout_seat);
        mSeatAdapter.setOnItemClickListener((viewHolder, v) -> {
            if (isAdapterItemClicked)
                return;

            isAdapterItemClicked = true;
            Intent intent = new Intent(getActivity(), SubSeatWebActivity.class)
                    .putExtra(TabLibrarySeatFragment.ITEM, (Parcelable) viewHolder.getItem())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // tracking 은 SubSeatWebActivity 에서 함.
            AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
            isAdapterItemClicked = false;
        });


        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        mSeatListView.setAdapter(mSeatAdapter);
        mSeatListView.setLayoutManager(mLayoutManager);
        mSeatListView.setItemAnimator(new SlideInDownAnimator());

        registerProgressView(view.findViewById(R.id.progress_layout));

        if (mSeatInfo.seatItemList.isEmpty()) {
            execute();
        }
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
              //  if (isTaskRunning()) {
              //      AppUtil.showToast(getActivity(), R.string.progress_while_loading, true);
              //      return true;
             //   }

                sendClickEvent("dismiss info");
                if (!mSeatDismissDialogFragment.isAdded())
                    mSeatDismissDialogFragment.show(getFragmentManager(), "SeatDismissInfo");

                return true;

            default:
                return false;
        }
    }

    private void execute() {
        mSeatListView.getAdapter().notifyItemRangeRemoved(0, mSeatInfo.seatItemList.size());
        mSeatInfo.seatItemList.clear();

        execute(AppRequests.LibrarySeats.request(),
                result -> {
                    if (mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);

                    updateTimeView();

                    mSeatInfo.clearAndAddAll(result);

                    mSeatListView.getAdapter().notifyItemRangeInserted(0, result.seatItemList.size());
                    mSeatDismissDialogFragment.notifyDataSetChanged();
                },
                e -> {
                    e.printStackTrace();
                    if (mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);

                    simpleErrorRespond(e);
                }
        );
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
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


    static class SeatViewHolder extends ViewHolder<SeatItem> {
        @Bind(R.id.tab_library_seat_list_text_room_name)
        TextView roomName;
        @Bind(R.id.ripple)
        View ripple;
        @Bind(R.id.tab_libray_seat_list_progress_img)
        TextView progressImg;

        final PieProgressDrawable drawable = new PieProgressDrawable();

        public SeatViewHolder(View convertView) {
            super(convertView);

            ripple.setOnClickListener(this);

            Context context = convertView.getContext();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            drawable.setBorderWidth(2, dm);

            drawable.setTextSize(15 * dm.scaledDensity);
            drawable.setTextColor(AppUtil.getAttrColor(context, R.attr.color_primary_text));
            drawable.setColor(ContextCompat.getColor(context, R.color.gray_red));
            drawable.setCenterColor(AppUtil.getAttrColor(context, R.attr.cardBackgroundColor));
            //noinspection deprecation
            progressImg.setBackgroundDrawable(drawable);
        }

        @Override
        protected void setView(int position) {
            super.setView(position);

            SeatItem item = getItem();
            roomName.setText(item.roomName);
            int progress = Math.round(item.utilizationRate);
            drawable.setText(item.vacancySeat.trim() + " / " + (Integer.parseInt(item.occupySeat.trim()) + Integer.parseInt(item.vacancySeat.trim())));
            drawable.setLevel(progress);
        }

    }
}
