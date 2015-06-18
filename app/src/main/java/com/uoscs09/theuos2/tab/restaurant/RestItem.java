package com.uoscs09.theuos2.tab.restaurant;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class RestItem implements Parcelable, Serializable {
    private static final long serialVersionUID = 8809096102781534332L;
    public String title;
    public String body;
    public String breakfast;
    public String lunch;
    public String supper;

    public static final transient RestItem EMPTY = new RestItem();

    public RestItem() {
        title = body = breakfast = lunch = supper = "";
    }

    public RestItem(String title, String body, String breakfast, String lunch, String supper) {
        this.title = title;
        this.body = body;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.supper = supper;
    }

    public static int findRestNameIndex(String name) {
        if(name.contains("학생")){
            return 0;
        } else if (name.contains("양식당")){
            return 1;
        } else if(name.contains("자연")){
            return 2;
        } else if(name.contains("본관")){
            return 3;
        } else if(name.contains("생활")){
            return 4;
        }

        return -1;
    }

    RestItem(Parcel source) {
        title = source.readString();
        body = source.readString();
        breakfast = source.readString();
        lunch = source.readString();
        supper = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(breakfast);
        dest.writeString(lunch);
        dest.writeString(supper);
    }

    public static final Creator<RestItem> CREATOR = new Creator<RestItem>() {

        @Override
        public RestItem createFromParcel(Parcel source) {
            return new RestItem(source);
        }

        @Override
        public RestItem[] newArray(int size) {
            return new RestItem[size];
        }

    };
}
