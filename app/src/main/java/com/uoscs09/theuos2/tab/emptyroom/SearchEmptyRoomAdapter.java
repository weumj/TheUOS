package com.uoscs09.theuos2.tab.emptyroom;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

import butterknife.Bind;

class SearchEmptyRoomAdapter extends AbsArrayAdapter<EmptyRoom, SearchEmptyRoomAdapter.Holder> {

    public SearchEmptyRoomAdapter(Context context, List<EmptyRoom> list) {
        super(context, R.layout.list_layout_empty_room, list);
    }

    @Override
    public void onBindViewHolder(int position, Holder holder) {
        EmptyRoom item = getItem(position);

        holder.building.setText(item.building);
        holder.room_no.setText(item.room_no);
        holder.room_div.setText(item.room_div);
        holder.person_cnt.setText(String.valueOf(item.person_cnt));

    }

    @Override
    public Holder onCreateViewHolder(View v, int viewType) {
        return new Holder(v);
    }

    static class Holder extends AbsArrayAdapter.ViewHolder {
        @Bind(R.id.etc_search_empty_room_list_text_name)
        public TextView building;
        @Bind(R.id.etc_search_empty_room_list_text_room_no)
        public TextView room_no;
        @Bind(R.id.etc_search_empty_room_list_text_subj)
        public TextView room_div;
        @Bind(R.id.etc_search_empty_room_list_text_person)
        public TextView person_cnt;

        public Holder(View convertView) {
            super(convertView);
        }
    }
}


