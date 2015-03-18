package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseRest extends JerichoParse<RestItem> {

	public ParseRest(String body) {
		super(body);
	}

	@Override
	protected ArrayList<RestItem> parseHttpBody(Source source) throws IOException {
		String AMP = "amp;";
		ArrayList<RestItem> resultList = new ArrayList<>();

		List<Element> restList = source.getAllElementsByClass("d1 talign_l");
		List<Element> menuList = source.getAllElementsByClass("right_L");
		String title, breakfast, lunch, supper;
		for (int i = 0; i < restList.size(); i++) {
			title = restList.get(i).getTextExtractor().toString().split(StringUtil.SPACE)[0].trim();

			breakfast = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 1)
							.getFirstElement(HTMLElementName.PRE).getContent()
							.toString()), AMP).trim();

			lunch = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 3)
							.getFirstElement(HTMLElementName.PRE).getContent()
							.toString()), AMP).trim();

			supper = StringUtil.remove(
					StringUtil.replaceHtmlCode(menuList.get(6 * i + 5)
							.getFirstElement(HTMLElementName.PRE).getContent()
							.toString()), AMP).trim();

			resultList.add(new RestItem(title, StringUtil.NULL, breakfast,lunch, supper));
		}
		return resultList;
	}
}
