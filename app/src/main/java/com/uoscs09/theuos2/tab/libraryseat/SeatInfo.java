package com.uoscs09.theuos2.tab.libraryseat;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SeatInfo implements Parcelable {
    public ArrayList<SeatItem> seatItemList;
    List<SeatDismissInfo> seatDismissInfoList;

    public SeatInfo() {
    }

    public void clearAndAddAll(SeatInfo info){
        seatDismissInfoList.clear();
        seatDismissInfoList.addAll(info.seatDismissInfoList);

        seatItemList.clear();
        seatItemList.addAll(info.seatItemList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(seatItemList);
        dest.writeTypedList(seatDismissInfoList);
    }

    protected SeatInfo(Parcel in) {
        this.seatItemList = in.createTypedArrayList(SeatItem.CREATOR);
        this.seatDismissInfoList = in.createTypedArrayList(SeatDismissInfo.CREATOR);
    }

    public static final Parcelable.Creator<SeatInfo> CREATOR = new Parcelable.Creator<SeatInfo>() {
        public SeatInfo createFromParcel(Parcel source) {
            return new SeatInfo(source);
        }

        public SeatInfo[] newArray(int size) {
            return new SeatInfo[size];
        }
    };
}
