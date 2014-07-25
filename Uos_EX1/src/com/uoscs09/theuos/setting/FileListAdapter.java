package com.uoscs09.theuos.setting;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class FileListAdapter extends AbsArrayAdapter<File> {
	//private final int width;
	//private final int height;

	private FileListAdapter(Context context) {
		super(context, 0);
		//final float density = context.getResources().getDisplayMetrics().density;
		//width = Math.round(30 * density);
		//height = Math.round(28 * density);
	}

	public FileListAdapter(Context context, int layout, List<File> list) {
		super(context, layout, list);
		//final float density = context.getResources().getDisplayMetrics().density;
		//width = Math.round(30 * density);
		//height = Math.round(28 * density);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		File file = getItem(position);
		h.tv.setText(file.getName());
		/*
		Context context = getContext();
		final Drawable drawable;
		int res;
		switch (AppUtil.theme) {
		case Black:
			// if (file.isDirectory()) {
			res = R.drawable.ic_action_collections_collection_dark;
			// } else {
			// res = R.drawable.ic_action_collections_view_as_list_dark;
			// }
			break;
		default:
			// if (file.isDirectory()) {
			res = R.drawable.ic_action_collections_collection;
			// } else {
			// res = R.drawable.ic_action_collections_view_as_list;
			// }
			h.tv.setTextColor(Color.BLACK);
			break;
		}
		drawable = context.getResources().getDrawable(res);
		drawable.setBounds(0, 0, width, height);
		h.tv.setCompoundDrawables(drawable, null, null, null);*/
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View convertView) {
		return new Holder(convertView);
	}

	protected class Holder implements ViewHolder {
		public TextView tv;

		public Holder(View v) {
			tv = (TextView) v.findViewById(android.R.id.text1);
		}
	}
}
