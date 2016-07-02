package com.uoscs09.theuos2.tab.announce;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class TabAnnounceFragment extends AbsProgressFragment<List<AnnounceItem>> implements AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener {

    static final String PAGE_NUM = "PAGE";
    static final String ITEM = "item";
    static final String INDEX_CATEGORY = "category_index";

    /**
     * 상단 액션바에 추가될 위젯, 페이지 인덱스
     */
    private TextView mPageIndexView;
    /**
     * 상단 액션바에 추가될 위젯, 카테고리 선택
     */
    private Spinner mCategorySpinner;
    private ArrayAdapter<AnnounceItem> mAnnounceAdapter;
    private View mListFooterView;

    @BindView(R.id.tab_announce_list_announce)
    ListView mListView;
    @BindView(R.id.tab_announce_empty_view)
    View mEmptyView;
    private Dialog mPageSelectDialog;
    private NumberPicker mPageNumberPicker;
    private MenuItem mSearchMenu;


    @AsyncData
    private ArrayList<AnnounceItem> mDataList;

    /**
     * (검색 메뉴 선택시)검색어를 저장함
     */
    private String mSearchQuery;
    private boolean mIsSearchRequesting = false;
    private int mCurrentPageIndex;

    /**
     * 표시할 공지사항목록의 변동이 생겼을 때 (분류 변경 등등..)<br>
     * 페이지의 최대값이 변경되어야 하는지를 가리키는 값
     */
    private boolean mShouldChangeMaxValueOfPage = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_NUM, mCurrentPageIndex);
        outState.putInt(INDEX_CATEGORY, mCategorySpinner.getSelectedItemPosition());
        outState.putParcelableArrayList(ITEM, mDataList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int currentCategoryIndex;
        if (savedInstanceState != null) {
            mCurrentPageIndex = savedInstanceState.getInt(PAGE_NUM);
            mDataList = savedInstanceState.getParcelableArrayList(ITEM);
            currentCategoryIndex = savedInstanceState.getInt(INDEX_CATEGORY);
        } else {
            mCurrentPageIndex = 1;
            mDataList = new ArrayList<>();
            currentCategoryIndex = 0;
        }

        initPageSelectDialog();

        super.onCreate(savedInstanceState);

        mAnnounceAdapter = new AnnounceAdapter(getActivity(), mDataList);

        initToolbarAndTab(currentCategoryIndex);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView.setVisibility(mDataList.size() != 0 ? View.INVISIBLE : View.VISIBLE);

        mListFooterView = LayoutInflater.from(view.getContext()).inflate(R.layout.view_tab_announce_bottom_more, mListView, false);
        mListView.addFooterView(mListFooterView);
        mListFooterView.setVisibility(View.GONE);
        mListFooterView.findViewById(android.R.id.text1).setOnClickListener(v -> {
            sendClickEvent("view more page");
            executeJob(mIsSearchRequesting, true, mCurrentPageIndex + 1);
        });

        mListView.setEmptyView(mEmptyView);
        // mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAnnounceAdapter);

        registerProgressView(view.findViewById(R.id.progress_layout));

    }

    @OnClick(R.id.tab_announce_empty_view)
    void emptyViewClick() {
        mCategorySpinner.performClick();
        sendEmptyViewClickEvent();
    }

    @Override
    protected int layoutRes() {
        return R.layout.tab_announce;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mAnnounceAdapter.isEmpty() && mListFooterView.getVisibility() != View.VISIBLE)
            mListFooterView.setVisibility(View.VISIBLE);
    }

    private int getCurrentCategoryIndex() {
        return mCategorySpinner.getSelectedItemPosition();
    }

    @OnItemClick(R.id.tab_announce_list_announce)
    void listItemClick(int position, View view) {
        if (!isMenuVisible() || getCurrentCategoryIndex() < 1)
            return;

        Intent intent = new Intent(getActivity(), SubAnnounceWebActivity.class)
                .putExtra(ITEM, mAnnounceAdapter.getItem(position))
                .putExtra(INDEX_CATEGORY, getCurrentCategoryIndex());

        AnimUtil.startActivityWithScaleUp(getActivity(), intent, view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_forward: {
                if (getCurrentCategoryIndex() < 1 || !isMenuVisible())
                    return true;

                sendTrackerEvent("page forward", "" + getCurrentCategoryIndex());
                executeJob(mIsSearchRequesting, false, mCurrentPageIndex + 1);
                return true;
            }
            case R.id.action_backward: {
                if (getCurrentCategoryIndex() < 1 || !isMenuVisible())
                    return true;

                if (mCurrentPageIndex > 1) {
                    sendTrackerEvent("page backward", "" + getCurrentCategoryIndex());
                    executeJob(mIsSearchRequesting, false, mCurrentPageIndex - 1);
                }
                return true;
            }

            default:
                return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_anounce, menu);
        mSearchMenu = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchMenu);

        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint(getText(R.string.search_hint));
            searchView.setOnQueryTextFocusChangeListener((view, queryTextFocused) -> {
                if (!queryTextFocused) {
                    MenuItemCompat.collapseActionView(mSearchMenu);
                    searchView.setQuery("", false);
                }
            });

        } else {
            AppUtil.showToast(getActivity(), "compact error");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * spinner 가 선택되면(카테고리 선택) 호출됨
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemId) {
        // Fragment 가 ViewPager 내부에 존재하지만, 사용자에게 보이지 않는 경우
        if (!isMenuVisible()) {
            return;
        }
        if (adapterView.getTag() != null) {
            adapterView.setTag(null);
            return;
        }

        if (position < 1)
            return;

        executeJob(false, false, 1);

        sendTrackerEvent("category changed", "", position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        InputMethodManager ipm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        SearchView v = (SearchView) MenuItemCompat.getActionView(mSearchMenu);
        ipm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        mSearchMenu.collapseActionView();

        if (getCurrentCategoryIndex() < 1) {
            AppUtil.showToast(getActivity(), R.string.tab_announce_invalid_category, true);

        } else {
            mSearchQuery = query.trim();
            executeJob(true, false, 1);
        }

        return true;
    }


    private void executeJob(boolean search, boolean more, int newPage) {
        mIsSearchRequesting = search;

        mEmptyView.setVisibility(View.INVISIBLE);
        mListFooterView.setClickable(false);

        if (search) {
            executeSearchJob(more, newPage, mSearchQuery);
        } else
            executeJob(more, newPage);
    }

    private void executeSearchJob(boolean moreRequest, int newPageIndex, String query) {
        execute(AppRequests.Announces.searchRequest(getCurrentCategoryIndex(), newPageIndex, query),
                result -> {
                    mListFooterView.setClickable(true);
                    if (moreRequest) updateWithResultInMoreRequest(result, newPageIndex);
                    else updateWithResult(result, true, newPageIndex);
                },
                this::onError
        );
    }

    private void executeJob(boolean moreRequest, int newPageIndex) {
        execute(AppRequests.Announces.normalRequest(getCurrentCategoryIndex(), newPageIndex),
                result -> {
                    mListFooterView.setClickable(true);
                    if (moreRequest) updateWithResultInMoreRequest(result, newPageIndex);
                    else updateWithResult(result, false, newPageIndex);
                },
                this::onError
        );
    }


    public void onError(Throwable e) {
        simpleErrorRespond(e);

        mListFooterView.setClickable(true);
        if (mAnnounceAdapter.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
    }


    public void updateWithResult(List<AnnounceItem> result, boolean searchRequest, int newPageIndex) {
        if (checkResultNotEmpty(result)) {

            mAnnounceAdapter.clear();
            mAnnounceAdapter.addAll(result);
            mAnnounceAdapter.notifyDataSetChanged();

            // 페이지 선택에서 이동 가능한 최대 페이지 번호를
            // 공지사항의 인덱스를 기준으로 설정함
            if (mShouldChangeMaxValueOfPage) {
                //mPageNumberPicker.setMaxValue(getMaximumAnnounceIndex(result) / 10 + 1);
                mShouldChangeMaxValueOfPage = false;
            }
            mListFooterView.setVisibility(mAnnounceAdapter.isEmpty() ? View.GONE : View.VISIBLE);

        } else if (searchRequest) {
            mAnnounceAdapter.clear();
            mAnnounceAdapter.notifyDataSetChanged();
            mCategorySpinner.setSelection(0, true);
        }

        updatePageNumber(newPageIndex);
    }

    public void updateWithResultInMoreRequest(List<AnnounceItem> result, int newPage) {
        if (checkResultNotEmpty(result)) {
            mAnnounceAdapter.addAll(result);
            mAnnounceAdapter.notifyDataSetChanged();

            // 검색된 결과가 10개 이하이면, 마지막 페이지에 도달한 것 이므로
            // '다음 페이지' 버튼을 보여주지 않는다.
            mListFooterView.setVisibility(result.size() < 10 ? View.GONE : View.VISIBLE);

            updatePageNumber(newPage);
        }
        // '다음 페이지 요청'의 경우 검색된 결과가 없다는 것은 마지막 페이지를 지났다는 것 이므로
        // 페이지를 업데이트 하지 않는다.
    }
/*
    private int getMaximumAnnounceIndex(List<AnnounceItem> announceItems) {
        final int size = announceItems.size();

        for (int i = 0; i < size; i++) {
            AnnounceItem item = announceItems.get(i);
            if (item.isTypeNotice())
                continue;

            return item.number;
        }

        return 0;
    }
    */

    private boolean checkResultNotEmpty(List<AnnounceItem> result) {
        if (result == null || result.size() == 0) {
            AppUtil.showToast(getActivity(), R.string.search_result_empty, true);

            if (mAnnounceAdapter.isEmpty()) {
                mListFooterView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }

            return false;
        } else
            return true;
    }

    /**
     * Toolbar 에 붙일 Tab (카테고리 Spinner & PageIndexView) 을 초기화 한다.
     */
    private void initToolbarAndTab(final int currentCategoryIndex) {
        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_announce_toolbar_menu, getToolbarParent(), false);

        mCategorySpinner = (Spinner) mTabParent.findViewById(R.id.tab_announce_action_spinner1);

        // 이전에 선택된 카테고리가 존재하는 경우 (mCurrentCategoryIndex != 0)
        // Spinner.setSelection() 이 호출되면 mSpinnerOnItemSelectedListener.onItemSelected() 가 호출된다.
        // 이미 공지사항 데이터가 존재하므로, 웹에서 공지사항을 얻어올 필요가 없으므로
        // spinner 에 tagging 을 하여 알려준다.
        mCategorySpinner.setTag(1);
        mCategorySpinner.setSelection(currentCategoryIndex);
        mCategorySpinner.setOnItemSelectedListener(this);

        mPageIndexView = (TextView) mTabParent.findViewById(R.id.tab_anounce_action_textView_page);
        mPageIndexView.setOnClickListener(v -> {
            if (getCurrentCategoryIndex() < 1) {
                AppUtil.showToast(getActivity(), R.string.tab_announce_invalid_category, true);
            } else {
                mPageSelectDialog.show();
            }
        });

        updatePageNumber(mCurrentPageIndex);

        registerTabParentView(mTabParent);
    }

    private void updatePageNumber(int pageIndex) {
        if (mPageIndexView != null)
            mPageIndexView.setText(String.format(Locale.getDefault(), "%d PAGE", pageIndex));
        mCurrentPageIndex = pageIndex;
    }

    /**
     * 페이지 선택 dialog 를 생성한다.
     */
    private void initPageSelectDialog() {
        Context context = getActivity();
        mPageNumberPicker = new NumberPicker(context);
        mPageNumberPicker.setLayoutParams(new NumberPicker.LayoutParams(NumberPicker.LayoutParams.WRAP_CONTENT, NumberPicker.LayoutParams.WRAP_CONTENT));

        mPageNumberPicker.setMinValue(1);
        mPageNumberPicker.setMaxValue(999);

        mPageSelectDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.tab_announce_plz_select_page)
                .setView(mPageNumberPicker)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    sendTrackerEvent("page changed", "" + getCurrentCategoryIndex(), mPageNumberPicker.getValue());
                    executeJob(mIsSearchRequesting, false, mPageNumberPicker.getValue());
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabAnnounceFragment";
    }

}
