package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.tab.booksearch.BookItem;

public class ParseBook implements IParseHttp {
	private final static String PATTERN_IMG = "<iframe src=\"";
	private final static String PATTERN_URL = "<a href=\"/search/detail/";
	private final static String PATTERN_TITLE = "<span class=\"object\">";
	private final static String PATTERN_INFO = "<span class=\"info\">";
	private final static String PATTERN_SITE = "<span class='book_state'>";
	private final static String PATTERN_SITE_PAGE = "<a href=\"/search/media";
	private final static String M_LIB = "http://mlibrary.uos.ac.kr/search/media";
	private final static String ETC_CHAR = "(\r|\t|\n)";
	private final static String SEARCH_DETAIL = "/search/detail/";
	private final static String BACK_SLASH = "\"";
	private final static String CLOSE_A = "</a>";
	private final static String BR = "<br/>";
	private final static String SRC = "src";
	private final static String CLOSE_TD = "</td>";
	private final static String CLOSE_SPAN = "</span>";
	private String body;

	protected ParseBook(String body) {
		this.body = body;
	}

	@Override
	public List<BookItem> parse() {
		String[] bodyArray = body.split("<span id=\"bookImg_");
		String[] tempArray;
		String coverSrc, writer, bookInfo, site, bookState, url, title, temp;
		ArrayList<BookItem> list = new ArrayList<BookItem>();
		for (int i = 1; i < bodyArray.length; i++) {
			temp = bodyArray[i];
			try {
				temp = temp.split(PATTERN_IMG)[1];
				coverSrc = temp.split(BACK_SLASH)[0];
			} catch (Exception e) {
				coverSrc = StringUtil.NULL;
			}
			temp = temp.split(PATTERN_URL)[1];
			url = SEARCH_DETAIL + temp.split(BACK_SLASH)[0];
			temp = temp.split(PATTERN_TITLE)[1];
			title = StringUtil.replaceHtmlCode(temp.split(CLOSE_SPAN)[0]);

			tempArray = temp.split(PATTERN_INFO);
			writer = StringUtil
					.replaceHtmlCode(tempArray[1].split(CLOSE_SPAN)[0]);

			bookInfo = StringUtil
					.replaceHtmlCode(tempArray[2].split(CLOSE_SPAN)[0]
							.replaceAll(ETC_CHAR, StringUtil.NULL));
			try {
				temp = tempArray[3];
				temp = temp.split(CLOSE_A)[1];
				String[] tempArr = temp.split(PATTERN_SITE);
				site = StringUtil.replaceHtmlCode(tempArr[0]);
				bookState = StringUtil.replaceHtmlCode(tempArr[1]
						.split(CLOSE_SPAN)[0]);
			} catch (Exception e) {
				try {
					temp = temp.split(PATTERN_SITE_PAGE)[1].split(CLOSE_TD)[0]
							.replaceAll(ETC_CHAR, StringUtil.NULL);
					site = M_LIB
							+ StringUtil
									.replaceHtmlCode(temp.split(BACK_SLASH)[0]);
					bookState = StringUtil.replaceHtmlCode(temp.split(BR)[1]);
				} catch (Exception ee) {
					site = bookState = StringUtil.NULL;
				}
			}
			list.add(new BookItem(title, writer, bookInfo, site, bookState,
					getImgSrc(coverSrc), url));
		}
		list.trimToSize();
		return list;
	}

	private String getImgSrc(String imgUrl) {
		String imgSrc = StringUtil.NULL;
		if (imgUrl == null || imgUrl.equals(StringUtil.NULL))
			return imgSrc;
		try {
			Source source = new Source(HttpRequest.getBody(imgUrl));
			imgSrc = source.getAllElements(HTMLElementName.IMG).get(0)
					.getAttributeValue(SRC);
		} catch (Exception e) {
		}
		return imgSrc;
	}

}
