package com.uoscs09.theuos2.tab.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseUnivSchedule;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class UnivScheduleFragment extends AbsProgressFragment<ArrayList<UnivScheduleItem>> {
    private static final String URL = OApiUtil.URL_API_MAIN_DB + '?' + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;

    private final ParseUnivSchedule mParser = new ParseUnivSchedule();

    private ArrayList<UnivScheduleItem> mList = new ArrayList<>();

    Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_univ_schedule, container, false);


        mAdapter = new Adapter(getActivity(), mList);

        StickyListHeadersListView listView = (StickyListHeadersListView) view.findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppUtil.showToast(getActivity(), mList.get(position).content);
            }
        });
        listView.setAdapter(mAdapter);

        execute();

        return view;
    }

    @Override
    public ArrayList<UnivScheduleItem> call() throws Exception {
        HttpURLConnection connection = HttpRequest.getConnection(URL);

        try {
            return mParser.parse(connection.getInputStream());

        } finally {
            connection.disconnect();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tab_restaurant, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                execute();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onTransactResult(ArrayList<UnivScheduleItem> result) {

        mList.clear();
        mList.addAll(result);
        mAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "UnivScheduleFragment";
    }

    private static class Adapter extends AbsArrayAdapter<UnivScheduleItem, ViewHolder> implements StickyListHeadersAdapter {

        public Adapter(Context context, List<UnivScheduleItem> list) {
            super(context, R.layout.list_layoutuniv_schedule, list);
        }

        @Override
        public void onBindViewHolder(int position, UnivScheduleFragment.ViewHolder holder) {
            UnivScheduleItem item = getItem(position);
            holder.textView1.setText(item.content);
            holder.textView2.setText(item.sch_date);
            /*
            UnivScheduleItem.ScheduleDate date = item.dateEnd;

           if (date.isEmpty())
                holder.textView2.setText("");
            else
                holder.textView2.setText("" + date.month + " / " + date.day);
                */

        }

        @Override
        public UnivScheduleFragment.ViewHolder getViewHolder(View convertView) {
            return new UnivScheduleFragment.ViewHolder(convertView);
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
            SimpleViewHolder holder;
            if (convertView == null) {
                holder = new SimpleViewHolder(LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false));
                convertView = holder.itemView;
                convertView.setTag(holder);

            } else {
                holder = (SimpleViewHolder) convertView.getTag();
            }

            UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
            holder.textView.setText("" + date.month + " / " + date.day);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
            return date.month * 100 + date.day;
        }

    }

    static class ViewHolder implements AbsArrayAdapter.ViewHolderable {
        TextView textView1, textView2;

        public ViewHolder(View view) {
            textView1 = (TextView) view.findViewById(android.R.id.text1);
            textView2 = (TextView) view.findViewById(android.R.id.text2);
        }
    }
}
