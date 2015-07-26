package com.uoscs09.theuos2.tab.booksearch;


import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BookItemViewHolder extends AbsArrayAdapter.ViewHolder implements ImageLoader.ImageListener, View.OnClickListener, View.OnLongClickListener {
    final View infoParentLayout;
    final TextView title, writer, publish_year, location, bookState;
    final ImageView coverImg;
    //final View ripple;
    final LinearLayout stateInfoLayout;

    /**
     * 책의 세부 위치 정보를 표현하는 뷰를 담은 리스트
     */
    final ArrayList<ChildHolder> mChildHolderList = new ArrayList<>();

    ImageLoader.ImageContainer imageContainer;
    AsyncTask<Void, Integer, ArrayList<BookStateInfo>> asyncTask;

    BookItem item;
    BookItemListAdapter.OnItemClickListener onItemClickListener;

    private static final XmlParserWrapper<ArrayList<BookStateInfo>> BOOK_STATE_INFO_PARSER = new XmlParserWrapper<>(XmlParser.newReflectionParser(BookStateInfo.class, "location", "noholding", "item"));


    public BookItemViewHolder(View v) {
        super(v);
        coverImg = (ImageView) v.findViewById(R.id.tab_booksearch_list_book_image);
        coverImg.setOnClickListener(this);

        infoParentLayout = v.findViewById(R.id.tab_booksearch_list_info_layout);
        infoParentLayout.setOnClickListener(this);

        bookState = (TextView) infoParentLayout.findViewById(R.id.tab_booksearch_list_book_state);
        location = (TextView) infoParentLayout.findViewById(R.id.tab_booksearch_list_book_site);
        location.setOnClickListener(this);

        title = (TextView) infoParentLayout.findViewById(R.id.tab_booksearch_list_book_title);
        title.setOnLongClickListener(this);
        publish_year = (TextView) infoParentLayout.findViewById(R.id.tab_booksearch_list_text_publish_and_year);
        publish_year.setOnLongClickListener(this);
        writer = (TextView) infoParentLayout.findViewById(R.id.tab_booksearch_list_book_writer);
        writer.setOnLongClickListener(this);

        stateInfoLayout = (LinearLayout) infoParentLayout.findViewById(R.id.tab_booksearch_layout_book_state);

        //ripple = v.findViewById(R.id.ripple);

    }

    @Override
    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
        if (imageContainer.getBitmap() != null) {
            coverImg.setImageBitmap(imageContainer.getBitmap());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    public BookItem getItem() {
        return item;
    }

    @Override
    public void onClick(View v) {
        if (item == null)
            return;

        if (v.getId() == R.id.tab_booksearch_list_info_layout)
            changeBookStateLayoutVisibility();
        else
            onItemClickListener.onItemClick(this, v);

    }

    @Override
    public boolean onLongClick(View v) {
        return item != null && onItemClickListener.onItemLongClick(this, v);
    }

    private void cancelBookStateLoadingTask() {
        if (AsyncUtil.isTaskRunning(asyncTask)) {
            AsyncUtil.cancelTask(asyncTask);
            asyncTask = null;
        }
    }

    public void changeBookStateLayoutVisibility() {
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
            if (item.infoUrl.equals(StringUtil.NULL)) {
                item.bookStateInfoList = Collections.emptyList();
                removeAllBookStateInLayout();

            } else {
                asyncTask = HttpRequest.Builder.newConnectionRequestBuilder(item.infoUrl)
                        .build()
                                //.checkNetworkState(holder.itemView.getContext())
                        .wrap(BOOK_STATE_INFO_PARSER)
                        .getAsyncOnExecutor(
                                new Request.ResultListener<ArrayList<BookStateInfo>>() {
                                    @Override
                                    public void onResult(ArrayList<BookStateInfo> result) {
                                        item.bookStateInfoList = result;

                                        // 처리 후, ViewHolder item 과 결과 item 이 다르다면 무시
                                        if (holder.item.equals(item))
                                            drawBookStateLayout(holder);
                                        else
                                            Log.d("BookItemViewHolder", "request item != current item");
                                    }
                                },
                                new Request.ErrorListener() {
                                    @Override
                                    public void onError(Exception e) {
                                        e.printStackTrace();
                                    }
                                }

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

    static void drawBookStateLayout(BookItemViewHolder holder) {
        holder.stateInfoLayout.setVisibility(View.VISIBLE);

        List<BookStateInfo> list = holder.item.bookStateInfoList;
        List<ChildHolder> childHolderList = holder.mChildHolderList;

        LinearLayout layout = holder.stateInfoLayout;

        final int attachingViewsSize = list.size();

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
            //TODO  ConcurrentModificationException
            childHolderList. removeAll(childHolderList.subList(attachingViewsSize, availableRecyledHolderCount));
        }

        // 캐쉬된 홀더에 정보 입력
        final int N = childHolderList.size();
        for (int i = 0; i < N; i++) {
            ChildHolder childHolder = childHolderList.get(i);
            childHolder.setContent(list.get(i));
            layout.addView(childHolder.itemView);
        }

        //layout.startAnimation(new FadeUpAnimation(layout));

    }


    static class ChildHolder {
        public final View itemView;
        public final TextView location;
        public final TextView code;
        public final TextView state;
        private final PieProgressDrawable drawable = new PieProgressDrawable();

        ChildHolder(View v) {
            itemView = v;
            location = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_location);
            code = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_code);
            state = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_state);
        }

        void setContent(BookStateInfo info) {
            code.setText(info.call_no);
            location.setText(info.place_name);
            BookItemListAdapter.setAvailableSpan(state, info.book_state, info.isBookAvailable());

            Resources r = itemView.getResources();
            drawable.setLevel(100);
            drawable.setBorderWidth(-1f, r.getDisplayMetrics());
            drawable.setColor(AppUtil.getAttrColor(itemView.getContext(), R.attr.colorAccent));
            int size = r.getDimensionPixelSize(R.dimen.book_state_icon_size);
            drawable.setBounds(0, 0, size, size);

            location.setCompoundDrawables(drawable, null, null, null);
        }

    }
}

