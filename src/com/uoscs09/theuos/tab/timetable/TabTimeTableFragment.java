package com.uoscs09.theuos.tab.timetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.AsyncLoader.OnTaskFinishedListener;
import com.uoscs09.theuos.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.GraphicUtil;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.TimeTableHttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabTimeTableFragment extends
		AbsDrawableProgressFragment<ArrayList<TimeTableItem>> implements
		DialogInterface.OnClickListener, View.OnClickListener {
	@ReleaseWhenDestroy
	private AlertDialog loginDialog;
	@AsyncData
	private List<TimeTableItem> mTimetableList;
	@ReleaseWhenDestroy
	protected ArrayAdapter<TimeTableItem> adapter;
	@ReleaseWhenDestroy
	protected View rootView;
	@ReleaseWhenDestroy
	protected EditText idView, passwdView;
	@ReleaseWhenDestroy
	private Spinner termSpinner;
	@ReleaseWhenDestroy
	protected TextView termTextView;
	@ReleaseWhenDestroy
	private AlertDialog deleteDialog;
	protected Term term;
	@ReleaseWhenDestroy
	protected ListView listView;
	@ReleaseWhenDestroy
	private TimeTableInfoCallback cb;
	private boolean mIsOnLoad;
	private Map<String, Integer> colorTable;

	private int[] titleViewIds = { R.id.tab_time_peroid, R.id.tab_time_mon,
			R.id.tab_time_tue, R.id.tab_time_wed, R.id.tab_time_thur,
			R.id.tab_time_fri, R.id.tab_time_sat, };
	protected static int px;
	public final static int NUM_OF_TIMETABLE_VIEWS = 7;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context context = getActivity();
		if (savedInstanceState != null) {
			mTimetableList = savedInstanceState
					.getParcelableArrayList(IOUtil.FILE_TIMETABLE);
			colorTable = (Map<String, Integer>) savedInstanceState
					.getSerializable("color");
		} else {
			mTimetableList = new ArrayList<TimeTableItem>();
			colorTable = new Hashtable<String, Integer>();
		}
		cb = new TimeTableInfoCallback(context);
		termTextView = (TextView) View.inflate(context,
				R.layout.action_textview, null);

		int termValue = PrefUtil.getInstance(context).get("timetable_term", -1);
		if (termValue != -1) {
			term = Term.values()[termValue];
			cb.setTerm(term);
			setTermTextViewText(term, context);
		} else {
			term = OApiUtil.getTerm();
			cb.setTerm(term);
		}
		adapter = new TimetableAdapter(context, R.layout.list_layout_timetable,
				mTimetableList, colorTable, cb);
		View wiseDialogLayout = View.inflate(context,
				R.layout.dialog_wise_input, null);
		idView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_id_input);
		passwdView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_passwd_input);
		termSpinner = (Spinner) wiseDialogLayout
				.findViewById(R.id.dialog_wise_spinner_term);
		loginDialog = new AlertDialog.Builder(context)
				.setTitle(R.string.tab_timetable_wise_login_title)
				.setView(wiseDialogLayout)
				.setPositiveButton(R.string.confirm, this)
				.setNegativeButton(R.string.cancel, this).create();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE: {
			String id = idView.getText().toString();
			Context context = getActivity();

			if (id.equals("123456789") && passwdView.length() < 1) {
				if (AppUtil.test) {
					AppUtil.test = false;
				} else {
					AppUtil.test = true;
					AppUtil.showToast(context, "test", isVisible());
				}
				PrefUtil.getInstance(context).put("test", AppUtil.test);
				clearText();
				return;
			}

			if (passwdView.length() < 1 || StringUtil.NULL.equals(id)) {
				AppUtil.showToast(context,
						R.string.tab_timetable_wise_login_warning_null, true);
				clearText();
				return;
			} else {
				excute();
			}
			break;
		}
		case DialogInterface.BUTTON_NEGATIVE: {
			clearPassWd();
			break;
		}
		default:
			break;
		}
	}

	private void clearText() {
		clearId();
		clearPassWd();
	}

	private void clearId() {
		if (idView != null && idView.length() > 0) {
			TextKeyListener.clear(idView.getText());
		}
	}

	private void clearPassWd() {
		if (passwdView != null && passwdView.length() > 0) {
			TextKeyListener.clear(passwdView.getText());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(IOUtil.FILE_TIMETABLE,
				(ArrayList<? extends Parcelable>) mTimetableList);
		outState.putSerializable("color", (Serializable) colorTable);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.tab_timetable, container, false);
		View emptyView = rootView.findViewById(R.id.tab_timetable_empty);
		emptyView.setOnClickListener(this);
		listView = (ListView) rootView.findViewById(R.id.time_table_listView1);
		listView.setEmptyView(emptyView);
		listView.setAdapter(adapter);

		setHolderTag(rootView);
		if (px > 0)
			setTitleViewSize(px);

		return rootView;
	}

	@Override
	public void onResume() {
		if (adapter.isEmpty()) {
			mIsOnLoad = true;
			excute();
		}
		super.onResume();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getView().getViewTreeObserver().addOnGlobalLayoutListener(viewObserver);
	}

	private ViewTreeObserver.OnGlobalLayoutListener viewObserver = new ViewTreeObserver.OnGlobalLayoutListener() {

		@Override
		public void onGlobalLayout() {
			if (listView != null) {
				int width = listView.getWidth();
				if (width > 0) {
					width = (width - 10) / NUM_OF_TIMETABLE_VIEWS;
					if (px != width) {
						px = width;
						setTitleViewSize(px);
						adapter.notifyDataSetChanged();
					}
				}
			}
		}
	};

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		View v = getView();
		if (v != null) {
			if (!isVisibleToUser) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					v.getViewTreeObserver().removeGlobalOnLayoutListener(
							viewObserver);
				} else {
					v.getViewTreeObserver().removeOnGlobalLayoutListener(
							viewObserver);
				}
			} else {
				v.getViewTreeObserver().addOnGlobalLayoutListener(viewObserver);
				setTitleViewSize(px);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_timetable, menu);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(termTextView);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_wise:
			if (isRunning()) {
				AppUtil.showToast(getActivity(), R.string.progress_ongoing,
						true);
			} else
				loginDialog.show();
			return true;
		case R.id.action_delete:
			if (deleteDialog == null)
				deleteDialog = getDeleteDialog();
			deleteDialog.show();
			return true;
		case R.id.action_save:
			saveTimetable();
			return true;
		default:
			return false;
		}
	}

	private void saveTimetable() {
		if (adapter.isEmpty()) {
			AppUtil.showToast(getActivity(), "시간표 정보가 없습니다.", true);
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(PrefUtil.getSaveRoute(getActivity())).append("timetable_")
				.append(OApiUtil.getYear()).append('_').append(term)
				.append('_').append(String.valueOf(System.currentTimeMillis()))
				.append(".png");
		String dir = sb.toString();
		ListViewBitmapWriteTask task = new ListViewBitmapWriteTask(
				getActivity(), dir, listView) {
			@Override
			public Bitmap getBitmap() {
				Bitmap capture = null, titleBitmap = null, bitmap = null;
				View title = null;
				try {
					bitmap = super.getBitmap();
					title = rootView.findViewById(R.id.tab_timetable_title);
					title.setDrawingCacheEnabled(true);
					title.buildDrawingCache(true);
					titleBitmap = title.getDrawingCache(true);
					if (titleBitmap == null)
						titleBitmap = GraphicUtil.createBitmapFromView(title);
					capture = GraphicUtil
							.getWholeListViewItemsToBitmap(listView);
					bitmap = GraphicUtil.merge(titleBitmap, capture);

					return bitmap;
				} finally {
					if (capture != null)
						capture.recycle();
					if (titleBitmap != null)
						titleBitmap.recycle();
					if (title != null) {
						title.destroyDrawingCache();
						title.setDrawingCacheEnabled(false);
					}
				}
			}
		};
		task.excute();
	}

	/**
	 * View를 찾기 쉽게하기위하여 메인 View에 '시간, 월 ~ 토' 를 나타내는 TextView를 등록한다.
	 */
	private void setHolderTag(View rootView) {
		for (int id : titleViewIds) {
			rootView.setTag(id, rootView.findViewById(id));
		}
	}

	/**
	 * '시간, 월 ~ 토' 를 나타내는 TextView의 너비를 설정한다.
	 */
	private void setTitleViewSize(int width) {
		for (int id : titleViewIds) {
			((TextView) rootView.getTag(id)).setWidth(width);
		}
	}

	@Override
	public void onTransactResult(ArrayList<TimeTableItem> result) {
		Context context = getActivity();
		if (result.isEmpty()) {
			if (mIsOnLoad) {
				mIsOnLoad = false;
			} else {
				AppUtil.showToast(context,
						R.string.tab_timetable_wise_login_warning_fail, true);
			}
			return;
		}
		mTimetableList.clear();
		mTimetableList.addAll(result);
		if (adapter != null)
			adapter.notifyDataSetChanged();

		setTermTextViewText(term, context);
	}

	private void setTermTextViewText(Term term, Context context) {
		if (termTextView != null)
			termTextView.setText(OApiUtil.getSemesterYear(term)
					+ " / "
					+ context.getResources().getStringArray(R.array.terms)[term
							.ordinal()]);
	}

	@Override
	protected void onTransactPostExcute() {
		super.onTransactPostExcute();
		clearPassWd();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<TimeTableItem> call() throws Exception {
		ArrayList<TimeTableItem> result;
		Context context = getActivity();
		// Fragment가 처음 Attach되었을 때, 파일에서 시간표을 읽어온다.
		if (mIsOnLoad) {
			result = (ArrayList<TimeTableItem>) readTimetable(context);
		} else { // 사용자가 WISE에 시간표 정보를 요청하였을 때
			term = Term.values()[termSpinner.getSelectedItemPosition()];
			String body = TimeTableHttpRequest.getHttpBodyPost(
					idView.getText(), passwdView.getText(), term);
			cb.setTerm(term);
			result = (ArrayList<TimeTableItem>) ParseFactory.create(
					ParseFactory.What.TimeTable, body, 0).parse();

			PrefUtil.getInstance(context).put("timetable_term", term.ordinal());
			TimeTableInfoCallback.clearAllAlarm(context);
			saveColorTable(context, makeColorTable(result));
		}

		// 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
		// 시간표의 과목과 과목의 색을 Mapping한다.
		if (!result.isEmpty()) {
			IOUtil.saveToFile(context, IOUtil.FILE_TIMETABLE,
					Activity.MODE_PRIVATE, result);
			colorTable.putAll(getColorTable(result, context));
		}
		return result;
	}

	@Override
	public void exceptionOccured(Exception e) {
		if (e instanceof ClientProtocolException
				|| e instanceof NullPointerException) {
			AppUtil.showToast(getActivity(),
					R.string.tab_timetable_wise_login_warning_fail,
					isMenuVisible());
		} else {
			super.exceptionOccured(e);
		}
	}

	/**
	 * 시간표 정보를 파일로부터 읽어온다.
	 * 
	 * @return 시간표 정보 list, 파일이 없다면 빈 list
	 */
	public static List<TimeTableItem> readTimetable(Context context) {
		List<TimeTableItem> list = IOUtil.readFromFileSuppressed(context,
				IOUtil.FILE_TIMETABLE);
		if (list == null) {
			list = new ArrayList<TimeTableItem>();
		}
		return list;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_timetable_empty:
			loginDialog.show();
			break;
		default:
			break;
		}
	}

	/**
	 * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 작성한다.
	 * 
	 * @param timetable
	 *            시간표
	 * @return 과목이름이 Key이고, Value가 컬러를 가리키는 Integer인 Map<br>
	 *         * 컬러는 단순한 정수이며, AppUtil을 통해 Color integer를 얻어와야 한다.
	 */
	public static Hashtable<String, Integer> makeColorTable(
			List<TimeTableItem> timetable) {
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		final int size = timetable.size();
		String name;
		TimeTableItem item;
		String[] array;
		int j = 0, h = 0;
		for (int i = 0; i < size; i++) {
			item = timetable.get(i);
			array = new String[] { item.mon, item.tue, item.wed, item.thr,
					item.fri, item.sat };
			for (h = 0; h < array.length; h++) {
				name = OApiUtil.getSubjectName(array[h]);
				if (!name.equals(StringUtil.NULL) && !name.equals(array[h])
						&& !table.containsKey(name)) {
					table.put(name, j++);
				}
			}
		}
		return table;
	}

	/**
	 * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 파일에서 읽어오거나 작성한다.
	 * 
	 * @param timetable
	 *            시간표
	 * @param context
	 * @return 시간표의 각 과목과 컬러를 mapping하는 Map
	 */
	public static Hashtable<String, Integer> getColorTable(
			List<TimeTableItem> timetable, Context context) {
		Hashtable<String, Integer> table = readColorTableFromFile(context);

		if (table == null || table.size() == 0) {
			table = makeColorTable(timetable);
			saveColorTable(context, table);
		}
		return table;
	}

	/**
	 * 주어진 시간표 컬러 Map을 파일로 저장한다.
	 * 
	 * @param colorTable
	 *            color map
	 * @param context
	 * */
	public static void saveColorTable(Context context,
			Hashtable<String, Integer> colorTable) {
		IOUtil.saveToFileAsync(context, IOUtil.FILE_COLOR_TABLE,
				Activity.MODE_PRIVATE, colorTable, null);
	}

	/**
	 * color map을 파일로 부터 읽어온다.
	 */
	public static Hashtable<String, Integer> readColorTableFromFile(
			Context context) {
		return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_COLOR_TABLE);
	}

	private AlertDialog getDeleteDialog() {
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.confirm_delete)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new AsyncLoader<Boolean>().excute(
										new Callable<Boolean>() {

											@Override
											public Boolean call()
													throws Exception {
												boolean b = getActivity()
														.deleteFile(
																IOUtil.FILE_TIMETABLE);
												if (b) {
													getActivity()
															.deleteFile(
																	IOUtil.FILE_COLOR_TABLE);
												}
												return b;
											}
										}, new OnTaskFinishedListener() {

											@Override
											public void onTaskFinished(
													boolean isExceptionOccoured,
													Object data) {
												boolean result = (Boolean) data;
												if (!isExceptionOccoured
														&& result) {
													adapter.clear();
													adapter.notifyDataSetChanged();
													mTimetableList.clear();
													Context context = getActivity();
													AppUtil.showToast(
															context,
															R.string.excute_delete,
															isVisible());
													TimeTableInfoCallback
															.clearAllAlarm(context);
													PrefUtil.getInstance(
															context).put(
															"timetable_term",
															-1);
													termTextView
															.setText(StringUtil.NULL);
												} else {
													AppUtil.showToast(
															getActivity(),
															R.string.file_not_found,
															isVisible());
												}
											}

										});
							}
						}).setNegativeButton(R.string.cancel, null).create();
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_wise);
	}
}
