package com.uoscs09.theuos.tab.libraryseat;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabLibrarySeatFragment extends
		AbsDrawableProgressFragment<ArrayList<SeatItem>> {
	/** 좌석 현황 리스트 뷰의 adapter */
	@ReleaseWhenDestroy
	private ArrayAdapter<SeatItem> mSeatAdapter;
	/** 해지 될 좌석 정보 리스트 뷰의 adapter */
	@ReleaseWhenDestroy
	private ArrayAdapter<String> mInfoAdapter;
	/** 좌석 정보 리스트 */
	@AsyncData
	private ArrayList<SeatItem> mSearList;
	/** 해지 될 좌석 정보 리스트 */
	private ArrayList<String> mDissmissInfoList;
	/** 좌석 정보 리스트 뷰 */
	@ReleaseWhenDestroy
	private ListView mSeatListView;
	/** 해지 될 좌석 정보 리스트 뷰, infoDialog에서 보여진다. */
	@ReleaseWhenDestroy
	private ListView mInfoListView;
	/** 상단 액션바에 설정되는 layout, timeTextView가 포함되어 동기화 시간을 나타낸다. */
	@ReleaseWhenDestroy
	private View mActionViewLayout;
	/** 상단 액션바에 설정되어 동기화 시간을 나타내는 TextView */
	@ReleaseWhenDestroy
	private TextView mTimeTextView;
	/**
	 * 상단 액션바에 설정되는 timeTextView에 설정될 Text.<br>
	 * 
	 * {@code onSaveonSaveInstanceState()} 에서 "COMMIT_TIME"라는 이름으로 저장된다.
	 */
	private String mCommitTime = StringUtil.NULL;
	/**
	 * 해지될 좌석 정보 버튼 ({@code R.id.action_info})을 선택하면 나타나는 AlertDialog<br>
	 * 해지될 좌석 정보를 보여준다.
	 */
	@ReleaseWhenDestroy
	private AlertDialog mInfoDialog;
	/** 중앙 도서관 좌석 정보 확인 페이지 */
	private final static String URL = "http://203.249.102.34:8080/seat/domian5.asp";
	/** bundle에서 동기화 시간 정보 String을 가리킨다. */
	private final static String COMMIT_TIME = "COMMIT_TIME";
	/** bundle에서 좌석 정보 List를 가리킨다. */
	private final static String BUNDLE_LIST = "SeatList";
	/** bundle에서 해지될 좌석 정보 List를 가리킨다. */
	private final static String INFO_LIST = "InfoList";
	/** {@code SubSeatWebActivity}에 전달할 SeatItem을 가리킨다. */
	protected final static String ITEM = "item";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setMenuRefresh(false);
		if (savedInstanceState != null) {
			mCommitTime = savedInstanceState.getString(COMMIT_TIME);
			mSearList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
			mDissmissInfoList = savedInstanceState
					.getStringArrayList(INFO_LIST);
		} else {
			this.mSearList = new ArrayList<SeatItem>();
			mDissmissInfoList = new ArrayList<String>();
		}
		Activity activity = getActivity();
		mActionViewLayout = View.inflate(activity,
				R.layout.action_tab_lib_seat_view, null);
		mTimeTextView = (TextView) mActionViewLayout
				.findViewById(R.id.tab_library_seat_action_text_last_commit_time);
		mInfoAdapter = new SeatDissmissInfoListAdapter(activity,
				R.layout.list_layout_two_text_view, mDissmissInfoList);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_libraryseat, container,
				false);
		Activity activity = getActivity();
		mSeatAdapter = new SeatListAdapter(activity, R.layout.list_layout_seat,
				mSearList);
		viewInit(rootView);

		mSeatListView.setOnItemClickListener(itemClickListenerOfLanguageList);

		mInfoListView = new ListView(activity);
		TextView emptyView = new TextView(activity);
		emptyView.setText(R.string.tab_library_seat_dissmiss_info_not_loading);
		mInfoListView.setEmptyView(emptyView);
		mInfoListView.setAdapter(mInfoAdapter);

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(COMMIT_TIME, mCommitTime);
		outState.putParcelableArrayList(BUNDLE_LIST, mSearList);
		outState.putStringArrayList(INFO_LIST, mDissmissInfoList);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		if (mSearList.isEmpty()) {
			if (getExecutor() != null || !isRunning()) {
				excute();
			}
		} else {
			mSeatListView.removeFooterView(getLoadingView());
		}
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_library_seat, menu);
		mTimeTextView.setText(mCommitTime);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setCustomView(mActionViewLayout);
		actionBar.setDisplayShowCustomEnabled(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			if (getExecutor() != null && VERSION.SDK_INT < VERSION_CODES.KITKAT
					&& !isRunning()) {
				mSeatListView.addFooterView(getLoadingView());
			}
			excute();
			return true;
		case R.id.action_info:
			if (getExecutor() != null && isRunning()) {
				AppUtil.showToast(getActivity(),
						R.string.tab_library_seat_dissmiss_info_not_loading,
						true);
				return true;
			}
			if (mInfoDialog == null) {
				mInfoDialog = new AlertDialog.Builder(getActivity())
						.setTitle(getText(R.string.action_dissmiss_info))
						.setView(mInfoListView).create();
			}
			mInfoDialog.show();
			return true;
		default:
			return false;
		}
	}

	private void viewInit(View rootView) {
		mSeatListView = (ListView) rootView
				.findViewById(R.id.tab_library_list_seat);
		if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
			mSeatListView.addFooterView(getLoadingView());
		}
		mSeatListView.setAdapter(mSeatAdapter);
	}

	@Override
	protected void excute() {
		if (VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR2) {
			if (getExecutor() == null) {
				mSeatListView.addFooterView(getLoadingView());
			} else if (!isRunning()) {
				mSeatListView.addFooterView(getLoadingView());
			}
		}
		mSearList.clear();
		mSeatAdapter.clear();
		super.excute();
	}

	@Override
	protected void onTransactPostExcute() {
		super.onTransactPostExcute();
		mSeatListView.removeFooterView(getLoadingView());
	}

	@Override
	public void onTransactResult(ArrayList<SeatItem> result) {
		updateTimeView();
		mSeatAdapter.clear();
		mSeatAdapter.addAll(result);
		mSeatAdapter.notifyDataSetChanged();
		mInfoAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<SeatItem> call() throws Exception {
		String body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR);
		ArrayList<SeatItem> callSeatList = (ArrayList<SeatItem>) ParseFactory
				.create(ParseFactory.What.Seat, body, 0).parse();

		// '해지될 좌석 정보' 정보를 리스트에 추가
		SeatItem dissmisInfo = callSeatList.remove(callSeatList.size() - 1);
		if (mDissmissInfoList != null)
			mDissmissInfoList.clear();
		if (mInfoAdapter != null)
			mInfoAdapter.clear();
		String[] array = dissmisInfo.occupySeat.split(StringUtil.NEW_LINE);
		for (int i = 0; i < array.length - 1; i += 2) {
			mDissmissInfoList.add(array[i] + "+" + array[i + 1]);
		}

		// 이용률이 50%가 넘는 스터디룸은 보여주지 않음
		if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_SEAT,
				false)) {
			getFilteredList(callSeatList);
		}
		return callSeatList;
	}

	private void getFilteredList(ArrayList<SeatItem> originalList) {
		SeatItem item;
		// 스터디룸 인덱스
		final int[] filterArr = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23,
				24, 25, 26, 27, 28 };
		final int size = filterArr.length;
		for (int i = size - 1; i > -1; i--) {
			item = originalList.get(filterArr[i]);
			if (Double.valueOf(item.utilizationRate) >= 50) {
				originalList.remove(item);
			}
		}
	}

	private OnItemClickListener itemClickListenerOfLanguageList = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (!isMenuVisible())
				return;
			SeatItem item = (SeatItem) arg0.getItemAtPosition(position);
			if (item == null)
				return;
			Activity activity = getActivity();
			Intent intent = new Intent(activity, SubSeatWebActivity.class);
			intent.putExtra(ITEM, item);
			startActivity(intent);
			AppUtil.overridePendingTransition(activity, 1);
		}
	};

	private void updateTimeView() {
		// Fragment가 Attatch 되지 않은 경우
		if (getActivity() == null)
			return;
		Calendar c = Calendar.getInstance();
		int ampm = c.get(Calendar.AM_PM);
		int h = c.get(Calendar.HOUR) == 0 ? 12 : c.get(Calendar.HOUR);
		int m = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);

		StringBuilder sb = new StringBuilder();
		sb.append(getText(R.string.tab_library_seat_last_update));

		if (ampm == Calendar.AM) {
			sb.append(StringUtil.STR_AM);
		} else {
			sb.append(StringUtil.STR_PM);
		}
		sb.append(h);
		sb.append(':');
		if (m < 10) {
			sb.append(0);
		}
		sb.append(m);
		sb.append(':');
		if (s < 10) {
			sb.append(0);
		}
		sb.append(s);
		mCommitTime = sb.toString();
		if (mTimeTextView != null) {
			mTimeTextView.setText(mCommitTime);
		}
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_refresh);
	}
}
