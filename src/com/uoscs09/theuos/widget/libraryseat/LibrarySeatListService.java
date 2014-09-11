package com.uoscs09.theuos.widget.libraryseat;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsListRemoteViewsFactory;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.libraryseat.SeatItem;

public class LibrarySeatListService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new ListRemoteViewsFactory(this, intent);
	}

	private static class ListRemoteViewsFactory extends
			AbsListRemoteViewsFactory<SeatItem> {

		public ListRemoteViewsFactory(Context context, Intent intent) {
			super(context);
			List<SeatItem> extraList = intent.getBundleExtra(
					LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA)
					.getParcelableArrayList(
							LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA);
			if (!extraList.isEmpty()) {
				clear();
				addAll(0, extraList);
			}
		}

		@Override
		public RemoteViews getViewAt(int position) {
			SeatItem item = getItem(position);
			RemoteViews rv = new RemoteViews(getContext().getPackageName(),
					R.layout.list_layout_widget_library_seat);
			String room = item.roomName;
			if (room.contains("전문"))
				room = room.replace("전문", StringUtil.NULL);
			rv.setTextViewText(android.R.id.text1, room);
			int size = Integer.valueOf(item.vacancySeat.trim())
					+ Integer.valueOf(item.occupySeat.trim());
			rv.setTextViewText(android.R.id.text2, item.vacancySeat);
			rv.setTextViewText(android.R.id.summary, "/" + size);
			int color;
			try {
				color = Float.valueOf(item.utilizationRate) < 50 ? android.R.color.holo_green_light
						: android.R.color.holo_red_light;
			} catch (Exception e) {
				color = android.R.color.holo_green_light;
			}
			rv.setInt(android.R.id.content, "setBackgroundResource", color);
			return rv;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onDataSetChanged() {
			super.onDataSetChanged();
			clear();
			addAll(0,
					(Collection<? extends SeatItem>) IOUtil
							.readFromFileSuppressed(getContext(),
									IOUtil.FILE_LIBRARY_SEAT));
		}
	}
}
