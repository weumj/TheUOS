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

import butterknife.Bind;

class SeatListAdapter extends ListRecyclerAdapter<SeatItem, SeatListAdapter.ViewHolder> {
    private final int textColor;

    @SuppressWarnings("deprecation")
    public SeatListAdapter(Context context, List<SeatItem> list) {
        super(list);
        textColor = context.getResources().getColor(AppUtil.getAttrValue(context, R.attr.color_primary_text));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_seat, parent, false), textColor);
    }

    static class ViewHolder extends ListRecyclerAdapter.ViewHolder<SeatItem> {
        @Bind(R.id.tab_library_seat_list_text_room_name)
        TextView roomName;
        @Bind(R.id.ripple)
        View ripple;
        @Bind(R.id.tab_libray_seat_list_progress_img)
        TextView progressImg;

        final PieProgressDrawable drawable = new PieProgressDrawable();


        @SuppressWarnings("deprecation")
        public ViewHolder(View convertView, int textColor) {
            super(convertView);

            ripple.setOnClickListener(this);

            Context context = convertView.getContext();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            drawable.setBorderWidth(2, dm);

            drawable.setTextSize(15 * dm.scaledDensity);
            drawable.setTextColor(textColor);
            drawable.setColor(context.getResources().getColor(R.color.gray_red));
            drawable.setCenterColor(AppUtil.getAttrColor(context, R.attr.cardBackgroundColor));
            progressImg.setBackgroundDrawable(drawable);
        }

        protected void setView() {
            SeatItem item = getItem();
            roomName.setText(item.roomName);
            int progress = Math.round(item.utilizationRate);
            drawable.setText(item.vacancySeat.trim() + " / " + (Integer.parseInt(item.occupySeat.trim()) + Integer.parseInt(item.vacancySeat.trim())));
            drawable.setLevel(progress);
        }
    }

}
