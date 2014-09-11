package com.uoscs09.theuos.tab.booksearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.JerichoParse;

public class BookItemListAdapter extends AbsArrayAdapter<BookItem> {
	private static final ImageLoader imageLoader = ImageLoader.getInstance();
	private View.OnLongClickListener ll;
	int imageWidth, imageHeight;

	public BookItemListAdapter(Context context, int layout,
			List<BookItem> list, View.OnLongClickListener ll) {
		super(context, layout, list);
		this.ll = ll;
		setupImgConfig();
	}

	private View.OnClickListener l = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			BookItem item;
			Object o = v.getTag();
			if (o != null && o instanceof BookItem) {
				item = (BookItem) o;
				if (v instanceof ImageView) {
					Intent i = AppUtil
							.setWebPageIntent("http://mlibrary.uos.ac.kr"
									+ item.url);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(i);
				} else if (v instanceof TextView) {
					if (item.site.startsWith("http")) {
						Intent i = AppUtil.setWebPageIntent(item.site);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getContext().startActivity(i);
					}
				}
			}
		}
	};

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		final GroupHolder h = (GroupHolder) holder;
		final BookItem item = getItem(position);
		// 책 이미지 설정
		imageLoader.displayImage(item.coverSrc, h.coverImg,
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							Bitmap bitmapResized = Bitmap
									.createScaledBitmap(loadedImage,
											imageWidth, imageHeight, false);
							((ImageView) view).setImageBitmap(bitmapResized);
						}
					}
				});
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

		if (item.bookStateInfoList != null)
			setBookStateLayout(h.stateInfoLayout, item.bookStateInfoList);
		h.stateInfoLayout.setVisibility(View.GONE);
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				// 설정된 데이터가 없다면
				// 해당 아이템을 처음 터치하는 것 이므로 데이터를 불러옴
				if (item.bookStateInfoList == null) {
					new AsyncLoader<List<BookStateInfo>>().excute(
							new Callable<List<BookStateInfo>>() {

								@Override
								public List<BookStateInfo> call()
										throws Exception {
									if (item.infoUrl.equals(StringUtil.NULL)) {
										return null;
									} else {
										return new ParseBookInfo(HttpRequest
												.getBody(item.infoUrl)).parse();
									}
								}
							}, new AsyncLoader.OnTaskFinishedListener() {

								@SuppressWarnings("unchecked")
								@Override
								public void onTaskFinished(
										boolean isExceptionOccured, Object data) {
									if (!isExceptionOccured && data != null) {
										item.bookStateInfoList = (List<BookStateInfo>) data;
										setBookStateLayout(h.stateInfoLayout,
												item.bookStateInfoList);
										h.stateInfoLayout
												.setVisibility(View.VISIBLE);
										v.requestLayout();
									}
								}
							});
				} else {
					if (h.stateInfoLayout.getVisibility() == View.GONE
							&& !item.bookStateInfoList.isEmpty())
						h.stateInfoLayout.setVisibility(View.VISIBLE);
					else
						h.stateInfoLayout.setVisibility(View.GONE);
					v.requestLayout();
				}
			}
		});
		return convertView;
	}

	void setBookStateLayout(LinearLayout layout, List<BookStateInfo> list) {
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

	private void setupImgConfig() {
		Context mContext = getContext();
		Drawable extraDrawable = mContext.getResources().getDrawable(
				R.drawable.noimg_en);
		imageHeight = extraDrawable.getMinimumHeight();
		imageWidth = extraDrawable.getMinimumWidth();
		if (!imageLoader.isInited()) {
			File cacheDir = mContext.getCacheDir();
			Executor ex = Executors.newCachedThreadPool(new ThreadFactory() {
				private final AtomicInteger mCount = new AtomicInteger(1);

				public Thread newThread(Runnable r) {
					return new Thread(r, "ImageLoader #"
							+ mCount.getAndIncrement());
				}
			});

			BitmapFactory.Options bitmapOpt = new BitmapFactory.Options();
			bitmapOpt.outHeight = extraDrawable.getMinimumHeight();
			bitmapOpt.outWidth = extraDrawable.getMinimumWidth();

			// Create configuration for ImageLoader
			DisplayImageOptions option = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(extraDrawable)
					.showImageOnFail(extraDrawable).decodingOptions(bitmapOpt)
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.showImageOnLoading(R.anim.loading_animation)
					.cacheInMemory(true).cacheOnDisk(true).build();
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
					.diskCache(new UnlimitedDiscCache(cacheDir))
					.diskCacheSize(50 * 1024 * 1024)
					.diskCacheFileCount(100)
					.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
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

class ParseBookInfo extends JerichoParse<BookStateInfo> {
	private static final String[] BOOK_STATE_XML_TAGS = { "call_no",
			"place_name", "book_state" };

	protected ParseBookInfo(String htmlBody) {
		super(htmlBody);
	}

	@Override
	protected List<BookStateInfo> parseHttpBody(Source source)
			throws IOException {
		ArrayList<BookStateInfo> bookStateInfoList = new ArrayList<BookStateInfo>();
		List<Element> itemList = source.getAllElements("item");
		final int size = itemList.size();
		for (int n = 0; n < size; n++) {
			Element infoItem = itemList.get(n);
			BookStateInfo stateInfo = new BookStateInfo();
			for (int i = 0; i < BOOK_STATE_XML_TAGS.length; i++) {
				Element element = infoItem
						.getFirstElement(BOOK_STATE_XML_TAGS[i]);
				if (element != null) {
					stateInfo.infoArray[i] = removeExtra(element.getContent()
							.toString());
				} else {
					stateInfo.infoArray[i] = StringUtil.NULL;
					if (i == 1) {
						element = infoItem.getFirstElement("shelf");
						if (element != null) {
							stateInfo.infoArray[i] = removeExtra(element
									.getContent().toString());
						}
					}
				}
			}
			bookStateInfoList.add(stateInfo);
		}

		return bookStateInfoList;
	}

	private String removeExtra(String str) {
		return str.substring(9, str.length() - 3).toString();
	}
}
