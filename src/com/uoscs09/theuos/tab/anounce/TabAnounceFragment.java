package com.uoscs09.theuos.tab.anounce;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabAnounceFragment extends
		AbsAsyncFragment<ArrayList<AnounceItem>> implements
		AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener,
		SearchView.OnQueryTextListener {
	/** 상단 액션바에 추가될 레이아웃, spinner와 pageView가 배치된다. */
	private View actionViewLayout;
	/** 상단 액션바에 추가될 위젯, 페이지 인덱스 */
	private TextView pageView;
	/** 상단 액션바에 추가될 위젯, 카테고리 선택 */
	protected Spinner spinner;
	private ProgressDialog progress;
	private ArrayAdapter<AnounceItem> adapter;
	private Hashtable<String, String> queryTable;
	private ArrayList<AnounceItem> list;
	/** searchView */
	private MenuItem searchMenu;
	/** viewpager 이동 시 스피너의 아이템 리스너를 한번만 발동시키게 하는 변수 */
	private boolean once;
	/** 검색기능 활성화시 true 가 되는 변수 */
	private boolean isSearch;
	/** (검색 메뉴 선택시)검색어를 저장함 */
	private String searchQuery;
	protected int spinnerSelection = 0;
	protected int pageNum;
	private AnimationAdapter aAdapter;
	/** 이전 페이지 번호, 공지사항 검색 결과가 없으면 현재 페이지 번호를 변하지 않게하는 역할 */
	private int prevPageNum = pageNum;
	protected static final String PAGE_NUM = "PAGE";
	protected static final String LIST_AN = "list_an";
	private static final String SP_SELECTION = "spinner_selection";

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(PAGE_NUM, pageNum);
		outState.putInt(SP_SELECTION, spinnerSelection);
		outState.putParcelableArrayList(LIST_AN, list);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		Context context = getActivity();
		progress = AppUtil.getProgressDialog(context, false,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelExecutor();
						pageNum = prevPageNum;
					}
				});
		if (savedInstanceState != null) {
			pageNum = savedInstanceState.getInt(PAGE_NUM);
			spinnerSelection = savedInstanceState.getInt(SP_SELECTION);
			list = savedInstanceState.getParcelableArrayList(LIST_AN);
			once = false;
		} else {
			pageNum = 1;
			list = new ArrayList<AnounceItem>();
			once = true;
		}
		actionViewLayout = View.inflate(context,
				R.layout.action_tab_anounce_spinner, null);
		spinner = (Spinner) actionViewLayout
				.findViewById(R.id.tab_announce_action_spinner1);
		spinner.setOnItemSelectedListener(this);
		pageView = (TextView) actionViewLayout
				.findViewById(R.id.tab_anounce_action_textView_page);
		switch (AppUtil.theme) {
		case Black:
			adapter = new AnounceAdapter(context,
					R.layout.list_layout_announce_dark, list);
			break;
		case BlackAndWhite:
			ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(
					context, R.array.tab_anounce_action_spinner,
					R.layout.spinner_simple_item_dark);
			aa.setDropDownViewResource(R.layout.spinner_simple_dropdown_item);
			spinner.setAdapter(aa);
			pageView.setTextColor(Color.WHITE);
			adapter = new AnounceAdapter(context,
					R.layout.list_layout_announce, list);
			break;
		case White:
		default:
			adapter = new AnounceAdapter(context,
					R.layout.list_layout_announce, list);
			break;
		}

		queryTable = new Hashtable<String, String>();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		switch (AppUtil.theme) {
		case Black:
			rootView = inflater.inflate(R.layout.tab_anounce_dark, container,
					false);
			break;
		case BlackAndWhite:
		case White:
		default:
			rootView = inflater.inflate(R.layout.tab_anounce, container, false);
			break;
		}

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
		listView.setOnItemClickListener(this);
		aAdapter = new SwingLeftInAnimationAdapter(adapter);
		aAdapter.setAbsListView(listView);
		listView.setAdapter(aAdapter);
		return rootView;
	}

	/** 공지사항 리스트중 하나가 선택되면 호출됨 */
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

	/** spinner가 선택되면(카테고리 선택) 호출됨 */
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
		pageNum = prevPageNum = 1;
		excute();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.tab_anounce_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_anounce, menu);
			break;
		}
		searchMenu = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenu.getActionView();
		searchView.setOnQueryTextListener(this);
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
			pageNum = 1;
			isSearch = true;
			excute();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forward: {
			if (spinnerSelection == 0 || !isMenuVisible())
				return true;
			prevPageNum = pageNum;
			pageNum += 1;
			excute();
			return true;
		}
		case R.id.action_backward: {
			if (spinnerSelection == 0 || !isMenuVisible())
				return true;
			if (pageNum != 1) {
				prevPageNum = pageNum;
				pageNum -= 1;
				excute();
			}
			return true;
		}
		default:
			return false;
		}
	}

	@Override
	protected void excute() {
		progress.show();
		super.excute();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<AnounceItem> call() throws Exception {
		// TODO 최적화 필요, 필요없이 지우고 쓰고 함
		queryTable.clear();
		int howTo;
		String url;
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
	public void onPostExcute() {
		super.onPostExcute();
		progress.dismiss();
	}

	@Override
	public void onResult(ArrayList<AnounceItem> result) {
		if (result == null || result.size() == 0) {
			pageNum = prevPageNum;
			AppUtil.showToast(getActivity(), R.string.search_result_empty, true);
		} else {
			adapter.clear();
			adapter.addAll(result);
			adapter.notifyDataSetChanged();
			aAdapter.notifyDataSetChanged();
			aAdapter.setShouldAnimateFromPosition(0);
		}
		updatePageNumber();
	}

	protected void updatePageNumber() {
		if (pageView != null)
			pageView.setText(String.valueOf(pageNum) + StringUtil.SPACE
					+ PAGE_NUM);
	}
}
