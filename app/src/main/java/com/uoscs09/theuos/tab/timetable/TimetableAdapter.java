package com.uoscs09.theuos.tab.timetable;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;

import java.util.List;
import java.util.Map;

//FIXME
public class TimetableAdapter extends AbsArrayAdapter<TimeTableItem, ViewWrapper> {
	private OnClickListener l;
	private Map<String, Integer> colorTable;
	private int maxTime;

	public TimetableAdapter(Context context, int layout,List<TimeTableItem> list, Map<String, Integer> colorTable, 	View.OnClickListener l) {
		super(context, layout, list);
		this.l = l;
		this.colorTable = colorTable;
		init();
	}

	private void init() {
		int size = super.getCount() - 1;

		// 마지막 수업시간 판별, 14부터 시작
		while (size > 0 && getItem(size).isTimeTableEmpty()) {
			size--;
		}

		maxTime = size + 1;
		if (maxTime < 0)
			maxTime = super.getCount() == 0 ? 0 : super.getCount() - 1;
	}

	@Override
	public void notifyDataSetChanged() {
		init();
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return PrefUtil.getInstance(getContext()).get(PrefUtil.KEY_TIMETABLE_LIMIT, false) ? maxTime : super.getCount();
	}

	@Override
	public View setView(int position, View convertView, ViewWrapper holder) {
		TimeTableItem item = getItem(position);

		String[] strArray = { item.time, item.mon, item.tue, item.wed,item.thr, item.fri, item.sat };

		holder.textArray[0]
				.setText(strArray[0].replace("\n\n", StringUtil.NEW_LINE));

		Integer idx;
		int color;
		String[] upperArray = null;
		if (position != 0) {
			TimeTableItem upperItem = getItem(position - 1);
			upperArray = new String[] { null, upperItem.mon, upperItem.tue,
					upperItem.wed, upperItem.thr, upperItem.fri, upperItem.sat };
		}

		for (int i = 1; i < 7; i++) {
			if (upperArray != null
					&& OApiUtil.getSubjectName(upperArray[i]).equals(
							OApiUtil.getSubjectName(strArray[i]))) {
				holder.textArray[i].setText(StringUtil.NULL);
			} else {
				holder.textArray[i].setText(OApiUtil
						.getCompressedString(strArray[i]).trim());
			}

			// 시간표 알림을 위한 시간표 정보 데이터 저장
			holder.textArray[i].setTag(strArray[i] + StringUtil.NEW_LINE + position
					+ StringUtil.NEW_LINE + i);

			idx = colorTable.get(OApiUtil.getSubjectName(strArray[i]));
			if (idx != null) {
				color = AppUtil.getColor(idx);
				if (color != -1) {
					holder.textArray[i].setBackgroundResource(color);
					holder.textArray[i].setOnClickListener(l);
				}
			} else {
				holder.textArray[i].setOnClickListener(null);
				holder.textArray[i].setBackgroundResource(AppUtil.getStyledValue(
						getContext(), R.attr.cardBackgroundColor));
			}
		}
		return convertView;
	}

	@Override
	public ViewWrapper getViewHolder(View view) {
		return new ViewWrapper(view);
	}

}
class ViewWrapper implements AbsArrayAdapter.ViewHolder {
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