package com.uoscs09.theuos2.tab.restaurant;

import android.util.SparseArray;

import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.List;

public class ParseRest extends JerichoParser<SparseArray<RestItem>> {
    //private static final String BR = "*br*";

    @Override
    protected SparseArray<RestItem> parseHtmlBody(Source source) throws Exception {
        SparseArray<RestItem> result = new SparseArray<>();

        List<Element> restList = source.getAllElementsByClass("d1 talign_l");
        List<Element> menuList = source.getAllElementsByClass("right_L");

        final int N = restList.size();
        for (int i = 0; i < N; i++) {
            String title = restList.get(i).getTextExtractor().toString().split(StringUtil.SPACE)[0].trim();

            String breakfast = extractContent(menuList.get(6 * i + 1));
            String lunch = extractContent(menuList.get(6 * i + 3));
            String supper = extractContent(menuList.get(6 * i + 5));

            int nameIndex = RestItem.findRestNameIndex(title);
            if (nameIndex > -1)
                result.put(nameIndex, new RestItem(title, StringUtil.NULL, breakfast, lunch, supper));
        }

        return result;
    }

    private static String extractContent(Element e) {
        return StringUtil.remove(StringUtil.replaceHtmlCode(e.getFirstElement(HTMLElementName.PRE).getContent().toString()), "amp;").trim();
    }

}
