package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.tab.booksearch.BookItem;

public class ParseBook extends JerichoParse<BookItem> {
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String IFRAME = "iframe";
	private static final String COVER = "cover";
	private static final String LI = "li";

	protected ParseBook(String body) {
		super(body);
	}

	@Override
	protected List<BookItem> parseHttpBody(Source source) throws IOException {
		List<Element> briefList = source.getAllElementsByClass("briefList");
		List<Element> bookHtmlList = briefList.get(0).getAllElements(LI);

		ArrayList<BookItem> bookItemList = new ArrayList<BookItem>();
		final int size = bookHtmlList.size();
		for (int i = 0; i<size;i++) {
			Element rawBookHtml = bookHtmlList.get(i);
			
			BookItem item = new BookItem();
			Element bookUrl = rawBookHtml.getFirstElement(HTMLElementName.A);
			if (bookUrl != null) {
				item.url = bookUrl.getAttributeValue(HREF);
			}

			try {
				Element cover = rawBookHtml.getFirstElementByClass(COVER)
						.getFirstElement(IFRAME);
				item.coverSrc = getImgSrc(cover.getAttributeValue(SRC));
			} catch (Exception e) {
			}

			Element title = rawBookHtml.getFirstElementByClass("object");
			if (title != null) {
				item.title = title.getTextExtractor().toString();
			}

			List<Element> infoList = rawBookHtml.getAllElementsByClass("info");
			if (infoList != null) {
				Element writer = infoList.get(0);
				item.writer = writer.getTextExtractor().toString();
				Element publisher = infoList.get(1);
				item.bookInfo = publisher.getTextExtractor().toString();

				if (infoList.size() > 2) {
					Element stateAndLocation = infoList.get(2);
					String[] stateAndLocations = stateAndLocation
							.getTextExtractor().toString()
							.split(StringUtil.SPACE);
					item.site = stateAndLocations[0];
					item.bookState = stateAndLocations[1];
				} else {
					List<Element> aElements = rawBookHtml
							.getAllElements(HTMLElementName.A);
					if (aElements != null && aElements.size() > 1) {
						Element onlineUrl = aElements.get(1);
						item.site = "http://mlibrary.uos.ac.kr"
								+ onlineUrl.getAttributeValue(HREF);
						item.bookState = "온라인 이용 가능";
					}
				}
			}
			Element bookStateInfos = rawBookHtml
					.getFirstElementByClass("downIcon");
			item.bookStateInfoList = null;
			if (bookStateInfos != null) {
				Element a = bookStateInfos.getFirstElement(HTMLElementName.A);
				if (a != null) {
					String functionCallLoc = a.getAttributeValue(HREF);
					String[] params = functionCallLoc.split("', '");

					String sysdCtrl = params[2], location = params[3]
							.split("'")[0];

					String stateInfoUrl = "http://mlibrary.uos.ac.kr/search/prevLoc/"
							+ sysdCtrl + "?loc=" + location;
					item.infoUrl = stateInfoUrl;
				}
			}

			bookItemList.add(item);
		}
		return bookItemList;
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
