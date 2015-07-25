package com.uoscs09.theuos2.util;

import com.google.android.gms.maps.model.LatLng;
import com.uoscs09.theuos2.parse.XmlParser;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * WISE OPEN API 관련 변수와 메소드를 가지는 클래스
 */
public class OApiUtil {

    //********** Parser *******************

    public static XmlParserWrapper<ArrayList<UnivScheduleItem>> getUnivScheduleParser() {
        return new XmlParserWrapper<>(XmlParser.newReflectionParser(UnivScheduleItem.class, "root", "schList", "list"));
    }

    /**
     * for CoursePlan, EmptyClassRoomItem, SubjectItem2, SubjectInfoItem,
     */
    public static <T> XmlParserWrapper<ArrayList<T>> getParser(Class<? extends T> clazz) {
        return new XmlParserWrapper<>(XmlParser.newReflectionParser(clazz, "root", "mainlist", "list"));
    }

    //********** Parser end *******************

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
        SPRING(10, "1학기", "Spring"), AUTUMN(20, "2학기", "Autumn"),
        SUMMER(11, "여름계절학기", "Summer"), WINTER(21, "겨울계절학기", "Winter");

        public final String code;
        public final int intCode;
        public final String shortCode;
        public final String nameKor, nameEng;

        Semester(int code, String nameKor, String nameEng) {
            this.intCode = code;
            this.code = "A" + intCode;
            this.shortCode = Integer.toString(code);
            this.nameKor = nameKor;
            this.nameEng = nameEng;
        }

        public static Semester getSemesterByCode(int code) {
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
            int year = Integer.parseInt(sThisYear);
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


    public enum UnivBuilding {
        Univ(0, "서울시립대학교", "Univ. of Seoul", 37.583921, 127.059011),

        Cheonnong(1, "전농관", "Cheonnong Hall", 37.583594, 127.056568),

        Engineering_1(2, "제1공학관", "The 1st Engineering Building", 37.58482, 127.058488),

        Architecture_and_CivilEngineering(3, "건설공학관", "Architecture and Civil Engineering", 37.583817, 127.057877),

        Changgong(4, "창공관", "Changgong Hall", 37.584593, 127.06068),

        Liberal_Arts(5, "인문학관", "Liberal Arts Building", 37.583759, 127.061098),

        Baebong(6, "배봉관", "Baebong Hall", 37.58468, 127.059655),

        University_Center(7, "대학본부", "University Center", 37.584765, 127.057708),

        Natural_Science(8, "자연과학관", "Natural Science Building", 37.582512, 127.059121),

        Music(9, "음악관", "Music Building", 37.583953, 127.055685),

        Architecture_Engineering_Lab(10, "경농관", "Architecture Engineering Lab", 37.582899, 127.056619),

        Engineering_2(11, "제2공학관", "The 2nd Engineering Building", 37.584703, 127.059041),

        Student_Hall(12, "학생회관", "Student Hall", 37.583702, 127.060073),

        Press_and_ROTC(13, "언무관", "University Press and ROTC Building", 37.58492, 127.060736),

        Science_and_Technology(14, "과학기술관", "Science and Technology Building", 37.585315, 127.057547),

        The_21st_Century(15, "21세기관", "The 21st Century Building", 37.58312, 127.058681),

        Design_and_Sculpture(16, "조형관", "Design and Sculpture Building", 37.584093, 127.056182),

        Gymnaseum(17, "체육관", "University Gymnaseum", 37.58442, 127.055543),

        Birch_Hall(18, "자작마루", "Birch Hall", 37.582841, 127.057662),

        IT(19, "정보기술관", "Information& Techology Building", 37.582896, 127.060771),

        Law(20, "법학관", "General Lecture and Law Institute", 37.582008, 127.05679),

        Library(21, "중앙도서관", "Main Library", 37.584809, 127.062131),

        Dormitory(22, "생활관", "Student Dormitory", 37.585409, 127.062823),

        Lab_Architeclural(23, "건축구조실험동", "Laboratory of Architeclural Engineering", 37.582295, 127.057818),

        Lumber_Mill(24, "토목구조실험동", "Lumber Mill", 37.582376, 127.057432),

        Media(25, "미디어관", "Media Hall", 37.582531, 127.060124),

        Greenhouse(26, "자동화온실", "Greenhouse", 37.582431, 127.060767),

        Main_Auditorium(27, "대강당", "Main Auditorium", 37.583009, 127.059765),

        Main_Stadium(28, "운동장", "Main Stadium", 37.585262, 127.056653),

        Museum(29, "박물관", "University Museum", 37.583124, 127.056932),

        Main_Gate(30, "정문", "Main Gate", 37.583447, 127.054974),

        Rear_Gate(31, "후문", "Rear Gate", 37.585215, 127.060939),

        Wellness(32, "웰니스센터", "Wellness Center", 37.582401, 127.056546),

        Mirae(33, "미래관", "Mirae Hall", 37.58441, 127.057145),

        International(34, "국제학사", "International House", 37.584342, 127.063399),

        Institute_Int_Coop_Edu(35, "국제교육원", "Institute of International Cooperation and Education", 37.584081, 127.060786);

        public final int code;
        public final String nameKor;
        public final String nameEng;
        public final double lat, lang;

        UnivBuilding(int code, String nameKor, String nameEng, double lat, double lang) {
            this.code = code;
            this.nameKor = nameKor;
            this.nameEng = nameEng;
            this.lat = lat;
            this.lang = lang;
        }

        public LatLng latLng() {
            return new LatLng(lat, lang);
        }

        public static UnivBuilding fromNumber(int number) {
            if (number > -1 && number < 36) {
                return values()[number];
            }

            return null;
        }

        public String getLocaleName() {
            return Locale.getDefault().equals(Locale.KOREA) ? nameKor : nameEng;
        }

    }

}
