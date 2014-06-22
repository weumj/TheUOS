package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OApiParse<T> implements IParseHttp {
	protected static final String CDATA = "CDATA";
	protected static final String PTN = "(.|\\n|\\r)*?";
	protected static final String LIST = "<list>";
	protected String body;

	protected OApiParse(String body) {
		this.body = body;
	}

	protected final static String getPattern(String str) {
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(str).append('>').append(PTN).append('<')
				.append('/').append(str).append('>');
		return sb.toString();
	}

	protected ArrayList<T> parseToArrayList(String[] splitedBody,
			String[] PATTERNS) {
		Pattern p;
		Matcher m;
		ArrayList<T> itemList = new ArrayList<T>();
		ArrayList<String> list = new ArrayList<String>();

		for (String str : splitedBody) {
			try {
				for (String ptn : PATTERNS) {
					p = Pattern.compile(getPattern(ptn));
					m = p.matcher(str);
					while (m.find()) {
						list.add(removePattern(ptn, m.group()));
					}
				}
				if (list.size() != 0)
					initItem(list, itemList);
			} catch (Exception e) {
			} finally {
				list.clear();
			}
		}
		return itemList;
	}

	protected static String removePattern(String ptn, String str) {
		if (str.contains(CDATA)) {
			return str.substring(ptn.length() + 11, str.length() - ptn.length()
					- 6);
		} else {
			return str.substring(ptn.length() + 2, str.length() - ptn.length()
					- 3);
		}
	}

	protected abstract void initItem(ArrayList<String> parsedStringList,
			ArrayList<T> returningList);
}
