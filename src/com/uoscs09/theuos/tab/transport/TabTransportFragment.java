package com.uoscs09.theuos.tab.transport;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.SeoulOApiUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabTransportFragment extends
		AbsDrawableProgressFragment<Map<String, ArrayList<TransportItem>>> {
	@ReleaseWhenDestroy
	private BaseExpandableListAdapter adapter;
	@AsyncData
	private Map<String, ArrayList<TransportItem>> data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setLoadingViewEnable(false);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_transport, container, false);
		ExpandableListView listview = (ExpandableListView) v
				.findViewById(R.id.tab_transport_listview);

		if (data == null) {
			data = new Hashtable<String, ArrayList<TransportItem>>();
		}
		adapter = new TransportAdapter(getActivity(),
				android.R.layout.simple_expandable_list_item_1,
				R.layout.list_layout_transport, data);
		listview.setAdapter(adapter);
		View empty = v.findViewById(R.id.empty_view);
		empty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				excute();
			}
		});
		listview.setEmptyView(empty);
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_restaurant, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			excute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTransactResult(Map<String, ArrayList<TransportItem>> result) {
		boolean empty = true;
		for (ArrayList<TransportItem> item : result.values()) {
			if (!item.isEmpty()) {
				empty = false;
				break;
			}
		}
		if (empty) {
			AppUtil.showToast(getActivity(), R.string.tab_rest_no_info,
					isMenuVisible());
		} else {
			data.clear();
			data.putAll(result);
			adapter.notifyDataSetChanged();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ArrayList<TransportItem>> call() throws Exception {
		Map<String, ArrayList<TransportItem>> map = new Hashtable<String, ArrayList<TransportItem>>();
		for (String s : SeoulOApiUtil.Metro.getValues()) {
			ArrayList<TransportItem> up = (ArrayList<TransportItem>) ParseFactory
					.create(ParseFactory.What.Transport,
							HttpRequest.getBody(getMetroUrl(s, 1)), 0).parse();
			for (int i = 0; i < up.size(); i++) {
				TransportItem item = up.get(i);
				item.isUpperLine = true;
				up.set(i, item);
			}
			ArrayList<TransportItem> down = (ArrayList<TransportItem>) ParseFactory
					.create(ParseFactory.What.Transport,
							HttpRequest.getBody(getMetroUrl(s, 2)), 0).parse();
			up.addAll(down);
			map.put(s, up);
		}
		return map;
	}

	private String getMetroUrl(String where, int inOut) {
		StringBuilder sb = new StringBuilder();
		sb.append(SeoulOApiUtil.HOST)
				.append(/* SeoulOApiUtil.OAPI_KEY */"sample" + "/")
				.append(SeoulOApiUtil.TYPE_XML + "/")
				.append(SeoulOApiUtil.METRO_ARRIVAL + "/").append("1" + "/")
				.append("3" + "/").append(where + "/").append(inOut + "/")
				.append(SeoulOApiUtil.getWeekTag() + "/");
		return sb.toString();
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_refresh);
	}

}
