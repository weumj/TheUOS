package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;

public class ParserSubjectInfo extends OApiParser<String, ArrayList<String>> {
    protected static final String[] PTNS = {"subject_nm", "subject_no", "prof_nm", "tel_no", "score_eval_rate", "book_nm"};
    protected static final String[] PTNS2 = {"week", "class_cont", "class_meth", "week_book", "prjt_etc"};
    protected static final String[] PTNS3 = {PTNS2[0], PTNS2[1], PTNS[5], PTNS2[3], PTNS2[4]};

    @Override
    public ArrayList<String> parse(String body) throws Exception {
        try {
            String[] split = body.split(OApiParser.LIST);
            String[] arr = {split[1]};

            ArrayList<String> list = parseToArrayList(arr, PTNS).get(0);
            ArrayList<ArrayList<String>> infoList;
            int size = list.size() - PTNS.length;
            if (size > 1) {
                infoList = parseToArrayList(split, PTNS3);
                for (int i = 0; i < infoList.size(); i++) {
                    infoList.get(i).remove(2);
                    infoList.get(i).remove(2);
                }
                while (size-- > 0) {
                    list.remove(list.size() - 1);
                }
            } else {
                infoList = parseToArrayList(split, PTNS2);
            }

            for (int i = 0; i < infoList.size()/*16*/; i++) {
                list.addAll(infoList.get(i));
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("수업계획정보가 없습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ArrayList<String>> returningList) {
        returningList.add((ArrayList<String>) parsedStringList.clone());
    }
}