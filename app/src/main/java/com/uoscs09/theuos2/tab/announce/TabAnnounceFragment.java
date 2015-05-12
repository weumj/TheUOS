package com.uoscs09.theuos2.tab.announce;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.base.TabHidingScrollListener;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseAnnounce;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.Hashtable;

public class TabAnnounceFragment extends AbsProgressFragment<ArrayList<AnnounceItem>> {
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
    @ReleaseWhenDestroy
    private ArrayAdapter<AnnounceItem> mAnnounceAdapter;
    private Hashtable<String, String> mQueryTable;
    @AsyncData
    private ArrayList<AnnounceItem> mDataList;

    private final ParseAnnounce mParser = new ParseAnnounce();
    /**
     * searchView
     */
    @ReleaseWhenDestroy
    private MenuItem mSearchMenu;

    /**
     * 현재 공지사항 검색을 시도하는지의 여부를 가리키는 변수
     */
    private boolean isSearching;
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
    private int prevPageNum = pageNum;
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

        mQueryTable = new Hashtable<>();

        initDialog();

        super.onCreate(savedInstanceState);

        mAnnounceAdapter = new AnnounceAdapter(getActivity(), mDataList);

        initToolbarAndTab();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_announce, container, false);

        final ListView mListView = (ListView) rootView.findViewById(R.id.tab_announce_list_announce);

        mEmptyView = rootView.findViewById(R.id.tab_announce_empty_view);
        mEmptyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategorySpinner.performClick();
                sendEmptyViewClickEvent();
            }
        });
        mEmptyView.setVisibility(mDataList.size() != 0 ? View.INVISIBLE : View.VISIBLE);

        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(mListViewOnItemClickListener);
        mListView.setAdapter(mAnnounceAdapter);
        mListView.setOnScrollListener(new TabHidingScrollListener.ForAbsListView(getToolBar()));
       /* mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItemPosition;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        int currentFirstVisiblePosition = mListView.getFirstVisiblePosition();

                        if (currentFirstVisiblePosition < mLastFirstVisibleItemPosition) {
                            //scroll down
                            getToolBar().setVisibility(View.VISIBLE);
                        } else if (currentFirstVisiblePosition > mLastFirstVisibleItemPosition) {
                            // scroll up
                            getToolBar().setVisibility(View.GONE);
                        }
                        mLastFirstVisibleItemPosition = currentFirstVisiblePosition;

                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });*/

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_forward: {
                if (mSpinnerSelection == 0 || !isMenuVisible())
                    return true;

                sendTrackerEvent("forward", "" + mSpinnerSelection);

                setPageValue(pageNum + 1);
                execute();
                return true;
            }
            case R.id.action_backward: {
                if (mSpinnerSelection == 0 || !isMenuVisible())
                    return true;

                if (pageNum != 1) {
                    sendTrackerEvent("backward", "" + mSpinnerSelection);

                    setPageValue(pageNum - 1);
                    execute();
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
            searchView.setOnQueryTextListener(mSearchViewOnQueryTextListener);
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
    /**
     * 공지사항 리스트중 하나가 선택되면 호출됨
     */
    private final AdapterView.OnItemClickListener mListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long itemId) {
            if (!isMenuVisible() || mSpinnerSelection == 0)
                return;

            Activity activity = getActivity();
            Intent intent = new Intent(activity, SubAnnounceWebActivity.class);
            intent.putExtra(ITEM, mAnnounceAdapter.getItem(pos));
            intent.putExtra(PAGE_NUM, mSpinnerSelection);

            ActivityCompat.startActivity(activity, intent, ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle());
        }
    };

    /**
     * spinner가 선택되면(카테고리 선택) 호출됨
     */
    private final AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemId) {
            // 카테고리가 변경되었으므로, 기존 검색사항 초기화.
            mSpinnerSelection = position;
            searchQuery = null;
            isSearching = false;

            // Fragment 가 ViewPager 내부에 존재하지만, 사용자에게 보이지 않는 경우
            if(!isMenuVisible()){
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
            execute();

            sendTrackerEvent("announce category change", "", mSpinnerSelection);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private final SearchView.OnQueryTextListener mSearchViewOnQueryTextListener = new SearchView.OnQueryTextListener() {

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
                AppUtil.showToast(getActivity(), R.string.tab_anounce_invaild_category, true);
                isSearching = false;

            } else {

                searchQuery = query.trim();
                setPageValue(1);
                isSearching = true;
                mShouldChangeMaxValueOfPage = true;
                execute();

            }

            return true;
        }
    };

	/* TODO Listener end */

    /* TODO AsyncExcutor Callback */

    @Override
    protected void execute() {
        mEmptyView.setVisibility(View.INVISIBLE);

        super.execute();
    }

    @Override
    public ArrayList<AnnounceItem> call() throws Exception {
        // TODO 최적화 필요, 필요없이 지우고 쓰고 함
        mQueryTable.clear();
        final int howTo;
        final String url;
        if (mSpinnerSelection == 3) {
            if (isSearching) {
                mQueryTable.put("sword", searchQuery);
                mQueryTable.put("skind", "title");
            }
            mQueryTable.put("process", "list");
            mQueryTable.put("brdbbsseq", "1");
            mQueryTable.put("x", "1");
            mQueryTable.put("y", "1");
            mQueryTable.put("w", "3");
            mQueryTable.put("pageNo", String.valueOf(pageNum));
            howTo = ParseAnnounce.SCHOLAR;
            url = "http://scholarship.uos.ac.kr/scholarship.do";
        } else {
            if (isSearching) {
                mQueryTable.put("searchCnd", "1");
                mQueryTable.put("searchWrd", searchQuery);
            }
            mQueryTable.put("list_id", mSpinnerSelection == 1 ? "FA1" : "FA2");
            mQueryTable.put("pageIndex", String.valueOf(pageNum));
            howTo = 0;
            url = "http://www.uos.ac.kr/korNotice/list.do";
        }

        String body = HttpRequest.getBodyByPost(url, StringUtil.ENCODE_UTF_8, mQueryTable, StringUtil.ENCODE_UTF_8);
        mParser.setHowTo(howTo);
        return mParser.parse(body);
    }

    @Override
    public void onTransactResult(ArrayList<AnnounceItem> result) {
        if (result == null || result.size() == 0) {
            pageNum = prevPageNum;
            AppUtil.showToast(getActivity(), R.string.search_result_empty, true);
        } else {
            mAnnounceAdapter.clear();
            mAnnounceAdapter.addAll(result);
            mAnnounceAdapter.notifyDataSetChanged();

            // 페이지 선택에서 이동 가능한 최대 페이지 번호를
            // 공지사항의 인덱스를 기준으로 설정함
            if (mShouldChangeMaxValueOfPage) {
                final int size = result.size();
                for (int i = 0; i < size; i++) {
                    AnnounceItem item = result.get(i);
                    if (item.type.equals("공지"))
                        continue;

                    try {
                        int maxPageNumber = Integer.parseInt(item.type);
                        mPageNumberPicker.setMaxValue(maxPageNumber / 10 + 1);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mShouldChangeMaxValueOfPage = false;
            }
        }
        updatePageNumber();
    }

	/* TODO AsyncExcutor Callback end */

    /** Toolbar 에 붙일 Tab (카테고리 Spinner & PageIndexView) 을 초기화 한다.*/
    private void initToolbarAndTab(){
        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_announce_toolbar_menu, getToolbarParent(), false);

        mCategorySpinner = (Spinner) mTabParent.findViewById(R.id.tab_announce_action_spinner1);

        // 이전에 선택된 카테고리가 존재하는 경우 (mSpinnerSelection != 0)
        // Spinner.setSelection() 이 호출되면 mSpinnerOnItemSelectedListener.onItemSelected() 가 호출된다.
        // 이미 공지사항 데이터가 존재하므로, 웹에서 공지사항을 얻어올 필요가 없으므로
        // spinner 에 tagging 을 하여 알려준다.
        mCategorySpinner.setTag(1);
        mCategorySpinner.setSelection(mSpinnerSelection);
        mCategorySpinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mPageIndexView = (TextView) mTabParent.findViewById(R.id.tab_anounce_action_textView_page);
        mPageIndexView.setOnClickListener(new View.OnClickListener() {
            // 페이지를 나타내는 버튼을 선택했을 시, 페이지를 선택하는 메뉴를 띄운다.
            @Override
            public void onClick(View v) {
                if (mSpinnerSelection == 0) {
                    AppUtil.showToast(getActivity(), R.string.tab_anounce_invaild_category, true);
                } else {
                    mPageSelectDialog.show();
                }
            }
        });

        updatePageNumber();

        registerTabParentView(mTabParent);
    }

    void updatePageNumber() {
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
                .setTitle(R.string.tab_anounce_plz_select_page)
                .setView(mPageNumberPicker)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendTrackerEvent("page change", "" + mSpinnerSelection, mPageNumberPicker.getValue());
                        setPageValue(mPageNumberPicker.getValue());
                        execute();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();

    }

    /**
     * 현재 page의 번호를 설정한다.
     */
    void setPageValue(int newValue) {
        prevPageNum = pageNum;
        pageNum = newValue;
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "TabAnnounceFragment";
    }
}
