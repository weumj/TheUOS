package com.uoscs09.theuos.tab.libraryseat;

import java.util.List;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.PieProgressDrawable;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

public class SeatListAdapter extends AbsArrayAdapter<SeatItem> {
	int textColor;

	private SeatListAdapter(Context context) {
		super(context, 0);
	}

	public SeatListAdapter(Context context, int resource, List<SeatItem> list) {
		super(context, resource, list);
		textColor = context.getResources().getColor(
				AppUtil.theme == AppTheme.Black ? android.R.color.white
						: R.color.dark_blue_gray);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		SeatItem item = getItem(position);
		h.roomName.setText(item.roomName);
		int progress = Math.round(Float.parseFloat(item.utilizationRate));
		h.drawable.setTextColor(textColor);
		h.drawable.setText(item.vacancySeat.trim()
				+ " / "
				+ (Integer.valueOf(item.occupySeat.trim()) + Integer
						.valueOf(item.vacancySeat.trim())));
		h.drawable.setLevel(progress);

		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	private static class Holder implements ViewHolder {
		TextView roomName;
		PieProgressDrawable drawable = new PieProgressDrawable();
		TextView progressImg;

		@SuppressWarnings("deprecation")
		public Holder(View convertView) {
			roomName = (TextView) convertView
					.findViewById(R.id.tab_library_seat_list_text_room_name);
			DisplayMetrics dm = convertView.getContext().getResources()
					.getDisplayMetrics();
			drawable.setBorderWidth(2, dm);
			progressImg = (TextView) convertView
					.findViewById(R.id.tab_libray_seat_list_progress_img);
			drawable.setTextSize(15 * dm.scaledDensity);
			drawable.setColor(convertView.getContext().getResources()
					.getColor(R.color.gray_red));
			progressImg.setBackgroundDrawable(drawable);
		}
	}
}
