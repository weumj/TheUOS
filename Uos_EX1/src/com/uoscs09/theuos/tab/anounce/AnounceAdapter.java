package com.uoscs09.theuos.tab.anounce;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

public class AnounceAdapter extends AbsArrayAdapter<AnounceItem> {
	private AnounceAdapter(Context context) {
		super(context, 0);
	}

	public AnounceAdapter(Context context, int layout, List<AnounceItem> list) {
		super(context, layout, list);
	}

	@Override
	public View setView(int position, View v, ViewHolder holder) {
		AnounceItem item = getItem(position);
		Holder h = (Holder) holder;
		Spanned span = Html.fromHtml(item.type);
		h.textArray[0].setText(span == null ? span : item.type);
		h.textArray[1]
				.setTextColor(AppUtil.theme == AppTheme.Black ? Color.WHITE
						: Color.BLACK);
		span = Html.fromHtml(item.title);
		h.textArray[1].setText(span == null ? span : item.title);
		span = Html.fromHtml(item.date);
		h.textArray[2].setText(span == null ? span : item.date);
		return v;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	protected static class Holder implements ViewHolder {
		public TextView[] textArray;

		public Holder(View v) {
			textArray = new TextView[3];
			textArray[0] = (TextView) v.findViewById(R.id.tab_anounce_list_text_type);
			textArray[1] = (TextView) v
					.findViewById(R.id.tab_anounce_list_text_title);
			textArray[2] = (TextView) v.findViewById(R.id.tab_anounce_list_text_date);
		}
	}
}
