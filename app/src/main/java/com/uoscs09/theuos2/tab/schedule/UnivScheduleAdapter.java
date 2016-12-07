package com.uoscs09.theuos2.tab.schedule;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

class UnivScheduleAdapter extends AbsArrayAdapter<UnivScheduleItem, UnivScheduleAdapter.ViewHolder> implements StickyListHeadersAdapter {

    private LayoutInflater inflater;

    UnivScheduleAdapter(Context context, List<UnivScheduleItem> list) {
        super(context, R.layout.list_layout_univ_schedule, list);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        holder.setView(position, getItem(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new ViewHolder(convertView);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder(inflater.inflate(R.layout.list_layout_univ_schedule_header, viewGroup, false));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        holder.setView(getItem(position));
        return convertView;
    }



    @Override
    public long getHeaderId(int position) {
        UnivScheduleItem item = getItem(position);
        if (item != null) {
            UnivScheduleItem.ScheduleDate date = item.dateStart;
            return date.month * 100 + date.day;
        } else {
            return -1;
        }
    }


    static class ViewHolder extends AbsArrayAdapter.ViewHolder {
        @BindView(android.R.id.text1)
        TextView textView1;
        @BindView(android.R.id.text2)
        TextView textView2;
        /*
        @BindView(R.id.card_view)
        CardView cardView;
        */
        //UnivScheduleItem item;
        final PieProgressDrawable drawable = new PieProgressDrawable();

        @SuppressWarnings("deprecation")
        public ViewHolder(View view) {
            super(view);

            drawable.setLevel(100);
            drawable.setBorderWidth(-1f, view.getResources().getDisplayMetrics());
            int size = itemView.getResources().getDimensionPixelSize(R.dimen.univ_schedule_list_drawable_size);
            drawable.setBounds(0, 0, size, size);

            textView1.setCompoundDrawables(drawable, null, null, null);
        }


        void setView(int position, UnivScheduleItem item) {
            if (item != null) {
                textView1.setText(item.content);
                textView2.setText(item.scheduleDate);

                drawable.setColor(ResourceUtil.getOrderedColor(textView1.getContext(), position));
                //holder.drawable.setCentorColor(getContext().getResources().getColor(AppUtil.getColor(position)));

                textView1.invalidateDrawable(drawable);
            } else {
                textView1.setText("");
                textView2.setText("");

                drawable.setColor(Color.TRANSPARENT);
                //holder.drawable.setCentorColor(getContext().getResources().getColor(AppUtil.getColor(position)));

                textView1.invalidateDrawable(drawable);
            }
        }

    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        @BindView(android.R.id.text2)
        TextView textView2;
        @BindView(R.id.bar)
        View bar;

        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("E", Locale.getDefault());

        HeaderViewHolder(View view) {
            super(view);
        }

        void setView(UnivScheduleItem item) {
            if (item != null) {
                UnivScheduleItem.ScheduleDate date = item.dateStart;
                Calendar c = item.getDate(true);

                textView.setText(String.valueOf(date.day));
                if (c != null)
                    textView2.setText(dateFormat.format(c.getTime()));
            } else {
                textView.setText("");
                textView2.setText("");
            }
        }

        void setBarVisible(boolean visible){
            bar.setVisibility(visible? View.VISIBLE : View.INVISIBLE);
        }
    }

}

