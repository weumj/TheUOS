package com.uoscs09.theuos;

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.uoscs09.theuos.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos.base.BaseFragment;
import com.uoscs09.theuos.common.SimpleTextViewAdapter;
import com.uoscs09.theuos.common.SimpleTextViewAdapter.DrawblePosition;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.AppUtil.AppTheme;

import java.util.List;

public class TabHomeFragment extends BaseFragment implements OnItemClickListener {
    @ReleaseWhenDestroy
    private ArrayAdapter<Integer> adapter;
    @ReleaseWhenDestroy
    protected AlertDialog etcDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        List<Integer> list = AppUtil.loadEnabledPageOrder(context);
        if (AppUtil.isScreenSizeSmall(getActivity()) && list.size() > 8) {
            initDialog(context);

            list = list.subList(0, 7);
            list.add(R.string.title_section_etc);
        }
        list.add(R.string.setting);

        adapter = new SimpleTextViewAdapter.Builder(context,
                R.layout.list_layout_home, list)
                .setDrawablePosition(DrawblePosition.TOP)
                .setTextViewId(R.id.tab_home_text_title)
                .setTextViewTextColor(
                        getResources().getColor(android.R.color.white))
                .setDrawableTheme(AppTheme.Black).create();
    }

    private void initDialog(Context context) {
        List<Integer> list = AppUtil.loadEnabledPageOrder(context);
        list = list.subList(7, list.size());
        AppTheme theme = AppUtil.theme == AppTheme.BlackAndWhite ? AppTheme.White : AppUtil.theme;

        View v = View.inflate(context, R.layout.dialog_home_etc, null);
        ListView listView = (ListView) v.findViewById(R.id.dialog_home_listview);

        listView.setAdapter(new SimpleTextViewAdapter.Builder(context,
                android.R.layout.simple_list_item_1, list)
                .setDrawablePosition(DrawblePosition.LEFT)
                .setDrawableTheme(theme).create());

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
                .customView(v, false)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_home, container, false);

        GridView gridView = (GridView) v.findViewById(R.id.tab_home_gridview);
        SwingBottomInAnimationAdapter animatorAdapter = new SwingBottomInAnimationAdapter(adapter);
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
}
