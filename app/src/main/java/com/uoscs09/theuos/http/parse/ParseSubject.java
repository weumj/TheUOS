package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.tab.subject.SubjectItem;

import java.io.IOException;
import java.util.ArrayList;

public class ParseSubject extends OApiParse<SubjectItem, ArrayList<String>> {
	protected String[] PATTERNS = { "sub_dept", "subject_div", "subject_div2",
			"subject_no", "class_div", "subject_nm", "shyr", "credit",
			"prof_nm", "class_type", "class_nm", "tlsn_count",
			"tlsn_limit_count" };

	public ParseSubject(String body) {
		super(body);
	}

	@Override
	public ArrayList<SubjectItem> parse() throws IOException {
		ArrayList<ArrayList<String>> list = parseToArrayList(body.split(OApiParse.LIST), PATTERNS);
		ArrayList<SubjectItem> itemList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			try {
				itemList.add(new SubjectItem(list.get(i)));
			} catch (Exception e) {
                e.printStackTrace();
			}
		}
		return itemList;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initItem(ArrayList<String> parsedStringList,
			ArrayList<ArrayList<String>> returningList) {
		returningList.add((ArrayList<String>) parsedStringList.clone());
	}
}
