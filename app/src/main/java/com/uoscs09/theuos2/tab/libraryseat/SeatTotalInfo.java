package com.uoscs09.theuos2.tab.libraryseat;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SeatTotalInfo implements Parcelable {
    public ArrayList<SeatInfo> seatInfoList;
    List<SeatDismissInfo> seatDismissInfoList;

    public SeatTotalInfo() {
        seatInfoList = new ArrayList<>();
        seatDismissInfoList = new ArrayList<>();
    }

    public void clearAndAddAll(SeatTotalInfo info){
        seatDismissInfoList.clear();
        seatDismissInfoList.addAll(info.seatDismissInfoList);

        seatInfoList.clear();
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
