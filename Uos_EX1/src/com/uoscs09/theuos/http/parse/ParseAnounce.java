package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;
import java.util.List;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.anounce.AnounceItem;

public class ParseAnounce implements IParseHttp {
	private String htmlBody;
	private final static String TD = "<td";
	private final static String RM = "(>|</td>|\n|\t)";
	private final static String RM2 = "')";
	private final static String RMS = "(</a>|</td>|\n|\t)";
	private final static String ON_CLCK = "onclick=\"";
	private final static String HREF = "<a href=\"/";
	private final static String SPLITA = "\">";
	private final static String SPLIT = ";\">";
	private final static String SPLIT2 = ", '";
	private final static String RMO = "scholarship.do?";
	private final static String SPLIT3 = "title\">";
	private final static String SPLIT4 = "\" ";
	private int howTo;

	protected ParseAnounce(String htmlBody, int howTo) {
		this.htmlBody = htmlBody;
		this.howTo = howTo;
	}

	@Override
	public List<AnounceItem> parse() {
		String[] list = htmlBody.split("<tbody>")[1].split("<tr>");
		switch (howTo) {
		case ParseFactory.Value.BODY:
			return parseScholarShip(list);
		default:
			return parseAnounce(list);
		}
	}

	private List<AnounceItem> parseAnounce(String[] list) {
		ArrayList<AnounceItem> itemList = new ArrayList<AnounceItem>();
		String type, onClick, title, date;
		String[] tempArray, a;
		for (String temp : list) {
			try {
				tempArray = temp.split(TD);
				type = StringUtil.removeRegex(tempArray[1], RM);
				a = tempArray[2].split(SPLIT);
				onClick = StringUtil.remove(
						a[0].split(ON_CLCK)[1].split(SPLIT2)[1], RM2);
				title = a[1].substring(0, a[1].length() - 17);
				date = StringUtil.removeRegex(tempArray[4], RM);
				itemList.add(new AnounceItem(type, title, date, onClick));
			} catch (Exception e) {
			}
		}
		return itemList;
	}

	private List<AnounceItem> parseScholarShip(String[] list) {
		ArrayList<AnounceItem> itemList = new ArrayList<AnounceItem>();
		String type, onClick, title, date;
		String[] tempArray, a;
		for (String temp : list) {
			try {
				tempArray = temp.split(TD);
				type = StringUtil
						.removeRegex(tempArray[1].split(SPLITA)[1], RM).trim();
				a = tempArray[2].split(HREF)[1].split(SPLIT4);
				onClick = StringUtil.remove(
						a[0].replace(StringUtil.CODE_AMP_CODE, StringUtil.AMP),
						RMO);
				title = StringUtil.removeRegex(a[1].split(SPLIT3)[1], RMS)
						.trim();
				date = StringUtil
						.removeRegex(tempArray[4].split(SPLITA)[1], RM).trim();

				itemList.add(new AnounceItem(type, title, date, onClick));
			} catch (Exception e) {
			}
		}
		return itemList;
	}
}
