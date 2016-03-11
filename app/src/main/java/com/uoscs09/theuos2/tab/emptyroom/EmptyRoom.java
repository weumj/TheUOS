package com.uoscs09.theuos2.tab.emptyroom;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.annotation.KeepName;

import java.util.Comparator;

@KeepName
public class EmptyRoom implements Parcelable {

    public int person_cnt;

    public String room_no = "";

    public String room_div = "";

    public String visual_yn = "";

    public String building = "";

    public int assign_time;


    public EmptyRoom() {
    }

    private EmptyRoom(Parcel p) {
        person_cnt = p.readInt();
        room_no = p.readString();
        room_div = p.readString();
        visual_yn = p.readString();
        building = p.readString();
        assign_time = p.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(person_cnt);
        dest.writeString(room_no);
        dest.writeString(room_div);
        dest.writeString(visual_yn);
        dest.writeString(building);
        dest.writeInt(assign_time);
    }

    public static final Parcelable.Creator<EmptyRoom> CREATOR = new Parcelable.Creator<EmptyRoom>() {

        @Override
        public EmptyRoom createFromParcel(Parcel p) {
            return new EmptyRoom(p);
        }

        @Override
        public EmptyRoom[] newArray(int size) {
            return new EmptyRoom[size];
        }

    };

    /**
     * 리스트 정렬에 사용되는 Comparator 객체를 반환한다.
     *
     * @param field - 정렬 주체가 될 문자열 필드<br>
     */
    public static Comparator<EmptyRoom> getComparator(final int field, final boolean isReverse) {

        switch (field) {
            case 0:
            case 1:
            case 2:

                return (lhs, rhs) -> {
                    String l, r;
                    switch (field) {
                        case 0:
                            l = lhs.building;
                            r = rhs.building;
                            break;

                        case 1:
                            l = lhs.room_no;
                            r = rhs.room_no;
                            break;

                        case 2:
                            l = lhs.room_div;
                            r = rhs.room_div;
                            break;

                        default:
                            return 0;
                    }

                    return isReverse ? r.compareTo(l) : l.compareTo(r);
                };

            case 3:

                return (lhs, rhs) -> {
                    int result = lhs.person_cnt < rhs.person_cnt ? -1 : (lhs.person_cnt == rhs.person_cnt ? 0 : 1);
                    return isReverse ? -result : result;
                };

            default:
                return null;

        }

    }
}
