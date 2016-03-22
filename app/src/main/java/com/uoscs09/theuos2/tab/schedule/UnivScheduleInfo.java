package com.uoscs09.theuos2.tab.schedule;

import com.uoscs09.theuos2.parse.IParser;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class UnivScheduleInfo implements IParser.IPostParsing {
    @ListContainer(name = "schList")
    private List<UnivScheduleItem> univScheduleItemList;

    public List<UnivScheduleItem> univScheduleList() {
        return univScheduleItemList;
    }

    @Override
    public void afterParsing() {
        if (univScheduleItemList != null) {
            for (UnivScheduleItem item : univScheduleItemList) {
                item.afterParsing();
            }
        }
    }
}
