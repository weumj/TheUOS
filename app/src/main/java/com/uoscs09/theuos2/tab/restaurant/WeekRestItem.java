package com.uoscs09.theuos2.tab.restaurant;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class WeekRestItem implements Parcelable, Serializable {

    public ArrayList<RestItem> weekList = new ArrayList<>();

    public WeekRestItem() {
    }

    WeekRestItem(Parcel in) {
        int size = in.readInt();

        for (int i = 0; i < size; i++)
            weekList.add((RestItem)in.readParcelable(RestItem.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int size = weekList.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++)
            dest.writeParcelable(weekList.get(i), flags);
    }

    public static final Creator<WeekRestItem> CREATOR = new Creator<WeekRestItem>() {

        @Override
        public WeekRestItem createFromParcel(Parcel source) {
            return new WeekRestItem(source);
        }

        @Override
        public WeekRestItem[] newArray(int size) {
            return new WeekRestItem[size];
        }

    };
}
