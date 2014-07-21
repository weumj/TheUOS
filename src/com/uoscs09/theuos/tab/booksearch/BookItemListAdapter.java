package com.uoscs09.theuos.tab.booksearch;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class BookItemListAdapter extends AbsArrayAdapter<BookItem> {
	private static final ImageLoader imageLoader = ImageLoader.getInstance();
	private View.OnClickListener l;
	private View.OnLongClickListener ll;

	public BookItemListAdapter(Context context, int layout,
			List<BookItem> list, View.OnClickListener l,
			View.OnLongClickListener ll) {
		super(context, layout, list);
		this.l = l;
		this.ll = ll;
		setupImgConfig();
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		final GroupHolder h = (GroupHolder) holder;
		final BookItem item = getItem(position);
		// 책 이미지 설정
		imageLoader.displayImage(item.coverSrc, h.coverImg);
		h.coverImg.setOnClickListener(l);
		h.coverImg.setTag(item);
		h.title.setText(item.title);
		h.title.setOnLongClickListener(ll);
		h.writer.setText(item.writer);
		h.writer.setOnLongClickListener(ll);
		h.publish_year.setText(item.bookInfo);
		h.publish_year.setOnLongClickListener(ll);
		h.bookState.setText(setSpannableText(item.bookState, 2));
		h.location.setText(setSpannableText(item.site, 1));
		h.location.setOnClickListener(l);
		h.location.setTag(item);

		setBookStateLayout(h.stateInfoLayout, item.bookStateInfoList);
		h.stateInfoLayout.setVisibility(View.GONE);
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (h.stateInfoLayout.getVisibility() == View.GONE
						&& !item.bookStateInfoList.isEmpty())
					h.stateInfoLayout.setVisibility(View.VISIBLE);
				else
					h.stateInfoLayout.setVisibility(View.GONE);
				v.requestLayout();
			}
		});
		return convertView;
	}

	private void setBookStateLayout(LinearLayout layout,
			List<BookStateInfo> list) {
		final int attachingViewsSize = list.size();
		int childCount = layout.getChildCount();
		if (attachingViewsSize < childCount) {
			// bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 적은 경우
			// LinearLayout에 그 차이 만큼 View를 삭제하고, childCount를 변경한다.
			layout.removeViews(attachingViewsSize, childCount
					- attachingViewsSize);
			childCount = layout.getChildCount();
		} else if (attachingViewsSize > childCount) {
			// bookStateInfo의 갯수가 LinearLayout의 childView의 갯수보다 많은 경우
			// LinearLayout에 그 차이 만큼 View를 추가한다.
			for (int i = childCount; i < attachingViewsSize; i++) {
				View v = View.inflate(getContext(),
						R.layout.list_layout_book_state, null);
				setChildViewContent(v, list.get(i));
				layout.addView(v);
			}
		}
		for (int i = 0; i < childCount; i++) {
			setChildViewContent(layout.getChildAt(i), list.get(i));
		}
	}

	private void setChildViewContent(View v, BookStateInfo info) {
		ChildHolder h = new ChildHolder(v);
		h.code.setText(info.infoArray[0]);
		h.location.setText(info.infoArray[1]);
		h.state.setText(setSpannableText(info.infoArray[2], 2));
	}

	@Override
	public ViewHolder getViewHolder(View convertView) {
		return new GroupHolder(convertView);
	}

	protected static class GroupHolder implements ViewHolder {
		public TextView title;
		public TextView writer;
		public TextView publish_year;
		public TextView location;
		public TextView bookState;
		public ImageView coverImg;
		public LinearLayout stateInfoLayout;

		public GroupHolder(View v) {
			bookState = (TextView) v
					.findViewById(R.id.tab_booksearch_list_text_book_state);
			coverImg = (ImageView) v
					.findViewById(R.id.tab_booksearch_list_image_book_image);
			location = (TextView) v
					.findViewById(R.id.tab_booksearch_list_text_book_site);
			title = (TextView) v
					.findViewById(R.id.tab_booksearch_list_text_book_title);
			publish_year = (TextView) v
					.findViewById(R.id.tab_booksearch_list_text_book_publish_and_year);
			writer = (TextView) v
					.findViewById(R.id.tab_booksearch_list_text_book_writer);
			stateInfoLayout = (LinearLayout) v
					.findViewById(R.id.tab_booksearch_layout_book_state);
		}
	}

	protected static class ChildHolder {
		public TextView location;
		public TextView code;
		public TextView state;

		public ChildHolder(View v) {
			location = (TextView) v
					.findViewById(R.id.tab_booksearch_bookstate_location);
			code = (TextView) v
					.findViewById(R.id.tab_booksearch_bookstate_code);
			state = (TextView) v
					.findViewById(R.id.tab_booksearch_bookstate_state);
		}
	}

	protected void setupImgConfig() {
		if (!imageLoader.isInited()) {
			Context mContext = getContext();
			File cacheDir = mContext.getCacheDir();
			Executor ex = Executors.newCachedThreadPool(new ThreadFactory() {
				private final AtomicInteger mCount = new AtomicInteger(1);

				public Thread newThread(Runnable r) {
					return new Thread(r, "ImageLoader #"
							+ mCount.getAndIncrement());
				}
			});
			BitmapFactory.Options bitmapOpt = new BitmapFactory.Options();
			bitmapOpt.inSampleSize = 4;
			// Create configuration for ImageLoader
			DisplayImageOptions option = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.noimg_en1)
					.showImageOnFail(R.drawable.noimg_en1)
					.decodingOptions(bitmapOpt)
					.showImageOnLoading(R.anim.loading_animation)
					.cacheInMemory(true).cacheOnDisc(true).build();
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
					mContext)
					.memoryCacheExtraOptions(480, 800)
					.taskExecutor(ex)
					.taskExecutorForCachedImages(ex)
					.threadPoolSize(
							ImageLoaderConfiguration.Builder.DEFAULT_THREAD_POOL_SIZE)
					.threadPriority(Thread.NORM_PRIORITY - 1)
					.tasksProcessingOrder(QueueProcessingType.FIFO)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(
							new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
					.memoryCacheSize(2 * 1024 * 1024)
					.discCache(new UnlimitedDiscCache(cacheDir))
					.discCacheSize(50 * 1024 * 1024)
					.discCacheFileCount(100)
					.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
					.imageDownloader(new BaseImageDownloader(mContext))
					.defaultDisplayImageOptions(option).build();

			// Initialize ImageLoader with created configuration. Do it once.
			imageLoader.init(config);
		}
	}

	protected Spannable setSpannableText(final String title, int which) {
		Spannable styledText = new Spannable.Factory().newSpannable(title);
		int last_length = styledText.length();
		switch (which) {
		case 1:
			if (title.startsWith("http")) {
				styledText = new Spannable.Factory().newSpannable("URL");
				styledText.setSpan(new URLSpan(title), 0, 3,
						Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				styledText.setSpan(new StyleSpan(Typeface.ITALIC), 0, 3,
						Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
			break;
		case 2:
			if (title.contains("대출가능") || title.contains("온라인")) {
				styledText.setSpan(
						new ForegroundColorSpan(getContext().getResources()
								.getColor(android.R.color.holo_green_light)),
						0, last_length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else {
				styledText.setSpan(new ForegroundColorSpan(getContext()
						.getResources()
						.getColor(android.R.color.holo_red_light)), 0,
						last_length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
		default:
			break;
		}
		return styledText;
	}

}
