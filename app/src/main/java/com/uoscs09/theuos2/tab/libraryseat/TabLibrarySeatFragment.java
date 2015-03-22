package com.uoscs09.theuos2.tab.libraryseat;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.AbsAsyncFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParserSeat;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * 도서관 좌석 정보 현황을 보여주는 페이지
 */
public class TabLibrarySeatFragment extends AbsAsyncFragment<ArrayList<SeatItem>> {
    /**
     * 좌석 현황 리스트 뷰의 adapter
     */
    @ReleaseWhenDestroy
    private RecyclerView.Adapter<SeatListAdapter.ViewHolder> mSeatAdapter;
    private StaggeredGridLayoutManager mLayoutManager;
    /**
     * 해지 될 좌석 정보 리스트 뷰의 adapter
     */
    @ReleaseWhenDestroy
    private ArrayAdapter<String> mInfoAdapter;
    /**
     * 좌석 정보 리스트
     */
    @AsyncData
    private ArrayList<SeatItem> mSeatList;
    /**
     * 해지 될 좌석 정보 리스트
     */
    private ArrayList<String> mDissmissInfoList;
    /**
     * 해지 될 좌석 정보 뷰, infoDialog에서 보여진다.
     */
    @ReleaseWhenDestroy
    private View mDismissDialogView;
    /**
     * 상단 액션바에 설정되는 timeTextView에 설정될 Text.<br>
     * <p/>
     * {@code onSaveonSaveInstanceState()} 에서 "COMMIT_TIME"라는 이름으로 저장된다.
     */
    private String mSearchTime = StringUtil.NULL;
    /**
     * 해지될 좌석 정보 버튼 ({@code R.id.action_info})을 선택하면 나타나는 AlertDialog<br>
     * 해지될 좌석 정보를 보여준다.
     */
    @ReleaseWhenDestroy
    private AlertDialog mInfoDialog;

    @ReleaseWhenDestroy
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final ParserSeat mParser = new ParserSeat();

    /**
     * 중앙 도서관 좌석 정보 확인 페이지
     */
    private final static String URL = "http://203.249.102.34:8080/seat/domian5.asp";
    /**
     * bundle에서 동기화 시간 정보 String을 가리킨다.
     */
    private final static String COMMIT_TIME = "COMMIT_TIME";
    /**
     * bundle에서 좌석 정보 List를 가리킨다.
     */
    private final static String BUNDLE_LIST = "SeatList";
    /**
     * bundle에서 해지될 좌석 정보 List를 가리킨다.
     */
    private final static String INFO_LIST = "InfoList";
    /**
     * {@code SubSeatWebActivity}에 전달할 SeatItem을 가리킨다.
     */
    public final static String ITEM = "item";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSearchTime = savedInstanceState.getString(COMMIT_TIME);
            mSeatList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
            mDissmissInfoList = savedInstanceState.getStringArrayList(INFO_LIST);

        } else {
            mSeatList = new ArrayList<>();
            mDissmissInfoList = new ArrayList<>();
        }

        Activity activity = getActivity();
        mInfoAdapter = new SeatDismissInfoListAdapter(activity, mDissmissInfoList);
        mSeatAdapter = new SeatListAdapter(getActivity(), mSeatList);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

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
                sendTrackerEvent("swipe" , "SwipeRefreshView");
                execute();
            }
        });

        /*
      좌석 정보 리스트 뷰
     */
        RecyclerView mSeatListView = (RecyclerView) rootView.findViewById(R.id.tab_library_list_seat);
        mSeatListView.setAdapter(mSeatAdapter);
        mSeatListView.setLayoutManager(mLayoutManager);
        mSeatListView.setItemAnimator(new DefaultItemAnimator());

        mDismissDialogView = View.inflate(getActivity(), R.layout.dialog_library_dismiss_info, null);

        ListView mInfoListView = (ListView) mDismissDialogView.findViewById(R.id.tab_library_listview_dismiss);
        mInfoListView.setEmptyView(mDismissDialogView.findViewById(android.R.id.empty));
        mInfoListView.setAdapter(mInfoAdapter);

        rootView.findViewById(R.id.action_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClickEvent("actionButton");
                mSwipeRefreshLayout.setRefreshing(true);
                execute();
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(COMMIT_TIME, mSearchTime);
        outState.putParcelableArrayList(BUNDLE_LIST, mSeatList);
        outState.putStringArrayList(INFO_LIST, mDissmissInfoList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        if (mSeatList.isEmpty()) {
            if (getExecutor() != null || !isRunning()) {
                execute();
            }
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_library_seat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.action_refresh:
                execute();
                return true;
                */
            case R.id.action_info:
                if (getExecutor() != null && isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_while_loading, true);
                    return true;
                }

                if (mInfoDialog == null) {
                    mInfoDialog = new MaterialDialog.Builder(getActivity())
                            .title(R.string.action_dissmiss_info)
                            .customView(mDismissDialogView, false)
                            .build();
                }

                sendClickEvent("dismiss info");
                mInfoDialog.show();

                return true;

            default:
                return false;
        }
    }

    @Override
    protected void execute() {
        mSeatList.clear();
        mSeatAdapter.notifyDataSetChanged();

        super.execute();
    }

    @Override
    protected void onTransactPostExecute() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onTransactResult(ArrayList<SeatItem> result) {
        updateTimeView();

        mSeatList.clear();
        mSeatList.addAll(result);

        mSeatAdapter.notifyDataSetChanged();
        mInfoAdapter.notifyDataSetChanged();
    }

    public static ArrayList<SeatItem> getLibrarySeatList(ParserSeat parser) throws Exception {
        String body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR);
        return parser.parse(body);
    }

    @Override
    public ArrayList<SeatItem> call() throws Exception {
        ArrayList<SeatItem> callSeatList = getLibrarySeatList(mParser);

        // '해지될 좌석 정보' 정보를 리스트에 추가
        SeatItem dismissInfo = callSeatList.remove(callSeatList.size() - 1);
        if (mDissmissInfoList != null)
            mDissmissInfoList.clear();

        if (mInfoAdapter != null)
            mInfoAdapter.clear();

        String[] array = dismissInfo.occupySeat.split(StringUtil.NEW_LINE);
        for (int i = 0; i < array.length - 1; i += 2) {
            mDissmissInfoList.add(array[i] + "+" + array[i + 1]);
        }

        // 이용률이 50%가 넘는 스터디룸은 보여주지 않음
        if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_SEAT, false)) {
            filterSeatList(callSeatList);
        }
        return callSeatList;
    }

    private void filterSeatList(ArrayList<SeatItem> originalList) {
        SeatItem item;

        // 스터디룸 인덱스
        final int[] filterArr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
        final int size = filterArr.length;
        for (int i = size - 1; i > -1; i--) {
            item = originalList.get(filterArr[i]);

            if (Double.valueOf(item.utilizationRate) >= 50)
                originalList.remove(item);

        }
    }

    private void updateTimeView() {
        // Fragment가 Attatch 되지 않은 경우
        if (getActivity() == null)
            return;
        mSearchTime = TimeUtil.sFormat_am_hms.format(new Date());
        setSubtitleWhenVisible(mSearchTime);
    }

    @Override
    protected CharSequence getSubtitle() {
        return mSearchTime;
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "TabLibrarySeatFragment";
    }
}
