package com.uoscs09.theuos2.appwidget.restaurant;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.util.AppRequests;

public class RestListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this, intent);
    }

    private static class ListRemoteViewsFactory implements RemoteViewsFactory {
        private int mRestPosition;
        private SparseArray<RestItem> mTable;
        private Context context;

        ListRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.mRestPosition = intent.getIntExtra(RestWidget.REST_WIDGET_POSITION, 0);
        }

        @Override
        public void onCreate() {
            onDataSetChanged();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_layout_widget_rest);

            RestItem item = null;
            if (mTable != null) {
                item = mTable.get(mRestPosition);
            }
            if (item == null)
                item = RestItem.EMPTY;

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
        public int getCount() {
            return 3;
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

        @Override
        public void onDestroy() {
            context = null;
            mTable = null;
        }


        @Override
        public void onDataSetChanged() {
            AppRequests.Restaurants.request(false)
                    .subscribe(result -> this.mTable = result);
        }


    }

}
