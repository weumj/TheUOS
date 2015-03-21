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

public class TransportAdapter extends
		AbsExpendableAdapter<String, TransportItem> {

	public TransportAdapter(Context context, int groupLayout, int childLayout,
			Map<String, ? extends List<TransportItem>> data) {
		super(context, groupLayout, childLayout, data);
	}

	@Override
	protected void setGroupView(int groupPosition, boolean isExpanded, View v,
			ViewGroup parent, ViewHolder h) {
		GroupHolder gh = (GroupHolder) h;
		gh.title.setText(SeoulOApiUtil
				.getStationName(getGroupKey(groupPosition).toString()));
	}

	@Override
	protected void setChildView(int groupPosition, int childPosition,
			boolean isLastChild, View v, ViewGroup parent, ViewHolder h) {
		ChildHolder ch = (ChildHolder) h;
		TransportItem item = getChild(groupPosition, childPosition);

		String text = item.location + StringUtil.SPACE + item.arrivalTime;
		if (item.isUpperLine) {
			ch.tvs[0].setText(text);
			ch.tvs[1].setText(StringUtil.NULL);
		} else {
			ch.tvs[0].setText(StringUtil.NULL);
			ch.tvs[1].setText(text);
		}
	}

	@Override
	protected ViewHolder getViewHolder(View v, boolean isChild) {
		return isChild ? new ChildHolder(v) : new GroupHolder(v);
	}

	private static class ChildHolder implements ViewHolder {
		public final TextView[] tvs;

		public ChildHolder(View v) {
			tvs = new TextView[] {
					(TextView) v.findViewById(R.id.tab_transport_up_text),
					(TextView) v.findViewById(R.id.tab_transport_down_text) };
		}
	}

	private static class GroupHolder implements ViewHolder {
		public final TextView title;

		public GroupHolder(View v) {
			title = (TextView) v.findViewById(android.R.id.text1);
		}
	}
}
