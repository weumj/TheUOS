package com.uoscs09.theuos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.uoscs09.theuos.common.SimpleTextViewAdapter;
import com.uoscs09.theuos.common.SimpleTextViewAdapter.DrawblePosition;
import com.uoscs09.theuos.common.impl.BaseFragment;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

import java.util.List;

public class TabHomeFragment extends BaseFragment implements
		OnItemClickListener {
	@ReleaseWhenDestroy
	private ArrayAdapter<Integer> adapter;
	@ReleaseWhenDestroy
	protected PagerInterface pl;
	@ReleaseWhenDestroy
	protected AlertDialog etcDialog;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof PagerInterface) {
			pl = (PagerInterface) activity;
		} else {
			pl = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getActivity();
		List<Integer> list = AppUtil.loadPageOrder(context);
		if (AppUtil.isScreenSizeSmall(getActivity())) {
			initDialog(context);
			list = list.subList(0, 7);
			list.add(R.string.title_section_etc);
		}
		list.add(R.string.setting);

		adapter = new SimpleTextViewAdapter.Builder(context,
				R.layout.list_layout_home, list)
				.setDrawablePosition(DrawblePosition.TOP)
				.setTextViewId(R.id.tab_home_text_title)
				.setTextViewTextColor(
						getResources().getColor(android.R.color.white))
				.setDrawableTheme(AppTheme.Black).create();
	}

	private void initDialog(Context context) {
		List<Integer> list = AppUtil.loadPageOrder(context);
		list = list.subList(7, list.size());
		AppTheme theme = AppUtil.theme == AppTheme.BlackAndWhite ? AppTheme.White
				: AppUtil.theme;

		View v = View.inflate(context, R.layout.dialog_home_etc, null);
		ListView listView = (ListView) v.findViewById(R.id.dialog_home_listview);

		listView.setAdapter(new SimpleTextViewAdapter.Builder(context,
				android.R.layout.simple_list_item_1, list)
				.setDrawablePosition(DrawblePosition.LEFT)
				.setDrawableTheme(theme).create());
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				etcDialog.dismiss();
				int item = (Integer) parent.getItemAtPosition(position);
				pl.sendCommand(PagerInterface.Type.PAGE, item);
			}
		});

		etcDialog = new MaterialDialog.Builder(context)
                .cancelable(true)
                .iconAttr(R.attr.ic_navigation_accept)
                .title(R.string.tab_etc_selection)
                .customView(v, false)
                .build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_home, container, false);
		GridView gridView = (GridView) v.findViewById(R.id.tab_home_gridview);
		SwingBottomInAnimationAdapter animatorAdapter = new SwingBottomInAnimationAdapter(
				adapter);
		animatorAdapter.setAbsListView(gridView);
		if (AppUtil.isScreenSizeSmall(getActivity())) {
			gridView.setOnItemClickListener(this);
		} else {
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position == parent.getAdapter().getCount() - 1)
						pl.sendCommand(PagerInterface.Type.SETTING, null);
					else
						pl.sendCommand(PagerInterface.Type.PAGE,
								parent.getItemAtPosition(position));
				}
			});
		}

		gridView.setAdapter(animatorAdapter);

		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
		if (pl != null) {
			switch (pos) {
			case 8:
				pl.sendCommand(PagerInterface.Type.SETTING, null);
				break;
			case 7:
				etcDialog.show();
				break;
			default:
				pl.sendCommand(PagerInterface.Type.PAGE,
						adapterView.getItemAtPosition(pos));
				break;
			}
		}
	}
}
