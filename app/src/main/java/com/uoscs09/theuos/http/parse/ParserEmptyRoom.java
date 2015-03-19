package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.tab.emptyroom.ClassRoomItem;

import java.util.ArrayList;

public class ParserEmptyRoom extends OApiParser<ClassRoomItem, ClassRoomItem> {
    private final String[] PATTERN_ARRAY = {"building", "room_no", "room_div", "person_cnt"};

    @Override
    public ArrayList<ClassRoomItem> parse(String body) {
        return parseToArrayList(body.split(OApiParser.LIST), PATTERN_ARRAY);
    }

    @Override
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ClassRoomItem> returningList) {
        returningList.add(new ClassRoomItem(parsedStringList));
    }
}
