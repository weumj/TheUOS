package com.uoscs09.theuos.tab.timetable;

import android.content.Context;
import android.graphics.Typeface;
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

    public TimetableAdapter(Context context, int layout, List<TimeTableItem> list, Map<String, Integer> colorTable, View.OnClickListener l) {
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

        String[] strArray = {item.time, item.mon, item.tue, item.wed, item.thr, item.fri, item.sat};

        TextView[] textViews = holder.textArray;
        final String timeDetailString = strArray[0].replace("\n\n", StringUtil.NEW_LINE);
        // 시간
        final int timePosition = position + 1;

        textViews[0].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                boolean isShowPosition = (boolean) v.getTag();

                if (isShowPosition) {
                    tv.setText(timeDetailString);
                    tv.setTextSize(13);
                    tv.setTypeface(Typeface.DEFAULT);
                } else {
                    tv.setText(Integer.toString(timePosition));
                    tv.setTextSize(16);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                }

                v.setTag(!isShowPosition);
            }
        });
        textViews[0].setTag(true);
        textViews[0].setText(Integer.toString(timePosition));
        textViews[0].setTextSize(16);
        textViews[0].setTypeface(Typeface.DEFAULT_BOLD);

        Integer idx;
        int color;
        String[] upperArray = null;
        if (position != 0) {
            TimeTableItem upperItem = getItem(position - 1);
            upperArray = new String[]{null, upperItem.mon, upperItem.tue, upperItem.wed, upperItem.thr, upperItem.fri, upperItem.sat};
        }

        // 월 ~ 토
        for (int i = 1; i < 7; i++) {
            if (upperArray != null && OApiUtil.getSubjectName(upperArray[i]).equals(OApiUtil.getSubjectName(strArray[i]))) {
                textViews[i].setText(StringUtil.NULL);
            } else {
                textViews[i].setText(OApiUtil.getCompressedString(strArray[i]).trim());
            }

            // 시간표 알림을 위한 시간표 정보 데이터 저장
            textViews[i].setTag(strArray[i] + StringUtil.NEW_LINE + position + StringUtil.NEW_LINE + i);

            idx = colorTable.get(OApiUtil.getSubjectName(strArray[i]));
            if (idx != null) {
                color = AppUtil.getColor(idx);
                if (color != -1) {
                    textViews[i].setBackgroundResource(color);
                    textViews[i].setOnClickListener(l);
                }
            } else {
                textViews[i].setOnClickListener(null);
                textViews[i].setBackgroundResource(AppUtil.getStyledValue(getContext(), R.attr.cardBackgroundColor));
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