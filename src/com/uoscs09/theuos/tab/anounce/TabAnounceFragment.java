package com.uoscs09.theuos.tab.anounce;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabAnounceFragment extends
		AbsDrawableProgressFragment<ArrayList<AnounceItem>> {
	/** 상단 액션바에 추가될 레이아웃, spinner와 pageView가 배치된다. */
	@ReleaseWhenDestroy
	private View actionViewLayout;
	/** 상단 액션바에 추가될 위젯, 페이지 인덱스 */
	@ReleaseWhenDestroy
	private TextView pageView;
	/** 상단 액션바에 추가될 위젯, 카테고리 선택 */
	@ReleaseWhenDestroy
	protected Spinner spinner;
	@ReleaseWhenDestroy
	private ArrayAdapter<AnounceItem> adapter;
	private Hashtable<String, String> queryTable;
	@AsyncData
	private ArrayList<AnounceItem> mDataList;
	/** searchView */
	@ReleaseWhenDestroy
	protected MenuItem searchMenu;
	/** viewpager 이동 시 스피너의 아이템 리스너를 한번만 발동시키게 하는 변수 */
	private boolean once;
	/** 검색기능 활성화시 true 가 되는 변수 */
	private boolean isSearch;
	/** (검색 메뉴 선택시)검색어를 저장함 */
	protected String searchQuery;
	protected int spinnerSelection = 0;
	protected int pageNum;
	@ReleaseWhenDestroy
	protected AlertDialog pageSelectDialog;
	@ReleaseWhenDestroy
	protected NumberPicker mPageNumberPicker;
	protected boolean mShouldChangeMaxValueOfPage = false;

	/** 이전 페이지 번호, 공지사항 검색 결과가 없으면 현재 페이지 번호를 변하지 않게하는 역할 */
	private int prevPageNum = pageNum;
	protected static final String PAGE_NUM = "PAGE";
	protected static final String LIST_AN = "list_an";
	private static final String SP_SELECTION = "spinner_selection";

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
		queryTable = new Hashtable<String, String>();
		getDataFromBundle(savedInstanceState);
		Context context = getActivity();
		initDialog();

		actionViewLayout = View.inflate(context,
				R.layout.action_tab_anounce_spinner, null);
		spinner = (Spinner) actionViewLayout
				.findViewById(R.id.tab_announce_action_spinner1);
		spinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
		pageView = (TextView) actionViewLayout
				.findViewById(R.id.tab_anounce_action_textView_page);
		pageView.setOnClickListener(new View.OnClickListener() {
			// 페이지를 나타내는 버튼을 선택했을 시, 페이지를 선택하는 메뉴를 띄운다.
			@Override
			public void onClick(View v) {
				if (spinnerSelection == 0) {
					AppUtil.showToast(getActivity(),
							R.string.tab_anounce_invaild_category, true);
				} else {
					pageSelectDialog.show();
				}
			}
		});
		// FIXME 테마 관련 코드
		adapter = new AnounceAdapter(context, R.layout.list_layout_announce,
				mDataList);
		if (AppUtil.theme == AppTheme.BlackAndWhite) {
			ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(
					context, R.array.tab_anounce_action_spinner,
					R.layout.spinner_simple_item_dark);
			aa.setDropDownViewResource(R.layout.spinner_simple_dropdown_item);
			spinner.setAdapter(aa);
			pageView.setBackgroundResource(R.drawable.selector_button_dark);
			adapter = new AnounceAdapter(context,
					R.layout.list_layout_announce, mDataList);
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.tab_anounce, container, false);

		ListView listView = (ListView) rootView
				.findViewById(R.id.tab_announce_list_announce);
		View emptyView = rootView.findViewById(R.id.tab_anounce_empty_view);
		emptyView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				spinner.performClick();
			}
		});
		listView.setEmptyView(emptyView);
		listView.setOnItemClickListener(mListViewOnItemClickListener);
		listView.setAdapter(adapter);

		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forward: {
			if (spinnerSelection == 0 || !isMenuVisible())
				return true;
			setPageValue(pageNum + 1);
			excute();
			return true;
		}
		case R.id.action_backward: {
			if (spinnerSelection == 0 || !isMenuVisible())
				return true;
			if (pageNum != 1) {
				setPageValue(pageNum - 1);
				excute();
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
		SearchView searchView = (SearchView) searchMenu.getActionView();
		searchView.setOnQueryTextListener(mSearchViewOnQueryTextListener);
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint(getText(R.string.search_hint));
		searchMenu.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		spinner.setSelection(spinnerSelection);
		updatePageNumber();
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setCustomView(actionViewLayout);
		actionBar.setDisplayShowCustomEnabled(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/* TODO Fragment Callback end */

	/* TODO Listener Callback */
	/** 공지사항 리스트중 하나가 선택되면 호출됨 */
	private AdapterView.OnItemClickListener mListViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int pos,
				long itemId) {
			if (!isMenuVisible() || spinnerSelection == 0)
				return;
			Activity activity = getActivity();
			Intent intent = new Intent(activity, SubAnounceWebActivity.class);
			intent.putExtra(
					LIST_AN,
					((AnounceItem) adapterView.getItemAtPosition(pos)).onClickString);
			intent.putExtra(PAGE_NUM, spinnerSelection);
			startActivity(intent);
			AppUtil.overridePendingTransition(activity, 1);
		}
	};

	/** spinner가 선택되면(카테고리 선택) 호출됨 */
	private AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view,
				int position, long itemId) {
			spinnerSelection = position;
			searchQuery = null;
			isSearch = false;
			if (!isMenuVisible() || !once) {
				once = true;
				return;
			}
			if (spinnerSelection == 0)
				return;
			setPageValue(1);
			mShouldChangeMaxValueOfPage = true;
			excute();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private SearchView.OnQueryTextListener mSearchViewOnQueryTextListener = new SearchView.OnQueryTextListener() {

		@Override
		public boolean onQueryTextChange(String newText) {
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			InputMethodManager ipm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			SearchView v = (SearchView) searchMenu.getActionView();
			ipm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			searchMenu.collapseActionView();

			if (spinnerSelection == 0) {
				AppUtil.showToast(getActivity(),
						R.string.tab_anounce_invaild_category, true);
				isSearch = false;
			} else {
				searchQuery = query.trim();
				setPageValue(1);
				isSearch = true;
				mShouldChangeMaxValueOfPage = true;
				excute();
			}
			return true;
		}
	};

	/* TODO Listener end */

	/* TODO AsyncExcutor Callback */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<AnounceItem> call() throws Exception {
		// TODO 최적화 필요, 필요없이 지우고 쓰고 함
		queryTable.clear();
		final int howTo;
		final String url;
		if (spinnerSelection == 3) {
			if (isSearch) {
				queryTable.put("sword", searchQuery);
				queryTable.put("skind", "title");
			}
			queryTable.put("process", "list");
			queryTable.put("brdbbsseq", "1");
			queryTable.put("x", "1");
			queryTable.put("y", "1");
			queryTable.put("w", "3");
			queryTable.put("pageNo", String.valueOf(pageNum));
			howTo = ParseFactory.Value.BODY;
			url = "http://scholarship.uos.ac.kr/scholarship.do";
		} else {
			if (isSearch) {
				queryTable.put("searchCnd", "1");
				queryTable.put("searchWrd", searchQuery);
			}
			queryTable.put("list_id", spinnerSelection == 1 ? "FA1" : "FA2");
			queryTable.put("pageIndex", String.valueOf(pageNum));
			howTo = ParseFactory.Value.BASIC;
			url = "http://www.uos.ac.kr/korNotice/list.do";
		}
		String body = HttpRequest.getBodyByPost(url, StringUtil.ENCODE_UTF_8,
				queryTable, StringUtil.ENCODE_UTF_8);
		return (ArrayList<AnounceItem>) ParseFactory.create(
				ParseFactory.What.Anounce, body, howTo).parse();
	}

	@Override
	public void onTransactResult(ArrayList<AnounceItem> result) {
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
					try {
						int maxPageNumber = Integer
								.parseInt(result.get(i).type);
						mPageNumberPicker.setMaxValue(maxPageNumber / 10 + 1);
						break;
					} catch (Exception e) {
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
			pageView.setText(String.valueOf(pageNum) + StringUtil.SPACE
					+ PAGE_NUM);
	}

	/** 저장된 bundle에서 데이터를 복구한다. */
	private void getDataFromBundle(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			pageNum = savedInstanceState.getInt(PAGE_NUM);
			spinnerSelection = savedInstanceState.getInt(SP_SELECTION);
			mDataList = savedInstanceState.getParcelableArrayList(LIST_AN);
			once = false;
		} else {
			pageNum = 1;
			mDataList = new ArrayList<AnounceItem>();
			once = true;
		}
	}

	/** 페이지 선택 dialog를 생성한다. */
	private void initDialog() {
		Context context = getActivity();
		mPageNumberPicker = new NumberPicker(context);
		mPageNumberPicker.setLayoutParams(new NumberPicker.LayoutParams(
				NumberPicker.LayoutParams.WRAP_CONTENT,
				NumberPicker.LayoutParams.WRAP_CONTENT));
		mPageNumberPicker.setMinValue(1);
		mPageNumberPicker.setMaxValue(999);
		pageSelectDialog = new AlertDialog.Builder(context)
				.setTitle(R.string.tab_anounce_plz_select_page)
				.setView(mPageNumberPicker)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setPageValue(mPageNumberPicker.getValue());
								excute();
							}
						}).setNegativeButton(android.R.string.no, null)
				.create();
	}

	/** 현재 page의 번호를 설정한다. */
	protected void setPageValue(int newValue) {
		prevPageNum = pageNum;
		pageNum = newValue;
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_search);
	}
}
