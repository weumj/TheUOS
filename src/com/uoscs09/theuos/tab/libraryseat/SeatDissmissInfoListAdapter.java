package com.uoscs09.theuos.tab.libraryseat;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;

public class SeatDissmissInfoListAdapter extends AbsArrayAdapter<String> {
	private SeatDissmissInfoListAdapter(Context context) {
		super(context, 0);
	}

	public SeatDissmissInfoListAdapter(Context context, int layout,
			List<String> list) {
		super(context, layout, list);
	}

	@Override
	public View setView(int position, View v, AbsArrayAdapter.ViewHolder holder) {
		String[] strs = getItem(position).split("\\+");
		Holder h = (Holder) holder;
		h.texts[0].setText(strs[0]);
		h.texts[1].setText(strs[1]);
		if (AppUtil.theme != AppUtil.AppTheme.Black) {
			h.texts[0].setTextColor(Color.BLACK);
			h.texts[1].setTextColor(Color.BLACK);
		}
		return v;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	protected static class Holder implements ViewHolder {
		public TextView[] texts;

		public Holder(View v) {
			texts = new TextView[2];
			texts[0] = (TextView) v
					.findViewById(R.id.tab_libray_seat_info_time);
			texts[1] = (TextView) v
					.findViewById(R.id.tab_libray_seat_info_number);
		}
	}
}
