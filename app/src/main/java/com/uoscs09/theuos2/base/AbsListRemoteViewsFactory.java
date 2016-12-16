package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collection View 를 사용하는 AppWidget을 위한 RemoteViewsFactory를 대략 구현한 것
 *
 * @see RemoteViews
 * @see RemoteViewsFactory
 * @see RemoteViewsService
 */
public abstract class AbsListRemoteViewsFactory<T> implements RemoteViewsFactory {
    private Context mContext;
    private List<T> mDataList = new ArrayList<>();
    private final int mAppWidgetId;

    public AbsListRemoteViewsFactory(Context context, Intent intent) {
        this.mContext = context;
        this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    protected int getAppWidgetId() {
        return mAppWidgetId;
    }

    protected boolean addAll(Collection<? extends T> collection) {
        return collection != null && mDataList.addAll(collection);
    }

    protected void clear() {
        mDataList.clear();
    }

    protected Context getContext() {
        return mContext;
    }

    protected T getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        mContext = null;
        mDataList = null;
    }

    @Override
    public int getCount() {
        return mDataList.size();
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
