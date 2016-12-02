package com.uoscs09.theuos2.tab.subject;

import com.uoscs09.theuos2.tab.timetable.SimpleSubject;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class SubjectWrapper {
    @ListContainer(name = "mainlist")
    private List<SimpleSubject> simpleSubjects;

    public List<SimpleSubject> subjectInfoList() {
        return simpleSubjects;
    }
}
