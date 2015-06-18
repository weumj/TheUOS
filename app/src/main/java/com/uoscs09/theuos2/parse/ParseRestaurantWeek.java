package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.tab.restaurant.WeekRestItem;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class ParseRestaurantWeek extends JerichoParser<WeekRestItem>{
    private static final String BR = "*br*";

    @Override
    public WeekRestItem parse(String param) throws Exception {
        param =  param.replaceAll("<br*+/>", BR);
        return super.parse(param);
    }

    @Override
    protected WeekRestItem parseHttpBody(Source source) throws Exception {
        Element restTable = source.getFirstElementByClass("tblType03 mt10");

        WeekRestItem weekRestItem = new WeekRestItem();

        ArrayList<RestItem> weekList = weekRestItem.weekList;
        for (Element trElement : restTable.getFirstElement(HTMLElementName.TBODY).getChildElements()){
            RestItem item = new RestItem();

            List<Element> tdElements = trElement.getChildElements();

            item.title = extractContent(tdElements.get(0));
            item.breakfast = extractContent(tdElements.get(1));
            item.lunch = extractContent(tdElements.get(2));
            item.supper = extractContent(tdElements.get(3));

            weekList.add(item);

        }

        return weekRestItem;

    }

    private String extractContent(Element e){
        return e.getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE).trim();
    }


}
