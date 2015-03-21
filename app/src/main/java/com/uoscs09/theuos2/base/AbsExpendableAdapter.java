package com.uoscs09.theuos2.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.List;
import java.util.Map;

public abstract class AbsExpendableAdapter<K, V> extends BaseExpandableListAdapter {
	private final Map<K, ? extends List<V>> mGroupData;
	private final int mGroupLayout;
	private final int mChildLayout;
	private K[] mKeys;
	private final Context mContext;

	public AbsExpendableAdapter(Context context, int grouplayout,			int childLayout, Map<K, ? extends List<V>> data) {
		this.mChildLayout = childLayout;
		this.mGroupData = data;
		this.mGroupLayout = grouplayout;
		this.mKeys = (K[]) data.keySet().toArray();
		this.mContext = context;
	}

	protected Context getContext() {
		return mContext;
	}

	@Override
	public int getGroupCount() {
		return mGroupData.size();
	}

	protected Object getGroupKey(int groupPosition) {
		return mKeys[groupPosition];
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroupData.get(mKeys[groupPosition]).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupData.get(mKeys[groupPosition]);
	}

	@Override
	public V getChild(int groupPosition, int childPosition) {
		return mGroupData.get(mKeys[groupPosition]).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,			View convertView, ViewGroup parent) {
		ViewHolder h;
		if (convertView == null) {
			convertView = View.inflate(mContext, mGroupLayout, null);
			h = getViewHolder(convertView, false);
			convertView.setTag(h);
		} else {
			h = (ViewHolder) convertView.getTag();
		}

		setGroupView(groupPosition, isExpanded, convertView, parent, h);
		return convertView;
	}

	protected abstract void setGroupView(int groupPosition, boolean isExpanded,	View v, ViewGroup parent, ViewHolder h);

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder h;
		if (convertView == null) {
			convertView = View.inflate(mContext, mChildLayout, null);
			h = getViewHolder(convertView, true);
			convertView.setTag(h);
		} else {
			h = (ViewHolder) convertView.getTag();
		}
		setChildView(groupPosition, childPosition, isLastChild, convertView,
				parent, h);
		return convertView;
	}

	protected abstract void setChildView(int groupPosition, int childPosition,
			boolean isLastChild, View v, ViewGroup parent, ViewHolder h);

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	protected abstract ViewHolder getViewHolder(View v, boolean isChild);

	@Override
	public void notifyDataSetChanged() {
		this.mKeys = (K[]) mGroupData.keySet().toArray();
		super.notifyDataSetChanged();
	}

	protected static interface ViewHolder {
	}

}
