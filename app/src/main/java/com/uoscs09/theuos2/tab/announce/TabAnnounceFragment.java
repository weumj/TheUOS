package com.uoscs09.theuos2.tab.announce;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
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
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.customview.NestedListView;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;

public class TabAnnounceFragment extends AbsProgressFragment<ArrayList<AnnounceItem>>
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener, Request.ErrorListener {

    static final String PAGE_NUM = "PAGE";
    static final String ITEM = "item";
    static final String INDEX_CATEGORY = "category_index";
    private static final ParseAnnounce PARSER = ParseAnnounce.getParser();
    private static final ParseAnnounce SCHOLARSHIP_PARSER = ParseAnnounce.getScholarshipParser();

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
    private View mEmptyView;
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
    private ArrayMap<String, String> mQueryTable;

    private final RequestHelper mRequestHelper = new RequestHelper(), mMoreRequestHelper = new MoreRequestHelper();


    /* TODO Fragment Callback */
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

        mQueryTable = new ArrayMap<>();

        initPageSelectDialog();

        super.onCreate(savedInstanceState);

        mAnnounceAdapter = new AnnounceAdapter(getActivity(), mDataList);

        initToolbarAndTab(currentCategoryIndex);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_announce, container, false);

        NestedListView mListView = (NestedListView) rootView.findViewById(R.id.tab_announce_list_announce);

        mEmptyView = rootView.findViewById(R.id.tab_announce_empty_view);
        mEmptyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategorySpinner.performClick();
                sendEmptyViewClickEvent();
            }
        });

        mEmptyView.setVisibility(mDataList.size() != 0 ? View.INVISIBLE : View.VISIBLE);

        mListFooterView = inflater.inflate(R.layout.view_tab_announce_bottom_more, mListView, false);
        mListView.addFooterView(mListFooterView);
        mListFooterView.setVisibility(View.GONE);
        mListFooterView.findViewById(android.R.id.text1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClickEvent("view more page");
                executeJob(mIsSearchRequesting, true, mCurrentPageIndex + 1);
            }
        });

        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAnnounceAdapter);

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        return rootView;
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
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean queryTextFocused) {
                    if (!queryTextFocused) {
                        MenuItemCompat.collapseActionView(mSearchMenu);
                        searchView.setQuery("", false);
                    }
                }
            });

        } else {
            AppUtil.showToast(getActivity(), "compact error");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

	/* TODO Fragment Callback end */

    /* TODO Listener Callback */

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long itemId) {
        if (!isMenuVisible() || getCurrentCategoryIndex() < 1)
            return;

        Intent intent = new Intent(getActivity(), SubAnnounceWebActivity.class)
                .putExtra(ITEM, mAnnounceAdapter.getItem(pos))
                .putExtra(INDEX_CATEGORY, getCurrentCategoryIndex());

        AppUtil.startActivityWithScaleUp(getActivity(), intent, view);
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

        clearParams();
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
            clearParams();
            mSearchQuery = query.trim();
            executeJob(true, false, 1);
        }

        return true;
    }

	/* TODO Listener end */

    private void clearParams() {
        mSearchQuery = null;
        mCurrentPageIndex = 1;
        mShouldChangeMaxValueOfPage = true;
        mIsSearchRequesting = false;

        // TODO 최적화 필요, 필요없이 지우고 쓰고 함
        mQueryTable.clear();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mEmptyView.setVisibility(View.INVISIBLE);
        mListFooterView.setClickable(false);
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        mListFooterView.setClickable(true);
    }

    private void executeJob(boolean searchRequest, boolean moreRequest, int newPageIndex) {
        RequestHelper requestHelper = moreRequest ? mMoreRequestHelper : this.mRequestHelper;
        requestHelper.init(searchRequest, newPageIndex, getCurrentCategoryIndex());

        execute(true, requestHelper.getRequest(), requestHelper, this, true);
    }

    @Override
    public void onError(Exception e) {
        if (mAnnounceAdapter.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
    }

    private boolean checkResultNotEmpty(ArrayList<AnnounceItem> result) {
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

	/* TODO AsyncJob end */

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
        mPageIndexView.setOnClickListener(new View.OnClickListener() {
            // 페이지를 나타내는 버튼을 선택했을 시, 페이지를 선택하는 메뉴를 띄운다.
            @Override
            public void onClick(View v) {
                if (currentCategoryIndex < 1) {
                    AppUtil.showToast(getActivity(), R.string.tab_announce_invalid_category, true);
                } else {
                    mPageSelectDialog.show();
                }
            }
        });

        updatePageNumber(mCurrentPageIndex);

        registerTabParentView(mTabParent);
    }

    private void updatePageNumber(int pageIndex) {
        if (mPageIndexView != null)
            mPageIndexView.setText(Integer.toString(pageIndex) + StringUtil.SPACE + PAGE_NUM);
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
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendTrackerEvent("page changed", "" + getCurrentCategoryIndex(), mPageNumberPicker.getValue());
                        executeJob(mIsSearchRequesting, false, mPageNumberPicker.getValue());
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabAnnounceFragment";
    }


    private class RequestHelper implements Request.ResultListener<ArrayList<AnnounceItem>>, Processor<ArrayList<AnnounceItem>, ArrayList<AnnounceItem>> {
        private boolean categoryScholarship = false;
        private boolean searchRequest = false;
        private int mCurrentCategoryIndex;
        protected int mNewPageIndex;

        void init(boolean searching, int mNewPageIndex, int category) {
            this.mCurrentCategoryIndex = category;
            this.mNewPageIndex = mNewPageIndex;
            categoryScholarship = category == 3;
            this.searchRequest = searching;
        }

        private ParseAnnounce getParser() {
            return categoryScholarship ? SCHOLARSHIP_PARSER : PARSER;
        }

        private void initParams() {
            mQueryTable.put("pageIndex", Integer.toString(mNewPageIndex));
            if (categoryScholarship) {
                mQueryTable.put("brdBbsseq", "1");
                if (searchRequest) {
                    mQueryTable.put("sword", mSearchQuery);
                    mQueryTable.put("skind", "title");

                }
            } else {
                mQueryTable.put("list_id", mCurrentCategoryIndex == 1 ? "FA1" : "FA2");
                if (searchRequest) {
                    mQueryTable.put("searchCnd", "1");
                    mQueryTable.put("searchWrd", mSearchQuery);
                }
            }
        }

        private String getUrl() {
            return categoryScholarship ? "http://scholarship.uos.ac.kr/scholarship/notice/notice/list.do" : "http://www.uos.ac.kr/korNotice/list.do";
        }

        Request<ArrayList<AnnounceItem>> getRequest() {
            initParams();

            return HttpRequest.Builder.newStringRequestBuilder(getUrl())
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .setParams(mQueryTable)
                    .build()
                    .checkNetworkState(getActivity())
                    .wrap(getParser())
                    .wrap(this);
        }

        @Override
        public ArrayList<AnnounceItem> process(ArrayList<AnnounceItem> result) throws Exception {
            if (searchRequest || PrefUtil.getInstance(getContext()).get(PrefUtil.KEY_ANNOUNCE_EXCEPT_TYPE_NOTICE, false)) {
                removeNoticeTypeAnnounce(result);
            }
            return result;
        }

        void removeNoticeTypeAnnounce(ArrayList<AnnounceItem> announceItems) {
            final int size = announceItems.size();
            for (int i = size - 1; i >= 0; i--) {
                AnnounceItem item = announceItems.get(i);
                if (item.isTypeNotice())
                    announceItems.remove(i);
            }
        }

        @Override
        public void onResult(ArrayList<AnnounceItem> result) {
            if (checkResultNotEmpty(result)) {

                mAnnounceAdapter.clear();
                mAnnounceAdapter.addAll(result);
                mAnnounceAdapter.notifyDataSetChanged();

                // 페이지 선택에서 이동 가능한 최대 페이지 번호를
                // 공지사항의 인덱스를 기준으로 설정함
                if (mShouldChangeMaxValueOfPage) {
                    mPageNumberPicker.setMaxValue(getMaximumAnnounceIndex(result) / 10 + 1);
                    mShouldChangeMaxValueOfPage = false;
                }
                mListFooterView.setVisibility(mAnnounceAdapter.isEmpty() ? View.GONE : View.VISIBLE);

            } else if (searchRequest) {
                mAnnounceAdapter.clear();
                mAnnounceAdapter.notifyDataSetChanged();
                clearParams();
                mCategorySpinner.setSelection(0, true);
            }

            updatePageNumber();
        }

        private int getMaximumAnnounceIndex(ArrayList<AnnounceItem> announceItems) {
            final int size = announceItems.size();

            for (int i = 0; i < size; i++) {
                AnnounceItem item = announceItems.get(i);
                if (item.isTypeNotice())
                    continue;

                return item.number;
            }

            return 0;
        }

        protected void updatePageNumber() {
            TabAnnounceFragment.this.updatePageNumber(mNewPageIndex);
            TabAnnounceFragment.this.mIsSearchRequesting = searchRequest;
        }
    }

    private class MoreRequestHelper extends RequestHelper {

        @Override
        public ArrayList<AnnounceItem> process(ArrayList<AnnounceItem> result) throws Exception {
            removeNoticeTypeAnnounce(result);
            return result;
        }

        @Override
        public void onResult(ArrayList<AnnounceItem> result) {
            if (checkResultNotEmpty(result)) {
                mAnnounceAdapter.addAll(result);
                mAnnounceAdapter.notifyDataSetChanged();

                // 검색된 결과가 10개 이하이면, 마지막 페이지에 도달한 것 이므로
                // '다음 페이지' 버튼을 보여주지 않는다.
                mListFooterView.setVisibility(result.size() < 10 ? View.GONE : View.VISIBLE);

                updatePageNumber();
            }
            // '다음 페이지 요청'의 경우 검색된 결과가 없다는 것은 마지막 페이지를 지났다는 것 이므로
            // 페이지를 업데이트 하지 않는다.
        }
    }
}
