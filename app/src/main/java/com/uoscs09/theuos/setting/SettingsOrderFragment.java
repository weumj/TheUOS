package com.uoscs09.theuos.setting;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.util.Swappable;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.SimpleTextViewAdapter;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.AppUtil.AppTheme;

import java.util.ArrayList;
import java.util.List;

/** page 순서를 바꾸는 설정이 있는 fragment */
public class SettingsOrderFragment extends Fragment {
	ArrayList<Integer> orderList;
	DynamicListView mListView;
	private ArrayAdapter<Integer> mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		orderList = AppUtil.loadPageOrder(getActivity());
		mAdapter = new SwapAdapter(getActivity(), R.layout.list_layout_order,
				orderList);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		ActionBarActivity activity = (ActionBarActivity) getActivity();
		activity.getSupportActionBar().setTitle(R.string.setting_order);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.setting_order, container,
				false);
		mListView = (DynamicListView) rootView
				.findViewById(R.id.setting_dynamiclistview);

		mListView.setDrawingCacheEnabled(true);
		mListView.setAdapter(mAdapter);
		mListView.enableDragAndDrop();
		mListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(final AdapterView<?> parent,
							View view, int position, long id) {
						mListView.startDragging(position);
						return true;
					}
				});

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.setting_order, menu);
	}

	private void saveTabOrderList() {
		AppUtil.savePageOrder(orderList, getActivity());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity activity = getActivity();
		switch (item.getItemId()) {
		case R.id.action_apply:
			saveTabOrderList();
			finish();
			activity.setResult(AppUtil.RELAUNCH_ACTIVITY);
			return true;
		case R.id.action_cancel:
			refresh(AppUtil.loadPageOrder(activity));
			AppUtil.showCanceledToast(activity, true);
			finish();
			return true;
		case R.id.action_goto_default:
			refresh(AppUtil.loadDefaultPageOrder());
			AppUtil.showToast(activity, R.string.apply_default, true);
			return true;
		default:
			return false;
		}
	}

	private void finish() {
		getActivity().onBackPressed();
	}

	private void refresh(ArrayList<Integer> newList) {
		mAdapter.clear();
		mAdapter.addAll(newList);
		mAdapter.notifyDataSetChanged();
		mListView.destroyDrawingCache();
		mListView.setDrawingCacheEnabled(true);
	}

	private class SwapAdapter extends SimpleTextViewAdapter implements
			Swappable {

		public SwapAdapter(Context context, int layout, List<Integer> list) {
			super(context, layout, list);
			this.textViewId = R.id.setting_order_list_text_tab_title;
			if (AppUtil.theme == AppTheme.BlackAndWhite) {
				this.iconTheme = AppTheme.White;
			}
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).longValue();
		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			int x = orderList.get(positionOne);
			int y = orderList.get(positionTwo);
			orderList.remove(positionOne);
			orderList.add(positionOne, y);

			orderList.remove(positionTwo);
			orderList.add(positionTwo, x);

		}
	}
}
