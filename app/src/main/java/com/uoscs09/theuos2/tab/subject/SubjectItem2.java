package com.uoscs09.theuos2.tab.subject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.KeepName;
import com.uoscs09.theuos2.parse.IParser;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

@KeepName
public class SubjectItem2 implements Parcelable, IParser.AfterParsable {

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

    public ArrayList<ClassInformation> classInformationList = new ArrayList<>();

    private String classRoomInformation = StringUtil.NULL;

    public String getClassRoomInformation(Context context) {
        if (classRoomInformation.equals(StringUtil.NULL)) {
            buildClassRoomInfoString(context);
        }

        return classRoomInformation;
    }

    private void buildClassRoomInfoString(Context context){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classInformationList.size(); i++) {
            sb.append(classInformationList.get(i).toString(context)).append('\n');
        }
        if (!classInformationList.isEmpty())
            sb.deleteCharAt(sb.length() - 1);
        classRoomInformation = sb.toString();
    }

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

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            ClassInformation information = in.readParcelable(ClassInformation.class.getClassLoader());
            classInformationList.add(information);
        }

        classRoomInformation = in.readString();
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

        dest.writeString(classRoomInformation);

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

    @Override
    public void afterParsing() {
        setInfoArray();

        parseClassTimeAndRoom();
    }

    private void parseClassTimeAndRoom() {
        if (class_nm == null || class_nm.equals(StringUtil.NULL)) {
            return;
        }

        ClassInfoParser parser = new ClassInfoParser();

        try {
            classInformationList.addAll(parser.parse(class_nm));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static class ClassInfoParser implements IParser<String, ArrayList<ClassInformation>> {

        private int i;
        private String class_nm;

        private int prevParsed;

        private static final int PARSED_DAY_IN_WEEK = 0;
        private static final int PARSED_TIME = 1;
        private static final int PARSED_BUILDING_AND_ROOM = 2;

        @Override
        public ArrayList<ClassInformation> parse(String class_nm) throws Exception {
            this.class_nm = class_nm;
            i = 0;

            ArrayList<ClassInformation> list = new ArrayList<>();
            ClassInformation information = new ClassInformation();
            int length = class_nm.length();
            parseWeek(information);
            parseTimes(information);

            do {
                char peek = peek();
                switch (peek) {
                    case ',':

                        switch (prevParsed) {
                            //다음에 올 것은 시간 (',00')
                            case PARSED_TIME:
                                parseTime(information);
                                break;

                            case PARSED_BUILDING_AND_ROOM:
                                skip();
                                // 다음에 올 것은 다음과목 (', 월~~')
                                if (peek() == ' ') {
                                    skip();

                                    information = new ClassInformation();
                                    parseWeek(information);
                                    parseTimes(information);

                                }

                                break;

                        }
                        break;

                    case '/':

                        switch (prevParsed) {
                            // ',' 가 나타날때까지의 문자열이 건물과 강의실 정보
                            case PARSED_TIME:
                                skip();
                                int index = class_nm.indexOf(',', i);
                                if (index == -1)
                                    index = length;

                                information.buildingAndRoom = getString(index);
                                prevParsed = PARSED_BUILDING_AND_ROOM;

                                list.add(information);

                                break;
                        }

                        break;

                    default:
                        /*
                        int value = getTimeInt();
                        if(value )
                        */
                        break;
                }

            } while (i < length);

            return list;
        }

        private void parseTimes(ClassInformation information) {
            int index = class_nm.indexOf('/', i);

            if (index == -1)
                return;

            String timeString = getString(index);

            String[] timeArray = timeString.split(",");

            for (String str : timeArray) {
                information.times.add(Integer.valueOf(str));
            }
            prevParsed = PARSED_TIME;
        }

        private void parseWeek(ClassInformation information) {
            information.dayInWeek = getWeekInt();
            prevParsed = PARSED_DAY_IN_WEEK;
        }

        private void parseTime(ClassInformation information) {
            int value = getTimeInt();
            information.times.add(value);
            prevParsed = PARSED_TIME;
        }


        private char peek() {
            return class_nm.charAt(i);
        }

        private void skip() {
            i++;
        }

        private String getString(int end) {
            String result = class_nm.substring(i, end);
            i = end;
            return result;
        }

        /**
         * 시작지점부터 2글자 읽어서 강의 시간을 반환
         */
        private int getTimeInt() {
            int result = Integer.valueOf(class_nm.substring(i, i + 2));
            i += 2;
            return result;
        }


        private int getWeekInt() {
            int result;
            char weekInChar = peek();
            switch (weekInChar) {
                case '월':
                    result = 0;
                    break;
                case '화':
                    result = 1;
                    break;
                case '수':
                    result = 2;
                    break;
                case '목':
                    result = 3;
                    break;
                case '금':
                    result = 4;
                    break;
                case '토':
                    result = 5;
                    break;

                case '일':
                    result = 6;
                    break;

                default:
                    return -1;
            }

            i++;
            return result;
        }
    }


    public static class ClassInformation implements Parcelable, Serializable{

        private static final long serialVersionUID = 8612642740508029423L;
        public int dayInWeek = -1;
        public ArrayList<Integer> times = new ArrayList<>(7);
        public String buildingAndRoom = "";

        public ClassInformation() {
        }

        @Override
        public String toString() {
            return dayInWeek + times.toString() + '/' + buildingAndRoom;
        }

        @StringRes
        public int getDayInWeek() {
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

        public String toString(Context context) {
            return (dayInWeek != -1 ? context.getString(getDayInWeek()) : "") + times.toString() + " / " + buildingAndRoom;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(dayInWeek);
            dest.writeString(buildingAndRoom);

            int size = times.size();
            dest.writeInt(size);
            for (int i = 0; i < size; i++)
                dest.writeInt(times.get(i));
        }

        ClassInformation(Parcel source) {
            dayInWeek = source.readInt();
            buildingAndRoom = source.readString();

            int size = source.readInt();
            for (int i = 0; i < size; i++)
                times.add(source.readInt());
        }

        public static final Creator<ClassInformation> CREATOR = new Creator<ClassInformation>() {

            @Override
            public ClassInformation createFromParcel(Parcel source) {
                return new ClassInformation(source);
            }

            @Override
            public ClassInformation[] newArray(int size) {
                return new ClassInformation[size];
            }

        };
    }
}
