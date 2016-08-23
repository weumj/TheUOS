package com.uoscs09.theuos2;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.base.BaseTabFragment;
import com.uoscs09.theuos2.base.ViewHolder;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.List;

import butterknife.BindView;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;

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
    ListRecyclerAdapter<AppUtil.Page, HomeViewHolder> adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        List<AppUtil.Page> list = AppUtil.loadEnabledPageOrderWithSetting();
        final int listLastCount = list.size() - 1;

        final int viewCount = AppUtil.isScreenSizeSmall() ? 3 : 5;

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), viewCount));
        adapter = ListRecyclerUtil.newSimpleAdapter(list, HomeViewHolder.class, R.layout.list_layout_home);
        adapter.setOnItemClickListener((homeViewHolder, view1) -> {
            if(getUosMainActivity() == null)
                return;

            int position = homeViewHolder.getAdapterPosition();
            if (listLastCount == position) {
                getUosMainActivity().startSettingActivity();
            } else {
                getUosMainActivity().navigateItem(position + 1, false);
            }
        });
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addItemDecoration(ListRecyclerUtil.squareViewItemDecoration(getActivity(), viewCount, R.dimen.tab_home_item_side_margin));

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
    }

    static class HomeViewHolder extends ViewHolder<AppUtil.Page> {
        @BindView(android.R.id.text1)
        TextView textView;

        public HomeViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        protected void setView(int i) {
            AppUtil.Page item = getItem();
            textView.setCompoundDrawablesWithIntrinsicBounds(0, AppUtil.getPageIconWhite(item.stringId), 0, 0);
            textView.setText(textView.getContext().getString(item.stringId));
        }
    }
}
