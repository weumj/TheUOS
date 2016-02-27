package com.uoscs09.theuos2.tab.restaurant;

import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class ParseRestaurantWeek extends JerichoParser<WeekRestItem> {
    private static final String BR = "*br*";

    @Override
    public WeekRestItem parse(String param) throws Throwable {
        param = param.replaceAll("<br*+/>", BR);
        return super.parse(param);
    }

    @Override
    protected WeekRestItem parseHtmlBody(Source source) throws Throwable{
        Element restTable = source.getFirstElementByClass("tblType03 mt10");

        WeekRestItem weekRestItem = new WeekRestItem();

        ArrayList<RestItem> weekList = weekRestItem.weekList;
        List<Element> trElementList = restTable.getFirstElement(HTMLElementName.TBODY).getChildElements();

        // 크기가 1이면 "글이 없습니다."
        // 즉 내용이 없는 경우이므로, 무시함.
        if (trElementList.size() != 1) {
            for (Element trElement : trElementList) {
                RestItem item = new RestItem();

                List<Element> tdElements = trElement.getChildElements();

                // date
                item.title = extractContent(tdElements.get(0));
                item.breakfast = extractContent(tdElements.get(1));
                item.lunch = extractContent(tdElements.get(2));
                item.supper = extractContent(tdElements.get(3));

                weekList.add(item);

            }
        }

        weekRestItem.afterParsing();

        return weekRestItem;

    }

    private static String extractContent(Element e) {
        return e.getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE).trim();
    }


}
