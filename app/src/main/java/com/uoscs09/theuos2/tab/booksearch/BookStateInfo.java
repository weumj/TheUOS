package com.uoscs09.theuos2.tab.booksearch;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.parse.IParser;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Root(name = "item")
public class BookStateInfo implements Parcelable, IParser.IPostParsing {

    /**
     * 도서 코드
     */
    @Element(name = "call_no", cdata = true)
    public String call_no;
    /**
     * 장소
     */
    @Element(name = "place_name", cdata = true)
    public String place_name;
    /**
     * 상태
     */
    @Element(name = "book_state", cdata = true)
    public String book_state;

    int bookStateInt;

    // 반납일
    //public String return_plan_date;
    // 예약가능여부
    //public String reservation; // Y or N
    // 예약 횟수
    //public String reservation_count;
    // 예약 가능 횟수
    //public String can_reserve_count;

    public BookStateInfo() {
    }

    @Override
    public void afterParsing() {
        /*switch (book_state){
            case "대출가능":
                bookStateInt = BookItem.BOOK_STATE_AVAILABLE;
                break;

            case ""
        }*/
        bookStateInt = book_state.contains("가능") ? BookItem.BOOK_STATE_AVAILABLE : BookItem.BOOK_STATE_NOT_AVAILABLE;
    }

    public boolean isBookAvailable() {
        return (bookStateInt & BookItem.BOOK_STATE_AVAILABLE) == BookItem.BOOK_STATE_AVAILABLE;
    }

    @Override
    public String toString() {
        return "[Code : " + call_no + " , Place : " + place_name + " , State : " + book_state + "]";
    }

    /*
    public BookStateInfo(String placeName, String state, String bookCode) {
        infoArray[0] = bookCode;
        infoArray[1] = placeName;
        infoArray[2] = state;
    }
    */

    BookStateInfo(Parcel source) {
        call_no = source.readString();
        place_name = source.readString();
        book_state = source.readString();
        bookStateInt = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(call_no);
        dest.writeString(place_name);
        dest.writeString(book_state);
        dest.writeInt(bookStateInt);
    }

    public static final Parcelable.Creator<BookStateInfo> CREATOR = new Parcelable.Creator<BookStateInfo>() {
        @Override
        public BookStateInfo[] newArray(int size) {
            return new BookStateInfo[size];
        }

        @Override
        public BookStateInfo createFromParcel(Parcel source) {
            return new BookStateInfo(source);
        }
    };

}