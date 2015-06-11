package com.uoscs09.theuos2.parse;


import android.support.annotation.NonNull;

import com.uoscs09.theuos2.tab.emptyroom.EmptyClassRoomItem;

public class ParseEmptyRoom2 extends OApiParser2.ReflectionParser<EmptyClassRoomItem>{
    @NonNull
    @Override
    protected EmptyClassRoomItem newInstance() {
        return new EmptyClassRoomItem();
    }

    @NonNull
    @Override
    public String getListTag() {
        return "mainlist";
    }
}
