package com.uoscs09.theuos.setting;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.DropAndDragListView;
import com.uoscs09.theuos.common.DropAndDragListView.DragListener;
import com.uoscs09.theuos.common.DropAndDragListView.DropListener;
import com.uoscs09.theuos.common.util.AppUtil;

/** page 순서를 바꾸는 설정이 있는 fragment */
public class SettingsOrderFragment extends Fragment implements DragListener,
		DropListener {
	private ArrayList<Integer> orderList;
	private DropAndDragListView listView;
	private boolean isDragAndDropEnable = false;
	private ArrayAdapter<Integer> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		Activity activity = getActivity();
		ActionBar actionBar = activity.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setTitle(R.string.setting_order);
		orderList = AppUtil.loadPageOrder(activity);

		switch (AppUtil.theme) {
		case Black:
			adapter = new OrderListAdapter(activity,
					R.layout.list_layout_order_dark, orderList);
			break;
		case White:
		case BlackAndWhite:
		default:
			adapter = new OrderListAdapter(activity,
					R.layout.list_layout_order, orderList);
			break;
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.setting_order, container,
				false);
		listView = (DropAndDragListView) rootView
				.findViewById(R.id.order_dropAndDragListView);

		listView.setDragImageX(60);
		listView.setDrawingCacheEnabled(true);
		listView.setDragListener(this);
		listView.setDropListener(this);
		listView.setAdapter(adapter);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.setting_order_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.setting_order, menu);
			break;
		}
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

	@Override
	public void drag(int from, int to) {
		if (!isDragAndDropEnable) {
			isDragAndDropEnable = true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(int from, int to) {
		if (isDragAndDropEnable) {
			if (from == to)
				return;
			ArrayList<Integer> newList = (ArrayList<Integer>) orderList.clone();
			int fromItem = newList.remove(from);
			newList.add(to, fromItem);
			refresh(newList);
			isDragAndDropEnable = false;
		}
	}

	private void refresh(ArrayList<Integer> newList) {
		adapter.clear();
		adapter.addAll(newList);
		adapter.notifyDataSetChanged();
		listView.destroyDrawingCache();
		listView.setDrawingCacheEnabled(true);
	}
}
