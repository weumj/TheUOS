package com.uoscs09.theuos.tab.transport;

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
import com.uoscs09.theuos.annotaion.AsyncData;
import com.uoscs09.theuos.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.base.AbsProgressFragment;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParserTransport;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.SeoulOApiUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class TabTransportFragment extends AbsProgressFragment<Map<String, ArrayList<TransportItem>>> {
    @ReleaseWhenDestroy
    private BaseExpandableListAdapter adapter;
    @AsyncData
    private Map<String, ArrayList<TransportItem>> data;

    private final ParserTransport mParser = new ParserTransport();
    @ReleaseWhenDestroy
    private View empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_transport, container, false);
        ExpandableListView listview = (ExpandableListView) v.findViewById(R.id.tab_transport_listview);

        if (data == null) {
            data = new Hashtable<>();
        }

        adapter = new TransportAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, R.layout.list_layout_transport, data);
        listview.setAdapter(adapter);
        empty = v.findViewById(R.id.empty_view);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                execute();
            }
        });
        listview.setEmptyView(empty);

        registerProgressView(v.findViewById(R.id.progress_layout));

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
                execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void execute() {
        empty.setVisibility(View.INVISIBLE);

        super.execute();
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
        Map<String, ArrayList<TransportItem>> map = new Hashtable<>();
        for (String s : SeoulOApiUtil.Metro.getValues()) {
            ArrayList<TransportItem> up = new ArrayList<>();
            up.addAll(mParser.parse(HttpRequest.getBody(getMetroUrl(s, 1))));

            for (int i = 0; i < up.size(); i++) {
                TransportItem item = up.get(i);
                item.isUpperLine = true;
                up.set(i, item);
            }

            up.addAll(mParser.parse(HttpRequest.getBody(getMetroUrl(s, 2))));
            map.put(s, up);
        }
        return map;
    }

    private String getMetroUrl(String where, int inOut) {
        return SeoulOApiUtil.HOST + "sample" + "/" + SeoulOApiUtil.TYPE_XML + "/" + SeoulOApiUtil.METRO_ARRIVAL + "/" + "1" + "/" + "3" + "/" + where + "/" + inOut + "/" + SeoulOApiUtil.getWeekTag() + "/";
    }


}
