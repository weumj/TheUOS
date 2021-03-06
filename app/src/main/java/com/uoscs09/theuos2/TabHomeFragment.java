package com.uoscs09.theuos2;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.base.BaseTabFragment;
import com.uoscs09.theuos2.base.ViewHolder;
import com.uoscs09.theuos2.util.AppUtil.TabInfo;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.List;

import butterknife.BindView;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;
import mj.android.utils.recyclerview.ViewHolderFactory;

import static com.uoscs09.theuos2.util.AppUtil.TabInfo.loadEnabledTabOrderForHome;

public class TabHomeFragment extends BaseTabFragment {

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabHomeFragment";
    }

    @Override
    protected int layoutRes() {
        return R.layout.tab_home;
    }

    @BindView(R.id.tab_home_recycler_view)
    RecyclerView mRecyclerView;
    ListRecyclerAdapter<TabInfo, HomeViewHolder> adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        List<TabInfo> list = loadEnabledTabOrderForHome();

        final int viewCount = ResourceUtil.isScreenSizeSmall() ? 3 : 5;

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), viewCount));
        adapter = new ListRecyclerAdapter<>(list, new ViewHolderFactory<TabInfo, HomeViewHolder>() {
            @Override
            public HomeViewHolder newViewHolder(ViewGroup viewGroup, int i) {
                return new HomeViewHolder(ListRecyclerUtil.makeViewHolderItemView(viewGroup, R.layout.list_layout_home));
            }
        });
        adapter.setOnItemClickListener((homeViewHolder, view1) -> {
            if (getUosMainActivity() == null)
                return;


            int position = homeViewHolder.getAdapterPosition();

            switch (homeViewHolder.getItem()) {
                case Setting:
                    getUosMainActivity().startSettingActivity();
                    break;

                default:
                    getUosMainActivity().navigateItem(position + 1, false);
                    break;
            }
        });
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(ListRecyclerUtil.squareViewItemDecoration(getActivity(), viewCount, R.dimen.tab_home_item_side_margin));

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
    }

    static class HomeViewHolder extends ViewHolder<TabInfo> {
        @BindView(android.R.id.text1)
        TextView textView;

        HomeViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        protected void setView(int i) {
            TabInfo item = getItem();
            textView.setCompoundDrawablesWithIntrinsicBounds(0, item.getLightIcon(), 0, 0);
            textView.setText(item.titleResId);
        }
    }
}
