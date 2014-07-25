package com.uoscs09.theuos.tab.timetable;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.StringUtil;

public class TimetableAdapter extends AbsArrayAdapter<TimeTableItem> {
	private OnClickListener l;
	private Hashtable<String, Integer> colorTable;
	private List<TimeTableItem> list;

	private TimetableAdapter(Context context) {
		super(context, 0);
	}

	public TimetableAdapter(Context context, int layout,
			List<TimeTableItem> list) {
		super(context, layout, list);
		l = null;
		this.list = list;
		initColorInfo(context);
	}

	public TimetableAdapter(Context context, int layout,
			List<TimeTableItem> list, View.OnClickListener l) {
		super(context, layout, list);
		this.l = l;
		this.list = list;
		initColorInfo(context);
	}

	private void initColorInfo(Context context) {
		colorTable = TabTimeTableFragment.getColorTable(list, context);
	}

	@Override
	public int getCount() {
		int limit = AppUtil.timetable_limit;
		return Math.min(list.size(), limit);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Context context = getContext();

		int width = context.getResources().getDisplayMetrics().widthPixels
				/ TabTimeTableFragment.NUM_OF_TIMETABLE_VIEWS;
		ViewWrapper w = (ViewWrapper) holder;
		TimeTableItem item = getItem(position);

		String[] strArray = { item.time, item.mon, item.tue, item.wed,
				item.thr, item.fri, item.sat };

		w.textArray[0]
				.setText(strArray[0].replace("\n\n", StringUtil.NEW_LINE));
		w.textArray[0].setWidth(width);
		int baseSelector;
		switch (AppUtil.theme) {
		case Black:
			baseSelector = R.drawable.selector_button_dark;
			break;
		case BlackAndWhite:
		case White:
		default:
			baseSelector = R.drawable.selector_button;
			break;
		}

		Integer idx;
		int color;
		TimeTableItem upperItem;
		if (position != 0) {
			upperItem = getItem(position - 1);
		} else {
			upperItem = new TimeTableItem();
		}
		String[] upperArray = { null, upperItem.mon, upperItem.tue,
				upperItem.wed, upperItem.thr, upperItem.fri, upperItem.sat };

		for (int i = 1; i < TabTimeTableFragment.NUM_OF_TIMETABLE_VIEWS; i++) {
			if (OApiUtil.getSubjectName(upperArray[i]).equals(
					OApiUtil.getSubjectName(strArray[i]))) {
				w.textArray[i].setText(StringUtil.NULL);
			} else {
				w.textArray[i].setText(OApiUtil
						.getCompressedString(strArray[i]).trim());
			}

			w.textArray[i].setWidth(width);
			w.textArray[i].setTextColor(Color.BLACK);
			w.textArray[i].setTag(strArray[i] + StringUtil.NEW_LINE + position
					+ StringUtil.NEW_LINE + i);
			if (colorTable == null)
				initColorInfo(getContext());
			idx = colorTable.get(OApiUtil.getSubjectName(strArray[i]));
			if (idx != null) {
				color = AppUtil.getColor(idx);
				if (color != -1) {
					w.textArray[i].setBackgroundResource(color);
					w.textArray[i].setOnClickListener(l);
					color = -1;
				}
				idx = null;
			} else {
				w.textArray[i].setOnClickListener(null);
				w.textArray[i].setBackgroundResource(baseSelector);
			}
		}
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View view) {
		return new ViewWrapper(view);
	}

	protected class ViewWrapper implements ViewHolder {
		public TextView[] textArray;

		public ViewWrapper(View convertView) {
			textArray = new TextView[7];

			textArray[0] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_peroid);
			textArray[1] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_mon);
			textArray[2] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_tue);
			textArray[3] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_wed);
			textArray[4] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_thr);
			textArray[5] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_fri);
			textArray[6] = (TextView) convertView
					.findViewById(R.id.tab_timetable_list_text_sat);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		initColorInfo(getContext());
	}
}
