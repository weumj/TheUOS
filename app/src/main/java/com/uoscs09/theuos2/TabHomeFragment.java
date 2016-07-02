package com.uoscs09.theuos2;


import android.graphics.Rect;
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

        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int viewCount = AppUtil.isScreenSizeSmall() ? 3 : 5;

        final int marginSize = getResources().getDimensionPixelSize(R.dimen.dp8);
        final int viewSize = (screenWidth - ((viewCount + 1) * marginSize)) / viewCount;

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), viewCount));
        adapter = ListRecyclerUtil.newSimpleAdapter(list, HomeViewHolder.class, R.layout.list_layout_home);
        adapter.setOnItemClickListener((homeViewHolder, view1) -> {
            int position = homeViewHolder.getAdapterPosition();
            if (listLastCount == position) {
                getUosMainActivity().startSettingActivity();
            } else {
                getUosMainActivity().navigateItem(position + 1, false);
            }
        });
        mRecyclerView.setAdapter(adapter);

        final int half = marginSize / 2;
        final int lastCount = viewCount - 1;

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = viewSize;
                params.width = viewSize;

                view.setLayoutParams(params);

                int position = parent.getChildAdapterPosition(view);

                int rowPosition = position % viewCount;

                if (rowPosition == 0) {
                    outRect.set(marginSize, marginSize, half, 0);
                } else if (rowPosition == lastCount) {
                    outRect.set(half, marginSize, marginSize, 0);
                } else {
                    outRect.set(half, marginSize, half, 0);
                }
            }
        });


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
