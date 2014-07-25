package com.uoscs09.theuos.widget.libraryseat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.tab.libraryseat.SeatItem;

public class LibrarySeatListService extends RemoteViewsService {
	private ListRemoteViewsFactory mFactory;

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		mFactory = new ListRemoteViewsFactory(getApplicationContext(), intent);
		return mFactory;
	}

	private static class ListRemoteViewsFactory implements RemoteViewsFactory {
		private Context mContext;
		protected List<SeatItem> mDataList = new ArrayList<SeatItem>();

		public ListRemoteViewsFactory(Context context, Intent intent) {
			this.mContext = context;
			setList(intent);
		}

		private void setList(Intent intent) {
			List<SeatItem> extraList = intent.getBundleExtra(
					LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA)
					.getParcelableArrayList(
							LibrarySeatWidget.LIBRARY_SEAT_WIDGET_DATA);
			if (!extraList.isEmpty()) {
				mDataList.clear();
				mDataList.addAll(extraList);
			}

		}

		@Override
		public void onCreate() {
		}

		@Override
		public void onDataSetChanged() {
		}

		@Override
		public void onDestroy() {
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public RemoteViews getViewAt(int position) {
			SeatItem item = mDataList.get(position);
			RemoteViews rv = new RemoteViews(mContext.getPackageName(),
					R.layout.list_layout_widget_library_seat);
			rv.setTextViewText(android.R.id.text1, item.roomName);
			if (Double.parseDouble(item.utilizationRate) < 50d) {
				rv.setInt(android.R.id.text2, "setBackgroundResource",
						android.R.color.holo_green_light);
			} else {
				rv.setInt(android.R.id.text2, "setBackgroundResource",
						android.R.color.holo_red_light);
			}
			int size = Integer.valueOf(item.vacancySeat.trim())
					+ Integer.valueOf(item.occupySeat.trim());
			rv.setTextViewText(android.R.id.text2, item.vacancySeat + "/"
					+ size);
			return rv;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

	}
}
