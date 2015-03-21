package com.uoscs09.theuos2.base;

import android.content.Context;
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
public abstract class AbsListRemoteViewsFactory<T> implements
		RemoteViewsFactory {
	private final Context mContext;
	private final List<T> mDataList = new ArrayList<>();

	public AbsListRemoteViewsFactory(Context context) {
		this.mContext = context;
	}

	protected boolean addAll(int position, Collection<? extends T> collection) {
		return mDataList.addAll(collection);
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
