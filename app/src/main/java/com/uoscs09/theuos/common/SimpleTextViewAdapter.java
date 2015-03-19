package com.uoscs09.theuos.common;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.uoscs09.theuos.base.AbsArrayAdapter;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.AppUtil.AppTheme;

import java.util.List;

/** TextView가 하나있고, icon이 붙은 layout의 Adapter를 제공하는 클래스. */
public class SimpleTextViewAdapter extends AbsArrayAdapter<Integer, Holder> {
	protected int textViewId;
	protected int textViewTextColor = 0;
	protected AppTheme iconTheme;
	protected DrawblePosition position;
	protected Rect drawableBound;
	protected boolean isDrawableForMenu = false;

	public enum DrawblePosition {
		TOP, BOTTOM, LEFT, RIGHT
	}

	public SimpleTextViewAdapter(Context context, int layout, List<Integer> list) {
		super(context, layout, list);
		this.textViewId = android.R.id.text1;
		this.position = DrawblePosition.LEFT;
		this.iconTheme = AppUtil.theme;
	}

	@Override
	public View setView(int position, View convertView, Holder holder) {
		int item = getItem(position);
		TextView tv = holder.tv;
		tv.setText(item);
		Drawable d;
		if (iconTheme != null) {
			d = getContext().getResources().getDrawable(
					AppUtil.getPageIcon(item, iconTheme));
		} else {
			if (isDrawableForMenu) {
				d = getContext().getResources().getDrawable(
						AppUtil.getPageIconForMenu(getContext(), item));
			} else {
				d = getContext().getResources().getDrawable(
						AppUtil.getPageIcon(getContext(), item));
			}
		}
		switch (this.position) {
		case TOP:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, d, null, null);
			} else
				tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
			break;
		case BOTTOM:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, null, null, d);
			} else
				tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
			break;
		case RIGHT:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, null, d, null);
			} else
				holder.tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d,
						null);
			break;
		default:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(d, null, null, null);
			} else
				holder.tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null,
						null);
			break;
		}
		holder.tv.setTextColor(textViewTextColor == 0 ? getContext().getResources()
				.getColor(
						AppUtil.getStyledValue(getContext(),
								android.R.attr.colorForeground))
				: textViewTextColor);
		return convertView;
	}

	@Override
	public Holder getViewHolder(View convertView) {
		return new Holder(convertView, textViewId);
	}


	/** SimpleTextviewAdater의 Builder class */
	public static class Builder {
		private final SimpleTextViewAdapter product;

		public Builder(Context context, int layout, List<Integer> list) {
			this.product = new SimpleTextViewAdapter(context, layout, list);
		}

		/** layout에 포함된 TextView의 id를 설정한다 */
		public Builder setTextViewId(int id) {
			product.textViewId = id;
			return this;
		}

		/** TextView의 icon의 위치를 설정한다. */
		public Builder setDrawablePosition(DrawblePosition position) {
			product.position = position;
			return this;
		}

		/** TextView의 icon의 크기를 설정한다. */
		public Builder setDrawableBounds(Rect bounds) {
			product.drawableBound = bounds;
			return this;
		}

		/**
		 * TextView의 icon의 테마를 설정한다.<br>
		 * <i>추후에 이 메소드를 삭제할 예정</i>
		 */
		@Deprecated
		public Builder setDrawableTheme(AppTheme theme) {
			product.iconTheme = theme;
			return this;
		}

		public Builder setTextViewTextColor(int color) {
			product.textViewTextColor = color;
			return this;
		}

		public Builder setDrawableForMenu(boolean forMenu) {
			product.isDrawableForMenu = forMenu;
			return this;
		}

		public ArrayAdapter<Integer> create() {
			return product;
		}
	}
}
class Holder implements AbsArrayAdapter.ViewHolder {
    public final TextView tv;

    public Holder(View v, int textViewId) {
        tv = (TextView) v.findViewById(textViewId);
    }
}
