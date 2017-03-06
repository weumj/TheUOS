package com.uoscs09.theuos2.tab.emptyroom;

import android.support.annotation.Keep;

import java.util.List;

import mj.android.utils.xml.ListContainer;
import mj.android.utils.xml.Root;

@Keep
@Root(name = "root", charset = "euc-kr")
public class EmptyRoomWrapper {
    @ListContainer(name = "mainlist")
    private List<EmptyRoom> list;

    public List<EmptyRoom> emptyRoomList() {
        return list;
    }
}
