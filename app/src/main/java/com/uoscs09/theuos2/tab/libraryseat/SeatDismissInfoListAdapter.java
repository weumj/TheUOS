package com.uoscs09.theuos2.tab.libraryseat;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

public class SeatDismissInfoListAdapter extends AbsArrayAdapter<String, Holder> {

    public SeatDismissInfoListAdapter(Context context, List<String> list) {
        super(context, R.layout.list_layout_two_text_view, list);
    }

    @Override
    public void onBindViewHolder(int position, Holder holder) {
        String[] strs = getItem(position).split("\\+");
        holder.texts[0].setText(strs[0]);
        holder.texts[1].setText(strs[1]);
    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }


}

class Holder implements AbsArrayAdapter.ViewHoldable {
    public final TextView[] texts;

    public Holder(View v) {
        texts = new TextView[2];
        texts[0] = (TextView) v.findViewById(R.id.tab_libray_seat_info_time);
        texts[1] = (TextView) v.findViewById(R.id.tab_libray_seat_info_number);
    }
}
