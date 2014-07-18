package com.uoscs09.theuos.tab.timetable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.Callable;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;
import com.uoscs09.theuos.tab.map.SubMapActivity;
import com.uoscs09.theuos.tab.subject.SubjectInfoDialFrag;
import com.uoscs09.theuos.tab.subject.SubjectItem;

/**
 * 시간표 탭에서 과목을 선택하면 불리게 되는 클래스<br>
 * 과목 선택시 AlertDialog를 띄우고, AlertDialog에서 제공하는 메뉴를 처리한다.
 */
public class TimeTableInfoCallback implements View.OnClickListener {
	protected Hashtable<String, String> params;
	protected WeakReference<Context> contextRef;
	protected String building, subjectName;
	protected int day, pos;
	protected AlertDialog infoDialog, alarmDialog;
	protected AsyncExecutor<ArrayList<ArrayList<String>>> infoExecutor;
	protected Dialog progress;
	private Term term;
	private final static long DAY = 1000 * 60 * 60 * 24;
	private final static long WEEK = DAY * 7;
	public final static String ACTION_SET_ALARM = "com.uoscs09.theuos.tab.timetable.set_alarm";
	public final static String INTENT_TIME = "com.uoscs09.theuos.tab.timetable.INTENT_TIME";
	public final static String INTENT_CODE = "com.uoscs09.theuos.tab.timetable.INTENT_CODE";

	public TimeTableInfoCallback(Context context) {
		contextRef = new WeakReference<Context>(context);
		progress = AppUtil.getProgressDialog(context, false, null);
		initTable();
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public void doOnClickFromTimeTable(View v) {
		// 시간표를 터치하면 나타나는 dialog를 생성하는 과정
		if (v instanceof TextView) {
			// 전달되어 오는 TextView의 tag는
			// 시간표 정보 + 시간표 정보의 리스트 인덱스(0-15) + 시간표 요일 정보(1-7)
			TextView tv = (TextView) v;
			String tag = tv.getTag().toString();
			String[] split = tag.split(StringUtil.NEW_LINE);
			subjectName = split[0].trim();

			if (StringUtil.NULL.equals(subjectName)) {
				try {
					subjectName = split[1].trim();
				} catch (Exception e) {
					subjectName = StringUtil.NULL;
				}
			}
			params.put(OApiUtil.SUBJECT_NAME, subjectName);

			try {
				building = split[split.length - 3].split("-")[0];
			} catch (Exception e) {
				building = StringUtil.NULL;
			}
			if (building.equals(StringUtil.NULL))
				return;
			pos = Integer.valueOf(split[split.length - 2]);
			day = Integer.valueOf(split[split.length - 1]);

			final Context context = contextRef.get();
			if (context == null)
				return;
			View dialogView = View.inflate(context,
					R.layout.dialog_timetable_subject, null);
			dialogView.findViewById(R.id.dialog_timetable_button_map)
					.setOnClickListener(this);
			dialogView.findViewById(R.id.dialog_timetable_button_info)
					.setOnClickListener(this);
			Spinner spinner = (Spinner) dialogView
					.findViewById(R.id.timetable_callback_alarm_spinner);

			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					PrefUtil.getInstance(context).put(
							PrefUtil.KEY_TIMETABLE_NOTIFY_TIME + pos + "-"
									+ day, arg2);
					setOrCancelAlarm(pos, day, subjectName, arg2, arg2 > 0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			spinner.setSelection(getPrefNotiIndex(pos, day, context));
			infoDialog = new AlertDialog.Builder(context).setTitle(subjectName)
					.setView(dialogView).create();
			infoDialog.show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_timetable_list_text_mon:
		case R.id.tab_timetable_list_text_tue:
		case R.id.tab_timetable_list_text_wed:
		case R.id.tab_timetable_list_text_thr:
		case R.id.tab_timetable_list_text_fri:
		case R.id.tab_timetable_list_text_sat:
			doOnClickFromTimeTable(v);
			break;
		case R.id.dialog_timetable_button_info:
			whenInfoButtonClicked();
			break;
		case R.id.dialog_timetable_button_map:
			whenMapButtonClicked();
			break;
		default:
			break;
		}
	}

	protected int getPrefNotiIndex(int pos, int day, Context context) {
		return PrefUtil.getInstance(context).get(
				PrefUtil.KEY_TIMETABLE_NOTIFY_TIME + pos + "-" + day, 0);
	}

	protected int getPreNotiMinute(int pos, int day, Context context) {
		switch (getPrefNotiIndex(pos, day, context)) {
		case 2:
			return 15;
		case 3:
			return 20;
		case 4:
			return 30;
		case 5:
			return 45;
		case 6:
			return 60;
		default:
			return 10;
		}
	}

	private static int getAlarmCode(int position, int day) {
		return position * 10 + day;
	}

	protected void setOrCancelAlarm(int position, int day, String name,
			int spinnerSelection, boolean isSet) {
		Context context = contextRef.get();
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		int code = getAlarmCode(position, day);
		String when = context.getResources().getStringArray(
				R.array.tab_timetable_alarm_time_array)[spinnerSelection];

		Intent intent = new Intent(context.getApplicationContext(),
				TimeTableNotiReceiver.class).setAction(ACTION_SET_ALARM)
				.putExtra(OApiUtil.SUBJECT_NAME, name)
				.putExtra(INTENT_CODE, code)
				.putExtra(INTENT_TIME, spinnerSelection);
		PendingIntent pi = PendingIntent.getBroadcast(context, code, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		IOUtil.saveToFileSuppressed(context, String.valueOf(code),
				Context.MODE_PRIVATE, when);
		if (isSet) {
			long notiTime = getNotificationTime(position, day);
			am.cancel(pi);
			am.setRepeating(AlarmManager.RTC_WAKEUP, notiTime, WEEK, pi);
			Log.d("timetable_alarm", "alaram time : " + notiTime);
		} else {
			am.cancel(pi);
		}
	}

	public static void clearAllAlarm(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		String when = context.getResources().getStringArray(
				R.array.tab_timetable_alarm_time_array)[0];
		Intent intent = new Intent(context.getApplicationContext(),
				TimeTableNotiReceiver.class).setAction(ACTION_SET_ALARM)
				.putExtra(INTENT_CODE, 0);
		int day;
		for (int pos = 0; pos < 15; pos++) {
			for (day = 1; day < 7; day++) {
				int code = getAlarmCode(pos, day);
				PendingIntent pi = PendingIntent.getBroadcast(context, code,
						intent.putExtra(INTENT_CODE, code),
						PendingIntent.FLAG_UPDATE_CURRENT);
				IOUtil.saveToFileAsync(context, String.valueOf(code),
						Context.MODE_PRIVATE, when, null);
				am.cancel(pi);
			}
		}

	}

	/**
	 * 알림 시간을 반환한다.
	 * 
	 * @param position
	 *            - 리스트 뷰에서의 위치. 즉, 시간대를 의미함. 범위는 0 ~ 14
	 * @param day
	 *            - 시간표에서의 날짜. 범위는 시간을 나타내는 첫 번째 칸을 제외하고<br>
	 *            월요일 1 ~ 토요일 6 임.
	 */
	private long getNotificationTime(int position, int day) {
		Calendar c = Calendar.getInstance(Locale.KOREA);
		int hour, minute;
		switch (position) {
		case 10:
			hour = 18;
			minute = 45;
			break;
		case 11:
			hour = 19;
			minute = 35;
			break;
		case 12:
			hour = 20;
			minute = 20;
			break;
		case 13:
			hour = 21;
			minute = 10;
			break;
		case 14:
			hour = 21;
			minute = 55;
			break;
		default:
			hour = position + 9;
			minute = 0;
			break;
		}
		int beforeMinute = getPreNotiMinute(position, day, contextRef.get());

		minute -= beforeMinute;
		if (minute < 0) {
			hour -= 1;
			minute += 60;
		}

		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.DAY_OF_WEEK, day + 1);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		Calendar now = Calendar.getInstance();
		if (c.before(now)) {
			c.add(Calendar.DATE, 7);
		}
		long time = c.getTimeInMillis();
		return time;
	}

	private void whenMapButtonClicked() {
		try {
			Context context = contextRef.get();
			int no = Integer.valueOf(building);
			Intent intent = new Intent(context, SubMapActivity.class);
			intent.putExtra("building", no);
			context.startActivity(intent);
			infoDialog.dismiss();
		} catch (Exception e) {
		}
	}

	private void whenInfoButtonClicked() {
		if (infoExecutor != null && !infoExecutor.isCancelled()) {
			infoExecutor.cancel(true);
		}
		infoExecutor = new AsyncExecutor<ArrayList<ArrayList<String>>>();
		infoExecutor.setCallable(infoCallable).setCallback(infoCallback)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		progress.show();
	}

	protected void initTable() {
		params = new Hashtable<String, String>();
		params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
	}

	/** 수업 계획서 메뉴를 선택하였을 시, 작업을 처리하는 Callable */
	protected final Callable<ArrayList<ArrayList<String>>> infoCallable = new Callable<ArrayList<ArrayList<String>>>() {
		@SuppressWarnings("unchecked")
		@Override
		public ArrayList<ArrayList<String>> call() throws Exception {
			params.put(OApiUtil.YEAR, OApiUtil.getSemesterYear(term));
			params.put(OApiUtil.TERM, OApiUtil.getTermCode(term));
			return (ArrayList<ArrayList<String>>) ParseFactory
					.create(ParseFactory.What.SubjectList,
							HttpRequest
									.getBody(
											"http://wise.uos.ac.kr/uosdoc/api.ApiApiSubjectList.oapi",
											StringUtil.ENCODE_EUC_KR, params,
											StringUtil.ENCODE_EUC_KR),
							ParseFactory.Value.BASIC).parse();
		}
	};

	/** 수업 계획서 메뉴를 선택하였을 시, 작업 결과를 처리하는 AsyncCallback */
	protected final AsyncCallback<ArrayList<ArrayList<String>>> infoCallback = new AsyncCallback<ArrayList<ArrayList<String>>>() {

		@Override
		public void onResult(ArrayList<ArrayList<String>> result) {
			infoDialog.dismiss();
			final Context context = contextRef.get();
			if (context == null)
				return;

			int size = result.size();
			if (size == 0) {
				return;
			}
			String subjNo = StringUtil.NULL;
			ArrayList<String> divList = new ArrayList<String>();
			ArrayList<String> tempList;
			final String subjectNm = params.get(OApiUtil.SUBJECT_NAME).trim();
			for (int i = 0; i < size; i++) {
				tempList = result.get(i);
				if (subjectNm.equals(tempList.get(1).trim())) {
					divList.add(tempList.get(2));
					subjNo = tempList.get(0);
				}
			}
			final String subjNO = subjNo;

			if (size == 1) {
				if (divList.size() < 1) {
					AppUtil.showToast(context, "교과목을 처리하는데 에러가 발생하였습니다.", true);
					return;
				}
				SubjectItem item = new SubjectItem();
				item.infoArray[4] = divList.get(0);
				item.infoArray[3] = subjNO;
				item.infoArray[5] = subjectNm;
				showDialFrag(
						((FragmentActivity) context)
								.getSupportFragmentManager(),
						item);
				return;
			}

			View dialogView = View.inflate(context,
					R.layout.dialog_timecallback, null);
			final AlertDialog dialog = new AlertDialog.Builder(context)
					.setTitle(params.get(OApiUtil.SUBJECT_NAME))
					.setView(dialogView).create();

			ListView divListView = (ListView) dialogView
					.findViewById(R.id.dialog_timetable_callback_listview_div);
			divListView.setAdapter(new ArrayAdapter<String>(context,
					android.R.layout.simple_list_item_1, divList));
			divListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View arg1,
						int pos, long arg3) {
					SubjectItem item = new SubjectItem();
					item.infoArray[4] = (String) adapter.getItemAtPosition(pos);
					item.infoArray[3] = subjNO;
					item.infoArray[5] = subjectNm;
					showDialFrag(((FragmentActivity) context)
							.getSupportFragmentManager(), item);

					dialog.dismiss();
				}
			});
			dialog.show();
		}

		@Override
		public void exceptionOccured(Exception e) {
			Context context = contextRef.get();
			if (context == null)
				return;
			if (e instanceof IOException) {
				AppUtil.showInternetConnectionErrorToast(context, true);
			} else {
				AppUtil.showErrorToast(context, e, true);
			}
		}

		@Override
		public void onPostExcute() {
			progress.dismiss();
		}

		@Override
		public void cancelled() {
		}
	};

	protected void showDialFrag(FragmentManager fm, SubjectItem item) {
		SubjectInfoDialFrag.showDialog(fm, item, contextRef.get(),
				term.ordinal());
	}
}
