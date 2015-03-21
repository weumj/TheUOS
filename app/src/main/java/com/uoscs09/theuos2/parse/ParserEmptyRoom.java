package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.tab.emptyroom.ClassRoomItem;

import java.util.ArrayList;

@Deprecated
public class ParserEmptyRoom extends OApiParser<ClassRoomItem, ClassRoomItem> {
    private final String[] PATTERN_ARRAY = {"building", "room_no", "room_div", "person_cnt"};

    @Override
    public ArrayList<ClassRoomItem> parse(String body) {
        return parseToArrayList(body.split(OApiParser.LIST), PATTERN_ARRAY);
    }

    @Override
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ClassRoomItem> returningList) {
        ClassRoomItem item = new ClassRoomItem();
        item.building = parsedStringList.get(0);
        item.room_no = parsedStringList.get(1);
        item.room_div = parsedStringList.get(2);
        item.person_cnt = Integer.valueOf(parsedStringList.get(3));

        returningList.add(item);
    }
}
