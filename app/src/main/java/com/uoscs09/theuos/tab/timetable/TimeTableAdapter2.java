package com.uoscs09.theuos.tab.timetable;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.base.AbsArrayAdapter;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.PrefUtil;
import com.uoscs09.theuos.util.StringUtil;

import java.util.Hashtable;
import java.util.Locale;


public class TimeTableAdapter2 extends AbsArrayAdapter<Subject[], TimeTableViewHolder> {
    private final Hashtable<String, Integer> colorTable;
    private OnItemClickListener onItemClickListener;
    private SparseBooleanArray mClickedArray = new SparseBooleanArray(15);
    private String[] periodTimeArray;
    private TimeTable mTimeTable;

    public TimeTableAdapter2(Context context, TimeTable timeTable, Hashtable<String, Integer> colorTable) {
        super(context, R.layout.list_layout_timetable2, timeTable.subjects);
        this.colorTable = colorTable;
        this.mTimeTable = timeTable;
        periodTimeArray = context.getResources().getStringArray(R.array.tab_timetable_timelist_only_time);
    }

    @Override
    public int getCount() {
        return PrefUtil.getInstance(getContext()).get(PrefUtil.KEY_TIMETABLE_LIMIT, false) ? mTimeTable.maxTime : super.getCount();
    }

    @Override
    public void onBindViewHolder(int position, TimeTableViewHolder holder) {
        holder.item = getItem(position);
        holder.position = position;
        holder.mClickedArray = mClickedArray;
        holder.onItemClickListener = onItemClickListener;
        holder.periodTimeArray = periodTimeArray;

        holder.setView(colorTable);

    }

    @Override
    public TimeTableViewHolder getViewHolder(View convertView) {
        return new TimeTableViewHolder(convertView);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public static interface OnItemClickListener {
        public void onItemClick(TimeTableViewHolder vh, View v, Subject subject);
    }

}

class SubjectViewHolder {
    public View view;
    public TextView subject, professor, location;

    public SubjectViewHolder(View parent) {
        this.view = parent;
        subject = (TextView) parent.findViewById(R.id.time_table_subject);
        professor = (TextView) parent.findViewById(R.id.time_table_professor);
        location = (TextView) parent.findViewById(R.id.time_table_location);
    }

    public void setView(Subject item) {

        if (item.isEqualToUpperPeriod) {
            subject.setText(StringUtil.NULL);
            professor.setText(StringUtil.NULL);
            location.setText(StringUtil.NULL);

        } else {
            if (Locale.getDefault().equals(Locale.KOREA)) {
                subject.setText(item.subjectNameShort);
                professor.setText(item.professor);

            } else {
                subject.setText(item.subjectNameEngShort);
                professor.setText(item.professorEng);
            }

            location.setText(item.building);

        }
    }

}

class TimeTableViewHolder extends AbsArrayAdapter.ViewHolder implements View.OnClickListener {
    public final TextView period;
    public final SubjectViewHolder[] subjectViews;
    public Subject[] item;
    public int position;

    TimeTableAdapter2.OnItemClickListener onItemClickListener;
    SparseBooleanArray mClickedArray;
    String[] periodTimeArray;

    static final int[] VIEW_IDS = new int[]{
            R.id.tab_timetable_list_text_mon,
            R.id.tab_timetable_list_text_tue,
            R.id.tab_timetable_list_text_wed,
            R.id.tab_timetable_list_text_thr,
            R.id.tab_timetable_list_text_fri,
            R.id.tab_timetable_list_text_sat
    };

    public TimeTableViewHolder(View itemView) {
        super(itemView);
        subjectViews = new SubjectViewHolder[6];

        View periodLayout = itemView.findViewById(R.id.tab_timetable_list_period);
        periodLayout.findViewById(R.id.tab_timetable_list_period_ripple).setOnClickListener(this);
        period = (TextView) periodLayout.findViewById(R.id.tab_timetable_list_text_period);

        int i = 0;
        for (int id : VIEW_IDS) {
            View v = itemView.findViewById(id);

            View ripple = v.findViewById(R.id.ripple);
            ripple.findViewById(android.R.id.widget_frame).setTag(i);
            subjectViews[i] = new SubjectViewHolder(v);
            ripple.setOnClickListener(this);
            i++;
        }

    }

    protected void setView(Hashtable<String, Integer> colorTable) {
        setPeriodView();

        Context context = itemView.getContext();

        int i = 0;
        int color;
        Integer idx;
        Subject subject;
        for (SubjectViewHolder subjectViewHolder : subjectViews) {

            // 과목 배경색
            subject = item[i++];
            idx = colorTable.get(subject.subjectName);
            if (idx != null) {
                color = AppUtil.getTimeTableColor(context, idx);
                if (color != 0)
                    subjectViewHolder.view.setBackgroundColor(color);
                else
                    subjectViewHolder.view.setBackgroundResource(AppUtil.getAttrValue(context, R.attr.cardBackgroundColor));
            } else
                subjectViewHolder.view.setBackgroundResource(AppUtil.getAttrValue(context, R.attr.cardBackgroundColor));

            // 과목 정보
            subjectViewHolder.setView(subject);

        }

    }

    private void setPeriodView() {
        TextView textView = period;
        if (mClickedArray.get(position)) {
            textView.setText(periodTimeArray[position]);
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT);

        } else {
            textView.setText(Integer.toString(position + 1));
            textView.setTextSize(16);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_list_text_period:

                if (mClickedArray.get(position)) {
                    mClickedArray.delete(position);
                    setPeriodView();

                } else {
                    mClickedArray.put(position, true);
                    setPeriodView();

                }

                break;

            default:
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(this, v, item[(int) v.getTag()]);

                break;
        }
    }
}

