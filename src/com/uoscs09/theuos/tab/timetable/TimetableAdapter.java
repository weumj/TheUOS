package com.uoscs09.theuos.tab.timetable;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
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
	private Map<String, Integer> colorTable;
	private List<TimeTableItem> list;
	private static int BACKGROUND_RES;
	private static final int[] SELECTOR_REF = { R.attr.selector_button };

	private TimetableAdapter(Context context) {
		super(context, 0);
	}

	public TimetableAdapter(Context context, int layout,
			List<TimeTableItem> list, Map<String, Integer> colorTable) {
		super(context, layout, list);
		l = null;
		this.list = list;
		this.colorTable = colorTable;
		init(context);
	}

	public TimetableAdapter(Context context, int layout,
			List<TimeTableItem> list, Map<String, Integer> colorTable,
			View.OnClickListener l) {
		super(context, layout, list);
		this.l = l;
		this.list = list;
		this.colorTable = colorTable;
		init(context);
	}

	private static void init(Context context) {
		// 시간표 기본 background resource을 얻어옴
		TypedValue value = new TypedValue();
		TypedArray a = context.obtainStyledAttributes(value.data, SELECTOR_REF);
		BACKGROUND_RES = a.getResourceId(0, R.drawable.selector_button);
		a.recycle();
	}

	@Override
	public int getCount() {
		int limit = AppUtil.timetable_limit;
		return Math.min(list.size(), limit);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		int width = TabTimeTableFragment.px;
		ViewWrapper w = (ViewWrapper) holder;
		TimeTableItem item = getItem(position);

		String[] strArray = { item.time, item.mon, item.tue, item.wed,
				item.thr, item.fri, item.sat };

		w.textArray[0]
				.setText(strArray[0].replace("\n\n", StringUtil.NEW_LINE));
		w.textArray[0].setWidth(width);

		Integer idx;
		int color;
		String[] upperArray = null;
		if (position != 0) {
			TimeTableItem upperItem = getItem(position - 1);
			upperArray = new String[] { null, upperItem.mon, upperItem.tue,
					upperItem.wed, upperItem.thr, upperItem.fri, upperItem.sat };
		}

		for (int i = 1; i < TabTimeTableFragment.NUM_OF_TIMETABLE_VIEWS; i++) {
			if (upperArray != null
					&& OApiUtil.getSubjectName(upperArray[i]).equals(
							OApiUtil.getSubjectName(strArray[i]))) {
				w.textArray[i].setText(StringUtil.NULL);
			} else {
				w.textArray[i].setText(OApiUtil
						.getCompressedString(strArray[i]).trim());
			}

			// 시간표 알림을 위한 시간표 정보 데이터 저장
			w.textArray[i].setTag(strArray[i] + StringUtil.NEW_LINE + position
					+ StringUtil.NEW_LINE + i);

			w.textArray[i].setWidth(width);
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
				w.textArray[i].setBackgroundResource(BACKGROUND_RES);
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
}
