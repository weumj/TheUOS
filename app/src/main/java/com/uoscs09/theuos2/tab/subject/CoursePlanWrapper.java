package com.uoscs09.theuos2.tab.subject;

import android.support.annotation.Keep;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "root", charset = "euc-kr")
public class CoursePlanWrapper {
    @ListContainer(name = "mainlist")
    private List<CoursePlan> list;

    public List<CoursePlan> coursePlanList() {
        return list;
    }
}
