package com.uoscs09.theuos2.tab.timetable;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


class TimeTableAdapter extends AbsArrayAdapter<Timetable2.Period, TimeTableAdapter.TimeTableViewHolder> {
    private OnItemClickListener onItemClickListener;
    private final SparseBooleanArray mClickedArray = new SparseBooleanArray(15);
    private final String[] periodTimeArray;
    private Timetable2 mTimeTable;

    private final int cardBackgroundColor;

    public TimeTableAdapter(Context context) {
        super(context, R.layout.list_layout_timetable2, new ArrayList<>());
        periodTimeArray = context.getResources().getStringArray(R.array.tab_timetable_timelist_only_time);
        cardBackgroundColor = ResourceUtil.getAttrColor(context, R.attr.cardBackgroundColor);
    }

    public void setTimeTable(Timetable2 timeTable) {
        this.mTimeTable = timeTable;
        clear();
        if (timeTable != null)
            addAll(timeTable.periods());

        notifyDataSetChanged();
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
        if (mTimeTable == null)
            return 0;

        return PrefHelper.TimeTables.isShowingLastEmptyPeriod() ? mTimeTable.maxPeriod() : super.getCount();
    }

    @Override
    public void onBindViewHolder(int position, TimeTableViewHolder holder) {
        holder.item = getItem(position);
        holder.position = position;

        holder.setView();

    }

    @Override
    public TimeTableViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new TimeTableViewHolder(convertView, this.layoutId == R.layout.list_layout_timetable2_for_image);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(TimeTableViewHolder vh, View v, Timetable2.SubjectInfo subject);
    }


    static final int[] VIEW_IDS = new int[]{
            R.id.tab_timetable_list_text_mon,
            R.id.tab_timetable_list_text_tue,
            R.id.tab_timetable_list_text_wed,
            R.id.tab_timetable_list_text_thr,
            R.id.tab_timetable_list_text_fri,
            R.id.tab_timetable_list_text_sat
    };

    class TimeTableViewHolder extends AbsArrayAdapter.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tab_timetable_list_text_period)
        public TextView period;
        public final SubjectViewHolder[] subjectViews;
        public Timetable2.Period item;
        public int position;


        public TimeTableViewHolder(View itemView, boolean forImage) {
            super(itemView);

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

        void setView() {
            setPeriodView();

            int i = 0;
            for (SubjectViewHolder subjectViewHolder : subjectViews) {

                // 과목 배경색
                Timetable2.SubjectInfo subject = item.getSubjectInfo(i++);

                int color = 0;
                if (subject != null) {
                    Integer idx = mTimeTable.colorTable().get(subject.nameKor());
                    if (idx != null)
                        color = TimetableUtil.getTimeTableColor(itemView.getContext(), idx);
                }
                subjectViewHolder.view.setBackgroundColor(color != 0 ? color : cardBackgroundColor);

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
                        onItemClickListener.onItemClick(this, v, item.getSubjectInfo((int) v.getTag()));

                    break;
            }
        }
    }

    static class SubjectViewHolder {
        public final View view;
        @BindView(R.id.time_table_subject)
        public TextView subject;
        @BindView(R.id.time_table_professor)
        public TextView professor;
        @BindView(R.id.time_table_location)
        public TextView location;

        public SubjectViewHolder(View parent) {
            this.view = parent;
            ButterKnife.bind(this, view);
        }

        public void setView(Timetable2.SubjectInfo item) {
            if (item == null || item.isEqualPrior()) {
                subject.setText("");
                professor.setText("");
                location.setText("");
            } else {
                subject.setText(item.name());
                professor.setText(item.professor());
                location.setText(item.location());
            }
        }

    }

}


