package com.uoscs09.theuos2.tab.timetable;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.Locale;
import java.util.Map;

import butterknife.Bind;


class TimeTableAdapter2 extends AbsArrayAdapter<Subject[], TimeTableAdapter2.TimeTableViewHolder> {
    private OnItemClickListener onItemClickListener;
    private final SparseBooleanArray mClickedArray = new SparseBooleanArray(15);
    private final String[] periodTimeArray;
    private final TimeTable mTimeTable;

    public TimeTableAdapter2(Context context, TimeTable timeTable) {
        super(context, R.layout.list_layout_timetable2, timeTable.subjects);
        this.mTimeTable = timeTable;
        periodTimeArray = context.getResources().getStringArray(R.array.tab_timetable_timelist_only_time);
    }

    /**
     * 이미지 캡쳐를 위해 임시적으로 레이아웃 리소스를 바꾼다.
     *
     * @param forImage 이미지 캡쳐를 위한 레이아웃 리소스를 사용하는 경우 true
     */
    public void changeLayout(boolean forImage) {
        this.layoutId = forImage ? R.layout.list_layout_timetable2_for_image : R.layout.list_layout_timetable2;
    }

    @Override
    public int getCount() {
        return PrefHelper.TimeTables.isShowingLastEmptyPeriod() ? mTimeTable.maxTime : super.getCount();
    }

    @Override
    public void onBindViewHolder(int position, TimeTableViewHolder holder) {
        holder.item = getItem(position);
        holder.position = position;
        holder.mClickedArray = mClickedArray;
        holder.onItemClickListener = onItemClickListener;
        holder.periodTimeArray = periodTimeArray;

        holder.setView(mTimeTable.getColorTable());

    }

    @Override
    public TimeTableViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new TimeTableViewHolder(convertView, this.layoutId == R.layout.list_layout_timetable2_for_image);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(TimeTableViewHolder vh, View v, Subject subject);
    }


    static class TimeTableViewHolder extends AbsArrayAdapter.ViewHolder implements View.OnClickListener {
        @Bind(R.id.tab_timetable_list_text_period)
        public TextView period;
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

        private final int cardBackgroundColor;

        public TimeTableViewHolder(View itemView, boolean forImage) {
            super(itemView);

            cardBackgroundColor = AppUtil.getAttrColor(itemView.getContext(), R.attr.cardBackgroundColor);

            subjectViews = new SubjectViewHolder[6];

            if (!forImage)
                period.setOnClickListener(this);

            int i = 0;
            for (int id : VIEW_IDS) {
                View v = itemView.findViewById(id);

                //View ripple = v.findViewById(R.id.ripple);
                //ripple.findViewById(android.R.id.widget_frame).setTag(i);
                if (!forImage) {
                    View frame = v.findViewById(android.R.id.widget_frame);
                    frame.setTag(i);
                    frame.setOnClickListener(this);
                }
                subjectViews[i] = new SubjectViewHolder(v);
                //ripple.setOnClickListener(this);
                i++;
            }
        }

        void setView(Map<String, Integer> colorTable) {
            setPeriodView();

            int i = 0;
            for (SubjectViewHolder subjectViewHolder : subjectViews) {

                // 과목 배경색
                Subject subject = item[i++];
                Integer idx = colorTable.get(subject.subjectName);
                if (idx != null) {
                    int color = TimetableUtil.getTimeTableColor(itemView.getContext(), idx);
                    if (color != 0)
                        subjectViewHolder.view.setBackgroundColor(color);
                    else
                        subjectViewHolder.view.setBackgroundColor(cardBackgroundColor);
                } else
                    subjectViewHolder.view.setBackgroundColor(cardBackgroundColor);

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
                textView.setText(String.valueOf(position + 1));
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

    static class SubjectViewHolder {
        public final View view;
        public final TextView subject;
        public final TextView professor;
        public final TextView location;

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

}


