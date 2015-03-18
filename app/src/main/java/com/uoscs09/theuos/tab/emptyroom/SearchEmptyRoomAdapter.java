package com.uoscs09.theuos.tab.emptyroom;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

import java.util.List;

public class SearchEmptyRoomAdapter extends AbsArrayAdapter<ClassRoomItem, Holder> {

    public SearchEmptyRoomAdapter(Context context, int layout, List<ClassRoomItem> list) {
        super(context, layout, list);
    }

    @Override
    public View setView(int position, View convertView, Holder holder) {
        ClassRoomItem item = getItem(position);
        int px = (getContext().getResources().getDisplayMetrics().widthPixels) / 4;

        int i = 0;

        for (TextView v : holder.array) {
            v.setText(item.array[i++]);
            v.setWidth(px);
        }

        return convertView;
    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }

}

class Holder implements AbsArrayAdapter.ViewHolder {
    public TextView[] array = new TextView[4];

    public Holder(View convertView) {
        array[0] = (TextView) convertView
                .findViewById(R.id.etc_search_empty_room_list_text_name);
        array[1] = (TextView) convertView
                .findViewById(R.id.etc_search_empty_room_list_text_room_no);
        array[2] = (TextView) convertView
                .findViewById(R.id.etc_search_empty_room_list_text_subj);
        array[3] = (TextView) convertView
                .findViewById(R.id.etc_search_empty_room_list_text_person);
    }
}
