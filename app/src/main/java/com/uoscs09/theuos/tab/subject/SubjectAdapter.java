package com.uoscs09.theuos.tab.subject;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

import java.util.List;

public class SubjectAdapter extends AbsArrayAdapter<SubjectItem, Holder> {

    public SubjectAdapter(Context context, int layout, List<SubjectItem> list) {
        super(context, layout, list);
    }

    @Override
    public View setView(int position, View convertView, Holder holder) {
        SubjectItem item = getItem(position);
        int i = 0;

        for (TextView v : holder.tvArray) {
            if (i == 2 || i == 9) {
                i++;
            }
            v.setText(item.infoArray[i++]);
        }
        return convertView;
    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }

}

class Holder implements AbsArrayAdapter.ViewHolder {
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
        final int[] idArray = {R.id.list_subject_text_sub_dept,
                R.id.list_subject_text_sub_div, R.id.list_subject_text_no,
                R.id.list_subject_text_class_div,
                R.id.list_subject_text_sub_nm, R.id.list_subject_text_yr,
                R.id.list_subject_text_credit,
                R.id.list_subject_text_prof_nm,
                R.id.list_subject_text_class_nm,
                R.id.list_subject_text_tlsn_cnt,
                R.id.list_subject_text_tlsn_limit};
        for (int i = 0; i < 11; i++) {
            tvArray[i] = (TextView) v.findViewById(idArray[i]);
        }
    }
}