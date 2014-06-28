package com.uoscs09.theuos.tab.booksearch;

import com.uoscs09.theuos.common.util.StringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class BookItem implements Parcelable {
	public String title;
	public String writer;
	public String bookInfo;
	public String site;
	public String bookState;
	public String coverSrc;
	public String url;

	public BookItem() {
		title = writer = bookInfo = site = bookState = coverSrc = url = StringUtil.NULL;
	}

	public BookItem(String title, String writer, String bookInfo, String site,
			String bookState, String coverSrc, String url) {
		this.title = title.trim();
		this.writer = writer.trim();
		this.bookInfo = bookInfo.trim();
		this.site = site.trim();
		this.bookState = bookState.trim();
		this.coverSrc = coverSrc.trim();
		this.url = url.trim();
	}

	private BookItem(Parcel source) {
		title = source.readString();
		writer = source.readString();
		bookInfo = source.readString();
		site = source.readString();
		bookState = source.readString();
		coverSrc = source.readString();
		url = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(writer);
		dest.writeString(bookInfo);
		dest.writeString(site);
		dest.writeString(bookState);
		dest.writeString(coverSrc);
		dest.writeString(url);
	}

	public static final Parcelable.Creator<BookItem> CREATOR = new Parcelable.Creator<BookItem>() {
		@Override
		public BookItem[] newArray(int size) {
			return new BookItem[size];
		}

		@Override
		public BookItem createFromParcel(Parcel source) {
			return new BookItem(source);
		}
	};
}
