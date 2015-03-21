package com.uoscs09.theuos2.widget.libraryseat;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsListRemoteViewsFactory;
import com.uoscs09.theuos2.tab.libraryseat.SeatItem;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.Collection;
import java.util.List;

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
			int vacancySeatCount;
			if (item.vacancySeat.trim().equals("")) {
				vacancySeatCount = 0;
			} else {
				try {
					vacancySeatCount = Integer.valueOf(item.vacancySeat.trim());
				} catch (Exception e) {
					vacancySeatCount = 0;
				}
			}
			int occupySeatCount;
			if (item.occupySeat.trim().equals("")) {
				occupySeatCount = 0;
			} else {
				try {
					occupySeatCount = Integer.valueOf(item.occupySeat.trim());
				} catch (Exception e) {
					occupySeatCount = 0;
				}
			}
			int size = vacancySeatCount + occupySeatCount;
			rv.setTextViewText(android.R.id.text2, item.vacancySeat);
			rv.setTextViewText(android.R.id.summary, "/" + size);
			int color;
			try {
				color = Float.valueOf(item.utilizationRate) < 50 ? R.color.material_green_200
						: R.color.material_red_200;
			} catch (Exception e) {
				color = R.color.material_green_200;
			}
			rv.setInt(android.R.id.content, "setBackgroundResource", color);

			//Bundle extras = new Bundle();
			//extras.putSerializable(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA,
			//		(Serializable) item);
			//Intent fillInIntent = new Intent();
			//fillInIntent
			//		.setAction(LibrarySeatWidget.LIBRARY_SEAT_WIDGET_ACTIVITY);
			//fillInIntent.putExtras(extras);
			//rv.setOnClickFillInIntent(android.R.id.widget_frame, fillInIntent);

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
