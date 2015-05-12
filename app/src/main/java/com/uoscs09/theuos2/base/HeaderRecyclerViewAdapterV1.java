package com.uoscs09.theuos2.base;

/*
 * Copyright (C) 2014 sebnapi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by sebnapi on 08.11.14.
 * <p/>
 * This is a Plug-and-Play Approach for adding a Header or Footer to
 * a RecyclerView backed list
 * <p/>
 * Just wrap your regular adapter like this
 * <p/>
 * new HeaderRecyclerViewAdapterV1(new RegularAdapter())
 * <p/>
 * Let RegularAdapter implement HeaderRecyclerView, FooterRecyclerView or both
 * and you are ready to go.
 * <p/>
 * I'm absolutely not sure how this will behave with changes in the dataset.
 * You can always wrap a fresh adapter and make sure to not change the old one or
 * use my other approach.
 * <p/>
 * With the other approach you need to let your Adapter extend HeaderRecyclerViewAdapterV2
 * (and therefore change potentially more code) but possible omit these shortcomings.
 * <p/>
 * TOTALLY UNTESTED - USE WITH CARE - HAVE FUN :)
 */
@SuppressWarnings("rawtypes")
public class HeaderRecyclerViewAdapterV1 extends RecyclerView.Adapter {
    private static final int TYPE_HEADER = Integer.MIN_VALUE;
    private static final int TYPE_FOOTER = Integer.MIN_VALUE + 1;
    private static final int TYPE_ADAPTEE_OFFSET = 2;

    private final RecyclerView.Adapter mAdaptee;


    public HeaderRecyclerViewAdapterV1(RecyclerView.Adapter adaptee) {
        mAdaptee = adaptee;
    }

    public RecyclerView.Adapter getAdaptee() {
        return mAdaptee;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER && mAdaptee instanceof HeaderRecyclerView) {
            return ((HeaderRecyclerView) mAdaptee).onCreateHeaderViewHolder(parent, viewType);
        } else if (viewType == TYPE_FOOTER && mAdaptee instanceof FooterRecyclerView) {
            return ((FooterRecyclerView) mAdaptee).onCreateFooterViewHolder(parent, viewType);
        }
        return mAdaptee.onCreateViewHolder(parent, viewType - TYPE_ADAPTEE_OFFSET);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0 && holder.getItemViewType() == TYPE_HEADER && useHeader()) {
            ((HeaderRecyclerView) mAdaptee).onBindHeaderView(holder, position);
        } else if (position == mAdaptee.getItemCount() && holder.getItemViewType() == TYPE_FOOTER && useFooter()) {
            ((FooterRecyclerView) mAdaptee).onBindFooterView(holder, position);
        } else {
            mAdaptee.onBindViewHolder(holder, position - (useHeader() ? 1 : 0));
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = mAdaptee.getItemCount();
        if (useHeader()) {
            itemCount += 1;
        }
        if (useFooter()) {
            itemCount += 1;
        }
        return itemCount;
    }

    private boolean useHeader() {
        return mAdaptee instanceof HeaderRecyclerView;
    }

    private boolean useFooter() {
        return mAdaptee instanceof FooterRecyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && useHeader()) {
            return TYPE_HEADER;
        }
        if (position == mAdaptee.getItemCount() && useFooter()) {
            return TYPE_FOOTER;
        }
        if (mAdaptee.getItemCount() >= Integer.MAX_VALUE - TYPE_ADAPTEE_OFFSET) {
            throw new IllegalStateException("HeaderRecyclerViewAdapter offsets your BasicItemType by " + TYPE_ADAPTEE_OFFSET + ".");
        }
        return mAdaptee.getItemViewType(position) + TYPE_ADAPTEE_OFFSET;
    }


    public static interface HeaderRecyclerView {
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType);

        public void onBindHeaderView(RecyclerView.ViewHolder holder, int position);
    }

    public static interface FooterRecyclerView {
        public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType);

        public void onBindFooterView(RecyclerView.ViewHolder holder, int position);
    }

}
