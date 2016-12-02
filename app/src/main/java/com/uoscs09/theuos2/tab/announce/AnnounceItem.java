package com.uoscs09.theuos2.tab.announce;

import android.os.Parcel;
import android.os.Parcelable;

public class AnnounceItem implements Parcelable {
    public String title = "";
    public String date = "";
    public String pageURL = "";

    public AnnounceItem(){
    }

    private AnnounceItem(Parcel source) {
        title = source.readString();
        date = source.readString();
        pageURL = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(pageURL);
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
