package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;

import com.uoscs09.theuos.tab.emptyroom.ClassRoomItem;

public class ParseEmptyRoom extends OApiParse<ClassRoomItem> {
	private String[] PATTERN_ARRAY = { "building", "room_no", "room_div",
			"person_cnt" };

	protected ParseEmptyRoom(String body) {
		super(body);
	}

	@Override
	public ArrayList<ClassRoomItem> parse() {
		return parseToArrayList(body.split(OApiParse.LIST), PATTERN_ARRAY);
	}

	@Override
	protected void initItem(ArrayList<String> parsedStringList,
			ArrayList<ClassRoomItem> returningList) {
		returningList.add(new ClassRoomItem(parsedStringList));
	}
}
