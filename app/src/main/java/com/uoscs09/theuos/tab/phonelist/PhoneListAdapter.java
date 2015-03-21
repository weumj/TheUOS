package com.uoscs09.theuos.tab.phonelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.base.AbsArrayAdapter;
import com.uoscs09.theuos.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PhoneListAdapter extends AbsArrayAdapter<PhoneItem, Holder> {
    protected Filter filter;
    private final View.OnClickListener l;

    public PhoneListAdapter(Context context, int layout,List<PhoneItem> telList, View.OnClickListener l) {
        super(context, layout, telList);
        this.l = l;
    }

    @Override
    public void onBindViewHolder(int position, Holder holder) {
        PhoneItem item = getItem(position);
        holder.siteView.setText(item.siteName);
        holder.phoneView.setText(item.sitePhoneNumber);
        holder.imgButton.setOnClickListener(l);
        holder.imgButton.setTag(item);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
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
                    List<PhoneItem> beFilteredList = PhoneNumberDB.getInstance(getContext()).readAll(StringUtil.NULL);
                    List<PhoneItem> filterList = new ArrayList<>();

                    if (constraint != null && constraint.length() > 0) {
                        PhoneItem item;
                        for (int i = 0; i < beFilteredList.size(); i++) {
                            item = beFilteredList.get(i);
                            if (item.siteName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
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
    public Holder getViewHolder(View view) {
        return new Holder(view);
    }


}

class Holder implements AbsArrayAdapter.ViewHolderable {
    public final TextView siteView;
    public final TextView phoneView;
    public final ImageButton imgButton;

    public Holder(View convertView) {
        phoneView = (TextView) convertView.findViewById(R.id.tab_phone_list_text_site_tel_number);
        siteView = (TextView) convertView.findViewById(R.id.tab_phone_list_text_site_name);
        imgButton = (ImageButton) convertView.findViewById(R.id.tab_phone_list_button_site_call);
    }
}