package com.uoscs09.theuos2.tab.emptyroom;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Root(name = "list")
public class EmptyRoom implements Parcelable {

    @Element(name = "person_cnt")
    public int personCount;
    @Element(name = "room_no", cdata = true)
    public String roomNo = "";
    @Element(name = "room_div", cdata = true)
    public String roomDiv = "";
    @Element(name = "visual_yn", cdata = true)
    public String visualYn = "";
    @Element(name = "building", cdata = true)
    public String building = "";
    @Element(name = "assign_time")
    public int assignTime;


    public EmptyRoom() {
    }

    private EmptyRoom(Parcel p) {
        personCount = p.readInt();
        roomNo = p.readString();
        roomDiv = p.readString();
        visualYn = p.readString();
        building = p.readString();
        assignTime = p.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(personCount);
        dest.writeString(roomNo);
        dest.writeString(roomDiv);
        dest.writeString(visualYn);
        dest.writeString(building);
        dest.writeInt(assignTime);
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
                            l = lhs.roomNo;
                            r = rhs.roomNo;
                            break;

                        case 2:
                            l = lhs.roomDiv;
                            r = rhs.roomDiv;
                            break;

                        default:
                            return 0;
                    }

                    return isReverse ? r.compareTo(l) : l.compareTo(r);
                };

            case 3:

                return (lhs, rhs) -> {
                    int result = lhs.personCount < rhs.personCount ? -1 : (lhs.personCount == rhs.personCount ? 0 : 1);
                    return isReverse ? -result : result;
                };

            default:
                return null;

        }

    }
}
