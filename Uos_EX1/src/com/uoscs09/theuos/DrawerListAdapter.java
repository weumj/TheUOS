package com.uoscs09.theuos;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;

public class DrawerListAdapter extends AbsArrayAdapter<Integer> {
	private final int width;
	private final int height;

	private DrawerListAdapter(Context context) {
		super(context, 0);
		final float density = context.getResources().getDisplayMetrics().density;
		width = Math.round(30 * density);
		height = Math.round(28 * density);
	}

	public DrawerListAdapter(Context context, int layout, List<Integer> list) {
		super(context, layout, list);
		final float density = context.getResources().getDisplayMetrics().density;
		width = Math.round(30 * density);
		height = Math.round(28 * density);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		int item = getItem(position);
		Context context = getContext();

		final Drawable drawable = context.getResources().getDrawable(
				AppUtil.getPageIcon(item));

		drawable.setBounds(0, 0, width, height);
		h.textView.setText(item);
		h.textView.setCompoundDrawables(drawable, null, null, null);
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View convertView) {
		return new Holder(convertView);
	}

	protected static class Holder implements ViewHolder {
		public TextView textView;

		public Holder(View v) {
			textView = (TextView) v.findViewById(android.R.id.text1);
		}
	}
}
