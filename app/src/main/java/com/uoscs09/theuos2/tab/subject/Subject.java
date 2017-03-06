package com.uoscs09.theuos2.tab.subject;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.parse.IParser;
import com.uoscs09.theuos2.util.AppUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "list")
public class Subject implements Parcelable, IParser.IPostParsing {

    /**
     * 학부
     */
    @Element(name = "sub_dept", cdata = true)
    public String sub_dept;
    /**
     * 교과구분
     */
    @Element(name = "subject_div", cdata = true)
    public String subject_div;
    /**
     * 세부영역
     */
    @Element(name = "subject_div2", cdata = true)
    public String subject_div2;
    /**
     * 교과번호
     */
    @Element(name = "subject_no", cdata = true)
    public String subject_no;
    /**
     * 분반
     */
    @Element(name = "class_div", cdata = true)
    public String class_div;
    /**
     * 교과목명
     */
    @Element(name = "subject_nm", cdata = true)
    public String subject_nm;
    /**
     * 학년
     */
    @Element(name = "shyr", cdata = true)
    public int shyr;
    /**
     * 학점
     */
    @Element(name = "credit", cdata = true)
    public int credit;
    /**
     * 담당교수
     */
    @Element(name = "prof_nm", cdata = true)
    public String prof_nm;
    /**
     * 주야
     */
    @Element(name = "day_night_nm", cdata = true)
    public String day_night_nm;
    /**
     * 강의유형
     */
    @Element(name = "class_type", cdata = true)
    public String class_type;
    /**
     * 강의시간 및 강의실
     */
    @Element(name = "class_nm", cdata = true)
    public String class_nm;
    /**
     * 수강인원
     */
    @Element(name = "tlsn_count")
    public int tlsn_count;
    /**
     * 수강정원
     */
    @Element(name = "tlsn_limit_count", cdata = true)
    public int tlsn_limit_count;

    // 전공 과목
    /**
     * 타과허용
     */
    @Element(name = "etc_permit_yn", cdata = true)
    public String etc_permit_yn = "";
    // 전공 과목
    /**
     * 복수전공
     */
    @Element(name = "sec_permit_yn", cdata = true)
    public String sec_permit_yn = "";
    @Element(name = "year", cdata = true)
    public String year;
    @Element(name = "term", cdata = true)
    public String term;

    private transient ArrayList<ClassInformation> classInformationList = new ArrayList<>();
    private transient String classRoomTimeInformation = "";
    private transient String[] infoArray = null;

    public List<ClassInformation> classInformation() {
        return classInformationList;
    }

    public String getClassRoomTimeInformation() {
        if (TextUtils.isEmpty(classRoomTimeInformation)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < classInformationList.size(); i++) {
                sb.append(classInformationList.get(i).toString()).append('\n');
            }
            if (!classInformationList.isEmpty())
                sb.deleteCharAt(sb.length() - 1);
            classRoomTimeInformation = sb.toString();
        }
        return classRoomTimeInformation;
    }

    public String[] getInfoArray() {
        if (infoArray == null) {
            infoArray = new String[]{
                    sub_dept, subject_div, subject_no, class_div, subject_nm,
                    Integer.toString(shyr), Integer.toString(credit), prof_nm, class_nm,
                    Integer.toString(tlsn_count), Integer.toString(tlsn_limit_count)

            };
        }
        return infoArray;
    }

    public static Comparator<Subject> getComparator(final int field, final boolean isInverse) {
        switch (field) {
            case 0:
            case 1:
            case 4:
            case 7:
            case 8:
                return (lhs, rhs) -> isInverse ? rhs.getInfoArray()[field].compareTo(lhs.getInfoArray()[field])
                        : lhs.getInfoArray()[field].compareTo(rhs.getInfoArray()[field]);
            case 2:
            case 3:
            case 5:
            case 6:
            case 9:
            case 10:
                return (lhs, rhs) -> {
                    int r, l;
                    try {
                        r = Integer.parseInt(rhs.getInfoArray()[field]);
                    } catch (Exception e) {
                        r = Integer.MAX_VALUE;
                    }
                    try {
                        l = Integer.parseInt(lhs.getInfoArray()[field]);
                    } catch (Exception e) {
                        l = Integer.MAX_VALUE;
                    }
                    return isInverse ? r - l : l - r;
                };
            default:
                return null;
        }
    }

    @Override
    public void afterParsing() {
        getInfoArray();

        parseClassTimeAndRoom();
    }

    private void parseClassTimeAndRoom() {
        if (TextUtils.isEmpty(class_nm)) {
            return;
        }
        ClassInfoParser parser = new ClassInfoParser();
        try {
            classInformationList.addAll(parser.parse(class_nm));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Subject() {
    }


    /* Parcelable ---------------------*/

    protected Subject(Parcel in) {
        sub_dept = in.readString();
        subject_div = in.readString();
        subject_div2 = in.readString();
        subject_no = in.readString();
        class_div = in.readString();
        subject_nm = in.readString();
        shyr = in.readInt();
        credit = in.readInt();
        prof_nm = in.readString();
        day_night_nm = in.readString();
        class_type = in.readString();
        class_nm = in.readString();
        tlsn_count = in.readInt();
        tlsn_limit_count = in.readInt();

        etc_permit_yn = in.readString();
        sec_permit_yn = in.readString();

        year = in.readString();
        term = in.readString();

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            ClassInformation information = in.readParcelable(ClassInformation.class.getClassLoader());
            classInformationList.add(information);
        }

        classRoomTimeInformation = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sub_dept);
        dest.writeString(subject_div);
        dest.writeString(subject_div2);
        dest.writeString(subject_no);
        dest.writeString(class_div);
        dest.writeString(subject_nm);
        dest.writeInt(shyr);
        dest.writeInt(credit);
        dest.writeString(prof_nm);
        dest.writeString(day_night_nm);
        dest.writeString(class_type);
        dest.writeString(class_nm);
        dest.writeInt(tlsn_count);
        dest.writeInt(tlsn_limit_count);

        dest.writeString(etc_permit_yn);
        dest.writeString(sec_permit_yn);

        dest.writeString(year);
        dest.writeString(term);

        int size = classInformationList.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            ClassInformation information = classInformationList.get(i);
            dest.writeParcelable(information, information.describeContents());
        }

        dest.writeString(classRoomTimeInformation);

    }

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {

        @Override
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }

    };

    /* ----------------- Parcelable */

    /** 수업의 요일/교시/장소 정보*/
    public static class ClassInformation implements Parcelable, Serializable {

        private static final long serialVersionUID = 8612642740508029423L;
        public int dayInWeek = -1;
        public ArrayList<Integer> times;
        public String buildingAndRoom = "";

        public ClassInformation() {
            times = new ArrayList<>(7);
        }

        @Override
        public String toString() {
            int dayInWeekStringRes = dayInWeekStringRes();
            // ex) 월[5, 6, 7] / 20-311
            return String.format("%s %s / %s", (dayInWeekStringRes != -1 ? AppUtil.context().getString(dayInWeekStringRes) : ""), times.toString(), buildingAndRoom);
        }

        @StringRes
        int dayInWeekStringRes() {
            switch (dayInWeek) {
                case 0:
                    return R.string.tab_timetable_mon;
                case 1:
                    return R.string.tab_timetable_tue;
                case 2:
                    return R.string.tab_timetable_wed;
                case 3:
                    return R.string.tab_timetable_thr;
                case 4:
                    return R.string.tab_timetable_fri;
                case 5:
                    return R.string.tab_timetable_sat;

                default:
                    return -1;
            }
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.dayInWeek);
            dest.writeList(this.times);
            dest.writeString(this.buildingAndRoom);
        }

        ClassInformation(Parcel in) {
            this.dayInWeek = in.readInt();
            this.times = new ArrayList<>(7);
            in.readList(this.times, List.class.getClassLoader());
            this.buildingAndRoom = in.readString();
        }

        public static final Creator<ClassInformation> CREATOR = new Creator<ClassInformation>() {
            public ClassInformation createFromParcel(Parcel source) {
                return new ClassInformation(source);
            }

            public ClassInformation[] newArray(int size) {
                return new ClassInformation[size];
            }
        };
    }
}
