package com.uoscs09.theuos.parse;

import com.uoscs09.theuos.tab.subject.SubjectItem;

import java.util.ArrayList;
@Deprecated
public class ParserSubject extends OApiParser<SubjectItem, ArrayList<String>> {
    protected final String[] PATTERNS = {
            "sub_dept", "subject_div", "subject_div2", "subject_no",
            "class_div", "subject_nm", "shyr", "credit", "prof_nm",
            "class_type", "class_nm", "tlsn_count", "tlsn_limit_count"};


    @Override
    public ArrayList<SubjectItem> parse(String body) throws Exception {
        ArrayList<ArrayList<String>> list = parseToArrayList(body.split(OApiParser.LIST), PATTERNS);
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
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ArrayList<String>> returningList) {
        returningList.add((ArrayList<String>) parsedStringList.clone());
    }
}
