package com.uoscs09.theuos.tab.anounce;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.annotation.AsyncData;
import com.uoscs09.theuos.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos.base.AbsProgressFragment;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.parse.ParseAnnounce;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.StringUtil;

import java.util.ArrayList;
import java.util.Hashtable;

public class TabAnounceFragment extends AbsProgressFragment<ArrayList<AnnounceItem>> {
    /**
     * 상단 액션바에 추가될 위젯, 페이지 인덱스
     */
    @ReleaseWhenDestroy
    private TextView pageView;
    private ViewGroup mToolBarParent;
    @ReleaseWhenDestroy
    private ViewGroup mTabParent;

    /**
     * 상단 액션바에 추가될 위젯, 카테고리 선택
     */
    @ReleaseWhenDestroy
    protected Spinner spinner;
    @ReleaseWhenDestroy
    private ArrayAdapter<AnnounceItem> adapter;
    private Hashtable<String, String> mQueryTable;
    @AsyncData
    private ArrayList<AnnounceItem> mDataList;

    private final ParseAnnounce mParser = new ParseAnnounce();
    /**
     * searchView
     */
    @ReleaseWhenDestroy
    protected MenuItem searchMenu;
    /**
     * viewpager 이동 시 스피너의 아이템 리스너를 한번만 발동시키게 하는 변수
     */
    //XXX onMenuVisibleChange()로 해결 할 수 있을듯?
    private boolean once;
    /**
     * 현재 공지사항 검색을 시도하는지의 여부를 가리키는 변수
     */
    private boolean isSearching;
    /**
     * (검색 메뉴 선택시)검색어를 저장함
     */
    protected String searchQuery;
    protected int spinnerSelection = 0;
    protected int pageNum;
    @ReleaseWhenDestroy
    protected AlertDialog pageSelectDialog;
    @ReleaseWhenDestroy
    protected NumberPicker mPageNumberPicker;
    /**
     * 표시할 공지사항목록의 변동이 생겼을 때 (분류 변경 등등..)<br>
     * 페이지의 최대값이 변경되어야 하는지를 가리키는 값
     */
    protected boolean mShouldChangeMaxValueOfPage = false;

    /**
     * 이전 페이지 번호, 공지사항 검색 결과가 없으면 현재 페이지 번호를 변하지 않게하는 역할
     */
    private int prevPageNum = pageNum;
    protected static final String PAGE_NUM = "PAGE";
    protected static final String LIST_AN = "list_an";
    private static final String SP_SELECTION = "spinner_selection";
    @ReleaseWhenDestroy
    private View mEmptyView;
    @ReleaseWhenDestroy
    private ListView mListView;

    /* TODO Fragment Callback */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PAGE_NUM, pageNum);
        outState.putInt(SP_SELECTION, spinnerSelection);
        outState.putParcelableArrayList(LIST_AN, mDataList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mQueryTable = new Hashtable<>();
        getDataFromBundle(savedInstanceState);
        Context context = getActivity();
        initDialog();

        adapter = new AnounceAdapter(context, R.layout.list_layout_announce, mDataList);
        super.onCreate(savedInstanceState);

        mToolBarParent = (ViewGroup) getActivity().findViewById(R.id.toolbar_parent);
        mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_announce_toolbar_menu, mToolBarParent, false);

        spinner = (Spinner) mTabParent.findViewById(R.id.tab_announce_action_spinner1);
        spinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        pageView = (TextView) mTabParent.findViewById(R.id.tab_anounce_action_textView_page);
        pageView.setOnClickListener(new View.OnClickListener() {
            // 페이지를 나타내는 버튼을 선택했을 시, 페이지를 선택하는 메뉴를 띄운다.
            @Override
            public void onClick(View v) {
                if (spinnerSelection == 0) {
                    AppUtil.showToast(getActivity(), R.string.tab_anounce_invaild_category, true);
                } else {
                    pageSelectDialog.show();
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_anounce, container, false);

        mListView = (ListView) rootView.findViewById(R.id.tab_announce_list_announce);
        mEmptyView = rootView.findViewById(R.id.tab_anounce_empty_view);
        mEmptyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        mEmptyView.setVisibility(mDataList.size() != 0 ? View.INVISIBLE : View.VISIBLE);

        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener(mListViewOnItemClickListener);
        mListView.setAdapter(adapter);

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_forward: {
                if (spinnerSelection == 0 || !isMenuVisible())
                    return true;
                setPageValue(pageNum + 1);
                execute();
                return true;
            }
            case R.id.action_backward: {
                if (spinnerSelection == 0 || !isMenuVisible())
                    return true;
                if (pageNum != 1) {
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
        searchMenu = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(searchMenu);
        if (searchView != null) {
            searchView.setOnQueryTextListener(mSearchViewOnQueryTextListener);
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint(getText(R.string.search_hint));
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean queryTextFocused) {
                    if (!queryTextFocused) {
                        MenuItemCompat.collapseActionView(searchMenu);
                        searchView.setQuery("", false);
                    }
                }
            });
        } else {
            AppUtil.showToast(getActivity(), "compact error");
        }
        spinner.setSelection(spinnerSelection);
        updatePageNumber();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            addOrRemoveTabMenu(true);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        addOrRemoveTabMenu(isVisibleToUser);
    }

    private void addOrRemoveTabMenu(boolean visible) {
        if (mToolBarParent == null || mTabParent == null)
            return;
        if (visible) {
            if (mTabParent.getParent() == null)
                mToolBarParent.addView(mTabParent);
        } else if (mToolBarParent.indexOfChild(mTabParent) > 0) {
            mToolBarParent.removeView(mTabParent);
        }
    }


	/* TODO Fragment Callback end */

	/* TODO Listener Callback */
    /**
     * 공지사항 리스트중 하나가 선택되면 호출됨
     */
    private final AdapterView.OnItemClickListener mListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos,  long itemId) {
            if (!isMenuVisible() || spinnerSelection == 0)
                return;
            Activity activity = getActivity();
            Intent intent = new Intent(activity, SubAnounceWebActivity.class);
            intent.putExtra(LIST_AN, ((AnnounceItem) adapterView.getItemAtPosition(pos)).onClickString);
            intent.putExtra(PAGE_NUM, spinnerSelection);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                getActivity().startActivity(intent, ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle());
            } else{
                startActivity(intent);
            }
        }
    };

    /**
     * spinner가 선택되면(카테고리 선택) 호출됨
     */
    private final AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemId) {
            spinnerSelection = position;
            searchQuery = null;
            isSearching = false;
            if (!isMenuVisible() || !once) {
                once = true;
                return;
            }
            if (spinnerSelection == 0)
                return;
            setPageValue(1);
            mShouldChangeMaxValueOfPage = true;
            execute();
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

            SearchView v = (SearchView) MenuItemCompat.getActionView(searchMenu);
            ipm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            searchMenu.collapseActionView();

            if (spinnerSelection == 0) {
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
        if (spinnerSelection == 3) {
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
            mQueryTable.put("list_id", spinnerSelection == 1 ? "FA1" : "FA2");
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
            adapter.clear();
            adapter.addAll(result);
            adapter.notifyDataSetChanged();

            // 페이지 선택에서 이동 가능한 최대 페이지 번호를
            // 공지사항의 인덱스를 기준으로 설정함
            if (mShouldChangeMaxValueOfPage) {
                final int size = result.size();
                for (int i = 0; i < size; i++) {
                    AnnounceItem item = result.get(i);
                    if(item.type.equals("공지"))
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

    protected void updatePageNumber() {
        if (pageView != null)
            pageView.setText(String.valueOf(pageNum) + StringUtil.SPACE + PAGE_NUM);
    }

    /**
     * 저장된 bundle에서 데이터를 복구한다.
     */
    private void getDataFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pageNum = savedInstanceState.getInt(PAGE_NUM);
            spinnerSelection = savedInstanceState.getInt(SP_SELECTION);
            mDataList = savedInstanceState.getParcelableArrayList(LIST_AN);
            once = false;
        } else {
            pageNum = 1;
            mDataList = new ArrayList<>();
            once = true;
        }
    }

    /**
     * 페이지 선택 dialog를 생성한다.
     */
    private void initDialog() {
        Context context = getActivity();
        mPageNumberPicker = new NumberPicker(context);
        mPageNumberPicker.setLayoutParams(new NumberPicker.LayoutParams(
                NumberPicker.LayoutParams.WRAP_CONTENT,
                NumberPicker.LayoutParams.WRAP_CONTENT));
        mPageNumberPicker.setMinValue(1);
        mPageNumberPicker.setMaxValue(999);
        pageSelectDialog = new MaterialDialog.Builder(context)
                .title(R.string.tab_anounce_plz_select_page)
                .customView(mPageNumberPicker, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        setPageValue(mPageNumberPicker.getValue());
                        execute();
                    }
                })
                .build();
    }

    /**
     * 현재 page의 번호를 설정한다.
     */
    protected void setPageValue(int newValue) {
        prevPageNum = pageNum;
        pageNum = newValue;
    }

}
