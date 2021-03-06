package com.uoscs09.theuos2.tab.booksearch;


import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

class BookItemViewHolder extends AbsArrayAdapter.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @BindView(R.id.tab_booksearch_list_book_title)
    TextView title;
    @BindView(R.id.tab_booksearch_list_book_writer)
    TextView writer;
    @BindView(R.id.tab_booksearch_list_text_publish_and_year)
    TextView publish_year;
    @BindView(R.id.tab_booksearch_list_book_site)
    TextView location;
    @BindView(R.id.tab_booksearch_list_book_state)
    TextView bookState;
    @BindView(R.id.tab_booksearch_list_book_image)
    ImageView coverImg;
    //final View ripple;
    @BindView(R.id.tab_booksearch_layout_book_state)
    LinearLayout stateInfoLayout;

    /**
     * 책의 세부 위치 정보를 표현하는 뷰를 담은 리스트
     */
    private final List<ChildHolder> mChildHolderList = new CopyOnWriteArrayList<>();

    private Subscription subscription;

    BookItem item;
    BookItemListAdapter.OnItemClickListener onItemClickListener;

    BookItemViewHolder(View v) {
        super(v);

        coverImg.setOnClickListener(this);
        location.setOnClickListener(this);

        title.setOnLongClickListener(this);
        publish_year.setOnLongClickListener(this);
        writer.setOnLongClickListener(this);
        //ripple = v.findViewById(R.id.ripple);
    }


    public BookItem getItem() {
        return item;
    }

    @Override
    public void onClick(View v) {
        if (item == null)
            return;

        onItemClickListener.onItemClick(this, v);
    }

    @Override
    public boolean onLongClick(View v) {
        return item != null && onItemClickListener.onItemLongClick(this, v);
    }

    private void cancelBookStateLoadingTask() {
        if (subscription != null)
            subscription.unsubscribe();
        subscription = null;
    }

    @OnClick(R.id.tab_booksearch_list_info_layout)
    void changeBookStateLayoutVisibility() {
        if (stateInfoLayout.getChildCount() > 0)
            removeAllBookStateInLayout();
        else
            setUpBookStateLayout();
    }

    private void setUpBookStateLayout() {
        cancelBookStateLoadingTask();

        final BookItemViewHolder holder = this;
        final BookItem item = holder.item;

        if (item.bookStateInfoList == null) {
            if (TextUtils.isEmpty(item.infoUrl)) {
                item.bookStateInfoList = Collections.emptyList();
                removeAllBookStateInLayout();
            } else {
                subscription = AppRequests.Books.requestBookStateInfo(item.infoUrl).subscribe(
                        result -> {
                            item.bookStateInfoList = result;

                            // 처리 후, ViewHolder item 과 결과 item 이 다르다면 무시
                            if (holder.item.equals(item))
                                drawBookStateLayout(holder);
                            else Log.d("BookItemViewHolder", "request item != current item");
                        },
                        t -> AppUtil.showErrorToast(holder.itemView.getContext(), t, true)
                );
            }

        } else if (!item.bookStateInfoList.isEmpty()) {
            drawBookStateLayout(this);
        } else {
            removeAllBookStateInLayout();
        }

    }

    void removeAllBookStateInLayout() {
        cancelBookStateLoadingTask();

        if (stateInfoLayout.getVisibility() != View.GONE) {
            stateInfoLayout.setVisibility(View.GONE);
            stateInfoLayout.invalidate();

            stateInfoLayout.removeAllViews();
        }

    }

    private static void drawBookStateLayout(BookItemViewHolder holder) {
        //if (holder.itemView.getContext() instanceof Activity)
        //   BookDetailActivity.start((Activity) holder.itemView.getContext(), holder.coverImg, holder.item);
        holder.stateInfoLayout.setVisibility(View.VISIBLE);

        List<BookStateInfo> list = holder.item.bookStateInfoList;
        List<ChildHolder> childHolderList = holder.mChildHolderList;

        LinearLayout layout = holder.stateInfoLayout;

        final int attachingViewsSize = list == null ? 0 : list.size();

        final int availableRecyledHolderCount = childHolderList.size();
        final int diff = attachingViewsSize - availableRecyledHolderCount;

        if (diff > 0) {
            // 홀더 추가 생성
            LayoutInflater inflater = LayoutInflater.from(layout.getContext());
            for (int i = 0; i < diff; i++) {
                childHolderList.add(new ChildHolder(inflater.inflate(R.layout.list_layout_book_state, layout, false)));
            }

        } else if (diff < 0) {
            // 홀더 삭제
            //possible ConcurrentModificationException
            childHolderList.removeAll(childHolderList.subList(attachingViewsSize, availableRecyledHolderCount));
        }

        // 캐쉬된 홀더에 정보 입력
        final int N = childHolderList.size();
        for (int i = 0; i < N; i++) {
            ChildHolder childHolder = childHolderList.get(i);
            // if(list != null) list 가 null 일 경우 N = 0
            //noinspection ConstantConditions
            childHolder.setContent(list.get(i));
            layout.addView(childHolder.itemView);
        }

        //layout.startAnimation(new FadeUpAnimation(layout));

    }


    static class ChildHolder {
        public final View itemView;
        @BindView(R.id.tab_booksearch_bookstate_location)
        public TextView location;
        @BindView(R.id.tab_booksearch_bookstate_code)
        public TextView code;
        @BindView(R.id.tab_booksearch_bookstate_state)
        TextView state;

        private final PieProgressDrawable drawable = new PieProgressDrawable();

        ChildHolder(View v) {
            itemView = v;
            ButterKnife.bind(this, itemView);
        }

        void setContent(BookStateInfo info) {
            code.setText(info.call_no);
            location.setText(info.place_name);
            BookItemListAdapter.setAvailableSpan(state, info.book_state, info.isBookAvailable());

            Resources r = itemView.getResources();
            drawable.setLevel(100);
            drawable.setBorderWidth(-1f, r.getDisplayMetrics());
            drawable.setColor(ResourceUtil.getAttrColor(itemView.getContext(), R.attr.colorAccent));
            int size = r.getDimensionPixelSize(R.dimen.book_state_icon_size);
            drawable.setBounds(0, 0, size, size);

            location.setCompoundDrawables(drawable, null, null, null);
        }

    }
}

