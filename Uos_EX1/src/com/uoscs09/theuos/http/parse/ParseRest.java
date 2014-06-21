package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class ParseRest extends JerichoParse<RestItem> {

	protected ParseRest(String body) {
		super(body);
	}

	@Override
	protected List<RestItem> parseHttpBody(Source source)
			throws IOException {
		String AMP = "amp;";
		ArrayList<RestItem> resultList = new ArrayList<RestItem>();

		List<Element> restList = source.getAllElementsByClass("d1 talign_l");
		List<Element> menuList = source.getAllElementsByClass("right_L");
		RestItem item;
		String title, breakfast, lunch, supper;
		for (int i = 0; i < restList.size(); i++) {
			title = restList.get(i).getTextExtractor().toString()
					.split(StringUtil.SPACE)[0];
			breakfast = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 1)
							.getAllElements(HTMLElementName.PRE).get(0)
							.getContent().toString()), AMP);
			lunch = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 3)
							.getAllElements(HTMLElementName.PRE).get(0)
							.getContent().toString()), AMP);
			supper = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 5)
							.getAllElements(HTMLElementName.PRE).get(0)
							.getContent().toString()), AMP);

			item = new RestItem(title, StringUtil.NULL, breakfast, lunch,
					supper);
			resultList.add(item);
		}
		resultList.trimToSize();
		return resultList;
	}
}
