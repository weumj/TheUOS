package com.uoscs09.theuos.tab.libraryseat;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class SeatListAdapter extends AbsArrayAdapter<SeatItem> {

	private SeatListAdapter(Context context) {
		super(context, 0);
	}

	public SeatListAdapter(Context context, int resource, List<SeatItem> list) {
		super(context, resource, list);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		SeatItem item = getItem(position);
		h.roomName.setText(item.roomName);
		h.occupySeat.setText(item.occupySeat);
		h.vacancySeat.setText(item.vacancySeat);
		h.rateBar.setEnabled(false);
		h.rateBar
				.setProgress(Math.round(Float.parseFloat(item.utilizationRate)));
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	private static class Holder implements ViewHolder {
		TextView roomName, occupySeat, vacancySeat;
		SeekBar rateBar;

		public Holder(View convertView) {
			roomName = (TextView) convertView.findViewById(R.id.tab_library_seat_list_text_room_name);
			occupySeat = (TextView) convertView.findViewById(R.id.tab_libray_seat_list_text_occupy_seat);
			vacancySeat = (TextView) convertView.findViewById(R.id.tab_library_seat_list_text_vacancy_seat);
			rateBar = (SeekBar) convertView.findViewById(R.id.tab_libray_seat_list_text_rate_seek_bar);
		}
	}
}
