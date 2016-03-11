package com.uoscs09.theuos2.tab.booksearch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;

import java.util.List;

class BookItemListAdapter extends AbsArrayAdapter<BookItem, BookItemViewHolder> {
    interface OnItemClickListener extends com.uoscs09.theuos2.base.OnItemClickListener<BookItemViewHolder> {
        boolean onItemLongClick(BookItemViewHolder holder, View v);
    }

    private final OnItemClickListener onItemClickListener;
    private static final int COLOR_AVAILABLE = Color.rgb(114, 213, 114) //R.color.material_green_200
            , COLOR_NOT_AVAILABLE = Color.rgb(246, 153, 136); // R.color.material_red_200

    public BookItemListAdapter(Context context, List<BookItem> list, OnItemClickListener onItemClickListener) {
        super(context, R.layout.list_layout_book, list);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(int position, final BookItemViewHolder holder) {
        final BookItem item = getItem(position);

        if (!TextUtils.isEmpty(item.coverSrc))
            Glide.with(getContext())
                    .load(item.coverSrc)
                    .error(R.drawable.noimg_en)
                    .into(holder.coverImg);


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

}