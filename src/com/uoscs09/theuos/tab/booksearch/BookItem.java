package com.uoscs09.theuos.tab.booksearch;

import java.util.ArrayList;
import java.util.List;

import com.uoscs09.theuos.common.util.StringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class BookItem implements Parcelable {
	/** 책 제목 */
	public String title;
	/** 저자 */
	public String writer;
	/** 출판사 / 연도 */
	public String bookInfo;
	/** 책 위치 */
	public String site;
	/** 대여 상태 */
	public String bookState;
	/** 책 표지 url */
	public String coverSrc;
	/** 책 url */
	public String url;
	/** 책 정보 리스트 */
	public List<BookStateInfo> bookStateInfoList;
	/** 책 정보 리스트의 정보가 담긴 Url */
	public String infoUrl;

	public BookItem() {
		title = writer = bookInfo = site = bookState = coverSrc = url = infoUrl = StringUtil.NULL;
		bookStateInfoList = null;
	}

	public BookItem(String title, String writer, String bookInfo, String site,
			String bookState, String coverSrc, String url, String infoUrl,
			List<BookStateInfo> list) {
		this.title = title.trim();
		this.writer = writer.trim();
		this.bookInfo = bookInfo.trim();
		this.site = site.trim();
		this.bookState = bookState.trim();
		this.coverSrc = coverSrc.trim();
		this.url = url.trim();
		this.infoUrl = infoUrl;
		this.bookStateInfoList = list;
	}

	private BookItem(Parcel source) {
		title = source.readString();
		writer = source.readString();
		bookInfo = source.readString();
		site = source.readString();
		bookState = source.readString();
		coverSrc = source.readString();
		url = source.readString();
		infoUrl = source.readString();
		bookStateInfoList = new ArrayList<BookStateInfo>();
		source.readList(bookStateInfoList, BookStateInfo.class.getClassLoader());
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
		dest.writeString(infoUrl);
		dest.writeList(bookStateInfoList);
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
