package com.uoscs09.theuos.tab.booksearch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabBookSearchFragment extends
		AbsDrawableProgressFragment<ArrayList<BookItem>> implements
		OnQueryTextListener, AbsListView.OnScrollListener,
		View.OnClickListener, View.OnLongClickListener {
	/** 리스트 뷰가 스크롤 되는지 여부 */
	private boolean isInvokeScroll = true;
	/** 비동기 작업 결과가 비었는지 여부 */
	private boolean isResultEmpty = true;
	/** 현재 page */
	protected int page = 1;
	/** 중앙 도서관에 질의할 매개변수들 */
	private String query;
	private String rawQuery;
	protected ArrayAdapter<BookItem> bookListAdapter;
	private ArrayList<BookItem> bookList;
	private AnimationAdapter aAdapter;
	private ListView mListView;
	/** ListView의 emptyView */
	private View emptyView;
	/** ActionBar에 띄울 View, 도서 검색 option이 선택된 사항을 나타낸다. */
	private TextView optionTextView;
	private TextView queryTextview;
	private View actionView;
	/** option : catergory */
	protected Spinner oi;
	/** option : sort */
	protected Spinner os;
	/** 옵션을 선택하게 하는 Dialog */
	protected AlertDialog optionDialog;
	/** 검색 메뉴, 검색할 단어가 입력되는 곳 */
	protected MenuItem searchMenu;
	protected ActionMode actionMode;

	private static final String BUNDLE_LIST = "BookList";
	private static final String BUNDLE_PAGE = "BookPage";
	private static final String QUERY = "Query";
	private static final String OI_SEL = "oi";
	private static final String OS_SEL = "os";
	private static final String URL = "http://mlibrary.uos.ac.kr/search/tot/result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&pn=";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// setMenuRefresh(false);
		int oiSelect = 0, osSelect = 0;
		if (savedInstanceState != null) {
			bookList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
			oiSelect = savedInstanceState.getInt(OI_SEL);
			osSelect = savedInstanceState.getInt(OS_SEL);
			query = savedInstanceState.getString(QUERY);
			page = savedInstanceState.getInt(BUNDLE_PAGE);
			rawQuery = savedInstanceState
					.getString("rawQuery", StringUtil.NULL);
		} else {
			bookList = new ArrayList<BookItem>();
			page = 1;
		}
		Context context = getActivity();
		actionView = View.inflate(context, R.layout.action_tab_book, null);
		queryTextview = (TextView) actionView.findViewById(R.id.tab_book_query);
		optionTextView = (TextView) actionView
				.findViewById(R.id.tab_book_option);
		queryTextview.setText(rawQuery);

		int dialogIcon;
		switch (AppUtil.theme) {
		case Black:
			dialogIcon = R.drawable.ic_action_action_help_dark;
			break;
		case BlackAndWhite:
			dialogIcon = R.drawable.ic_action_action_help;
			queryTextview.setTextColor(Color.WHITE);
			optionTextView.setTextColor(Color.WHITE);
			break;
		case White:
		default:
			dialogIcon = R.drawable.ic_action_action_help;
			break;
		}
		View dialogLayout = View.inflate(context,
				R.layout.dialog_tab_book_spinners, null);
		oi = (Spinner) dialogLayout
				.findViewById(R.id.tab_book_action_spinner_oi);
		os = (Spinner) dialogLayout
				.findViewById(R.id.tab_book_action_spinner_os);
		oi.setSelection(oiSelect);
		os.setSelection(osSelect);
		optionDialog = new AlertDialog.Builder(context)
				.setView(dialogLayout)
				.setTitle(R.string.tab_book_book_opt)
				.setMessage(R.string.tab_book_book_opt_sub)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setActionText();
								if (!bookListAdapter.isEmpty()) {
									bookListAdapter.clear();
									page = 1;
									excute();
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								oi.setSelection(0);
								os.setSelection(0);
								setActionText();
							}
						}).setIcon(dialogIcon).create();
		super.onCreate(savedInstanceState);
	}

	protected void setActionText() {
		String text1 = oi.getSelectedItemPosition() == 0 ? StringUtil.NULL
				: "분류 : " + oi.getSelectedItem();
		String text2 = os.getSelectedItemPosition() == 0 ? StringUtil.NULL
				: "정렬 : " + os.getSelectedItem();
		String text = !text1.equals(StringUtil.NULL) ? text1
				+ StringUtil.NEW_LINE + text2 : text2;
		optionTextView.setText(text);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(BUNDLE_LIST, bookList);
		outState.putInt(OI_SEL, oi.getSelectedItemPosition());
		outState.putInt(OS_SEL, os.getSelectedItemPosition());
		outState.putInt(BUNDLE_PAGE, page);
		outState.putString(QUERY, query);
		outState.putString("rawQuery", rawQuery);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Context context = getActivity();
		// 어댑터
		View rootView;
		switch (AppUtil.theme) {
		case Black:
			bookListAdapter = new BookItemListAdapter(context,
					R.layout.list_layout_book_dark, bookList, l, this);
			rootView = inflater.inflate(R.layout.tab_book_search_dark,
					container, false);
			break;
		case BlackAndWhite:
		case White:
		default:
			bookListAdapter = new BookItemListAdapter(context,
					R.layout.list_layout_book, bookList, l, this);
			rootView = inflater.inflate(R.layout.tab_book_search, container,
					false);
			break;
		}

		emptyView = rootView.findViewById(R.id.tab_book_empty);
		emptyView.findViewById(R.id.tab_book_search_empty_info1)
				.setOnClickListener(this);
		emptyView.findViewById(R.id.tab_book_search_empty_info2)
				.setOnClickListener(this);
		if (bookList.size() != 0) {
			emptyView.setVisibility(View.INVISIBLE);
			isResultEmpty = false;
		} else {
			emptyView.setVisibility(View.VISIBLE);
		}
		// 리스트 뷰
		mListView = (ListView) rootView.findViewById(R.id.tab_book_list_search);
		mListView.addFooterView(getLoadingView());
		aAdapter = new AlphaInAnimationAdapter(bookListAdapter);
		aAdapter.setAbsListView(mListView);
		// 스크롤 리스너 등록
		mListView.setOnScrollListener(this);
		mListView.setAdapter(aAdapter);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_book_search_empty_info1:
			searchMenu.expandActionView();
			break;
		case R.id.tab_book_search_empty_info2:
			optionDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.tab_book_search_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_book_search, menu);
			break;
		}

		searchMenu = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenu.getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint(getText(R.string.search_hint));
		searchMenu.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		ActionBar actionBar = getActivity().getActionBar();
		setActionText();
		actionBar.setCustomView(actionView);
		actionBar.setDisplayShowCustomEnabled(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	protected void excute() {
		emptyView.setVisibility(View.GONE);
		super.excute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			optionDialog.show();
			return true;
		case R.id.action_search:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onQueryTextSubmit(String q) {
		InputMethodManager ipm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		rawQuery = q.trim();
		if (rawQuery.equals(StringUtil.NULL)) {
			AppUtil.showToast(getActivity(), R.string.search_input_empty,
					isMenuVisible());
		} else {
			ipm.hideSoftInputFromWindow(searchMenu.getActionView()
					.getWindowToken(), 0);
			searchMenu.collapseActionView();
			String lastQuery;
			try {
				lastQuery = URLEncoder
						.encode(rawQuery, StringUtil.ENCODE_UTF_8);
			} catch (UnsupportedEncodingException e) {
				AppUtil.showToast(getActivity(), "Text Encoding Error!",
						isMenuVisible());
				return true;
			}
			query = lastQuery;
			page = 1;
			bookListAdapter.clear();
			queryTextview.setText(rawQuery);
			excute();
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}

	protected String getSpinnerItemString(int which, int pos) {
		switch (which) {
		case 0:
			switch (pos) {
			case 1:
				return "DISP01";
			case 2:
				return "DISP02";
			case 3:
				return "DISP03";
			case 4:
				return "DISP04";
			case 5:
				return "DISP06";
			}
		case 1:
			switch (pos) {
			case 1:
				return "ASC";
			case 2:
				return "DESC";
			}
		default:
			return StringUtil.NULL;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<BookItem> call() throws Exception {
		String OS = getSpinnerItemString(1, os.getSelectedItemPosition());
		String OI = getSpinnerItemString(0, oi.getSelectedItemPosition());
		boolean check = true;
		StringBuilder sb = new StringBuilder();
		sb.append(URL).append(page).append("&q=").append(query);
		String lastQuery = null;
		String RM = "&websysdiv=tot";
		if (!OI.equals(StringUtil.NULL)) {
			sb.append("&oi=").append(OI);
			lastQuery = StringUtil.remove(sb.toString(), RM);
			check = false;
		}
		if (!OS.equals(StringUtil.NULL)) {
			sb.append("&os=").append(OS);
			lastQuery = sb.toString();
			if (check) {
				lastQuery = StringUtil.remove(lastQuery, RM);
				check = false;
			}
		}
		if (check) {
			lastQuery = sb.toString();
		}
		String body = HttpRequest.getBody(lastQuery);

		ArrayList<BookItem> bookList = (ArrayList<BookItem>) ParseFactory
				.create(ParseFactory.What.Book, body, ParseFactory.Value.BASIC)
				.parse();

		// 대여 가능 도서만 가져옴
		if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_BORROW,
				false)
				&& bookList.size() > 0) {
			bookList = getFilteredList(bookList);
		}
		return bookList;
	}

	@Override
	public void onResult(ArrayList<BookItem> result) {
		Context context = getActivity();
		if (context != null) {
			if (result.size() == 0) {
				AppUtil.showToast(context, R.string.search_result_empty,
						isMenuVisible());
				isResultEmpty = true;
			} else {
				AppUtil.showToast(context, String.valueOf(result.size())
						+ context.getString(R.string.search_found),
						isMenuVisible());
				bookListAdapter.addAll(result);
				bookListAdapter.notifyDataSetChanged();
				aAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onPostExcute() {
		super.onPostExcute();
		isResultEmpty = false;
	}

	protected ArrayList<BookItem> getFilteredList(
			ArrayList<BookItem> originalList) {
		ArrayList<BookItem> newList = new ArrayList<BookItem>();
		BookItem item;
		final String cando = "가능";
		int size = originalList.size();
		for (int i = 0; i < size; i++) {
			item = originalList.get(i);
			if (item.bookState.contains(cando)) {
				newList.add(item);
			}
		}
		if (newList.size() == 0) {
			item = new BookItem();
			item.bookInfo = getString(R.string.tab_book_not_found);
			newList.add(item);
		}
		return newList;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 리스트의 마지막에 도달하였을 경우에
		// 이 이벤트가 처음 일어났고, 이전에 검색결과가 0이 아닌 경우에만
		// 새로운 검색을 시도한다.
		if (totalItemCount > 1
				&& (firstVisibleItem + visibleItemCount) == totalItemCount) {
			if (!isInvokeScroll && !isResultEmpty) {
				isInvokeScroll = true;
				page++;
				excute();
			}
		} else {
			isInvokeScroll = false;
		}
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			switch (AppUtil.theme) {
			case Black:
			case BlackAndWhite:
				inflater.inflate(R.menu.tab_book_contextual_dark, menu);
				break;
			case White:
			default:
				inflater.inflate(R.menu.tab_book_contextual, menu);
				break;
			}

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
			actionMode = null;
		}
	};

	@Override
	public void onDestroyOptionsMenu() {
		if (actionMode != null)
			actionMode.finish();
	}

	protected void copyItem(String text) {
		ClipboardManager clipboard = (ClipboardManager) getActivity()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("copy", text);
		clipboard.setPrimaryClip(clip);
	}

	protected void searchItem(String text) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_WEB_SEARCH);
		intent.putExtra(SearchManager.QUERY, text);
		startActivity(intent);
	}

	/**
	 * listView의 내부 View를 선택할 시 불리는 Callback <br>
	 * Adapter에서 호출된다.
	 */
	private View.OnClickListener l = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			BookItem item;
			Object o = v.getTag();
			if (o != null && o instanceof BookItem) {
				item = (BookItem) o;
				if (v instanceof ImageView) {
					Intent i = AppUtil
							.setWebPageIntent("http://mlibrary.uos.ac.kr"
									+ item.url);
					// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
				} else if (v instanceof TextView) {
					if (item.site.startsWith("http")) {
						Intent i = AppUtil.setWebPageIntent(item.site);
						// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}
				}
			}
		}
	};

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_search);
	}

	/** listView 내부의 TextView에서 호출 됨 */
	@Override
	public boolean onLongClick(View v) {
		if (actionMode == null)
			actionMode = getActivity().startActionMode(mActionModeCallback);

		// View prevView = (View) actionMode.getTag();
		// if (prevView != null)
		// prevView.setSelected(false);
		// v.setSelected(true);
		actionMode.setTag(v);
		actionMode.setTitle(((TextView) v).getText());
		return true;
	}
}
