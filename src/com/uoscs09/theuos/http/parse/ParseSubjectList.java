package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;

public class ParseSubjectList extends OApiParse<ArrayList<String>> {
	private String[] PTN = { "subject_no", "subject_nm", "class_div" };

	protected ParseSubjectList(String body) {
		super(body);
	}

	@Override
	public ArrayList<ArrayList<String>> parse() throws IOException {
		return parseToArrayList(body.split(OApiParse.LIST), PTN);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initItem(ArrayList<String> parsedStringList,
			ArrayList<ArrayList<String>> returningList) {
		returningList.add((ArrayList<String>) parsedStringList.clone());
	}
}
