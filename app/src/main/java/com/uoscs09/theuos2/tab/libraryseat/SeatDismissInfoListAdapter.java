package com.uoscs09.theuos2.tab.libraryseat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.ListRecyclerAdapter;

import java.util.List;

class SeatDismissInfoListAdapter extends ListRecyclerAdapter<SeatDismissInfo, SeatDismissInfoListAdapter.Holder> {

    public SeatDismissInfoListAdapter(List<SeatDismissInfo> list) {
        super(list);
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_seat_dismiss_info,parent,false));
    }

    static class Holder extends ListRecyclerAdapter.ViewHolder<SeatDismissInfo> {
        public final TextView time,count;

        public Holder(View v) {
            super(v);
            time = (TextView) v.findViewById(R.id.tab_libray_seat_info_time);
            count = (TextView) v.findViewById(R.id.tab_libray_seat_info_number);
        }

        @Override
        protected void setView() {
            SeatDismissInfo info = getItem();

            time.setText(time.getContext().getString(R.string.tab_library_seat_dismiss_info_time_within, info.time));
            count.setText(Integer.toString(info.seatCount));
        }
    }
}


