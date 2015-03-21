package com.uoscs09.theuos.parse;

import android.util.Log;

import com.uoscs09.theuos.tab.timetable.TimeTableItem;
import com.uoscs09.theuos.util.OApiUtil;
import com.uoscs09.theuos.util.StringUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Deprecated
public class ParseTimetable implements IParser<String, ArrayList<TimeTableItem>> {
    private final String[] PTN = {"time", "str01", "str02", "str03", "str04", "str05", "str06"};


    @Override
    public ArrayList<TimeTableItem> parse(String body) {
        Log.i("timetable", body);
        return parseToArrayList(body.split(OApiParser.LIST));
    }

    protected ArrayList<TimeTableItem> parseToArrayList(String[] splitBody) {
        Pattern p;
        Matcher m;
        ArrayList<TimeTableItem> itemList = new ArrayList<>();
        String[] timeArray = new String[7];
        clearArray(timeArray);
        int i = 0;
        String timeReplace = "\n~\n", cr = "\n", pri = "교시", temp;

        for (int j = 1; j < splitBody.length; j++) {
            try {
                for (String ptn : PTN) {
                    p = Pattern.compile(OApiParser.getPattern(ptn));
                    m = p.matcher(splitBody[j]);
                    while (m.find()) {

                        temp = OApiParser.removePattern(ptn, m.group());
                        if (i == 0) {

                            timeArray[i] = temp.replace(cr, timeReplace)
                                    + StringUtil.NEW_LINE + StringUtil.NEW_LINE
                                    + String.valueOf(j) + pri;

                        } else {
                            timeArray[i] = temp.replace(cr, StringUtil.NEW_LINE);
                            try {

                                String[] buildingSplitArray = timeArray[i].split(StringUtil.NEW_LINE);
                                if (buildingSplitArray.length > 1) {
                                    String buildingNo = timeArray[i].split(StringUtil.NEW_LINE)[2].split("-")[0];
                                    String building = OApiUtil.getBuildingName(buildingNo);

                                    if (building != null) {
                                        timeArray[i] = timeArray[i].replace(buildingNo + "-", building);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }
                    i++;
                }
                itemList.add(new TimeTableItem(timeArray));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clearArray(timeArray);
                i = 0;
            }
        }
        return itemList;
    }

    private void clearArray(String[] array) {
        for (int i = 0; i < 7; i++) {
            array[i] = StringUtil.NULL;
        }
    }
}
