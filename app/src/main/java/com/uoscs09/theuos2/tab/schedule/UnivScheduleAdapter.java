package com.uoscs09.theuos2.tab.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

class UnivScheduleAdapter extends AbsArrayAdapter<UnivScheduleItem, UnivScheduleAdapter.ViewHolder> implements StickyListHeadersAdapter {

    public UnivScheduleAdapter(Context context, List<UnivScheduleItem> list) {
        super(context, R.layout.list_layout_univ_schedule, list);
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        UnivScheduleItem item = getItem(position);

        //holder.item = item;

        holder.textView1.setText(item.content);
        holder.textView2.setText(item.scheduleDate);


        holder.drawable.setColor(AppUtil.getOrderedColor(getContext(), position));
        //holder.drawable.setCentorColor(getContext().getResources().getColor(AppUtil.getColor(position)));

        holder.textView1.invalidateDrawable(holder.drawable);

    }

    @Override
    public ViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new ViewHolder(convertView);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_layout_univ_schedule_header, viewGroup, false));
            convertView = holder.itemView;
            convertView.setTag(holder);

        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
        Calendar c = getItem(position).getDate(true);

        if (c == null) {
            holder.textView.setText("");
            holder.textView2.setText("");
        } else {
            holder.textView.setText(String.valueOf(date.day));
            holder.textView2.setText(dateFormat.format(new Date(c.getTimeInMillis())));
        }

        return convertView;
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("E", Locale.getDefault());

    @Override
    public long getHeaderId(int position) {
        UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
        return date.month * 100 + date.day;
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

    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        @BindView(android.R.id.text2)
        TextView textView2;

        public HeaderViewHolder(View view) {
            super(view);
        }

    }
}

