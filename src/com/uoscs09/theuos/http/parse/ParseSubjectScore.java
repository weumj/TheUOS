package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.uoscs09.theuos.tab.score.DetailScoreItem;
import com.uoscs09.theuos.tab.score.ScoreItem;

public class ParseSubjectScore extends OApiParse<ArrayList<String>> {
	private String[] PTN = { "subject_nm", "subject_no", "class_div",
			"class_person", "prof_nm", "dept" };
	private String[] PTN2 = { "type", "class_eval_item", "raw_score",
			"eval_grade", "ave" };

	protected ParseSubjectScore(String body) {
		super(body);
	}

	@Override
	public List<ScoreItem> parse() throws IOException {
		String[] split = body.split(OApiParse.LIST);
		String[] firstSplit = { split[1] };
		ArrayList<ScoreItem> itemList = new ArrayList<ScoreItem>();
		ArrayList<ArrayList<String>> list = parseToArrayList(split, PTN2);
		ArrayList<String> string = parseToArrayList(firstSplit, PTN).get(0);
		int size = list.size();
		List<DetailScoreItem> dItemList = new ArrayList<DetailScoreItem>();
		ArrayList<String> temp;
		for (int i = 0; i < size; i++) {
			temp = list.get(i);
			dItemList.add(new DetailScoreItem(temp.get(0), temp.get(1), temp
					.get(2), temp.get(3), temp.get(4)));
		}
		itemList.add(new ScoreItem(getTitle(string), dItemList));
		return itemList;
	}

	private String getTitle(ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		sb.append(list.get(0)).append(" (").append(list.get(1)).append('/')
				.append(list.get(2)).append(") \n").append(list.get(3))
				.append("¸í ¼ö°­\n").append(list.get(4)).append(" (")
				.append(list.get(5)).append(')');
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initItem(ArrayList<String> parsedStringList,
			ArrayList<ArrayList<String>> returningList) {
		returningList.add((ArrayList<String>) parsedStringList.clone());
	}
}
