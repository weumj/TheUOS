package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.tab.score.DetailScoreItem;
import com.uoscs09.theuos2.tab.score.ScoreItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserSubjectScore extends OApiParser<ScoreItem, ArrayList<String>> {
    private final String[] PTN = {"subject_nm", "subject_no", "class_div", "class_person", "prof_nm", "dept"};
    private final String[] PTN2 = {"type", "class_eval_item", "raw_score", "eval_grade", "ave"};

    @Override
    public ArrayList<ScoreItem> parse(String body) throws IOException {
        String[] split = body.split(OApiParser.LIST);
        String[] firstSplit = {split[1]};

        ArrayList<ScoreItem> itemList = new ArrayList<>();
        ArrayList<ArrayList<String>> list = parseToArrayList(split, PTN2);
        ArrayList<String> string = parseToArrayList(firstSplit, PTN).get(0);

        int size = list.size();

        List<DetailScoreItem> dItemList = new ArrayList<>();
        ArrayList<String> temp;
        for (int i = 0; i < size; i++) {
            temp = list.get(i);

            dItemList.add(new DetailScoreItem(temp.get(0), temp.get(1), temp.get(2), temp.get(3), temp.get(4)));

        }
        itemList.add(new ScoreItem(getTitle(string), dItemList));
        return itemList;
    }

    private String getTitle(ArrayList<String> list) {
        return list.get(0) + " (" + list.get(1) + '/' + list.get(2) + ") \n" + list.get(3) + "명 수강\n" + list.get(4) + " (" + list.get(5) + ')';
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ArrayList<String>> returningList) {
        returningList.add((ArrayList<String>) parsedStringList.clone());
    }
}
