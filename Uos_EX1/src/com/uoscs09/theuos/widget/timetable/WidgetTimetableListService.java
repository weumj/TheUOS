package com.uoscs09.theuos.widget.timetable;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.timetable.TabTimeTableFragment;
import com.uoscs09.theuos.tab.timetable.TimeTableItem;

public abstract class WidgetTimetableListService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return getListRemoteViewsFactory(this.getApplicationContext(), intent);
	}

	protected abstract ListRemoteViewsFactory getListRemoteViewsFactory(
			Context context, Intent intent);

	protected abstract class ListRemoteViewsFactory implements
			RemoteViewsService.RemoteViewsFactory {
		private List<TimeTableItem> mWidgetItems;
		private Context mContext;
		private int mAppWidgetId;
		private Hashtable<String, Integer> colorTable;
		private final int[] viewIds = { R.id.widget_time_table_list_peroid,
				R.id.widget_time_table_list_mon,
				R.id.widget_time_table_list_tue,
				R.id.widget_time_table_list_wed,
				R.id.widget_time_table_list_thr,
				R.id.widget_time_table_list_fri, };

		public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
			this.mContext = applicationContext;
			this.mAppWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		@Override
		public int getCount() {
			if (mWidgetItems == null)
				getData();
			int showingSize = PrefUtil.getInstance(mContext).get(
					PrefUtil.KEY_TIMETABLE_LIMIT, PrefUtil.TIMETABLE_LIMIT_MAX);

			return Math.min(mWidgetItems.size(), showingSize);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		protected abstract boolean isBigSize();

		@Override
		public RemoteViews getViewAt(int position) {
			TimeTableItem item = mWidgetItems.get(position);
			RemoteViews views;

			if (isBigSize()) {
				views = new RemoteViews(mContext.getPackageName(),
						R.layout.list_layout_widget_timetable_5x4);
			} else {
				views = new RemoteViews(mContext.getPackageName(),
						R.layout.list_layout_widget_timetable_4x4);
			}

			String[] arr = { item.time, item.mon, item.tue, item.wed, item.thr,
					item.fri };

			views.setTextViewText(viewIds[0], arr[0].split("\n\n")[0]);

			if (colorTable == null) {
				getData();
			}
			Integer idx;
			TimeTableItem upperItem;
			if (position != 0) {
				upperItem = mWidgetItems.get(position - 1);
			} else {
				upperItem = new TimeTableItem();
			}
			String[] upperArray = { null, upperItem.mon, upperItem.tue,
					upperItem.wed, upperItem.thr, upperItem.fri };

			int id;
			for (int i = 1; i < viewIds.length; i++) {
				id = viewIds[i];
				views.setTextColor(id, Color.WHITE);
				// views.setInt(id, "setWidth", width);
				if (OApiUtil.getSubjectName(upperArray[i]).equals(
						OApiUtil.getSubjectName(arr[i]))) {
					views.setTextViewText(id, StringUtil.NULL);
				} else {
					views.setTextViewText(id, removeTimetableProf(arr[i]));
				}

				idx = colorTable.get(OApiUtil.getSubjectName(arr[i]));
				if (idx != null) {
					views.setInt(id, "setBackgroundResource",
							AppUtil.getColor(idx));
				} else {
					views.setInt(id, "setBackgroundResource", 0);
				}
			}

			int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
			if (day > 0 && day < viewIds.length) {
				views.setTextColor(viewIds[day], Color.BLACK);
			}
			PrefUtil pref = PrefUtil.getInstance(mContext);
			if (pref.get(TimeTableWidget.WIDGET_TIMETABLE_DAY, 0) != day) {
				pref.put(TimeTableWidget.WIDGET_TIMETABLE_DAY, day);
				mContext.sendBroadcast(new Intent(
						AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
						AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
			}
			return views;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public void onCreate() {
		}

		@Override
		public void onDataSetChanged() {
			getData();
		}

		@Override
		public void onDestroy() {
		}

		private void getData() {
			mWidgetItems = TabTimeTableFragment.readTimetable(mContext);
			colorTable = TabTimeTableFragment.getColorTable(mWidgetItems,
					mContext);
		}

		private String removeTimetableProf(String timetable) {
			String[] arr = timetable.trim().split(StringUtil.NEW_LINE);
			if (arr.length > 3) {
				return arr[0] + StringUtil.NEW_LINE + arr[2]
						+ StringUtil.NEW_LINE + arr[3];
			} else {
				return timetable;
			}
		}
	}
}
