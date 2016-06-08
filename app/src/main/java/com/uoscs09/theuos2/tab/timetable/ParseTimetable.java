package com.uoscs09.theuos2.tab.timetable;

import android.text.TextUtils;

import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.tab.subject.SubjectItem2;
import com.uoscs09.theuos2.util.OApiUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mj.android.utils.xml.Element;
import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;
import mj.android.utils.xml.XmlParser;

public class ParseTimetable {

    public Timetable2 parse(InputStream inputStream) throws Exception {
        TimetableBuilder builder = new XmlParser<>(TimetableBuilder.class).parse(inputStream);

        OApiUtil.Semester semester = OApiUtil.Semester.getSemesterByCode(Integer.parseInt(builder.semesterCode));
        int year = Integer.parseInt(builder.year);

        List<Timetable2.Period> periods = subjectInfo(builder);
        int maxTime = calculateLastExistPeriod(periods);

        return new Timetable2(semester, year, studentInfo(builder),
                periods, buildColorTable(periods),
                maxTime,
                buildClassTimeInformationTable(periods, maxTime));
    }

    private List<Timetable2.Period> subjectInfo(TimetableBuilder builder) {
        List<TimetableBuilder.Period> periodList = builder.list;
        final int N = periodList.size();

        List<Timetable2.Period> list = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            TimetableBuilder.Period period = periodList.get(i), periodPrior;

            periodPrior = i > 0 ? periodList.get(i - 1) : null;

            Timetable2.SubjectInfo[] subjectInfoArray = subjectInfoArray(period, periodPrior);
            Timetable2.Period period1 = new Timetable2.Period(period.period, period.periodEng, period.time.replace("\r", "\n~\n"), subjectInfoArray);

            list.add(period1);
        }

        return list;
    }

    private Timetable2.SubjectInfo[] subjectInfoArray(TimetableBuilder.Period period, TimetableBuilder.Period periodPrior) {
        Timetable2.SubjectInfo[] subjectInfoArray = new Timetable2.SubjectInfo[6];

        for (int j = 0; j < 6; j++) {
            Timetable2.SubjectInfo subjectInfo;

            String subjectK = period.subject(j);
            String subjectE = period.subjectEng(j);

            if (TextUtils.isEmpty(subjectK)) {
                subjectInfo = null;
            } else {
                String priorSubjectK = periodPrior != null ? periodPrior.subject(j) : null;
                boolean equalPrior = subjectK.equals(priorSubjectK);

                String[] arr = subjectK.split("\r");
                if (arr.length < 3)
                    subjectInfo = new Timetable2.SubjectInfo(subjectK, subjectE, "", "", "", "", null, equalPrior);
                else {
                    String[] buildingArr = arr[2].trim().split("-");

                    OApiUtil.UnivBuilding univBuilding;

                    if (buildingArr.length < 2)
                        univBuilding = null;
                    else {
                        int building;
                        try {
                            building = Integer.parseInt(buildingArr[0].trim());
                        } catch (Exception e) {
                            building = -1;
                        }
                        univBuilding = OApiUtil.UnivBuilding.fromNumber(building);
                    }

                    String[] arrE = subjectE.split("\r");
                    if (arrE.length < 3) {
                        subjectInfo = new Timetable2.SubjectInfo(arr[0].trim(), subjectE, arr[1].trim(), "", arr[2].trim(), "", univBuilding, equalPrior);
                    } else {
                        subjectInfo = new Timetable2.SubjectInfo(arr[0].trim(), arrE[0].trim(), arr[1].trim(), arrE[1].trim(),
                                arr[2].trim(), arrE[2].trim(), univBuilding, equalPrior);
                    }
                }
            }

            subjectInfoArray[j] = subjectInfo;

        }

        return subjectInfoArray;
    }

    private Timetable2.StudentInfo studentInfo(TimetableBuilder builder) {
        String register = builder.register;


        if (TextUtils.isEmpty(register)) {
            return null;
        }

        String[] strings = register.split("\\([0-9]{10}\\)");
        if (strings.length < 2)
            return null;

        String nameK = strings[0].trim();
        String depK = strings[1].trim();
        int num = Integer.parseInt(register.substring(strings[0].length(), register.indexOf(strings[1])).trim()
                .replace("(", "").replace(")", ""));

        String registerEng = builder.registerEng;
        String[] strings2 = registerEng.split("\\([0-9]{10}\\)");
        if (strings2.length < 2)
            return null;

        String nameE = strings2[0].trim();
        String depE = strings2[1].trim();

        return new Timetable2.StudentInfo(nameK, nameE, num, depK, depE);
    }


    private SerializableArrayMap<String, Integer> buildColorTable(List<Timetable2.Period> periods) {
        SerializableArrayMap<String, Integer> colorTable = new SerializableArrayMap<>();
        final int N = periods.size();
        int idx = 0;
        //todo
        for (int i = 0; i < N; i++) {
            Timetable2.Period period = periods.get(i);
            for (Timetable2.SubjectInfo subjectInfo : period.subjectInformation) {
                if (subjectInfo == null)
                    continue;

                String subjectName = subjectInfo.nameKor();

                if (!TextUtils.isEmpty(subjectName) && !colorTable.containsKey(subjectName)) {
                    colorTable.put(subjectName, idx++);
                }
            }
        }

        return colorTable;
    }

    private int calculateLastExistPeriod(List<Timetable2.Period> periods) {

        final int N = periods.size() - 1;
        //todo
        for (int i = N; i > -1; i--) {
            Timetable2.Period period = periods.get(i);

            for (Timetable2.SubjectInfo subjectInfo : period.subjectInformation) {
                if (subjectInfo != null && !TextUtils.isEmpty(subjectInfo.name()))
                    return i + 1;
            }
        }

        return 1;
    }

    private SerializableArrayMap<String, ArrayList<SubjectItem2.ClassInformation>> buildClassTimeInformationTable(List<Timetable2.Period> periods, int maxTime) {
        SerializableArrayMap<String, ArrayList<SubjectItem2.ClassInformation>> map = new SerializableArrayMap<>();
        // 날짜 선택
        for (int i = 0; i < 6; i++) {

            // 하나의 요일 계산 (세로줄)
            for (int j = 0; j < maxTime; j++) {
                Timetable2.Period period = periods.get(j);

                if (period.subjectInformation.length <= i)
                    continue;

                // 요일 - i , 시간 - j
                Timetable2.SubjectInfo subject = period.subjectInformation[i];
                if (subject != null && !TextUtils.isEmpty(subject.name())) {
                    String key = subject.nameKor();

                    ArrayList<SubjectItem2.ClassInformation> infoList = map.get(key);
                    if (infoList == null) {
                        infoList = new ArrayList<>();
                        map.put(key, infoList);
                    }

                    SubjectItem2.ClassInformation info;
                    boolean needAdd = true;

                    if (!infoList.isEmpty()) {
                        int infoListSize = infoList.size();
                        for (int k = 0; k < infoListSize; k++) {
                            info = infoList.get(k);
                            if (info.dayInWeek == i) {
                                // 기존 저장된 장소, 날짜가 완전히 같으면, 시간만 추가한다.
                                if (info.buildingAndRoom.equals(subject.location())) {
                                    // times 는 1부터 시작 (1교시)
                                    info.times.add(j + 1);
                                    needAdd = false;
                                    break;
                                }
                                // 날짜만 같으면, 다음 항목에서 검사한다.
                                // else {}

                            }
                            // 장소, 날짜가 모두 다르면, 다음 항목에서 검사한다.
                            // else {}

                            // 모두 검사해서 일치하는 항목이 없으면 새로 추가한다.
                        }

                    }

                    // 모두 검사해서 일치하는 항목이 없으면 새로 추가한다.
                    if (needAdd) {
                        info = new SubjectItem2.ClassInformation();
                        info.dayInWeek = i;
                        info.buildingAndRoom = subject.location();
                        // times 는 1부터 시작 (1교시)
                        info.times.add(j + 1);

                        infoList.add(info);
                    }

                }
            }

        }

        return map;
    }


    @Root(name = "root", charset = "euc-kr")
    private static class TimetableBuilder {
        @Element(name = "strSmtCd")
        String semesterCode;
        @Element(name = "strSchYear")
        String year;

        @Element(name = "strMyShreg", require = false)
        String register;
        @Element(name = "strMyEngShreg", require = false)
        String registerEng;

        @ListContainer(name = "timeList")
        List<Period> list;

        @Root(name = "list")
        private static class Period {
            @Element(name = "lttm")
            String period;
            @Element(name = "lttm_eng")
            String periodEng;
            @Element(name = "time")
            String time;

            //todo @ArrayElement(a,b,c,...)
            @Element(name = "str01")
            String subject01;
            @Element(name = "str02")
            String subject02;
            @Element(name = "str03")
            String subject03;
            @Element(name = "str04")
            String subject04;
            @Element(name = "str05")
            String subject05;
            @Element(name = "str06")
            String subject06;
            @Element(name = "str01_eng")
            String subjectEng01;
            @Element(name = "str02_eng")
            String subjectEng02;
            @Element(name = "str03_eng")
            String subjectEng03;
            @Element(name = "str04_eng")
            String subjectEng04;
            @Element(name = "str05_eng")
            String subjectEng05;
            @Element(name = "str06_eng")
            String subjectEng06;

            String subject(int i) {
                switch (i) {
                    case 0:
                        return subject01;
                    case 1:
                        return subject02;
                    case 2:
                        return subject03;
                    case 3:
                        return subject04;
                    case 4:
                        return subject05;
                    case 5:
                        return subject06;
                    default:
                        return null;
                }
            }

            String subjectEng(int i) {
                switch (i) {
                    case 0:
                        return subjectEng01;
                    case 1:
                        return subjectEng02;
                    case 2:
                        return subjectEng03;
                    case 3:
                        return subjectEng04;
                    case 4:
                        return subjectEng05;
                    case 5:
                        return subjectEng06;
                    default:
                        return null;
                }
            }
        }
    }
}
