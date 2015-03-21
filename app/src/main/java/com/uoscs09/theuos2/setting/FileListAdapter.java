package com.uoscs09.theuos2.setting;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.io.File;
import java.util.List;

public class FileListAdapter extends AbsArrayAdapter<File, Holder> {
	//private final int width;
	//private final int height;

	public FileListAdapter(Context context, int layout, List<File> list) {
		super(context, layout, list);
		//final float density = context.getResources().getDisplayMetrics().density;
		//width = Math.round(30 * density);
		//height = Math.round(28 * density);
	}

	@Override
	public void onBindViewHolder(int position, Holder holder) {
		File file = getItem(position);
		holder.tv.setText(file.getName());
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
	}

	@Override
	public Holder getViewHolder(View convertView) {
		return new Holder(convertView);
	}

}


class Holder implements AbsArrayAdapter.ViewHolderable {
    public final TextView tv;

    public Holder(View v) {
        tv = (TextView) v.findViewById(android.R.id.text1);
    }
}
