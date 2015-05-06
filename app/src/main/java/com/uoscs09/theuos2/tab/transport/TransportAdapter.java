package com.uoscs09.theuos2.tab.transport;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsExpendableAdapter;
import com.uoscs09.theuos2.util.SeoulOApiUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.List;
import java.util.Map;

public class TransportAdapter extends AbsExpendableAdapter<String, TransportItem, GroupHolder, ChildHolder> {

    public TransportAdapter(Context context, int groupLayout, Map<String, ? extends List<TransportItem>> data) {
        super(context, groupLayout, R.layout.list_layout_transport, data);
    }

    @Override
    protected void setGroupView(int groupPosition, boolean isExpanded, View v, ViewGroup parent, GroupHolder h) {
        h.title.setText(SeoulOApiUtil.getStationName(getGroupKey(groupPosition).toString()));
    }

    @Override
    protected void setChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent, ChildHolder h) {
        TransportItem item = getChild(groupPosition, childPosition);

        String text = item.location + StringUtil.SPACE + item.arrivalTime;
        if (item.isUpperLine) {
            h.tvs[0].setText(text);
            h.tvs[1].setText(StringUtil.NULL);
        } else {
            h.tvs[0].setText(StringUtil.NULL);
            h.tvs[1].setText(text);
        }
    }

    @Override
    protected GroupHolder getGroupViewHolder(View v) {
        return new GroupHolder(v);
    }

    @Override
    protected ChildHolder getChildViewHolder(View v) {
        return new ChildHolder(v);
    }

}

class ChildHolder implements AbsExpendableAdapter.ViewHolder {
    public final TextView[] tvs;

    public ChildHolder(View v) {
        tvs = new TextView[]{
                (TextView) v.findViewById(R.id.tab_transport_up_text),
                (TextView) v.findViewById(R.id.tab_transport_down_text)
        };
    }
}

class GroupHolder implements AbsExpendableAdapter.ViewHolder {
    public final TextView title;

    public GroupHolder(View v) {
        title = (TextView) v.findViewById(android.R.id.text1);
    }
}