package com.uoscs09.theuos2.tab.subject;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

@Deprecated
public class SubjectInfoAdapter extends AbsArrayAdapter<String, SubjectInfoAdapter.SubjectInfoHolder> {
    private final String[] array;

    public SubjectInfoAdapter(Context context, int layout, List<String> list) {
        super(context, layout, list);
        array = context.getResources().getStringArray(R.array.subject_info_array);
    }

    @Override
    public void onBindViewHolder(int position, SubjectInfoHolder holder) {

        holder.title.setText(getTitleByIndex(position));
        holder.content.setText(getItem(position));
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

    public static class SubjectInfoHolder implements AbsArrayAdapter.ViewHolderable {
        public final TextView title;
        public final TextView content;

        public SubjectInfoHolder(View v) {
            title = (TextView) v.findViewById(R.id.list_subject_info_text_title);
            content = (TextView) v.findViewById(R.id.list_subject_info_text_contents);
        }
    }
}

