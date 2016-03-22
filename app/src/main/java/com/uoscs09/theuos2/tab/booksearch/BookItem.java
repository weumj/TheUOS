package com.uoscs09.theuos2.tab.booksearch;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class BookItem implements Parcelable {
    /**
     * 책 제목
     */
    public String title;
    /**
     * 저자
     */
    public String writer;
    /**
     * 출판사 / 연도
     */
    public String bookInfo;
    /**
     * 책 위치
     */
    public String site;
    /**
     * 대여 상태
     */
    public String bookState;
    /**
     * 책 표지 url
     */
    public String coverSrc;
    /**
     * 책 url
     */
    public String url;
    /**
     * 책 정보 리스트
     */
    public List<BookStateInfo> bookStateInfoList; // lazy
    /**
     * 책 정보 리스트의 정보가 담긴 Url
     */
    public String infoUrl;

    public int bookStateInt;

    /**
     * 이용 가능
     */
    public static final int BOOK_STATE_AVAILABLE = 1;
    /**
     * 이용가능하지 않음
     */
    public static final int BOOK_STATE_NOT_AVAILABLE = BOOK_STATE_AVAILABLE << 1;

    /**
     * 온라인 이용 가능
     */
    public static final int BOOK_STATE_ONLINE = BOOK_STATE_AVAILABLE << 2 | BOOK_STATE_AVAILABLE;
    /**
     * 정리중
     */
    public static final int BOOK_STATE_IN_ARRANGE = BOOK_STATE_NOT_AVAILABLE << 1 | BOOK_STATE_NOT_AVAILABLE;
    /**
     * 파오손
     */
    public static final int BOOK_STATE_BROKEN_OR_DIRTY = BOOK_STATE_NOT_AVAILABLE << 2 | BOOK_STATE_NOT_AVAILABLE;

    /**
     * 분실
     */
    public static final int BOOK_STATE_MISSING = BOOK_STATE_NOT_AVAILABLE << 3 | BOOK_STATE_NOT_AVAILABLE;

    public BookItem() {
        title = writer = bookInfo = site = bookState = coverSrc = url = infoUrl = StringUtil.NULL;
        bookStateInfoList = null;
    }

    /*
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
    */

    public boolean isBookAvailable() {
        return (bookStateInt & BOOK_STATE_AVAILABLE) == BOOK_STATE_AVAILABLE;
    }

    private BookItem(Parcel source) {
        title = source.readString();
        writer = source.readString();
        bookInfo = source.readString();
        site = source.readString();
        bookState = source.readString();
        bookStateInt = source.readInt();

        coverSrc = source.readString();
        url = source.readString();
        infoUrl = source.readString();

        bookStateInfoList = new ArrayList<>();
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
        dest.writeInt(bookStateInt);

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
