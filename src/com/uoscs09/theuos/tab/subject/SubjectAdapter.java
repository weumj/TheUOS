package com.uoscs09.theuos.tab.subject;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class SubjectAdapter extends AbsArrayAdapter<SubjectItem> {
	private SubjectAdapter(Context context) {
		super(context, 0);
	}

	public SubjectAdapter(Context context, int layout, List<SubjectItem> list) {
		super(context, layout, list);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		SubjectItem item = getItem(position);
		Holder h = (Holder) holder;
		int i = 0;

		for (TextView v : h.tvArray) {
			if (i == 2 || i == 9) {
				i++;
			}
			v.setText(item.infoArray[i++]);
		}
		setViewSize(h, TabSearchSubjectFragment.width);
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		Holder h = new Holder(v);
		int px = getContext().getApplicationContext().getResources()
				.getDisplayMetrics().widthPixels / 12;
		setViewSize(h, px);
		return h;
	}

	private void setViewSize(Holder h, int px) {
		int[] sizes = { 2, 2, 2, 1, 4, 1, 1, 2, 5, 1, 1 };

		for (int i = 0; i < 11; i++) {
			h.tvArray[i].setWidth(px * sizes[i]);
		}
	}

	public static class Holder implements ViewHolder {
		public TextView[] tvArray;

		// public TextView sub_dept;
		// public TextView subject_div;
		// public TextView subject_no;
		// public TextView class_div;
		// public TextView subject_nm;
		// public TextView shyr;
		// public TextView credit;
		// public TextView prof_nm;
		// public TextView class_nm;
		// public TextView tlsn_count;
		// public TextView tlsn_limit_count;
		public Holder(View v) {
			tvArray = new TextView[11];
			int[] idArray = { R.id.list_subject_text_sub_dept,
					R.id.list_subject_text_sub_div, R.id.list_subject_text_no,
					R.id.list_subject_text_class_div,
					R.id.list_subject_text_sub_nm, R.id.list_subject_text_yr,
					R.id.list_subject_text_credit,
					R.id.list_subject_text_prof_nm,
					R.id.list_subject_text_class_nm,
					R.id.list_subject_text_tlsn_cnt,
					R.id.list_subject_text_tlsn_limit };
			for (int i = 0; i < 11; i++) {
				tvArray[i] = (TextView) v.findViewById(idArray[i]);
			}
		}
	}

}
