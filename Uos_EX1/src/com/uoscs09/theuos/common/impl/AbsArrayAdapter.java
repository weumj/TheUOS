package com.uoscs09.theuos.common.impl;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/** View Holder 패턴을 사용하는 ArrayAdapter */
public abstract class AbsArrayAdapter<T> extends ArrayAdapter<T> {
	protected int layout;

	public AbsArrayAdapter(Context context, int layout, List<T> list) {
		super(context.getApplicationContext(), layout, list);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, T[] array) {
		super(context.getApplicationContext(), layout, array);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout) {
		super(context.getApplicationContext(), layout);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, int textViewID) {
		super(context.getApplicationContext(), layout, textViewID);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, int textViewID,
			List<T> list) {
		super(context.getApplicationContext(), layout, textViewID, list);
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = View.inflate(getContext(), layout, null);
			holder = getViewHolder(view);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		return setView(position, view, holder);
	}

	/**
	 * getView 의 역할을 하는 메소드이다.
	 * 
	 * @param position
	 *            리스트의 위치
	 * @param convertView
	 *            설정 하려는 View
	 * @param holder
	 *            getView에서 설정되는 ViewHolder
	 * @return convertView - 반드시 설정한 convertView를 반환해야한다.
	 */
	public abstract View setView(int position, View convertView,
			ViewHolder holder);

	/**
	 * @param view
	 *            ViewHolder 객체를 만드는데 사용될 View
	 * @return AbsArrayAdapter를 상속받은 클래스가<br>
	 *         구현한 ViewHolder
	 */
	public abstract ViewHolder getViewHolder(View convertView);

	public static interface ViewHolder {
	}
}
