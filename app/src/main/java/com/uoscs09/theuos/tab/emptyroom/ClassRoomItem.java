package com.uoscs09.theuos.tab.emptyroom;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.annotation.KeepName;

import java.util.Comparator;

@KeepName
public class ClassRoomItem implements Parcelable {

    public int person_cnt;

    public String room_no = "";

    public String room_div = "";

    public String visual_yn = "";

    public String building = "";

    public int assign_time;


    public ClassRoomItem() {
    }

    protected ClassRoomItem(Parcel p) {
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

    public static final Parcelable.Creator<ClassRoomItem> CREATOR = new Parcelable.Creator<ClassRoomItem>() {

        @Override
        public ClassRoomItem createFromParcel(Parcel p) {
            return new ClassRoomItem(p);
        }

        @Override
        public ClassRoomItem[] newArray(int size) {
            return new ClassRoomItem[size];
        }

    };

    /**
     * 리스트 정렬에 사용되는 Comparator 객체를 반환한다.
     *
     * @param field - 정렬 주체가 될 문자열 필드<br>
     */
    public static Comparator<ClassRoomItem> getComparator(final int field, final boolean isReverse) {

        switch (field) {
            case 0:
            case 1:
            case 2:

                return new Comparator<ClassRoomItem>() {

                    @Override
                    public int compare(ClassRoomItem lhs, ClassRoomItem rhs) {
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

                        return isReverse ? -(l.compareTo(r)) : l.compareTo(r);
                    }
                };

            case 3:

                return new Comparator<ClassRoomItem>() {
                    @Override
                    public int compare(ClassRoomItem lhs, ClassRoomItem rhs) {
                        int result = lhs.person_cnt < rhs.person_cnt ? -1 : (lhs.person_cnt == rhs.person_cnt ? 0 : 1);
                        return isReverse ? -result : result;
                    }
                };

            default:
                return null;

        }

    }
}
