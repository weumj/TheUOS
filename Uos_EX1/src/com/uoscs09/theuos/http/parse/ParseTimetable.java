package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.timetable.TimeTableItem;

public class ParseTimetable implements IParseHttp {
	private String body;
	private String[] PTN = { "time", "str01", "str02", "str03", "str04",
			"str05", "str06" };

	protected ParseTimetable(String body) {
		this.body = body;
	}

	@Override
	public List<TimeTableItem> parse() {
		return parseToArrayList(body.split(OApiParse.LIST));
	}

	protected List<TimeTableItem> parseToArrayList(String[] splitedBody) {
		Pattern p;
		Matcher m;
		ArrayList<TimeTableItem> itemList = new ArrayList<TimeTableItem>();
		String[] timeArray = new String[7];
		clearArray(timeArray);
		int i = 0;
		String timeReplace = "\n~\n", cr = "\n", pri = "±³½Ã", temp;

		for (int j = 1; j < splitedBody.length; j++) {
			try {
				for (String ptn : PTN) {
					p = Pattern.compile(OApiParse.getPattern(ptn));
					m = p.matcher(splitedBody[j]);
					while (m.find()) {
						temp = OApiParse.removePattern(ptn, m.group());
						if (i == 0) {
							timeArray[i] = temp.replace(cr, timeReplace)
									+ StringUtil.NEW_LINE + StringUtil.NEW_LINE
									+ String.valueOf(j) + pri;
						} else {
							timeArray[i] = temp.replace(cr, StringUtil.NEW_LINE);
							try {
								String buildingNo = timeArray[i]
										.split(StringUtil.NEW_LINE)[2].split("-")[0];
								String building = OApiUtil
										.getBuildingName(buildingNo);
								if (building != null) {
									timeArray[i] = timeArray[i].replace(
											buildingNo + "-", building);
								}
							} catch (Exception e) {
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
