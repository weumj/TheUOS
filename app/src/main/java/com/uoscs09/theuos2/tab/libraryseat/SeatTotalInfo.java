package com.uoscs09.theuos2.tab.libraryseat;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SeatTotalInfo implements Parcelable {
    private List<SeatInfo> seatInfoList;
    private List<SeatDismissInfo> seatDismissInfoList;

    public SeatTotalInfo(List<SeatInfo> seatInfoList, List<SeatDismissInfo> seatDismissInfoList) {
        this();
        this.seatInfoList.addAll(seatInfoList);
        this.seatDismissInfoList.addAll(seatDismissInfoList);
    }

    public SeatTotalInfo() {
        seatInfoList = new ArrayList<>();
        seatDismissInfoList = new ArrayList<>();
    }

    public boolean isSeatListEmpty() {
        return seatInfoList.isEmpty();
    }

    public int seatListSize() {
        return seatInfoList.size();
    }

    public List<SeatInfo> seatInfoList() {
        return seatInfoList;
    }

    public boolean isSeatDismissListEmpty() {
        return seatDismissInfoList.isEmpty();
    }

    public List<SeatDismissInfo> seatDismissInfoList() {
        return seatDismissInfoList;
    }

    public void clearAll() {
        seatDismissInfoList.clear();
        seatInfoList.clear();
    }

    public void addAll(SeatTotalInfo info) {
        seatDismissInfoList.addAll(info.seatDismissInfoList);
        seatInfoList.addAll(info.seatInfoList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(seatInfoList);
        dest.writeTypedList(seatDismissInfoList);
    }

    protected SeatTotalInfo(Parcel in) {
        this.seatInfoList = in.createTypedArrayList(SeatInfo.CREATOR);
        this.seatDismissInfoList = in.createTypedArrayList(SeatDismissInfo.CREATOR);
    }

    public static final Parcelable.Creator<SeatTotalInfo> CREATOR = new Parcelable.Creator<SeatTotalInfo>() {
        public SeatTotalInfo createFromParcel(Parcel source) {
            return new SeatTotalInfo(source);
        }

        public SeatTotalInfo[] newArray(int size) {
            return new SeatTotalInfo[size];
        }
    };
}
