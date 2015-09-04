package com.uoscs09.theuos2.tab.transport;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.SeoulOApiUtil;

import java.util.ArrayList;
import java.util.Map;

public class TabTransportFragment extends AbsProgressFragment<Map<String, ArrayList<TransportItem>>>
        implements Request.ResultListener<Map<String, ArrayList<TransportItem>>>, Request.ErrorListener {
    private BaseExpandableListAdapter adapter;
    @AsyncData
    private Map<String, ArrayList<TransportItem>> data;

    private final ParseTransport mParser = new ParseTransport();
    private View empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_transport, container, false);
        ExpandableListView listview = (ExpandableListView) v.findViewById(R.id.tab_transport_listview);

        if (data == null) {
            data = new SerializableArrayMap<>();
        }

        adapter = new TransportAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, data);
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

    private void execute() {
        empty.setVisibility(View.INVISIBLE);

        execute(true, mRequest, this, this, true);
    }

    Request<Map<String, ArrayList<TransportItem>>> mRequest = new AbstractRequest<Map<String,ArrayList<TransportItem>>>() {
        @Override
        public Map<String, ArrayList<TransportItem>> get() throws Exception {
            Map<String, ArrayList<TransportItem>> map = new SerializableArrayMap<>();


            for (String s : SeoulOApiUtil.Metro.getValues()) {
                ArrayList<TransportItem> up = new ArrayList<>();

                up.addAll(
                        HttpRequest.Builder.newStringRequestBuilder(getMetroUrl(s, 1))
                                .build()
                                .wrap(mParser)
                                .get()
                );

                for (int i = 0; i < up.size(); i++) {
                    TransportItem item = up.get(i);
                    item.isUpperLine = true;
                    up.set(i, item);
                }

                up.addAll(
                        HttpRequest.Builder.newStringRequestBuilder(getMetroUrl(s, 2))
                                .build()
                                .wrap(mParser)
                                .get()
                );
                map.put(s, up);
            }
            return map;
        }
    };

    @Override
    public void onResult(Map<String, ArrayList<TransportItem>> result) {
        boolean empty = true;
        for (ArrayList<TransportItem> item : result.values()) {
            if (!item.isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty) {
            AppUtil.showToast(getActivity(), R.string.tab_rest_no_info, isMenuVisible());
        } else {
            data.clear();
            data.putAll(result);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(Exception e) {

    }


    private static String getMetroUrl(String where, int inOut) {
        return SeoulOApiUtil.HOST + "sample" + "/" + SeoulOApiUtil.TYPE_XML + "/" + SeoulOApiUtil.METRO_ARRIVAL + "/" + "1" + "/" + "3" + "/" + where + "/" + inOut + "/" + SeoulOApiUtil.getWeekTag() + "/";
    }


    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabTransportFragment";
    }
}
