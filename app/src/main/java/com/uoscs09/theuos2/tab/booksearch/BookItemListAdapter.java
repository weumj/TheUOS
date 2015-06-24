package com.uoscs09.theuos2.tab.booksearch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncJob;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.parse.ParseUtil;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BookItemListAdapter extends AbsArrayAdapter<BookItem, BookItemListAdapter.BookItemViewHolder> {
    interface OnItemClickListener extends com.uoscs09.theuos2.base.OnItemClickListener<BookItemViewHolder>{
        boolean onItemLongClick(BookItemViewHolder holder, View v);
    }

    private final OnItemClickListener onItemClickListener;
    private final ImageLoader imageLoader;
    private static final int COLOR_AVAILABLE = Color.rgb(114, 213, 114) //R.color.material_green_200
            , COLOR_NOT_AVAILABLE = Color.rgb(246, 153, 136); // R.color.material_red_200

    public BookItemListAdapter(Context context, List<BookItem> list, ImageLoader imageLoader, OnItemClickListener onItemClickListener) {
        super(context, R.layout.list_layout_book, list);
        this.imageLoader = imageLoader;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(int position, final BookItemViewHolder holder) {
        final BookItem item = getItem(position);

        if (holder.imageContainer != null)
            holder.imageContainer.cancelRequest();

        holder.coverImg.setImageResource(R.drawable.noimg_en);
        if (!item.coverSrc.equals(StringUtil.NULL))
            holder.imageContainer = imageLoader.get(item.coverSrc, holder);

        holder.item = item;
        holder.onItemClickListener = onItemClickListener;

        holder.title.setText(item.title);
        holder.writer.setText(item.writer);
        holder.publish_year.setText(item.bookInfo);

        setAvailableSpan(holder.bookState, item.bookState, item.isBookAvailable());
        setLocationSpan(holder.location, item);

        holder.removeAllBookStateInLayout();
        /*
        if (item.bookStateInfoList != null)
            setBookStateLayout(holder.stateInfoLayout, item.bookStateInfoList);

        holder.stateInfoLayout.setVisibility(View.GONE);
        */

    }

    /*
        static void setBookStateLayout(LinearLayout layout, List<BookStateInfo> list) {
            final int attachingViewsSize = list.size();
            int childCount = layout.getChildCount();
            if (attachingViewsSize < childCount) {
                // bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 적은 경우
                // LinearLayout에 그 차이 만큼 View를 삭제하고, childCount를 변경한다.
                layout.removeViews(attachingViewsSize, childCount - attachingViewsSize);
                childCount = layout.getChildCount();

            } else if (attachingViewsSize > childCount) {
                // bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 많은 경우
                // LinearLayout에 그 차이 만큼 View를 추가한다.
                LayoutInflater inflater = LayoutInflater.from(layout.getContext());
                for (int i = childCount; i < attachingViewsSize; i++) {
                    View v = inflater.inflate(R.layout.list_layout_book_state, layout, false);
                    setChildViewContent(v, list.get(i));
                    layout.addView(v);
                }
            }

            for (int i = 0; i < childCount; i++) {
                setChildViewContent(layout.getChildAt(i), list.get(i));
            }
        }

        private static void setChildViewContent(View v, BookStateInfo info) {
            ChildHolder h = (ChildHolder) v.getTag();
            if (h == null) {
                h = new ChildHolder(v);
                v.setTag(h);
            }

            h.code.setText(info.call_no);
            h.location.setText(info.place_name);
            setAvailableSpan(h.state, info.book_state, info.isBookAvailable());
        }
    */
    @Override
    public BookItemViewHolder onCreateViewHolder(View convertView, int viewType) {
        return new BookItemViewHolder(convertView);
    }

    static void setLocationSpan(TextView v, BookItem item) {
        if (item.bookStateInt == BookItem.BOOK_STATE_ONLINE) {
            Spannable styledText = new Spannable.Factory().newSpannable("URL");
            styledText.setSpan(new URLSpan(item.site), 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            styledText.setSpan(new StyleSpan(Typeface.ITALIC), 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            v.setText(styledText);
        } else {
            v.setText(item.site);
        }
    }

    static void setAvailableSpan(TextView v, String s, boolean available) {
        Spannable styledText = new Spannable.Factory().newSpannable(s);

        styledText.setSpan(new ForegroundColorSpan(available ? COLOR_AVAILABLE : COLOR_NOT_AVAILABLE),
                0, styledText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        v.setText(styledText);
    }

    static class BookItemViewHolder extends AbsArrayAdapter.ViewHolder implements ImageLoader.ImageListener, View.OnClickListener, View.OnLongClickListener {

        final View infoParentLayout;
        final TextView title, writer, publish_year, location, bookState;
        final ImageView coverImg;
        //final View ripple;
        final LinearLayout stateInfoLayout;
        final ArrayList<ChildHolder> mChildHolderList = new ArrayList<>();

        ImageLoader.ImageContainer imageContainer;
        AsyncTask<Void, Void, ArrayList<BookStateInfo>> asyncTask;

        BookItem item;
        OnItemClickListener onItemClickListener;

        private static final XmlParser<ArrayList<BookStateInfo>> BOOK_STATE_INFO_PARSER = XmlParser.newReflectionParser(BookStateInfo.class, "location", "noholding", "item");


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
            if (asyncTask != null && AsyncUtil.isTaskRunning(asyncTask)) {
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
                    asyncTask = AsyncUtil.execute(new AsyncJob.Base<ArrayList<BookStateInfo>>() {

                        @Override
                        public ArrayList<BookStateInfo> call() throws Exception {
                            return ParseUtil.parseXml(holder.itemView.getContext(), BOOK_STATE_INFO_PARSER, item.infoUrl);
                        }

                        @Override
                        public void onResult(ArrayList<BookStateInfo> result) {
                            item.bookStateInfoList = result;

                            drawBookStateLayout(holder);
                        }

                    });
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
                for (int i = 0; i < attachingViewsSize; i++) {
                    childHolderList.add(new ChildHolder(inflater.inflate(R.layout.list_layout_book_state, layout, false)));
                }

            } else {
                // 홀더 삭제
                childHolderList.removeAll(childHolderList.subList(attachingViewsSize, availableRecyledHolderCount));
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

    }

    static class ChildHolder {
        public final View itemView;
        public final TextView location;
        public final TextView code;
        public final TextView state;
        private PieProgressDrawable drawable = new PieProgressDrawable();

        ChildHolder(View v) {
            itemView = v;
            location = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_location);
            code = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_code);
            state = (TextView) v.findViewById(R.id.tab_booksearch_bookstate_state);
        }

        void setContent(BookStateInfo info) {
            code.setText(info.call_no);
            location.setText(info.place_name);
            setAvailableSpan(state, info.book_state, info.isBookAvailable());

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




