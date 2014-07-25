package com.uoscs09.theuos.tab.timetable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.TextKeyListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos.common.impl.AbsAsyncFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.GraphicUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.TimeTableHttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabTimeTableFragment extends
		AbsAsyncFragment<ArrayList<TimeTableItem>> implements
		DialogInterface.OnClickListener, View.OnClickListener {
	private AlertDialog loginDialog;
	private List<TimeTableItem> timetable_list;
	protected ArrayAdapter<TimeTableItem> adapter;
	private ProgressDialog progress;
	protected View rootView;
	protected EditText idView, passwdView;
	private AlertDialog deleteDialog;
	protected ListView listView;
	private int[] titleViewIds = new int[] { R.id.tab_time_peroid,
			R.id.tab_time_mon, R.id.tab_time_tue, R.id.tab_time_wed,
			R.id.tab_time_thur, R.id.tab_time_fri, R.id.tab_time_sat, };
	public final static int NUM_OF_TIMETABLE_VIEWS = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		Context context = getActivity();
		if (savedInstanceState != null) {
			timetable_list = savedInstanceState
					.getParcelableArrayList(AppUtil.FILE_TIMETABLE);
		} else {
			timetable_list = readTimetable(context);
		}
		switch (AppUtil.theme) {
		case Black:
			adapter = new TimetableAdapter(context,
					R.layout.list_layout_timetable_dark, timetable_list,
					new TimeTableInfoCallback(context));
			break;
		case BlackAndWhite:
		case White:
		default:
			adapter = new TimetableAdapter(context,
					R.layout.list_layout_timetable, timetable_list,
					new TimeTableInfoCallback(context));
			break;
		}

		progress = AppUtil.getProgressDialog(context, false,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelExecutor();
					}
				});

		final RelativeLayout wiseDialogLayout = (RelativeLayout) View.inflate(
				context, R.layout.dialog_wise_input, null);
		idView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_id_input);
		passwdView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_passwd_input);

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
		if (idView.length() > 0) {
			TextKeyListener.clear(idView.getText());
		}
	}

	private void clearPassWd() {
		if (passwdView.length() > 0) {
			TextKeyListener.clear(passwdView.getText());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(AppUtil.FILE_TIMETABLE,
				(ArrayList<? extends Parcelable>) timetable_list);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		switch (AppUtil.theme) {
		case Black:
			rootView = inflater.inflate(R.layout.tab_timetable_dark, container,
					false);
			break;
		case BlackAndWhite:
		case White:
		default:
			rootView = inflater.inflate(R.layout.tab_timetable, container,
					false);
			break;
		}

		View emptyView = rootView.findViewById(R.id.tab_timetable_empty);
		emptyView.setOnClickListener(this);
		listView = (ListView) rootView.findViewById(R.id.time_table_listView1);
		listView.setEmptyView(emptyView);
		listView.setAdapter(adapter);
		setHolderTag(rootView);

		return rootView;
	}

	@Override
	public void onResume() {
		setTitleViewSize(getResources().getDisplayMetrics().widthPixels / 7);
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.tab_timetable_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_timetable, menu);
			break;
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_wise:
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

	private AlertDialog getDeleteDialog() {
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.confirm_delete)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Context context = getActivity();
								if (context.deleteFile(AppUtil.FILE_TIMETABLE)) {
									adapter.clear();
									adapter.notifyDataSetChanged();
									context.deleteFile(AppUtil.FILE_COLOR_TABLE);
									AppUtil.showToast(context,
											R.string.excute_delete, isVisible());
									TimeTableInfoCallback
											.clearAllAlarm(context);
								} else {
									AppUtil.showToast(context,
											R.string.file_not_found,
											isVisible());
								}
							}
						}).setNegativeButton(R.string.cancel, null).create();
	}

	private void saveTimetable() {
		StringBuilder sb = new StringBuilder();
		sb.append(PrefUtil.getSaveRoute(getActivity())).append("timetable_")
				.append(OApiUtil.getYear()).append('_')
				.append(OApiUtil.getTerm()).append('_')
				.append(String.valueOf(System.currentTimeMillis()))
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				newConfig.screenWidthDp, getResources().getDisplayMetrics())
				/ NUM_OF_TIMETABLE_VIEWS;
		setTitleViewSize(px);
		super.onConfigurationChanged(newConfig);
	}

	private void setHolderTag(View rootView) {
		for (int id : titleViewIds) {
			rootView.setTag(id, rootView.findViewById(id));
		}
	}

	private void setTitleViewSize(int width) {
		for (int id : titleViewIds) {
			((TextView) rootView.getTag(id)).setWidth(width);
		}
	}

	@Override
	public void onResult(ArrayList<TimeTableItem> result) {
		Context context = getActivity();
		if (result.size() == 0) {
			AppUtil.showToast(context,
					R.string.tab_timetable_wise_login_warning_fail, true);
			return;
		}
		AppUtil.saveToFile(context, AppUtil.FILE_TIMETABLE,
				Activity.MODE_PRIVATE, result);
		readColorTableFromFile(context);
		adapter.clear();
		timetable_list = result;
		adapter.addAll(result);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onPostExcute() {
		progress.dismiss();
	}

	@Override
	protected void excute() {
		progress.show();
		super.excute();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<TimeTableItem> call() throws Exception {
		String body = TimeTableHttpRequest.getHttpBodyPost(idView.getText()
				.toString(), passwdView.getText());
		ArrayList<TimeTableItem> result = (ArrayList<TimeTableItem>) ParseFactory
				.create(ParseFactory.TIMETABLE, body, 0).parse();
		getColorTable(result, getActivity());
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

	public static List<TimeTableItem> readTimetable(Context context) {
		List<TimeTableItem> list = AppUtil.readFromFile(context,
				AppUtil.FILE_TIMETABLE);
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

	public static Hashtable<String, Integer> getColorTable(
			List<TimeTableItem> list, Context context) {
		Hashtable<String, Integer> table = readColorTableFromFile(context);

		if (table == null || table.size() == 0) {
			table = new Hashtable<String, Integer>();
			int size = list.size();
			String name;
			TimeTableItem item;
			String[] array;
			int j = 0, h = 0;
			for (int i = 0; i < size; i++) {
				item = list.get(i);
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
			saveColorTable(context, table);
		}
		return table;
	}

	public static boolean saveColorTable(Context context,
			Hashtable<String, Integer> colorTable) {
		return AppUtil.saveToFile(context, AppUtil.FILE_COLOR_TABLE,
				Activity.MODE_PRIVATE, colorTable);
	}

	public static Hashtable<String, Integer> readColorTableFromFile(
			Context context) {
		return AppUtil.readFromFile(context, AppUtil.FILE_COLOR_TABLE);
	}

}
