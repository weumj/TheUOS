package com.uoscs09.theuos2.tab.subject;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

import butterknife.Bind;


class SubjectAdapter2 extends AbsArrayAdapter<SubjectItem2, SubjectAdapter2.ViewHolder> {

    public SubjectAdapter2(Context context, List<SubjectItem2> list) {
        super(context, R.layout.list_layout_subject, list);
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        SubjectItem2 item = getItem(position);

        TextView[] array = holder.tvArray;

        int i = 0;
        item.setInfoArray();
        for (String a : item.infoArray) {
            array[i++].setText(a);
        }

        array[8].setText(item.getClassRoomInformation());

        /*
        array[0].setText(item.sub_dept);
        array[1].setText(item.subject_div);
        array[2].setText(item.subject_no);
        array[3].setText(item.classDiv);
        array[4].setText(item.subject_nm);
        array[5].setText(Integer.toString(item.shyr));
        array[6].setText(Integer.toString(item.credit));
        array[7].setText(item.prof_nm);
        array[8].setText(item.class_nm);
        array[9].setText(Integer.toString(item.tlsn_count));
        array[10].setText(Integer.toString(item.tlsn_limit_count));
        */
    }

    @Override
    public ViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new ViewHolder(convertView);
    }

    static class ViewHolder extends AbsArrayAdapter.ViewHolder {
        @Bind({
                R.id.list_subject_text_sub_dept,
                R.id.list_subject_text_sub_div,
                R.id.list_subject_text_no,
                R.id.list_subject_text_class_div,
                R.id.list_subject_text_sub_nm,
                R.id.list_subject_text_yr,
                R.id.list_subject_text_credit,
                R.id.list_subject_text_prof_nm,
                R.id.list_subject_text_class_nm,
                R.id.list_subject_text_tlsn_cnt,
                R.id.list_subject_text_tlsn_limit})
        public TextView[] tvArray;

        // public SubjectItem2 item;

        public ViewHolder(View view) {
            super(view);
        }
    }
}
