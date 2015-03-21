package com.uoscs09.theuos2.tab.subject;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.annotation.KeepName;

import java.util.Comparator;
@KeepName
public class SubjectItem2 implements Parcelable {

    /**
     * 학부
     */
    public String sub_dept;
    /**
     * 교과구분
     */
    public String subject_div;
    /**
     * 세부영역
     */
    public String subject_div2;
    /**
     * 교과번호
     */
    public String subject_no;
    /**
     * 분반
     */
    public String class_div;
    /**
     * 교과목명
     */
    public String subject_nm;
    /**
     * 학년
     */
    public int shyr;
    /**
     * 학점
     */
    public int credit;
    /**
     * 담당교수
     */
    public String prof_nm;
    /**
     * 주야
     */
    public String day_night_nm;
    /**
     * 강의유형
     */
    public String class_type;
    /**
     * 강의시간 및 강의실
     */
    public String class_nm;
    /**
     * 수강인원
     */
    public int tlsn_count;
    /**
     * 수강정원
     */
    public int tlsn_limit_count;

    // 전공 과목
    /**
     * 타과허용
     */
    public String etc_permit_yn = "";
    // 전공 과목
    /**
     * 복수전공
     */
    public String sec_permit_yn = "";


    public String year;
    public String term;

    public SubjectItem2() {
    }

    protected SubjectItem2(Parcel in) {
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

    }

    public static final Creator<SubjectItem2> CREATOR = new Creator<SubjectItem2>() {

        @Override
        public SubjectItem2 createFromParcel(Parcel source) {
            return new SubjectItem2(source);
        }

        @Override
        public SubjectItem2[] newArray(int size) {
            return new SubjectItem2[size];
        }

    };

    public String[] infoArray = null;

    public void setInfoArray() {
        if (infoArray == null)
            infoArray = new String[]{
                    sub_dept, subject_div, subject_no, class_div, subject_nm,
                    Integer.toString(shyr), Integer.toString(credit), prof_nm, class_nm,
                    Integer.toString(tlsn_count), Integer.toString(tlsn_limit_count)

            };
    }

    public static Comparator<SubjectItem2> getComparator(final int field, final boolean isInverse) {
        switch (field) {
            case 0:
            case 1:
            case 4:
            case 7:
            case 8:
                return new Comparator<SubjectItem2>() {
                    @Override
                    public int compare(SubjectItem2 lhs, SubjectItem2 rhs) {
                        lhs.setInfoArray();
                        rhs.setInfoArray();
                        int result = lhs.infoArray[field].compareTo(rhs.infoArray[field]);
                        return isInverse ? -result : result;
                    }
                };
            case 2:
            case 3:
            case 5:
            case 6:
            case 9:
            case 10:
                return new Comparator<SubjectItem2>() {
                    @Override
                    public int compare(SubjectItem2 lhs, SubjectItem2 rhs) {
                        lhs.setInfoArray();
                        rhs.setInfoArray();
                        int r, l;
                        try {
                            r = Integer.valueOf(rhs.infoArray[field]);
                        } catch (Exception e) {
                            r = Integer.MAX_VALUE;
                        }
                        try {
                            l = Integer.valueOf(lhs.infoArray[field]);
                        } catch (Exception e) {
                            l = Integer.MAX_VALUE;
                        }
                        return isInverse ? r - l : l - r;
                    }
                };
            default:
                return null;
        }
    }
}
