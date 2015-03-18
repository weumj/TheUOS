package com.uoscs09.theuos.tab.libraryseat;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

import java.util.List;

public class SeatDismissInfoListAdapter extends AbsArrayAdapter<String, Holder> {

    public SeatDismissInfoListAdapter(Context context, int layout, List<String> list) {
        super(context, layout, list);
    }

    @Override
    public View setView(int position, View v, Holder holder) {
        String[] strs = getItem(position).split("\\+");
        holder.texts[0].setText(strs[0]);
        holder.texts[1].setText(strs[1]);
        return v;
    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }


}

class Holder implements AbsArrayAdapter.ViewHolder {
    public TextView[] texts;

    public Holder(View v) {
        texts = new TextView[2];
        texts[0] = (TextView) v.findViewById(R.id.tab_libray_seat_info_time);
        texts[1] = (TextView) v.findViewById(R.id.tab_libray_seat_info_number);
    }
}
