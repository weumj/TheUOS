package com.uoscs09.theuos2.tab.restaurant;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.parse.IParser;

import java.io.Serializable;
import java.util.ArrayList;

public class WeekRestItem implements Parcelable, Serializable, IParser.AfterParsable {

    public ArrayList<RestItem> weekList = new ArrayList<>();
    public int startDate, endDate;

    public WeekRestItem() {
    }

    @Override
    public void afterParsing() {
        if (weekList.isEmpty()) {
            startDate = endDate = 0;
        } else {
            startDate = getDate(weekList.get(0));
            endDate = getDate(weekList.get(weekList.size() - 1));
        }

    }

    public String getPeriodString() {
        if (weekList.size() > 2)
            return (weekList.get(0).title + " ~ " + weekList.get(weekList.size() - 1).title);
        else if (weekList.size() == 1)
            return (weekList.get(0).title);
        else
            return (null);
    }

    private int getDate(RestItem item) {
        try {
            String[] monthAndDay = item.title.split(" ")[0].split("/");
            return Integer.valueOf(monthAndDay[0]) * 100 + Integer.valueOf(monthAndDay[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    WeekRestItem(Parcel in) {
        startDate = in.readInt();
        endDate = in.readInt();

        final int size = in.readInt();
        for (int i = 0; i < size; i++)
            weekList.add((RestItem) in.readParcelable(RestItem.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(startDate);
        dest.writeInt(endDate);

        final int size = weekList.size();
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
