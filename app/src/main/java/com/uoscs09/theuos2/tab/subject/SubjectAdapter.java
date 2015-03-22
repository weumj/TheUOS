package com.uoscs09.theuos2.tab.subject;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

@Deprecated
public class SubjectAdapter extends AbsArrayAdapter<SubjectItem, SubjectAdapter.Holder> {

    public SubjectAdapter(Context context, List<SubjectItem> list) {
        super(context, R.layout.list_layout_subject, list);
    }

    @Override
    public void onBindViewHolder(int position, Holder holder) {
        SubjectItem item = getItem(position);
        int i = 0;

        for (TextView v : holder.tvArray) {
            if (i == 2 || i == 9) {
                i++;
            }
            v.setText(item.infoArray[i++]);
        }
    }

    @Override
    public Holder getViewHolder(View v) {
        return new Holder(v);
    }

    public static class Holder implements AbsArrayAdapter.ViewHolderable {
        public final TextView[] tvArray;

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
}

