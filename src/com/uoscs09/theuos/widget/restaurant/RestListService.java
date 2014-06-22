package com.uoscs09.theuos.widget.restaurant;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;

public class RestListService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
	}

	private static class ListRemoteViewsFactory implements RemoteViewsFactory {
		private List<RestItem> list;
		private Context mContext;

		public ListRemoteViewsFactory(Context context, Intent intent) {
			this.mContext = context;
			this.list = RestWidget.getList(context);
			// this.position =
			// intent.getIntExtra(RestWidget.REST_WIDGET_POSITION, 0);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public RemoteViews getViewAt(int position) {
			RemoteViews rv = new RemoteViews(mContext.getPackageName(),
					R.layout.list_layout_widget_rest);
			RestItem item;
			try {
				item = list.get(PrefUtil.getInstance(mContext).get(
						RestWidget.REST_WIDGET_POSITION, 0));
			} catch (Exception e) {
				item = list.get(0);
				PrefUtil.getInstance(mContext).put(
						RestWidget.REST_WIDGET_POSITION, 0);
			}
			switch (position) {
			case 0:
				rv.setTextViewText(R.id.widget_rest_title, "아침");
				rv.setTextViewText(R.id.widget_rest_content, item.breakfast);
				break;
			case 1:
				rv.setTextViewText(R.id.widget_rest_title, "점심");
				rv.setTextViewText(R.id.widget_rest_content, item.lunch);
				break;
			case 2:
				rv.setTextViewText(R.id.widget_rest_title, "저녁");
				rv.setTextViewText(R.id.widget_rest_content, item.supper);
				break;
			default:
				break;
			}
			return rv;
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
			list = RestWidget.getList(mContext);
		}

		@Override
		public void onDestroy() {
		}

	}

}
