package com.uoscs09.theuos.http.parse;


import com.uoscs09.theuos.tab.timetable.Subject;
import com.uoscs09.theuos.tab.timetable.TimeTable;
import com.uoscs09.theuos.util.OApiUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

/** Thread-Unsafe*/
public class ParseTimeTable2 extends XmlParser<TimeTable> {

    private static final String LIST = "list";
    private TimeTable timeTable;

    @Override
    protected TimeTable parseContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "root");

        timeTable = new TimeTable(15);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch (parser.getName()) {
                case "strSmtCd":
                    timeTable.semesterCode = OApiUtil.Semester.getSemesterFromCode(Integer.valueOf(readText(parser)));
                    break;

                case "strMyEngShreg":
                    try {
                        timeTable.studentInfoEng = new TimeTable.StudentInfo(readText(parser));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "strMyShreg":
                    try {
                        timeTable.studentInfoKor = new TimeTable.StudentInfo(readText(parser));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                case "timeList":
                    readTimeList(parser);
                    break;

                case "strSchYear":
                    timeTable.year = Integer.valueOf(readText(parser));
                    break;

                default:
                    skip(parser);
                    break;
            }


        }
        timeTable.setMaxTime();
        return timeTable;
    }

    private void readTimeList(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "timeList");

        int count = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (parser.getName()) {
                case LIST:
                    readList(parser, count);
                    count++;
                    break;

                default:
                    skip(parser);
                    break;
            }

        }

    }

    private void readList(XmlPullParser parser, int count) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, LIST);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            switch (parser.getName()) {
                case "str01":
                    readSubject(parser, 0, count, false);
                    break;

                case "str02":
                    readSubject(parser, 1, count, false);
                    break;

                case "str03":
                    readSubject(parser, 2, count, false);
                    break;

                case "str04":
                    readSubject(parser, 3, count, false);
                    break;

                case "str05":
                    readSubject(parser, 4, count, false);
                    break;
                case "str06":
                    readSubject(parser, 5, count, false);
                    break;

                case "str01_eng":
                    readSubject(parser, 0, count, true);
                    break;

                case "str02_eng":
                    readSubject(parser, 1, count, true);
                    break;

                case "str03_eng":
                    readSubject(parser, 2, count, true);
                    break;

                case "str04_eng":
                    readSubject(parser, 3, count, true);
                    break;

                case "str05_eng":
                    readSubject(parser, 4, count, true);
                    break;

                case "str06_eng":
                    readSubject(parser, 5, count, true);
                    break;

                default:
                    skip(parser);
                    break;
            }

        }
    }

    private void readSubject(XmlPullParser parser, int day, int period, boolean isEng) throws IOException, XmlPullParserException {
        String text = readText(parser);

        Subject[] array = timeTable.subjects.get(period);
        if (text == null) {
            array[day] = Subject.EMPTY;
            return;
        }

        String rawText = OApiParser2.removeCDATA(text);

        if (rawText.length() < 2) {
            array[day] = Subject.EMPTY;
            return;
        }

        Subject subject = new Subject(rawText, day, period, isEng);

        if (isEng) {
            Subject subject1 = array[day];
            subject1.professorEng = subject.professorEng;
            subject1.subjectNameEng = subject.subjectNameEng;
            subject1.subjectNameEngShort = subject.subjectNameEngShort;

        } else {
            array[day] = subject;

            if (period != 0) {
                subject.isEqualToUpperPeriod = subject.isEqualsTo(timeTable.subjects.get(period - 1)[day]);
            }
        }

    }

}
