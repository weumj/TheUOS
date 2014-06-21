package com.uoscs09.theuos.tab.restaurant;

import java.io.IOException;
import java.util.ArrayList;

import pkg.asyncexcute.AsyncExecutor;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabRestaurantFragment extends
		AbsAsyncFragment<ArrayList<RestItem>> {
	protected ScrollView restScroll;
	private TextView restName, restSemester, restVacation, breakfast, lunch,
			supper, actionTextView;
	protected int buttonID;
	private ArrayList<RestItem> restList;
	private View actionViewLayout;

	private static final String BUTTON = "button";
	private static final String REST = "rest_list";
	private static final String NO_INFO = "정보가 없습니다.\n";
	private static final String[] timeSemester = {
			"학기중				\n조식 : 08:00~10:00	\n중식 : 11:00~14:00	\n 		       15:00~17:00",
			"학기중				\n중식 : 11:30~14:00	\n석식 : 15:00~19:00	\n토요일 : 휴무",
			"학기중					\n중식 : 11:30~13:30\n석식 : 17:00~18:30\n토요일 : 휴무",
			StringUtil.NULL, StringUtil.NULL };
	private static final String[] timeVacation = {
			"방학중				\n조식 : 09:00~10:00\n	         08:30~10:00 (계절학기 기간)\n중식 : 11:00~14:00\n	         15:00~17:00\n석식 : 17:00~18:30\n토요일 : 휴무",
			"방학중				\n중식 : 11:30~14:00\n석식 : 16:00~18:30\n토요일 : 휴무",
			"방학중 : 휴관		\n\n\n", StringUtil.NULL, StringUtil.NULL };

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(BUTTON, buttonID);
		outState.putParcelableArrayList(REST, restList);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		Context context = getActivity();

		if (savedInstanceState != null) {
			buttonID = savedInstanceState.getInt(BUTTON);
			restList = savedInstanceState.getParcelableArrayList(REST);
		} else {
			buttonID = R.id.tab_rest_button_students_hall;
		}

		if (OApiUtil.getDateTime()
				- PrefUtil.getInstance(context).get(
						PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
			restList = AppUtil.readFromFile(context, AppUtil.FILE_REST);
		}

		actionViewLayout = View.inflate(context,
				R.layout.action_tab_lib_seat_view, null);
		actionTextView = (TextView) actionViewLayout
				.findViewById(R.id.tab_library_seat_action_text_last_commit_time);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		switch (AppUtil.theme) {
		case Black:
			rootView = inflater.inflate(R.layout.tab_restaurant_dark,
					container, false);
			break;
		case BlackAndWhite:
		case White:
		default:
			rootView = inflater.inflate(R.layout.tab_restaurant, container,
					false);
			break;
		}
		rootView.findViewById(R.id.tab_rest_button_students_hall)
				.setOnClickListener(disp);
		rootView.findViewById(R.id.tab_rest_button_anekan).setOnClickListener(
				disp);
		rootView.findViewById(R.id.tab_rest_button_natural).setOnClickListener(
				disp);
		rootView.findViewById(R.id.tab_rest_button_main_8th)
				.setOnClickListener(disp);
		rootView.findViewById(R.id.tab_rest_button_living).setOnClickListener(
				disp);

		restScroll = (ScrollView) rootView.findViewById(R.id.tab_rest_scroll);
		restName = (TextView) rootView.findViewById(R.id.tab_rest_text_name);
		restSemester = (TextView) rootView
				.findViewById(R.id.tab_rest_text_semester);
		restVacation = (TextView) rootView
				.findViewById(R.id.tab_rest_text_vacation);
		breakfast = (TextView) rootView
				.findViewById(R.id.tab_rest_text_breakfast);
		lunch = (TextView) rootView.findViewById(R.id.tab_rest_text_lunch);
		supper = (TextView) rootView.findViewById(R.id.tab_rest_text_supper);

		performClick(buttonID);
		return rootView;
	}

	@Override
	public void onResume() {
		if (restList == null)
			excute();
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
			actionTextView.setTextColor(Color.WHITE);
			inflater.inflate(R.menu.tab_restaurant_dark, menu);
			break;
		case Black:
			inflater.inflate(R.menu.tab_restaurant_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_restaurant, menu);
			break;
		}
		ActionBar actionBar = getActivity().getActionBar();
		AsyncExecutor<ArrayList<RestItem>> executor = getExecutor();
		if (executor != null
				&& executor.getStatus().equals(AsyncTask.Status.RUNNING)) {
			actionTextView.setText("로딩중");
		} else {
			actionTextView.setText(StringUtil.NULL);
		}

		actionBar.setCustomView(actionViewLayout);
		actionBar.setDisplayShowCustomEnabled(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			excute();
			return true;
		default:
			return false;
		}
	}

	private final OnClickListener disp = new OnClickListener() {
		@Override
		public void onClick(View v) {
			restScroll.scrollTo(0, 0);
			buttonID = v.getId();
			performClick(buttonID);
		}
	};

	private void performClick(int buttonId) {
		switch (buttonId) {
		case R.id.tab_rest_button_students_hall:
			setSemesterAndVacationText(0);
			setBody("학생회관 1층");
			break;
		case R.id.tab_rest_button_anekan:
			setSemesterAndVacationText(1);
			setBody("양식당 (아느칸)");
			break;
		case R.id.tab_rest_button_natural:
			setSemesterAndVacationText(2);
			setBody("자연과학관");
			break;
		case R.id.tab_rest_button_main_8th:
			setSemesterAndVacationText(3);
			setBody("본관 8층");
			break;
		case R.id.tab_rest_button_living:
			setSemesterAndVacationText(4);
			setBody("생활관");
			break;
		default:
			return;
		}
	}

	private void setSemesterAndVacationText(int i) {
		restSemester.setText(timeSemester[i]);
		restVacation.setText(timeVacation[i]);
	}

	public void setBody(String name) {
		restName.setText(name);
		RestItem item = null;
		if (restList != null) {
			int size = restList.size();
			for (int i = 0; i < size; i++) {
				item = restList.get(i);
				if (name.contains(item.title)) {
					breakfast.setText(item.breakfast);
					lunch.setText(item.lunch);
					supper.setText(item.supper);
					break;
				}
				item = null;
			}
		}
		if (item == null) {
			breakfast.setText(NO_INFO);
			lunch.setText(NO_INFO);
			supper.setText(NO_INFO);
		}
	}

	@Override
	protected void excute() {
		setActionText(getString(R.string.progress_while_updating));
		super.excute();
	}

	@Override
	public ArrayList<RestItem> call() throws Exception {
		return getRestListFromWeb(getActivity());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<RestItem> getRestListFromWeb(Context context)
			throws IOException {
		ArrayList<RestItem> list = new ArrayList<RestItem>();
		String body = HttpRequest
				.getBody("http://m.uos.ac.kr/mkor/food/list.do");
		list = (ArrayList<RestItem>) ParseFactory.create(ParseFactory.REST,
				body, ParseFactory.Value.BASIC).parse();
		AppUtil.saveToFile(context, AppUtil.FILE_REST, Activity.MODE_PRIVATE,
				list);
		PrefUtil.getInstance(context).put(PrefUtil.KEY_REST_DATE_TIME,
				OApiUtil.getDate());
		return list;
	}

	@Override
	public void exceptionOccured(Exception e) {
		super.exceptionOccured(e);
		setActionText("로딩 실패");
	}

	@Override
	public void onResult(ArrayList<RestItem> result) {
		restList = result;
		performClick(R.id.tab_rest_button_students_hall);
		setActionText(StringUtil.NULL);
	}

	private void setActionText(String text) {
		actionTextView.setText(text);
	}

	@Override
	public void cancelled() {
	}

	@Override
	public void onPostExcute() {
	}
}
