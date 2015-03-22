package com.uoscs09.theuos2;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseFragment;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.List;

public class TabHomeFragment extends BaseFragment implements OnItemClickListener {
    @ReleaseWhenDestroy
    private AlertDialog etcDialog;

    private static class GridViewAdapter extends AbsArrayAdapter.SimpleAdapter<Integer> {

        public GridViewAdapter(Context context, List<Integer> list) {
            super(context, R.layout.list_layout_home, list);
        }

        @Override
        public void onBindViewHolder(int position, SimpleViewHolder holder) {
            super.onBindViewHolder(position, holder);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, AppUtil.getPageIconWhite(getItem(position)), 0, 0);
        }

        @Override
        public String getTextFromItem(Integer item) {
            return getContext().getString(item);
        }
    }

    private static class DialogAdapter extends AbsArrayAdapter.SimpleAdapter<Integer> {

        public DialogAdapter(Context context, List<Integer> list) {
            super(context, android.R.layout.simple_list_item_1, list);
        }

        @Override
        public void onBindViewHolder(int position, SimpleViewHolder holder) {
            super.onBindViewHolder(position, holder);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(AppUtil.getPageIcon(getContext(), getItem(position)), 0, 0, 0);
        }

        @Override
        public String getTextFromItem(Integer item) {
            return getContext().getString(item);
        }
    }

    private void initEtcMenuDialog(Context context) {
        List<Integer> list = AppUtil.loadEnabledPageOrder(context);
        list = list.subList(7, list.size());

        View dialogView = View.inflate(context, R.layout.dialog_home_etc, null);
        ListView listView = (ListView) dialogView.findViewById(R.id.dialog_home_listview);

        listView.setAdapter(new DialogAdapter(getActivity(), list));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etcDialog.dismiss();
                ((UosMainActivity) getActivity()).navigateItem(position + 8, false);
            }
        });

        etcDialog = new MaterialDialog.Builder(context)
                .cancelable(true)
                .iconAttr(R.attr.ic_navigation_accept)
                .title(R.string.tab_etc_selection)
                .customView(dialogView, false)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        List<Integer> list = AppUtil.loadEnabledPageOrder(context);
        if (AppUtil.isScreenSizeSmall(context) && list.size() > 8) {
            initEtcMenuDialog(context);

            list = list.subList(0, 7);
            list.add(R.string.title_section_etc);
        }
        list.add(R.string.setting);


        View v = inflater.inflate(R.layout.tab_home, container, false);

        GridView gridView = (GridView) v.findViewById(R.id.tab_home_gridview);
        SwingBottomInAnimationAdapter animatorAdapter = new SwingBottomInAnimationAdapter(new GridViewAdapter(getActivity(), list));
        animatorAdapter.setAbsListView(gridView);

        gridView.setOnItemClickListener(this);
        gridView.setAdapter(animatorAdapter);

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
        int size = adapterView.getCount() - 1;

        if (pos == size) {
            ((UosMainActivity) getActivity()).startSettingActivity();

        } else if (pos == size - 1) {

            if (etcDialog == null)
                ((UosMainActivity) getActivity()).navigateItem(pos + 1, false);
            else
                etcDialog.show();

        } else {
            ((UosMainActivity) getActivity()).navigateItem(pos + 1, false);
        }

    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "TabHomeFragment";
    }
}
