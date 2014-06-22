package com.uoscs09.theuos.setting;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

/** DragAndDrop ListView에 붙는 adapter */
public class OrderListAdapter extends ArrayAdapter<Integer> {
	private int layout;
	private final int width;
	private final int height;

	public OrderListAdapter(Context context, int resource, List<Integer> objects) {
		super(context.getApplicationContext(), resource, objects);
		this.layout = resource;
		final float density = context.getResources().getDisplayMetrics().density;
		width = Math.round(30 * density);
		height = Math.round(28 * density);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		Context context = getContext();
		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			textView = (TextView) convertView.findViewById(R.id.setting_order_list_text_tab_title);
			convertView.setTag(textView);
		} else {
			textView = (TextView) convertView.getTag();
		}
		int item = getItem(position);
		textView.setText(item);
		if (AppUtil.theme == AppTheme.White
				|| AppUtil.theme == AppTheme.BlackAndWhite) {
			textView.setTextColor(Color.BLACK);
		}
		final Drawable drawable;
		// 야매 코드
		if (AppUtil.theme == AppTheme.BlackAndWhite) {
			AppUtil.theme = AppTheme.White;
			drawable = context.getResources().getDrawable(
					AppUtil.getPageIcon(item));
			AppUtil.theme = AppTheme.BlackAndWhite;
		} else {
			drawable = context.getResources().getDrawable(
					AppUtil.getPageIcon(item));
		}
		drawable.setBounds(0, 0, width, height);
		textView.setText(item);
		textView.setCompoundDrawables(drawable, null, null, null);
		return convertView;
	}

}
