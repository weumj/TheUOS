package com.uoscs09.theuos2.tab.emptyroom;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Root(name = "root", charset = "euc-kr")
public class EmptyRoomInfo {
    @ListContainer(name = "mainlist")
    private List<EmptyRoom> list;

    public List<EmptyRoom> emptyRoomList() {
        return list;
    }
}
