package com.uoscs09.theuos.tab.booksearch;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
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
	private ImageLoaderConfiguration config;
	private ImageLoader imageLoader;
	private View.OnClickListener l;
	private View.OnLongClickListener ll;

	private BookItemListAdapter(Context context) {
		super(context, 0);
	}

	public BookItemListAdapter(Context context, int layout,
			List<BookItem> bookList, View.OnClickListener l,
			View.OnLongClickListener ll) {
		super(context, layout, bookList);
		this.l = l;
		this.ll = ll;
		imageLoader = ImageLoader.getInstance();
		setupImgConfig();
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		/* 뷰 설정 */
		final BookItem item = getItem(position);
		Holder h = (Holder) holder;

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

		return convertView;
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

	protected void setupImgConfig() {
		if (!imageLoader.isInited()) {
			File cacheDir = getContext().getCacheDir();
			Executor ex = Executors.newCachedThreadPool();
			// Create configuration for ImageLoader
			DisplayImageOptions option = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.noimg_en1)
					.cacheInMemory(true).cacheOnDisc(true).build();
			config = new ImageLoaderConfiguration.Builder(getContext())
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
					.imageDownloader(new BaseImageDownloader(getContext()))
					.defaultDisplayImageOptions(option).build();

			// Initialize ImageLoader with created configuration. Do it once.
			imageLoader.init(config);
		}
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	protected static class Holder implements ViewHolder {
		public TextView title;
		public TextView writer;
		public TextView publish_year;
		public TextView location;
		public TextView bookState;
		public ImageView coverImg;

		public Holder(View v) {
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
		}
	}
}
