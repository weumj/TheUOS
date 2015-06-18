package com.uoscs09.theuos2.tab.libraryseat;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.ListRecyclerAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.List;

public class SeatListAdapter extends ListRecyclerAdapter<SeatItem, SeatListAdapter.ViewHolder> {
    private final int textColor;

    public SeatListAdapter(Context context, List<SeatItem> list) {
        super(list);
        textColor = context.getResources().getColor(AppUtil.getAttrValue(context, R.attr.color_primary_text));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_seat, parent, false), textColor);
    }

    public static class ViewHolder extends ListRecyclerAdapter.ViewHolder<SeatItem> {
        final TextView roomName;
        final View ripple;
        final PieProgressDrawable drawable = new PieProgressDrawable();
        final TextView progressImg;

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

        protected void setView() {
            SeatItem item = getItem();
            roomName.setText(item.roomName);
            int progress = Math.round(Float.parseFloat(item.utilizationRate));
            drawable.setText(item.vacancySeat.trim() + " / " + (Integer.valueOf(item.occupySeat.trim()) + Integer.valueOf(item.vacancySeat.trim())));
            drawable.setLevel(progress);
        }
    }

}
