package com.uoscs09.theuos2.tab.buildings;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root")
public class ClassRoomTimeTable implements Parcelable, Serializable {

    private static final long serialVersionUID = -8771076559473478423L;

    @ListContainer(name = "mainlist")
    private List<Timetable> timetableList;

    public List<Timetable> timetables() {
        return timetableList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(timetableList);
    }

    public ClassRoomTimeTable() {
    }

    protected ClassRoomTimeTable(Parcel in) {
        this.timetableList = in.createTypedArrayList(Timetable.CREATOR);
    }

    public static final Creator<ClassRoomTimeTable> CREATOR = new Creator<ClassRoomTimeTable>() {
        @Override
        public ClassRoomTimeTable createFromParcel(Parcel source) {
            return new ClassRoomTimeTable(source);
        }

        @Override
        public ClassRoomTimeTable[] newArray(int size) {
            return new ClassRoomTimeTable[size];
        }
    };


    @Root(name = "list")
    public static class Timetable implements Parcelable, Serializable {
        private static final long serialVersionUID = -3195316989062654934L;
        @Element(name = "building")
        private String building;
        @Element(name = "room_no")
        private String roomNo;
        @Element(name = "room_div")
        private String roomDiv;
        @Element(name = "person_cnt")
        private int personCount;

        @Element(name = "period")
        private String period;
        @Element(name = "time")
        private String time;
        @Element(name = "mon")
        private String mon;
        @Element(name = "tue")
        private String tue;
        @Element(name = "wed")
        private String wed;
        @Element(name = "thu")
        private String thu;
        @Element(name = "fri")
        private String fri;
        @Element(name = "sat")
        private String sat;

        public String building() {
            return building;
        }

        public String roomNo() {
            return roomNo;
        }

        public String roomDiv() {
            return roomDiv;
        }

        public String period() {
            return period;
        }

        public String time() {
            return time;
        }

        public String mon() {
            return mon;
        }

        public String tue() {
            return tue;
        }

        public String wed() {
            return wed;
        }

        public String thu() {
            return thu;
        }

        public String fri() {
            return fri;
        }

        public String sat() {
            return sat;
        }

        public int personCount() {
            return personCount;
        }

        @Override
        public String toString() {
            return roomNo + " | " + roomDiv + " | " + building + "\n"
                    + period + "|\n"
                    + mon + "|\n"
                    + tue + "|\n"
                    + wed + "|\n"
                    + thu + "|\n"
                    + fri + "|\n"
                    + sat + "|\n";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.building);
            dest.writeString(this.roomNo);
            dest.writeString(this.roomDiv);
            dest.writeInt(this.personCount);
            dest.writeString(this.period);
            dest.writeString(this.time);
            dest.writeString(this.mon);
            dest.writeString(this.tue);
            dest.writeString(this.wed);
            dest.writeString(this.thu);
            dest.writeString(this.fri);
            dest.writeString(this.sat);
        }

        public Timetable() {
        }

        protected Timetable(Parcel in) {
            this.building = in.readString();
            this.roomNo = in.readString();
            this.roomDiv = in.readString();
            this.personCount = in.readInt();
            this.period = in.readString();
            this.time = in.readString();
            this.mon = in.readString();
            this.tue = in.readString();
            this.wed = in.readString();
            this.thu = in.readString();
            this.fri = in.readString();
            this.sat = in.readString();
        }

        public static final Creator<Timetable> CREATOR = new Creator<Timetable>() {
            @Override
            public Timetable createFromParcel(Parcel source) {
                return new Timetable(source);
            }

            @Override
            public Timetable[] newArray(int size) {
                return new Timetable[size];
            }
        };
    }


}
