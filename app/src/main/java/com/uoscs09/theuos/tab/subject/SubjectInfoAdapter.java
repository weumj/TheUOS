package com.uoscs09.theuos.tab.subject;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

import java.util.List;

public class SubjectInfoAdapter extends AbsArrayAdapter<String, SubjectInfoHolder> {
    private String[] array;

    public SubjectInfoAdapter(Context context, int layout, List<String> list) {
        super(context, layout, list);
        array = context.getResources().getStringArray(R.array.subject_info_array);
    }

    @Override
    public View setView(int position, View convertView, SubjectInfoHolder holder) {

        holder.title.setText(getTitleByIndex(position));
        holder.content.setText(getItem(position));
        return convertView;
    }

    @Override
    public SubjectInfoHolder getViewHolder(View v) {
        return new SubjectInfoHolder(v);
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

class SubjectInfoHolder implements AbsArrayAdapter.ViewHolder {
    public TextView title, content;

    public SubjectInfoHolder(View v) {
        title = (TextView) v.findViewById(R.id.list_subject_info_text_title);
        content = (TextView) v.findViewById(R.id.list_subject_info_text_contents);
    }
}