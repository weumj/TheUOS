package com.uoscs09.theuos2.tab.booksearch;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Spinner;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.UosMainActivity;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.CollectionUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TabBookSearchFragment extends AbsProgressFragment<List<BookItem>> implements OnQueryTextListener, UosMainActivity.OnBackPressListener, BookItemListAdapter.OnItemClickListener {

    /**
     * 비동기 작업 결과가 비었는지 여부
     */
    private boolean isResultEmpty = true;
    /**
     * 현재 mCurrentPage
     */
    private int mCurrentPage = 1;
    /**
     * 중앙 도서관에 질의할 매개변수들
     */
    private String mEncodedQuery;
    private String mRawQuery;
    // private String mOptionString;
    private BookItemListAdapter mBookListAdapter;
    private AnimationAdapter mAnimAdapter;
    private ArrayList<BookItem> mBookList;

    @BindView(R.id.tab_book_list_search)
    ListView mListView;
    @BindView(R.id.tab_book_empty)
    View mEmptyView;

    private Spinner oi;//option : catergory
    private Spinner os; // option : sort
    private int mOptionIndex = 0, mOptionSort = 0;

    /**
     * 검색 메뉴, 검색할 단어가 입력되는 곳
     */
    private MenuItem searchMenu;
    private ActionMode actionMode;


    private static final String BUNDLE_LIST = "BookList";
    private static final String BUNDLE_PAGE = "BookPage";
    private static final String QUERY = "Query";
    private static final String OI_SEL = "oi";
    private static final String OS_SEL = "os";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int oiSelect = 0, osSelect = 0;

        if (savedInstanceState != null) {
            mBookList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
            oiSelect = savedInstanceState.getInt(OI_SEL);
            osSelect = savedInstanceState.getInt(OS_SEL);
            mEncodedQuery = savedInstanceState.getString(QUERY);
            mCurrentPage = savedInstanceState.getInt(BUNDLE_PAGE);
            mRawQuery = savedInstanceState.getString("mRawQuery", "");
        } else {
            mBookList = new ArrayList<>();
            mCurrentPage = 1;
        }

        super.onCreate(savedInstanceState);

        initToolbarAndTab(oiSelect, osSelect);
    }

    @Override
    protected void setPrevAsyncData(List<BookItem> data) {
        if (mBookList.isEmpty()) CollectionUtil.addAll(mBookList, data);
    }

    private void initToolbarAndTab(int oiSelect, int osSelect) {
        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_book_toolbar_menu, getToolbarParent(), false);
        oi = (Spinner) mTabParent.findViewById(R.id.tab_book_action_spinner_oi);
        os = (Spinner) mTabParent.findViewById(R.id.tab_book_action_spinner_os);
        oi.setSelection(oiSelect);
        os.setSelection(osSelect);

        mTabParent.findViewById(R.id.tab_book_toolbar_menu_action).setOnClickListener(v -> {
            if (!isOptionBothSelected()) {
                AppUtil.showToast(getActivity(), R.string.tab_book_subject_opt_both);
                return;
            }
            if (!mBookList.isEmpty()) {
                mOptionIndex = oi.getSelectedItemPosition();
                mOptionSort = os.getSelectedItemPosition();

                mBookList.clear();
                mListView.scrollTo(0, 0);
                mCurrentPage = 1;
                mBookListAdapter.notifyDataSetChanged();
                execute();
            }/*else {
                 AppUtil.showToast(getActivity(), R.string.tab_book_subject_opt_both);
            }*/
        });

        registerTabParentView(mTabParent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_LIST, mBookList);
        outState.putInt(OI_SEL, mOptionIndex);
        outState.putInt(OS_SEL, mOptionSort);
        outState.putInt(BUNDLE_PAGE, mCurrentPage);
        outState.putString(QUERY, mEncodedQuery);
        outState.putString("mRawQuery", mRawQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mBookList.size() != 0) {
            mEmptyView.setVisibility(View.INVISIBLE);
            isResultEmpty = false;
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        mBookListAdapter = new BookItemListAdapter(getActivity(), mBookList, this);

        mAnimAdapter = new AlphaInAnimationAdapter(mBookListAdapter);

        View progressLayout = LayoutInflater.from(view.getContext()).inflate(R.layout.view_tab_book_loading_layout, mListView, false);
        registerProgressView(progressLayout);
        mListView.addFooterView(progressLayout);

        mAnimAdapter.setAbsListView(mListView);
        mListView.setAdapter(mAnimAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * 리스트 뷰가 스크롤 되는지 여부
             */
            private boolean isInvokeScroll = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 리스트의 마지막에 도달하였을 경우에
                // 이 이벤트가 처음 일어났고, 이전에 검색결과가 0이 아닌 경우에만
                // 새로운 검색을 시도한다.
                if (totalItemCount > 1 && (firstVisibleItem + visibleItemCount) == totalItemCount - 1) {
                    if (!isInvokeScroll && !isResultEmpty) {
                        isInvokeScroll = true;
                        mCurrentPage++;
                        execute();
                    }
                } else {
                    isInvokeScroll = false;
                }
            }
        });
    }


    @Override
    protected int layoutRes() {
        return R.layout.tab_book_search;
    }

    @Override
    public void onItemClick(BookItemViewHolder holder, View v) {
        final BookItem item = holder.getItem();
        switch (v.getId()) {
            case R.id.tab_booksearch_list_book_image: {
                // Intent i = AppUtil.getWebPageIntent("http://mlibrary.uos.ac.kr" + item.url);
                //AnimUtil.startActivityWithScaleUp(getActivity(), i, v);
                sendClickEvent("detail book");

                BookDetailActivity.start(getActivity(), v, holder.getItem());
                break;
            }
            case R.id.tab_booksearch_list_book_site:
                if (item.bookStateInt == BookItem.BOOK_STATE_ONLINE) {
                    Intent i = AppUtil.getWebPageIntent(item.site);
                    AnimUtil.startActivityWithScaleUp(getActivity(), i, v);
                }
                break;

            default:
                break;
        }
    }


    @OnClick(R.id.tab_book_search_empty_info1)
    void expandSearchView() {
        if (searchMenu == null) {
            return;
        }
        sendClickEvent("search menu from empty view");
        searchMenu.expandActionView();
    }


    @Override
    public boolean onItemLongClick(BookItemViewHolder holder, View v) {
        String title;
        switch (v.getId()) {
            case R.id.tab_booksearch_list_book_title:
                title = holder.getItem().title;
                break;

            case R.id.tab_booksearch_list_text_publish_and_year:
                title = holder.getItem().bookInfo;
                break;

            case R.id.tab_booksearch_list_book_writer:
                title = holder.getItem().writer;
                break;

            default:
                return false;
        }
        if (actionMode == null)
            actionMode = getAppCompatActivity().startSupportActionMode(mActionModeCallback);

        // View prevView = (View) actionMode.getTag();
        // if (prevView != null)
        // prevView.setSelected(false);
        // v.setSelected(true);
        if (actionMode != null) {
            //actionMode.setTag(v);
            actionMode.setTitle(title);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_book_search, menu);
        searchMenu = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);

        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint(getText(R.string.search_hint));
            searchView.setOnQueryTextFocusChangeListener((view, queryTextFocused) -> {
                if (!queryTextFocused) {
                    searchMenu.collapseActionView();
                    searchView.setQuery("", false);
                }
            });

        } else {
            AppUtil.showToast(getActivity(), "compatibility error");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getUosMainActivity() != null)
            getUosMainActivity().setOnBackPressListener(isVisibleToUser ? this : null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String q) {
        mRawQuery = q.trim();

        if (TextUtils.isEmpty(mRawQuery)) {
            AppUtil.showToast(getActivity(), R.string.search_input_empty, isMenuVisible());

        } else {
            if (!isOptionBothSelected()) {
                AppUtil.showToast(getActivity(), R.string.tab_book_subject_opt_both);
                return true;
            }

            InputMethodManager ipm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            ipm.hideSoftInputFromWindow(searchMenu.getActionView().getWindowToken(), 0);

            searchMenu.collapseActionView();
            String lastQuery;
            try {
                lastQuery = URLEncoder.encode(mRawQuery, StringUtil.ENCODE_UTF_8);
            } catch (UnsupportedEncodingException e) {
                AppUtil.showToast(getActivity(), "Text Encoding Error!", isMenuVisible());
                return true;
            }

            mEncodedQuery = lastQuery;
            mCurrentPage = 1;
            mBookList.clear();
            mBookListAdapter.notifyDataSetChanged();
            setSubtitleWhenVisible(mRawQuery);
            mAnimAdapter.reset();
            execute();
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    private void showEmptyView() {
        if (mBookList.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
    }

    private boolean isOptionBothSelected() {
        int osSel = os.getSelectedItemPosition();
        int oiSel = oi.getSelectedItemPosition();
        return (oiSel == 0 && osSel == 0) || (oiSel != 0 && osSel != 0);
    }

    private void execute() {
        mEmptyView.setVisibility(View.GONE);

        appTask(AppRequests.Books.request(mRawQuery/*mEncodedQuery*/, mCurrentPage, mOptionSort, mOptionIndex))
                .subscribe(result -> {
                            isResultEmpty = false;
                            if (result.isEmpty()) {
                                AppUtil.showToast(getActivity(), R.string.search_result_empty, isMenuVisible());
                                isResultEmpty = true;

                                showEmptyView();

                            } else {
                                AppUtil.showToast(getActivity(), getString(R.string.search_found_amount, result.size()), isMenuVisible());
                                mBookList.addAll(result);
                                mBookListAdapter.notifyDataSetChanged();
                            }
                        },
                        e -> {
                            e.printStackTrace();
                            isResultEmpty = false;
                            showEmptyView();
                            simpleErrorRespond(e);
                        }
                );
    }


    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.tab_book_contextual, menu);
            //getActionBar().hide();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            String str = mode.getTitle().toString();
            switch (item.getItemId()) {
                case R.id.action_copy:
                    copyItem(str);
                    mode.finish();
                    return true;
                case R.id.action_search:
                    searchItem(str);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // View v = (View) mode.getTag();
            // if (v != null)
            // v.setSelected(false);
            // getActionBar().show();
            actionMode = null;
        }


        private void copyItem(String text) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copy", text);
            clipboard.setPrimaryClip(clip);
        }

        private void searchItem(String text) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, text);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                AppUtil.showToast(getActivity(), R.string.error_no_related_activity_found);
            }
        }
    };

    @Override
    public void onDestroyOptionsMenu() {
        if (actionMode != null)
            actionMode.finish();
    }


    @Override
    protected boolean putAsyncData(String key, List<BookItem> obj) {
        mBookList.addAll(obj);
        return super.putAsyncData(key, mBookList);
    }

    @Override
    protected CharSequence getSubtitle() {
        return mRawQuery;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabBookSearchFragment";
    }

    @Override
    public boolean onBackPress() {

        if (actionMode != null) {
            actionMode.finish();
            return true;

        } else
            return false;

    }

}
