package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.tab.anounce.AnnounceItem;
import com.uoscs09.theuos.util.StringUtil;

import java.util.ArrayList;

public class ParseAnnounce implements IParser<String, ArrayList<AnnounceItem>> {
    public static final int SCHOLAR = 1;

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

    public void setHowTo(int howTo){
        this.howTo = howTo;
    }

    /**  호출하기전 반드시 setHowTo(int) 를 호출할 것*/
	@Override
	public ArrayList<AnnounceItem> parse(String param) {
		String[] list = param.split("<tbody>")[1].split("<tr>");
		switch (howTo) {
		case SCHOLAR:
			return parseScholarShip(list);
		default:
			return parseAnnounce(list);
		}
	}

	private ArrayList<AnnounceItem> parseAnnounce(String[] list) {
		ArrayList<AnnounceItem> itemList = new ArrayList<>();
		String type, onClick, title, date;
		String[] tempArray, a;
		for (String temp : list) {
			try {
				tempArray = temp.split(TD);

                type = StringUtil.removeRegex(tempArray[1], RM);

                a = tempArray[2].split(SPLIT);
				onClick = StringUtil.remove(a[0].split(ON_CLCK)[1].split(SPLIT2)[1], RM2);

                title = a[1].substring(0, a[1].length() - 17);

                date = StringUtil.removeRegex(tempArray[4], RM);

                itemList.add(new AnnounceItem(type, title, date, onClick));

			} catch (Exception e) {
                e.printStackTrace();
			}
		}
		return itemList;
	}

	private ArrayList<AnnounceItem> parseScholarShip(String[] list) {
		ArrayList<AnnounceItem> itemList = new ArrayList<>();
		String type, onClick, title, date;
		String[] tempArray, a;
		for (String temp : list) {
			try {
				tempArray = temp.split(TD);

                type = StringUtil.removeRegex(tempArray[1].split(SPLITA)[1], RM).trim();

                a = tempArray[2].split(HREF)[1].split(SPLIT4);

                onClick = StringUtil.remove(a[0].replace(StringUtil.CODE_AMP_CODE, StringUtil.AMP),RMO);

                title = StringUtil.removeRegex(a[1].split(SPLIT3)[1], RMS).trim();

                date = StringUtil.removeRegex(tempArray[4].split(SPLITA)[1], RM).trim();

				itemList.add(new AnnounceItem(type, title, date, onClick));
			} catch (Exception e) {
                e.printStackTrace();
			}
		}
		return itemList;
	}
}
