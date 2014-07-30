package com.uoscs09.theuos.widget.restaurant;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsListRemoteViewsFactory;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;

public class RestListService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new ListRemoteViewsFactory(this, intent);
	}

	private static class ListRemoteViewsFactory extends
			AbsListRemoteViewsFactory<RestItem> {
		private int position;

		@SuppressWarnings("unchecked")
		public ListRemoteViewsFactory(Context context, Intent intent) {
			super(context);
			clear();
			addAll(0,
					(Collection<? extends RestItem>) intent
							.getBundleExtra(RestWidget.REST_WIDGET_ITEM)
							.getParcelableArrayList(RestWidget.REST_WIDGET_ITEM));
			this.position = intent.getIntExtra(RestWidget.REST_WIDGET_POSITION,
					0);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public RemoteViews getViewAt(int position) {
			RemoteViews rv = new RemoteViews(getContext().getPackageName(),
					R.layout.list_layout_widget_rest);
			RestItem item = getItem(this.position);

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

		@SuppressWarnings("unchecked")
		@Override
		public void onDataSetChanged() {
			super.onDataSetChanged();
			clear();
			addAll(0,
					(ArrayList<? extends RestItem>) IOUtil
							.readFromFileSuppressed(getContext(),
									IOUtil.FILE_REST));
		}
	}

}
