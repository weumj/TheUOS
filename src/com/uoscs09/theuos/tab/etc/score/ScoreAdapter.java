package com.uoscs09.theuos.tab.etc.score;

import java.lang.ref.WeakReference;
import java.util.List;

import com.uoscs09.theuos.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ScoreAdapter extends BaseExpandableListAdapter {
	private List<ScoreItem> list;
	private WeakReference<Context> contextRef;

	public ScoreAdapter(Context context, List<ScoreItem> list) {
		super();
		this.contextRef = new WeakReference<Context>(
				context.getApplicationContext());
		this.list = list;
	}

	@Override
	public Object getChild(int g, int c) {
		return list.get(g).list.get(c);
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		return 0;
	}

	@Override
	public View getChildView(int g, int c, boolean isLastChild, View v,
			ViewGroup parent) {
		TextView t1, t2, t3, t4, t5;
		if (v == null) {
			Context context = contextRef.get();
			if(context == null)
				throw new RuntimeException();
			v = LayoutInflater.from(context).inflate(
					R.layout.ex_list_layout_score_child, parent, false);
			t1 = (TextView) v.findViewById(R.id.score_textView1);
			t2 = (TextView) v.findViewById(R.id.score_textView2);
			t3 = (TextView) v.findViewById(R.id.score_textView3);
			t4 = (TextView) v.findViewById(R.id.score_textView4);
			t5 = (TextView) v.findViewById(R.id.score_textView5);

			v.setTag(R.id.score_textView1, t1);
			v.setTag(R.id.score_textView2, t2);
			v.setTag(R.id.score_textView3, t3);
			v.setTag(R.id.score_textView4, t4);
			v.setTag(R.id.score_textView5, t5);
		} else {
			t1 = (TextView) v.getTag(R.id.score_textView1);
			t2 = (TextView) v.getTag(R.id.score_textView2);
			t3 = (TextView) v.getTag(R.id.score_textView3);
			t4 = (TextView) v.getTag(R.id.score_textView4);
			t5 = (TextView) v.getTag(R.id.score_textView5);
		}
		DetailScoreItem item = (DetailScoreItem) getChild(g, c);
		t1.setText(item.type);
		t2.setText(item.class_eval_item);
		t3.setText("원점수 : " + item.raw_score);
		t4.setText("표준점수 : " + item.eval_grade);
		t5.setText("평균점수 :" + item.ave);
		return v;
	}

	@Override
	public int getChildrenCount(int g) {
		return list.get(g).list.size();
	}

	@Override
	public Object getGroup(int g) {
		return list.get(g);
	}

	@Override
	public int getGroupCount() {
		return list.size();
	}

	@Override
	public long getGroupId(int arg0) {
		return 0;
	}

	@Override
	public View getGroupView(int g, boolean isExpand, View v, ViewGroup parent) {
		TextView title;
		if (v == null) {
			Context context = contextRef.get();
			//if(context == null)
			//	throw new RuntimeException();
			v = LayoutInflater.from(context).inflate(
					R.layout.ex_list_layout_score_group, parent, false);
			title = (TextView) v.findViewById(R.id.score_textView1);
			v.setTag(R.id.score_textView1, title);
		} else {
			title = (TextView) v.getTag(R.id.score_textView1);
		}
		title.setText(((ScoreItem) getGroup(g)).title);
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int g, int c) {
		return false;
	}
}
