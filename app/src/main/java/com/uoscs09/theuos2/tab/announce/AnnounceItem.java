package com.uoscs09.theuos2.tab.announce;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.util.StringUtil;

public class AnnounceItem implements Parcelable {
    public String type;
    public String title;
    public String date;
    public String onClickString;

    public AnnounceItem() {
        type = title = date = onClickString = StringUtil.NULL;
    }

    public AnnounceItem(String type, String title, String date, String onClickString) {
        this.type = type;
        this.date = date;
        this.title = title;
        this.onClickString = onClickString;
    }

    private AnnounceItem(Parcel source) {
        type = source.readString();
        title = source.readString();
        date = source.readString();
        onClickString = source.readString();
    }

    /**
     * type, title, date, onClinkString 순으로 이루어진 StringArray를 반환한다.
     *
     * @since 2.31
     */
    public String[] toStringArray() {
        return new String[]{type, title, date, onClickString};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(onClickString);
    }

    public static final Parcelable.Creator<AnnounceItem> CREATOR = new Parcelable.Creator<AnnounceItem>() {

        @Override
        public AnnounceItem[] newArray(int size) {
            return new AnnounceItem[size];
        }

        @Override
        public AnnounceItem createFromParcel(Parcel source) {
            return new AnnounceItem(source);
        }
    };
}
