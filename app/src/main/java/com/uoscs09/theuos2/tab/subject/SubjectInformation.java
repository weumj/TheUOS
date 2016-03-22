package com.uoscs09.theuos2.tab.subject;

import com.uoscs09.theuos2.tab.timetable.SubjectInfoItem;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class SubjectInformation {
    @ListContainer(name = "mainlist")
    private List<SubjectInfoItem> subjectInfoItems;

    public List<SubjectInfoItem> subjectInfoList() {
        return subjectInfoItems;
    }
}
