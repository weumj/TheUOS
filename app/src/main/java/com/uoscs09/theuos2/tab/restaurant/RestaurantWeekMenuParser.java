package com.uoscs09.theuos2.tab.restaurant;

import com.uoscs09.theuos2.parse.JerichoParser;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import java.util.ArrayList;
import java.util.List;

public class RestaurantWeekMenuParser extends JerichoParser<RestWeekItem> {
    private static final String BR = "*br*";

    @Override
    public RestWeekItem parse(String param) throws Throwable {
        param = param.replaceAll("<br*+/>", BR);
        return super.parse(param);
    }

    @Override
    protected RestWeekItem parseHtmlBody(Source source) {
        Element restTable = source.getElementById("week");

        RestWeekItem restWeekItem = new RestWeekItem();

        ArrayList<RestItem> weekList = restWeekItem.weekList;
        List<Element> trElementList = restTable.getAllElements(HTMLElementName.TR);

        // 크기가 1이면 "글이 없습니다."
        // 즉 내용이 없는 경우이므로, 무시함.
        if (trElementList.size() > 1) {
            for (int i = 0; i < trElementList.size(); i += 3) {
                Element trElement = trElementList.get(i);
                List<Element> tdElements = trElement.getChildElements();

                RestItem item = new RestItem();
                // date
                item.title = extractContent(tdElements.get(0));

                item.breakfast = extractContentWithNewLine(tdElements.get(2));

                trElement = trElementList.get(i + 1);
                tdElements = trElement.getChildElements();
                item.lunch = extractContentWithNewLine(tdElements.get(1));

                trElement = trElementList.get(i + 2);
                tdElements = trElement.getChildElements();
                item.supper = extractContentWithNewLine(tdElements.get(1));

                weekList.add(item);
            }
        }
        restWeekItem.afterParsing();

        return restWeekItem;
    }

    private static String extractContent(Element e) {
        return e.getTextExtractor().toString().replace(HTMLElementName.BR, "\n").trim();
    }

    private static String extractContentWithNewLine(Element e) {
        Source s = new Source(e.toString().replace("<br>", ";;"));
        TextExtractor textExtractor = s.getTextExtractor();
        return textExtractor.toString().replace(";;", "\n").trim();
    }


}
