package com.uoscs09.theuos2.tab.subject;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class CoursePlanInfo {
    @ListContainer(name = "mainlist")
    private List<CoursePlanItem> list;

    public List<CoursePlanItem> coursePlanList() {
        return list;
    }
}
