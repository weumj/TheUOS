package com.uoscs09.theuos;

import java.util.List;

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

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.uoscs09.theuos.common.SimpleTextViewAdapter;
import com.uoscs09.theuos.common.SimpleTextViewAdapter.DrawblePosition;
import com.uoscs09.theuos.common.impl.BaseFragment;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

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
		list = list.subList(0, 7);
		list.add(R.string.title_section_etc);
		list.add(R.string.setting);
		adapter = new SimpleTextViewAdapter.Builder(context,
				R.layout.list_layout_home, list)
				.setDrawablePosition(DrawblePosition.TOP)
				.setTextViewId(R.id.tab_home_text_title)
				.setDrawableTheme(AppTheme.White).setTheme(AppTheme.White)
				.create();
		initDialog(context);
	}

	private void initDialog(Context context) {
		List<Integer> list = AppUtil.loadPageOrder(context);
		list = list.subList(7, list.size());
		AppTheme theme = AppUtil.theme == AppTheme.BlackAndWhite ? AppTheme.White
				: AppUtil.theme;

		View v = View.inflate(context, R.layout.dialog_home_etc, null);
		ListView listView = (ListView) v
				.findViewById(R.id.dialog_home_listview);

		listView.setAdapter(new SimpleTextViewAdapter.Builder(context,
				android.R.layout.simple_list_item_1, list)
				.setDrawablePosition(DrawblePosition.LEFT)
				.setDrawableTheme(theme).setTheme(AppUtil.theme).create());
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				etcDialog.dismiss();
				int item = (Integer) parent.getItemAtPosition(position);
				pl.sendCommand(PagerInterface.Type.PAGE, item);
			}
		});

		etcDialog = new AlertDialog.Builder(context)
				.setCancelable(true)
				.setIcon(AppUtil.getPageIcon(R.string.title_section_etc, theme))
				.setTitle(R.string.tab_etc_selection).create();
		etcDialog.setView(v, 10, 10, 10, 10);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_home, container, false);
		GridView gridView = (GridView) v.findViewById(R.id.tab_home_gridview);
		SwingBottomInAnimationAdapter animatorAdapter = new SwingBottomInAnimationAdapter(
				adapter, 80);
		animatorAdapter.setAbsListView(gridView);
		animatorAdapter.setInitialDelayMillis(150);
		gridView.setOnItemClickListener(this);
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
