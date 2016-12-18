package com.uoscs09.theuos2.tab.subject;

import com.uoscs09.theuos2.parse.IParser;

import java.util.ArrayList;
import java.util.List;
/* 수업의 시간과 장소 정보 문자열을 파싱 */
class ClassInfoParser extends IParser.Base<String, List<Subject.ClassInformation>> {
    private static final int PARSED_DAY_IN_WEEK = 0;
    private static final int PARSED_TIME = 1;
    private static final int PARSED_BUILDING_AND_ROOM = 2;

    private int i;
    private String class_nm;

    private int prevParsed;

    @Override
    public List<Subject.ClassInformation> parse(String class_nm) throws Exception {
        this.class_nm = class_nm;
        i = 0;

        ArrayList<Subject.ClassInformation> list = new ArrayList<>();
        Subject.ClassInformation information = new Subject.ClassInformation();
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

                                information = new Subject.ClassInformation();
                                parseWeek(information);
                                parseTimes(information);
                            }
                            break;

                        default:
                            break;

                    }
                    break;

                case '/':
                    switch (prevParsed) {
                        // ',' 가 나타날때까지의 문자열이 건물과 강의실 정보
                        case PARSED_TIME:
                            skip();
                            int index = class_nm.indexOf(',', i);
                            if (index == -1) index = length;

                            information.buildingAndRoom = getString(index);
                            prevParsed = PARSED_BUILDING_AND_ROOM;

                            list.add(information);
                            break;

                        default:
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

    private void parseTimes(Subject.ClassInformation information) {
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

    private void parseWeek(Subject.ClassInformation information) {
        information.dayInWeek = getWeekInt();
        prevParsed = PARSED_DAY_IN_WEEK;
    }

    private void parseTime(Subject.ClassInformation information) {
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
        int result = Integer.parseInt(class_nm.substring(i, i + 2));
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
