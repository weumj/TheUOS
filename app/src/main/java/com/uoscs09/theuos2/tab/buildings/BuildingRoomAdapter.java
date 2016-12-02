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

    BuildingRoomAdapter(Context context, BuildingRoom room) {
        super(context, R.layout.list_layout_building_room, room.roomInfoList());
        this.buildingRoom = room;
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        BuildingRoom.RoomInfo roomInfo = getItem(position);

        if (roomInfo != null) {
            holder.textView.setText(roomInfo.roomName());
        } else {
            holder.textView.setText("");
        }
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

        BuildingRoom.RoomInfo item = getItem(position);

        if (item != null) {
            String room = item.buildingCode();
            BuildingRoom.Pair pair = buildingRoom.roomInfoList(room);
            if (pair != null) {
                holder.textView.setText(pair.buildingInfo().name());
            } else {
                holder.textView.setText("");
            }
        } else {
            holder.textView.setText("");
        }
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        BuildingRoom.RoomInfo item = getItem(position);
        return item != null ? item.buildingCodeInt() : -1;
    }

    static class ViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {

        HeaderViewHolder(View view) {
            super(view);
        }

    }
}
