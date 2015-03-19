package com.uoscs09.theuos.tab.libraryseat;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.PieProgressDrawable;
import com.uoscs09.theuos.util.AppUtil;

import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.ViewHolder> {
    final int textColor;
    final List<SeatItem> mDataSet;
    final Context mContext;
    final LayoutInflater mInflater;

    public SeatListAdapter(Context context, List<SeatItem> list) {
        this.mDataSet = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        textColor = context.getResources().getColor(AppUtil.getStyledValue(mContext, R.attr.colorAccent));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        SeatItem item = mDataSet.get(position);

        h.item = item;

        h.roomName.setText(item.roomName);
        int progress = Math.round(Float.parseFloat(item.utilizationRate));
        h.drawable.setTextColor(textColor);
        h.drawable.setText(item.vacancySeat.trim() + " / " + (Integer.valueOf(item.occupySeat.trim()) + Integer.valueOf(item.vacancySeat.trim())));
        h.drawable.setLevel(progress);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.list_layout_seat, parent, false));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView roomName;
        final View ripple;
        final PieProgressDrawable drawable = new PieProgressDrawable();
        final TextView progressImg;
        SeatItem item;

        @SuppressWarnings("deprecation")
        public ViewHolder(View convertView) {
            super(convertView);


            ripple = convertView.findViewById(R.id.ripple);
            ripple.setOnClickListener(this);

            roomName = (TextView) convertView.findViewById(R.id.tab_library_seat_list_text_room_name);

            Context context = convertView.getContext();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            drawable.setBorderWidth(2, dm);

            progressImg = (TextView) convertView.findViewById(R.id.tab_libray_seat_list_progress_img);
            drawable.setTextSize(15 * dm.scaledDensity);
            drawable.setColor(context.getResources().getColor(R.color.gray_red));
            drawable.setCentorColor(context.getResources().getColor(AppUtil.getStyledValue(context, R.attr.cardBackgroundColor)));
            progressImg.setBackgroundDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            Context context = itemView.getContext();
            Intent intent = new Intent(context, SubSeatWebActivity.class);
            intent.putExtra(TabLibrarySeatFragment.ITEM, (Parcelable) item);
            context.startActivity(intent);
        }
    }

}
