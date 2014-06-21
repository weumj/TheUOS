package com.uoscs09.theuos.tab.subject;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class SubjectInfoAdapter extends AbsArrayAdapter<String> {
	private String[] array;

	private SubjectInfoAdapter(Context context) {
		super(context, 0);
	}

	public SubjectInfoAdapter(Context context, int layout, List<String> list) {
		super(context, layout, list);
		array = context.getResources().getStringArray(
				R.array.subject_info_array);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;

		h.title.setText(getTitleByIndex(position));
		h.content.setText(getItem(position));
		return convertView;
	}

	@Override
	public Holder getViewHolder(View v) {
		return new Holder(v);
	}

	private static class Holder implements ViewHolder {
		public TextView title, content;

		public Holder(View v) {
			title = (TextView) v.findViewById(R.id.list_subject_info_text_title);
			content = (TextView) v.findViewById(R.id.list_subject_info_text_contents);
		}
	}

	// 7~11 max - 95
	private String getTitleByIndex(int index) {
		if (index < 11) {
			return array[index];
		} else {
			while ((index -= 5) > 10)
				;
			return array[index];
		}
	}
}
