package com.uoscs09.theuos.util;

import java.util.Calendar;

/**
 * WISE OPEN API관련 변수와 메소드를 가지는 클래스
 */
public class OApiUtil {
    private static String sThisYear;
    private static String[] sYears;
    public static final String UOS_API_KEY = OApiKey.WISE_OAPI_KEY.toString();
    public static final String API_KEY = "apiKey";
    public static final String TERM = "term";
    public static final String YEAR = "year";
    public static final String SUBJECT_NAME = "subjectNm";
    public static final String SUBJECT_NO = "subjectNo";
    public static final String CLASS_DIV = "classDiv";

    public static final String URL_API_MAIN_DB = "http://wise.uos.ac.kr/uosdoc/api.ApiApiMainBd.oapi";

    public enum Semester {
        SPRING(10), AUTUMN(20), SUMMER(11), WINTER(21);

        public final String code;
        public final int intCode;
       public final String shortCode;

        Semester(int code) {
            this.intCode = code;
            this.code = "A" + intCode;
            this.shortCode = Integer.toString(code);
        }

        public static Semester getSemesterFromCode(int code) {
            switch (code) {
                case 10:
                    return SPRING;
                case 20:
                    return AUTUMN;
                case 11:
                    return SUMMER;
                case 21:
                    return WINTER;
                default:
                    return null;
            }
        }
    }

    /**
     * 현재 연도를 얻는다. ex) "2013"
     */
    public static synchronized String getYear() {
        if (sThisYear == null) {
            sThisYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)).intern();
        }
        return sThisYear;
    }

    /**
     * 현재 연도를 기준점으로 -2 ~ + 1 범위의 연도를얻는다. <br>
     * ex) 현재 연도가 "2013"이면 2011, 2012, 2013, 2014 배열을 얻게된다.
     */
    public static String[] getYears() {
        if (sThisYear == null) {
            getYear();
        }
        if (sYears == null) {
            int year = Integer.valueOf(sThisYear);
            sYears = new String[]{
                    Integer.toString((year - 2)),
                    Integer.toString((year - 1)),
                    sThisYear,
                    Integer.toString((year + 1))
            };
        }
        return sYears;
    }

    /**
     * 학기를 기준점으로 연도를 얻는다.<br>
     * 보통 {@link #getYear()}를 통해 얻는값과 크게 다르지 않지만, 겨울 계절학기의 경우 현재 연도 보다 -1된 값을
     * 얻는다
     */
    @Deprecated
    public static String getSemesterYear(Semester semester) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        if (semester == Semester.WINTER && (month < 3 || month == 12)) {
            year--;
        }

        return Integer.toString(year);
    }

    @Deprecated
    public static Semester getTerm() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;

        switch (m) {
            case 1:
            case 2:
                return Semester.WINTER;
            case 3:
            case 4:
            case 5:
            case 6:
                return Semester.SPRING;
            case 7:
            case 8:
                return Semester.SUMMER;
            default:
                return Semester.AUTUMN;
        }
    }

    /**
     * @return 날짜를 나타내는 숫자 <br>
     * 6월 1일 : 601<br>
     * 12월 12일 : 1212
     */
    public static int getDate() {
        Calendar c = Calendar.getInstance();
        return (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DATE);
    }

    /**
     * @return 날짜와 시간을 나타내는 숫자 <br>
     * 6월 1일 10시: 60110<br>
     * 6월 1일 23시: 60123<br>
     */
    public static int getDateTime() {
        Calendar c = Calendar.getInstance();
        return (c.get(Calendar.MONTH) + 1) * 10000 + c.get(Calendar.DATE) * 100
                + c.get(Calendar.HOUR_OF_DAY);
    }

    @Deprecated
    public static String getBuildingName(String buildingNo) {
        StringBuilder sb = new StringBuilder();
        switch (Integer.valueOf(buildingNo)) {
            case 1:
                sb.append("전농관");
                break;
            case 2:
                sb.append("제1공학관");
                break;
            case 3:
                sb.append("건설공학관");
                break;
            case 4:
                sb.append("창공관");
                break;
            case 5:
                sb.append("인문학관");
                break;
            case 6:
                sb.append("배봉관");
                break;
            case 8:
                sb.append("자연과학관");
                break;
            case 9:
                sb.append("음악관");
                break;
            case 10:
                sb.append("경농관");
                break;
            case 11:
                sb.append("제2공학관");
                break;
            case 13:
                sb.append("언무관");
                break;
            case 14:
                sb.append("과학기술관");
                break;
            case 15:
                sb.append("21세기관");
                break;
            case 16:
                sb.append("조형관");
                break;
            case 17:
                sb.append("체육관");
                break;
            case 19:
                sb.append("정보기술관");
                break;
            case 20:
                sb.append("법학관");
                break;
            case 23:
                sb.append("건축구조실험동");
                break;
            case 24:
                sb.append("토목구조실험동");
                break;
            case 25:
                sb.append("미디어관");
                break;
            case 26:
                sb.append("자동화온실");
                break;
            case 28:
                sb.append("운동장");
                break;
            case 33:
                sb.append("미래관");
                break;
            default:
                return null;
        }
        return sb.append('\n').append(buildingNo).append('-').toString();
    }

    @Deprecated
    public static String getSubjectName(String timetable) {
        try {
            return timetable.trim().split(StringUtil.NEW_LINE)[0];
        } catch (Exception e) {
            return timetable.trim();
        }
    }

    @Deprecated
    public static String getCompressedString(String timetable) {
        String[] arr = timetable.trim().split(StringUtil.NEW_LINE);
        if (arr.length > 3) {
            String str = arr[0];
            if (str.length() > 6) {
                str = str.substring(0, 5) + "..";
            }
            return str + StringUtil.NEW_LINE + arr[2] + StringUtil.NEW_LINE
                    + arr[3];
        } else {
            return timetable;
        }
    }
}
