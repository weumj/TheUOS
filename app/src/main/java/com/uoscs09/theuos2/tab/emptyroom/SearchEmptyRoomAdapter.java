package com.uoscs09.theuos2.tab.emptyroom;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

public class SearchEmptyRoomAdapter extends AbsArrayAdapter<EmptyClassRoomItem, SearchEmptyRoomAdapter.Holder> {

    public SearchEmptyRoomAdapter(Context context, List<EmptyClassRoomItem> list) {
        super(context, R.layout.list_layout_empty_room,  list);
    }

    @Override
    public void onBindViewHolder(int position, Holder holder) {
        EmptyClassRoomItem item = getItem(position);

        holder.building.setText(item.building);
        holder.room_no.setText(item.room_no);
        holder.room_div.setText(item.room_div);
        holder.person_cnt.setText(Integer.toString(item.person_cnt));

    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }

    static class Holder implements AbsArrayAdapter.ViewHoldable {
        public final TextView building, room_no, room_div, person_cnt;

        public Holder(View convertView) {
            building = (TextView) convertView.findViewById(R.id.etc_search_empty_room_list_text_name);
            room_no = (TextView) convertView.findViewById(R.id.etc_search_empty_room_list_text_room_no);
            room_div = (TextView) convertView.findViewById(R.id.etc_search_empty_room_list_text_subj);
            person_cnt = (TextView) convertView.findViewById(R.id.etc_search_empty_room_list_text_person);
        }
    }
}


