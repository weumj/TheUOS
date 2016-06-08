package com.uoscs09.theuos2.tab.buildings;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root")
public class ClassroomTimeTable implements Parcelable, Serializable {

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

    public ClassroomTimeTable() {
    }

    protected ClassroomTimeTable(Parcel in) {
        this.timetableList = in.createTypedArrayList(Timetable.CREATOR);
    }

    public static final Creator<ClassroomTimeTable> CREATOR = new Creator<ClassroomTimeTable>() {
        @Override
        public ClassroomTimeTable createFromParcel(Parcel source) {
            return new ClassroomTimeTable(source);
        }

        @Override
        public ClassroomTimeTable[] newArray(int size) {
            return new ClassroomTimeTable[size];
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

        /* lazy & shorten information */
        private String monL = null;
        private String tueL = null;
        private String wedL = null;
        private String thuL = null;
        private String friL = null;
        private String satL = null;
        /* lazy & shorten information */

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

        /**
         * @param day 1 (mon) ~ 6 (sat) , default = ""
         * */
        public String dateInfo(int day){
            switch (day){
                case 1:
                    return mon();
                case 2:
                    return tue();
                case 3:
                    return wed();
                case 4:
                    return thu();
                case 5:
                    return fri();
                case 6:
                    return sat();
                default:
                    return "";
            }
        }

        public String mon() {
            if (monL == null) {
                monL = lazyAndShorten(mon);
            }
            return monL;
        }

        public String tue() {
            if (tueL == null) {
                tueL = lazyAndShorten(tue);
            }
            return tueL;
        }

        public String wed() {
            if (wedL == null) {
                wedL = lazyAndShorten(wed);
            }
            return wedL;
        }

        public String thu() {
            if (thuL == null) {
                thuL = lazyAndShorten(thu);
            }
            return thuL;
        }

        public String fri() {
            if (friL == null) {
                friL = lazyAndShorten(fri);
            }
            return friL;
        }

        public String sat() {
            if (satL == null) {
                satL = lazyAndShorten(sat);
            }
            return satL;
        }

        /*
            original :
                    "과목명\r교수\r학년\r학부"

            return :
                    "과목명\n교수"

         */
        private String lazyAndShorten(String s) {
            String[] arr = s.replace("\r", "\n").split("\n");
            if (arr.length < 2)
                return s;

            return arr[0] + "\n\n" + arr[1];
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
