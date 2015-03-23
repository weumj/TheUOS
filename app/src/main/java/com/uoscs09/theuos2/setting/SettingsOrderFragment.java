package com.uoscs09.theuos2.setting;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.util.Swappable;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.UOSApplication;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * page 순서를 바꾸는 설정이 있는 fragment
 */
public class SettingsOrderFragment extends Fragment {
    private ArrayList<AppUtil.Page> orderList;
    private DynamicListView mListView;
    private SwapAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        orderList = AppUtil.loadPageOrder2(getActivity());
        mAdapter = new SwapAdapter(getActivity(), orderList);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.setting_order);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.setting_order, container, false);
        mListView = (DynamicListView) rootView.findViewById(R.id.setting_dynamiclistview);

        mListView.setDrawingCacheEnabled(true);
        mListView.setAdapter(mAdapter);
        mListView.enableDragAndDrop();
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
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

        StringBuilder sb = new StringBuilder();

        for(AppUtil.Page p : orderList){
            sb.append(p.order).append('-').append(p.isEnable).append('\n');
        }

        Tracker t = ((UOSApplication)getActivity().getApplication()).getTracker(UOSApplication.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory("setting order fragment")
                .setAction("change tab order")
                .setLabel(sb.toString())
                .build());

        AppUtil.savePageOrder2(orderList, getActivity());
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
                refresh(AppUtil.loadPageOrder2(activity));
                AppUtil.showCanceledToast(activity, true);
                finish();
                return true;
            case R.id.action_goto_default:
                refresh(AppUtil.loadDefaultOrder2());
                AppUtil.showToast(activity, R.string.apply_default, true);
                return true;
            default:
                return false;
        }
    }

    private void finish() {
        getActivity().onBackPressed();
    }

    private void refresh(ArrayList<AppUtil.Page> newList) {
        mAdapter.clear();
        mAdapter.addAll(newList);
        mAdapter.notifyDataSetChanged();
        mListView.destroyDrawingCache();
        mListView.setDrawingCacheEnabled(true);
    }

    private class SwapAdapter extends AbsArrayAdapter<AppUtil.Page, ViewHolder> implements Swappable {

        public SwapAdapter(Context context, List<AppUtil.Page> list) {
            super(context,R.layout.list_layout_order,  list);
        }

        @Override
        public void onBindViewHolder(int position, SettingsOrderFragment.ViewHolder holder) {
            AppUtil.Page item = getItem(position);
            holder.item = item;

            holder.textView.setText(item.stringId);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(AppUtil.getPageIcon(getContext(), item.stringId), 0 ,0, 0);
            holder.checkBox.setChecked(item.isEnable);
        }

        @Override
        public SettingsOrderFragment.ViewHolder getViewHolder(View convertView) {
            return new SettingsOrderFragment.ViewHolder(convertView);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).stringId;
        }

        @Override
        public void swapItems(int positionOne, int positionTwo) {
            AppUtil.Page x = orderList.get(positionOne);
            AppUtil.Page y = orderList.get(positionTwo);

            x.swap(y);
        }


    }

    static class ViewHolder extends AbsArrayAdapter.ViewHolder implements CheckBox.OnCheckedChangeListener{
        public final TextView textView;
        public final CheckBox checkBox;
        AppUtil.Page item;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.setting_order_list_text_tab_title);
            checkBox = (CheckBox) view.findViewById(R.id.setting_order_list_checkbox);
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            item.isEnable = isChecked;
        }
    }
}
