package com.uoscs09.theuos2.tab.buildings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

class BuildingRoomAdapter extends AbsArrayAdapter<BuildingRoom.RoomInfo, BuildingRoomAdapter.ViewHolder> implements StickyListHeadersAdapter {

    private BuildingRoom buildingRoom;

    public BuildingRoomAdapter(Context context, BuildingRoom room) {
        super(context, R.layout.list_layout_building_room, room.roomInfoList());
        this.buildingRoom = room;
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        BuildingRoom.RoomInfo roomInfo = getItem(position);
        holder.textView.setText(roomInfo.roomName());
    }

    @Override
    public ViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new ViewHolder(convertView);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_layout_building_room_header, parent, false));
            convertView = holder.itemView;
            convertView.setTag(holder);

        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String room = getItem(position).buildingCode();
        holder.textView.setText(buildingRoom.roomInfoList(room).buildingInfo().name());

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).buildingCodeInt();
    }

    static class ViewHolder  extends AbsArrayAdapter.SimpleViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {

        public HeaderViewHolder(View view) {
            super(view);
        }

    }
}
