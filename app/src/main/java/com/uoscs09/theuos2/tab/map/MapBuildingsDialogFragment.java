package com.uoscs09.theuos2.tab.map;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

public class MapBuildingsDialogFragment extends BaseDialogFragment {

    interface OnItemClickListener {
        boolean onItemClick(int pagerIndex, int listIndex);
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "MapBuildingsDialogFragment";
    }


    public static void showDialog(BaseActivity activity, View sharedTransitionView, OnItemClickListener l) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();


        MapBuildingsDialogFragment fragment = new MapBuildingsDialogFragment();
        fragment.l = l;

        fragment.show(transaction, fragment.getScreenNameForTracker());

    }

    private OnItemClickListener l;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = createView();
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        dialog.setOnShowListener(dialog1 -> AnimUtil.revealShow(view, null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (dialog.getWindow() != null)
                dialog.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        return dialog;
    }


    private View createView() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_map_menu, null, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.tab_map_menu_title);

        final ViewGroup viewGroup = (ViewGroup) v.findViewById(R.id.tab_map_bar_parent);
        viewGroup.getChildAt(0).setBackgroundColor(ResourceUtil.getAttrColor(getActivity(), R.attr.color_actionbar_title));

        ViewPager pager = (ViewPager) v.findViewById(R.id.viewpager);

        v.findViewById(R.id.tab_map_menu_select_1).setOnClickListener(v1 -> pager.setCurrentItem(0, true));

        v.findViewById(R.id.tab_map_menu_select_2).setOnClickListener(v1 -> pager.setCurrentItem(1, true));

        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return object.equals(view);
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.view_map_menu_1, container, false);
                ListView listView = (ListView) v.findViewById(R.id.list);
                listView.setAdapter(ArrayAdapter.createFromResource(getActivity(), position == 0 ? R.array.buildings_univ : R.array.tab_map_buildings_welfare, android.R.layout.simple_list_item_1));
                listView.setOnItemClickListener((adapterView, view, i, id) -> {
                    if (l != null && l.onItemClick(position, i))
                        dismiss();
                });

                container.addView(v);
                return v;
            }
        });
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewGroup.getChildAt(position).setBackgroundColor(ResourceUtil.getAttrColor(getActivity(), R.attr.color_actionbar_title));
                viewGroup.getChildAt(1 - position).setBackgroundColor(0);
            }
        });


        return v;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        l = null;
    }

}
