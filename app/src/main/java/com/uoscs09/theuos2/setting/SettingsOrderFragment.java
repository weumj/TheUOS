package com.uoscs09.theuos2.setting;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.util.Swappable;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseFragment;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * page 순서를 바꾸는 설정이 있는 fragment
 */
public class SettingsOrderFragment extends BaseFragment {
    private static final String TAG = "SettingsOrderFragment";

    @BindView(R.id.setting_dynamiclistview)
    DynamicListView mListView;

    private ArrayList<AppUtil.TabInfo> orderList;
    private SwapAdapter mAdapter;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        orderList = AppUtil.TabInfo.loadEnabledTabOrderForSetting();
        mAdapter = new SwapAdapter(getActivity(), orderList);
        super.onCreate(savedInstanceState);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(R.string.setting_order);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.setting_order, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        mListView.setDrawingCacheEnabled(true);
        mListView.setAdapter(mAdapter);
        mListView.enableDragAndDrop();
        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            mListView.startDragging(position);
            return true;
        });

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (unbinder != null) unbinder.unbind();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.setting_order, menu);
    }

    private void saveTabOrderList() {

        StringBuilder sb = new StringBuilder();

        for (AppUtil.TabInfo p : orderList) {
            sb.append(p.defaultOrder).append('-').append(p.isEnable()).append('\n');
        }

        sendTrackerEvent("change tab order", sb.toString());

        AppUtil.TabInfo.saveTabOrderList(orderList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_apply:
                saveTabOrderList();
                getActivity().setResult(AppUtil.RELAUNCH_ACTIVITY);
                finish();
                return true;

            case R.id.action_cancel:
                //refresh(AppUtil.TabInfo.loadEnabledTabOrder());
                AppUtil.showCanceledToast(getActivity(), true);
                finish();
                return true;

            case R.id.action_goto_default:
                refresh(AppUtil.TabInfo.loadDefaultOrder());
                AppUtil.showToast(getActivity(), R.string.apply_default, true);
                return true;

            default:
                return false;
        }
    }

    private void finish() {
        getActivity().onBackPressed();
    }

    private void refresh(ArrayList<AppUtil.TabInfo> newList) {
        mAdapter.clear();
        mAdapter.addAll(newList);
        mAdapter.notifyDataSetChanged();
        mListView.destroyDrawingCache();
        mListView.setDrawingCacheEnabled(true);
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return TAG;
    }

    private class SwapAdapter extends AbsArrayAdapter<AppUtil.TabInfo, ViewHolder> implements Swappable {

        SwapAdapter(Context context, List<AppUtil.TabInfo> list) {
            super(context, R.layout.list_layout_order, list);
        }

        @Override
        public void onBindViewHolder(int position, SettingsOrderFragment.ViewHolder holder) {
            AppUtil.TabInfo item = getItem(position);
            holder.item = item;

            holder.textView.setText(item.titleResId);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(holder.itemView.getContext()), 0,0,0);
            holder.checkBox.setChecked(item.isEnable());
        }

        @Override
        public SettingsOrderFragment.ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new SettingsOrderFragment.ViewHolder(convertView);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).titleResId;
        }

        @Override
        public void swapItems(int positionOne, int positionTwo) {
            Collections.swap(orderList, positionOne, positionTwo);
        }


    }

    static class ViewHolder extends AbsArrayAdapter.ViewHolder implements CheckBox.OnCheckedChangeListener {
        public final TextView textView;
        final CheckBox checkBox;
        AppUtil.TabInfo item;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.setting_order_list_text_tab_title);
            checkBox = (CheckBox) view.findViewById(R.id.setting_order_list_checkbox);
            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            item.setEnable(isChecked);
        }
    }
}
