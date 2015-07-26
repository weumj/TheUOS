package com.uoscs09.theuos2.tab.libraryseat;

import android.os.Parcel;
import android.os.Parcelable;

public class SeatDismissInfo implements Parcelable {
    public int time;
    public int seatCount;

    public SeatDismissInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(time);
        dest.writeInt(seatCount);
    }


    protected SeatDismissInfo(Parcel in) {
        this.time = in.readInt();
        this.seatCount = in.readInt();
    }

    public static final Parcelable.Creator<SeatDismissInfo> CREATOR = new Parcelable.Creator<SeatDismissInfo>() {
        public SeatDismissInfo createFromParcel(Parcel source) {
            return new SeatDismissInfo(source);
        }

        public SeatDismissInfo[] newArray(int size) {
            return new SeatDismissInfo[size];
        }
    };
}
