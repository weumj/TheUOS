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
    private static final String BR1 = "<br/>", BR2= "<br />", BR = "*br*";

    @Override
    public WeekRestItem parse(String param) throws Exception {
        param = param.replace(BR1, BR).replace(BR2, BR);
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

            item.title = tdElements.get(0).getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE);

            item.breakfast = tdElements.get(1).getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE);

            item.lunch = tdElements.get(2).getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE);

            item.supper = tdElements.get(3).getTextExtractor().toString().replace(BR, StringUtil.NEW_LINE);

            weekList.add(item);

        }
        return weekRestItem;
    }


}
