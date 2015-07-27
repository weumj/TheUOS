package com.uoscs09.theuos2.tab.announce;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.MenuItemCompat;
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
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.customview.NestedListView;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;

public class TabAnnounceFragment extends AbsProgressFragment<ArrayList<AnnounceItem>>
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener {
    /**
     * 상단 액션바에 추가될 위젯, 페이지 인덱스
     */
    @ReleaseWhenDestroy
    private TextView mPageIndexView;

    /**
     * 상단 액션바에 추가될 위젯, 카테고리 선택
     */
    @ReleaseWhenDestroy
    private Spinner mCategorySpinner;
    private ArrayAdapter<AnnounceItem> mAnnounceAdapter;
    @ReleaseWhenDestroy
    private View mListFooterView;

    private AsyncFragmentJob<ArrayList<AnnounceItem>> mOrdinaryJob, mSearchJob, mMoreJob;

    private ArrayMap<String, String> mQueryTable;
    @AsyncData
    private ArrayList<AnnounceItem> mDataList;

    private AsyncTask<Void, Void, ArrayList<AnnounceItem>> mAsyncTask;

    private static final ParseAnnounce PARSER = ParseAnnounce.getParser();
    private static final ParseAnnounce SCHOLARSHIP_PARSER = ParseAnnounce.getScholarshipParser();

    /**
     * searchView
     */
    @ReleaseWhenDestroy
    private MenuItem mSearchMenu;

    /**
     * (검색 메뉴 선택시)검색어를 저장함
     */
    private String searchQuery;
    private int mSpinnerSelection = 0;
    private int pageNum;
    @ReleaseWhenDestroy
    private Dialog mPageSelectDialog;
    @ReleaseWhenDestroy
    private NumberPicker mPageNumberPicker;
    /**
     * 표시할 공지사항목록의 변동이 생겼을 때 (분류 변경 등등..)<br>
     * 페이지의 최대값이 변경되어야 하는지를 가리키는 값
     */
    private boolean mShouldChangeMaxValueOfPage = false;

    /**
     * 이전 페이지 번호, 공지사항 검색 결과가 없으면 현재 페이지 번호를 변하지 않게하는 역할
     */
    private int prevPageNum;
    static final String PAGE_NUM = "PAGE";
    static final String ITEM = "item";
    private static final String SP_SELECTION = "spinner_selection";
    @ReleaseWhenDestroy
    private View mEmptyView;

    /* TODO Fragment Callback */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_NUM, pageNum);
        outState.putInt(SP_SELECTION, mSpinnerSelection);
        outState.putParcelableArrayList(ITEM, mDataList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pageNum = savedInstanceState.getInt(PAGE_NUM);
            mSpinnerSelection = savedInstanceState.getInt(SP_SELECTION);
            mDataList = savedInstanceState.getParcelableArrayList(ITEM);
        } else {
            pageNum = 1;
            mDataList = new ArrayList<>();
        }

        mQueryTable = new ArrayMap<>();

        initDialog();

        super.onCreate(savedInstanceState);

        mAnnounceAdapter = new AnnounceAdapter(getActivity(), mDataList);

        initToolbarAndTab();

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
                executeMoreJob();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_forward: {
                if (mSpinnerSelection == 0 || !isMenuVisible())
                    return true;

                sendTrackerEvent("forward", "" + mSpinnerSelection);

                setPageValue(pageNum + 1);
                executeOrdinaryJob();
                return true;
            }
            case R.id.action_backward: {
                if (mSpinnerSelection == 0 || !isMenuVisible())
                    return true;

                if (pageNum != 1) {
                    sendTrackerEvent("backward", "" + mSpinnerSelection);

                    setPageValue(pageNum - 1);
                    executeOrdinaryJob();
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
        if (!isMenuVisible() || mSpinnerSelection == 0)
            return;

        Activity activity = getActivity();
        Intent intent = new Intent(activity, SubAnnounceWebActivity.class);
        intent.putExtra(ITEM, mAnnounceAdapter.getItem(pos));
        intent.putExtra(PAGE_NUM, mSpinnerSelection);

        AppUtil.startActivityWithScaleUp(activity, intent, view);
    }


    /**
     * spinner 가 선택되면(카테고리 선택) 호출됨
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemId) {
        // 카테고리가 변경되었으므로, 기존 검색사항 초기화.
        mSpinnerSelection = position;
        searchQuery = null;

        // Fragment 가 ViewPager 내부에 존재하지만, 사용자에게 보이지 않는 경우
        if (!isMenuVisible()) {
            return;
        }
        if (adapterView.getTag() != null) {
            adapterView.setTag(null);
            return;
        }

        if (mSpinnerSelection == 0)
            return;

        setPageValue(1);
        mShouldChangeMaxValueOfPage = true;
        executeOrdinaryJob();

        sendTrackerEvent("announce category change", "", mSpinnerSelection);
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

        if (mSpinnerSelection == 0) {
            AppUtil.showToast(getActivity(), R.string.tab_announce_invalid_category, true);

        } else {

            searchQuery = query.trim();
            setPageValue(1);
            mShouldChangeMaxValueOfPage = true;
            executeSearchJob();

        }

        return true;
    }

	/* TODO Listener end */


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mEmptyView.setVisibility(View.INVISIBLE);
        mListFooterView.setClickable(false);
    }


    private boolean checkAnotherJob() {
        if (AsyncUtil.isTaskRunning(mAsyncTask)) {
            AppUtil.showToast(getActivity(), R.string.progress_ongoing_another);
            return true;
        } else
            return false;
    }

    private void executeOrdinaryJob() {
        if (checkAnotherJob()) {
            return;
        }
        if (mOrdinaryJob == null)
            mOrdinaryJob = new NormalJob();
        mAsyncTask = super.execute(mOrdinaryJob);
    }

    private void executeSearchJob() {
        if (checkAnotherJob()) {
            return;
        }
        if (mSearchJob == null)
            mSearchJob = new SearchJob();
        mAsyncTask = super.execute(mSearchJob);
    }

    private void executeMoreJob() {
        if (checkAnotherJob()) {
            return;
        }
        if (mMoreJob == null)
            mMoreJob = new MoreJob();
        mAsyncTask = super.execute(mMoreJob);
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        mListFooterView.setClickable(true);
    }

    /**
     * 일반적인 공지사항 보기
     */
    private class NormalJob extends AsyncFragmentJob.Base<ArrayList<AnnounceItem>> {
        void initQueryTableNormal() {
            mQueryTable.put("list_id", mSpinnerSelection == 1 ? "FA1" : "FA2");
            mQueryTable.put("pageIndex", String.valueOf(pageNum));
        }

        void initQueryTableScholarship() {
            //mQueryTable.put("process", "list");
            mQueryTable.put("brdBbsseq", "1");
            //mQueryTable.put("x", "1");
            //mQueryTable.put("y", "1");
            //mQueryTable.put("w", "3");
            mQueryTable.put("pageIndex", String.valueOf(pageNum));
        }

        @Override
        public ArrayList<AnnounceItem> call() throws Exception {
            // TODO 최적화 필요, 필요없이 지우고 쓰고 함
            mQueryTable.clear();
            ParseAnnounce parser;
            final String url;

            if (mSpinnerSelection == 3) {
                initQueryTableScholarship();
                url = "http://scholarship.uos.ac.kr/scholarship/notice/notice/list.do";
                parser = SCHOLARSHIP_PARSER;

            } else {
                initQueryTableNormal();
                url = "http://www.uos.ac.kr/korNotice/list.do";
                parser = PARSER;
            }

            return HttpRequest.Builder.newStringRequestBuilder(url)
                    .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                    .setParams(mQueryTable)
                    .build()
                    .checkNetworkState(getActivity())
                    .wrap(parser)
                    .get();
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
                    final int size = result.size();

                    for (int i = 0; i < size; i++) {
                        AnnounceItem item = result.get(i);
                        if (item.isTypeNotice())
                            continue;

                        int maxPageNumber = item.number;
                        mPageNumberPicker.setMaxValue(maxPageNumber / 10 + 1);
                        break;
                    }

                    mShouldChangeMaxValueOfPage = false;
                }

                mListFooterView.setVisibility(mAnnounceAdapter.isEmpty() ? View.GONE : View.VISIBLE);

            }
            updatePageNumber();

        }

        boolean checkResultNotEmpty(ArrayList<AnnounceItem> result) {
            if (result == null || result.size() == 0) {
                pageNum = prevPageNum;
                AppUtil.showToast(getActivity(), R.string.search_result_empty, true);

                mListFooterView.setVisibility(View.GONE);

                showEmptyViewIfDataSetIsEmpty();

                return false;
            } else
                return true;
        }

        @Override
        public void onPostExcute() {
            super.onPostExcute();

            mAsyncTask = null;
        }

        void showEmptyViewIfDataSetIsEmpty() {
            if (mAnnounceAdapter.isEmpty())
                mEmptyView.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean exceptionOccurred(Exception e) {
            showEmptyViewIfDataSetIsEmpty();

            return super.exceptionOccurred(e);
        }
    }

    /**
     * 검색
     */
    private class SearchJob extends NormalJob {
        @Override
        void initQueryTableNormal() {
            super.initQueryTableNormal();
            mQueryTable.put("searchCnd", "1");
            mQueryTable.put("searchWrd", searchQuery);
        }

        @Override
        void initQueryTableScholarship() {
            super.initQueryTableScholarship();
            mQueryTable.put("sword", searchQuery);
            mQueryTable.put("skind", "title");
        }

    }

    /**
     * '더 보기' 를 선택하면 수행됨
     */
    private class MoreJob extends NormalJob {
        @Override
        public ArrayList<AnnounceItem> call() throws Exception {
            setPageValue(pageNum + 1);
            ArrayList<AnnounceItem> result = super.call();

            int size = result.size();
            for (int i = size - 1; i >= 0; i--) {
                AnnounceItem item = result.get(i);
                if (item.isTypeNotice())
                    result.remove(i);
            }

            return result;
        }

        @Override
        public void onResult(ArrayList<AnnounceItem> result) {
            if (checkResultNotEmpty(result)) {
                mAnnounceAdapter.addAll(result);
                mAnnounceAdapter.notifyDataSetChanged();
            }
            updatePageNumber();
        }

    }


	/* TODO AsyncJob end */

    /**
     * Toolbar 에 붙일 Tab (카테고리 Spinner & PageIndexView) 을 초기화 한다.
     */
    private void initToolbarAndTab() {
        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_announce_toolbar_menu, getToolbarParent(), false);

        mCategorySpinner = (Spinner) mTabParent.findViewById(R.id.tab_announce_action_spinner1);

        // 이전에 선택된 카테고리가 존재하는 경우 (mSpinnerSelection != 0)
        // Spinner.setSelection() 이 호출되면 mSpinnerOnItemSelectedListener.onItemSelected() 가 호출된다.
        // 이미 공지사항 데이터가 존재하므로, 웹에서 공지사항을 얻어올 필요가 없으므로
        // spinner 에 tagging 을 하여 알려준다.
        mCategorySpinner.setTag(1);
        mCategorySpinner.setSelection(mSpinnerSelection);
        mCategorySpinner.setOnItemSelectedListener(this);

        mPageIndexView = (TextView) mTabParent.findViewById(R.id.tab_anounce_action_textView_page);
        mPageIndexView.setOnClickListener(new View.OnClickListener() {
            // 페이지를 나타내는 버튼을 선택했을 시, 페이지를 선택하는 메뉴를 띄운다.
            @Override
            public void onClick(View v) {
                if (mSpinnerSelection == 0) {
                    AppUtil.showToast(getActivity(), R.string.tab_announce_invalid_category, true);
                } else {
                    mPageSelectDialog.show();
                }
            }
        });

        updatePageNumber();

        registerTabParentView(mTabParent);
    }

    private void updatePageNumber() {
        if (mPageIndexView != null)
            mPageIndexView.setText(String.valueOf(pageNum) + StringUtil.SPACE + PAGE_NUM);
    }

    /**
     * 페이지 선택 dialog를 생성한다.
     */
    private void initDialog() {
        Context context = getActivity();
        mPageNumberPicker = new NumberPicker(context);
        mPageNumberPicker.setLayoutParams(new NumberPicker.LayoutParams(NumberPicker.LayoutParams.WRAP_CONTENT, NumberPicker.LayoutParams.WRAP_CONTENT));

        mPageNumberPicker.setMinValue(1);
        mPageNumberPicker.setMaxValue(999);

        mPageSelectDialog = new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(R.string.tab_announce_plz_select_page)
                .setView(mPageNumberPicker)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendTrackerEvent("page change", "" + mSpinnerSelection, mPageNumberPicker.getValue());
                        setPageValue(mPageNumberPicker.getValue());
                        executeOrdinaryJob();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

    }

    /**
     * 현재 page의 번호를 설정한다.
     */
    private void setPageValue(int newValue) {
        prevPageNum = pageNum;
        pageNum = newValue;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabAnnounceFragment";
    }

}
