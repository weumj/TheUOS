package com.uoscs09.theuos2.tab.announce;

import android.os.Parcel;
import android.os.Parcelable;

public class AnnounceItem implements Parcelable {
    public enum Category {

        GENERAL("FA1"),  // 일반공지
        AFFAIRS("FA2"), // 학사공지
        SCHOLARSHIP("SCHOLARSHIP"), // 장학공지
        EMPLOY("FA34") // 채용공지
        ;

        public final String tag;

        Category(String tag) {
            this.tag = tag;
        }

        public static Category fromIndex(int index){
            return values()[index];
        }
    }


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
