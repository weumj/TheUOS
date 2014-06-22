package com.uoscs09.theuos.tab.phonelist;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class PhoneListAdapter extends AbsArrayAdapter<PhoneItem> {
	protected Filter filter;
	private View.OnClickListener l;

	private PhoneListAdapter(Context context) {
		super(context, 0);
	}

	public PhoneListAdapter(Context context, int layout,
			List<PhoneItem> telList, View.OnClickListener l) {
		super(context, layout, telList);
		this.l = l;
	}

	@Override
	public View setView(int position, View convertView, ViewHolder wrapper) {
		PhoneViewHolder h = (PhoneViewHolder) wrapper;

		PhoneItem item = getItem(position);
		h.siteView.setText(item.siteName);
		h.phoneView.setText(item.sitePhoneNumber);
		h.imgButton.setOnClickListener(l);
		h.imgButton.setTag(item);
		return convertView;
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results.count == 0) {
						notifyDataSetInvalidated();
					} else {
						clear();
						addAll((ArrayList<PhoneItem>) results.values);
						notifyDataSetChanged();
					}
				}

				@SuppressLint("DefaultLocale")
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults results = new FilterResults();
					List<PhoneItem> beFilteredList = PhoneNumberDB.getInstance(
							getContext()).readAll();
					List<PhoneItem> filterList = new ArrayList<PhoneItem>();
					if (constraint != null && constraint.length() > 0) {
						PhoneItem item;
						for (int i = 0; i < beFilteredList.size(); i++) {
							item = beFilteredList.get(i);
							if (item.siteName.toLowerCase().startsWith(
									constraint.toString().toLowerCase())) {
								filterList.add(item);
							}
						}
					} else {
						filterList.addAll(beFilteredList);
					}
					results.values = filterList;
					results.count = filterList.size();
					return results;
				}
			};
		}
		return filter;
	}

	@Override
	public ViewHolder getViewHolder(View view) {
		return new PhoneViewHolder(view);
	}

	protected static class PhoneViewHolder implements ViewHolder {
		public TextView siteView;
		public TextView phoneView;
		public ImageButton imgButton;

		public PhoneViewHolder(View convertView) {
			phoneView = (TextView) convertView
					.findViewById(R.id.tab_phone_list_text_site_tel_number);
			siteView = (TextView) convertView.findViewById(R.id.tab_phone_list_text_site_name);
			imgButton = (ImageButton) convertView
					.findViewById(R.id.tab_phone_list_button_site_call);
		}
	}
}
