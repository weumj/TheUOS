package com.uoscs09.theuos2.tab.booksearch;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.R;
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
     * 온라인 이용 가능
     */
    public static final int BOOK_STATE_ONLINE = BOOK_STATE_AVAILABLE << 2 | BOOK_STATE_AVAILABLE;

    /**
     * 이용가능하지 않음
     */
    public static final int BOOK_STATE_NOT_AVAILABLE = BOOK_STATE_AVAILABLE << 1;

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

    /**
     * 알 수 없음 (파싱 실패 등등..)
     */
    public static final int BOOK_STATE_UNKNOWN = BOOK_STATE_NOT_AVAILABLE << 4 | BOOK_STATE_NOT_AVAILABLE;


    public static int checkLocationState(String state) {
        switch (state) {
            case "대출가능":
                return BookItem.BOOK_STATE_AVAILABLE;
            case "대출중":
                return BookItem.BOOK_STATE_NOT_AVAILABLE;
            case "온라인이용가능":
                return BookItem.BOOK_STATE_ONLINE;
            case "정리중":
                return BookItem.BOOK_STATE_IN_ARRANGE;
            case "파오손":
                return BookItem.BOOK_STATE_BROKEN_OR_DIRTY;
            case "분실":
                return BookItem.BOOK_STATE_MISSING;
            default:
                return BookItem.BOOK_STATE_UNKNOWN;
        }
    }

    public static int bookStateStringRes(int state){
        switch (state){
            case BookItem.BOOK_STATE_AVAILABLE:
                return R.string.tab_book_state_available;
            case BookItem.BOOK_STATE_ONLINE:
                return R.string.tab_book_state_available_in_online;
            case BookItem.BOOK_STATE_NOT_AVAILABLE:
                return R.string.tab_book_state_not_available;
            case BookItem.BOOK_STATE_IN_ARRANGE:
                return R.string.tab_book_state_in_arrange;
            case BookItem.BOOK_STATE_BROKEN_OR_DIRTY:
                return R.string.tab_book_state_broken_or_dirty;
            case BookItem.BOOK_STATE_MISSING:
                return R.string.tab_book_state_missing;
            default:
                return R.string.tab_book_state_unknown;
        }
    }

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
