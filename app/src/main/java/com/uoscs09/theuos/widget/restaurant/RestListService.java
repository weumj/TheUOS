package com.uoscs09.theuos.widget.restaurant;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsListRemoteViewsFactory;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;

import java.util.ArrayList;

public class RestListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this, intent);
    }

    private static class ListRemoteViewsFactory extends AbsListRemoteViewsFactory<RestItem> {
        private int position;

        public ListRemoteViewsFactory(Context context, Intent intent) {
            super(context);
            clear();
            ArrayList<RestItem> list = intent.getBundleExtra(RestWidget.REST_WIDGET_ITEM)
                    .getParcelableArrayList(RestWidget.REST_WIDGET_ITEM);

            addAll(0, list);
            this.position = intent.getIntExtra(RestWidget.REST_WIDGET_POSITION, 0);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Context context = getContext();
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_layout_widget_rest);
            RestItem item;
            try {
                item = getItem(this.position);
            } catch (Exception e) {
                this.position = 0;
                item = getItem(this.position);
            }
            switch (position) {
                case 0:
                    rv.setTextViewText(R.id.widget_rest_title, context.getString(R.string.tab_rest_breakfast));
                    rv.setTextViewText(R.id.widget_rest_content, item.breakfast);
                    break;
                case 1:
                    rv.setTextViewText(R.id.widget_rest_title, context.getString(R.string.tab_rest_lunch));
                    rv.setTextViewText(R.id.widget_rest_content, item.lunch);
                    break;
                case 2:
                    rv.setTextViewText(R.id.widget_rest_title, context.getString(R.string.tab_rest_dinner));
                    rv.setTextViewText(R.id.widget_rest_content, item.supper);
                    break;
                default:
                    break;
            }
            return rv;
        }

        @Override
        public void onDataSetChanged() {
            super.onDataSetChanged();
            clear();
            ArrayList<RestItem> list = IOUtil.readFromFileSuppressed(getContext(), IOUtil.FILE_REST);
            addAll(0, list);
        }
    }

}
