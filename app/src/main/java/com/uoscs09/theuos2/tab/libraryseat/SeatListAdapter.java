package com.uoscs09.theuos2.tab.libraryseat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.OnItemClickListener;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.ViewHolder> {
    private final int textColor;
    private final List<SeatItem> mDataSet;
    private OnItemClickListener<ViewHolder> mListener;

    public SeatListAdapter(Context context, List<SeatItem> list) {
        this.mDataSet = list;
        textColor = context.getResources().getColor(AppUtil.getAttrValue(context, R.attr.color_primary_text));
    }

    public void setOnItemClickListener(OnItemClickListener<ViewHolder> listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        h.mOnItemClickListener = mListener;

        h.item = mDataSet.get(position);

        h.setView();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_seat, parent, false), textColor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView roomName;
        final View ripple;
        final PieProgressDrawable drawable = new PieProgressDrawable();
        final TextView progressImg;
        SeatItem item;

        OnItemClickListener<ViewHolder> mOnItemClickListener;

        @SuppressWarnings("deprecation")
        public ViewHolder(View convertView, int textColor) {
            super(convertView);

            ripple = convertView.findViewById(R.id.ripple);
            ripple.setOnClickListener(this);

            roomName = (TextView) convertView.findViewById(R.id.tab_library_seat_list_text_room_name);

            Context context = convertView.getContext();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            drawable.setBorderWidth(2, dm);

            progressImg = (TextView) convertView.findViewById(R.id.tab_libray_seat_list_progress_img);
            drawable.setTextSize(15 * dm.scaledDensity);
            drawable.setTextColor(textColor);
            drawable.setColor(context.getResources().getColor(R.color.gray_red));
            drawable.setCentorColor(context.getResources().getColor(AppUtil.getAttrValue(context, R.attr.cardBackgroundColor)));
            progressImg.setBackgroundDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(this, v);
        }

        protected void setView() {
            roomName.setText(item.roomName);
            int progress = Math.round(Float.parseFloat(item.utilizationRate));
            drawable.setText(item.vacancySeat.trim() + " / " + (Integer.valueOf(item.occupySeat.trim()) + Integer.valueOf(item.vacancySeat.trim())));
            drawable.setLevel(progress);
        }
    }

}
