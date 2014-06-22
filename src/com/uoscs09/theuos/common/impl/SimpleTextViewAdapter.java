package com.uoscs09.theuos.common.impl;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

/** TextView가 하나있고, icon이 붙은 layout의 Adapter를 제공하는 클래스. */
public class SimpleTextViewAdapter extends AbsArrayAdapter<Integer> {
	protected int textViewId;
	protected AppTheme theme;
	protected DrawblePosition position;

	public enum DrawblePosition {
		TOP, BOTTOM, LEFT, RIGHT
	}

	public SimpleTextViewAdapter(Context context, int layout, List<Integer> list) {
		super(context, layout, list);
		this.textViewId = android.R.id.text1;
		this.theme = AppTheme.White;
		this.position = DrawblePosition.LEFT;
	}

	private SimpleTextViewAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		int item = getItem(position);

		h.tv.setText(item);
		Drawable d = getContext().getResources().getDrawable(
				AppUtil.getPageIcon(item, theme));
		switch (this.position) {
		case TOP:
			h.tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
			break;
		case BOTTOM:
			h.tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
			break;
		case LEFT:
			h.tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
			break;
		default:
			h.tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
			break;
		}
		switch (theme) {
		case BlackAndWhite:
		case White:
			h.tv.setTextColor(Color.BLACK);
			break;
		case Black:
		default:
			h.tv.setTextColor(Color.WHITE);
			break;
		}
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View convertView) {
		return new Holder(convertView);
	}

	private class Holder implements ViewHolder {
		public TextView tv;

		public Holder(View v) {
			tv = (TextView) v.findViewById(textViewId);
		}
	}

	/** SimpleTextviewAdater의 Builder class */
	public static class Builder {
		private SimpleTextViewAdapter product;

		public Builder(Context context, int layout, List<Integer> list) {
			this.product = new SimpleTextViewAdapter(context, layout, list);
		}

		/** layout에 포함된 TextView의 id를 설정한다 */
		public Builder setTextViewId(int id) {
			product.textViewId = id;
			return this;
		}

		/** AdapterView에서 표현될 Theme를 설정한다 */
		public Builder setTheme(AppTheme theme) {
			product.theme = theme;
			return this;
		}

		/** TextView의 icon의 위치를 설정한다. */
		public Builder setDrawablePosition(DrawblePosition position) {
			product.position = position;
			return this;
		}

		public ArrayAdapter<Integer> create() {
			return product;
		}
	}
}
