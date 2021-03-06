package com.uoscs09.theuos2.tab.announce;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

import butterknife.BindView;

class AnnounceAdapter extends AbsArrayAdapter<AnnounceItem, AnnounceAdapter.Holder> {

    public AnnounceAdapter(Context context, List<AnnounceItem> list) {
        super(context, R.layout.list_layout_announce, list);
    }

    /**
     * <내용> 형식의 공지사항을 제대로 표시하기 위해 설정한 패턴
     */
    //private static final Pattern HTML_PATTERN = Pattern.compile(".*<[[a-z][A-Z][0-9]]+>.*");
    @Override
    public void onBindViewHolder(int position, Holder holder) {
        AnnounceItem item = getItem(position);

        if (item != null) {
            holder.title.setText(item.title);
            holder.date.setText(item.date);
        } else {
            holder.title.setText("");
            holder.date.setText("");
        }
        /*
        int i = 0;
        for (TextView tv : holder.textArray) {

            String content = array[i++];

            // HTML로 표현되어야 할 문자열을 HTML로 표현한다.
            // <>이 포함된 문자열이지만 HTML이 아닌것은 Patten으로 거른다.
            if (HTML_PATTERN.matcher(content).find()) {
                CharSequence span = Html.fromHtml(content);
                tv.setText(span != null ? span : content);
            } else {
                tv.setText(content);
            }
        }
        */
    }

    @Override
    public Holder onCreateViewHolder(View v, int viewType) {
        return new Holder(v);
    }


    static class Holder extends AbsArrayAdapter.ViewHolder {
        @BindView(R.id.tab_announce_list_text_title)
        public TextView title;
        @BindView(R.id.tab_announce_list_text_date)
        public TextView date;

        public Holder(View v) {
            super(v);
        }
    }

}

