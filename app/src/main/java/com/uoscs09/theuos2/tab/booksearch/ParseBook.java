package com.uoscs09.theuos2.tab.booksearch;

import android.text.TextUtils;

import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.OptimizeStrategy;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

import mj.android.utils.task.Func;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;

public class ParseBook extends JerichoParser<List<BookItem>> {
    private static final String LOG_TAG = "ParseBook";

    private static final String HREF = "href";
    private static final String SRC = "src";
    private static final String IFRAME = "iframe";
    private static final String COVER = "cover";
    private static final String LI = "li";

    @Override
    protected List<BookItem> parseHtmlBody(Source source) throws Throwable {
        List<Element> briefList = source.getAllElementsByClass("briefList");
        List<Element> bookHtmlList = briefList.get(0).getAllElements(LI);

        Task<List<BookItem>> task;
        final int size = bookHtmlList.size();
        if (size > 7 && OptimizeStrategy.isSafeToOptimize()) {
            task = parseListElementUsing2Thread(bookHtmlList, size);
        } else {
            task = parseTask(bookHtmlList);
        }

        return task.get();
    }

    private static Task<List<BookItem>> parseListElementUsing2Thread(List<Element> bookHtmlList, int size) {
        final int halfSize = size / 2;
        ArrayList<Task<List<BookItem>>> tasks = new ArrayList<>();
        tasks.add(parseTask(bookHtmlList.subList(0, halfSize)));
        tasks.add(parseTask(bookHtmlList.subList(halfSize, size)));

        return Tasks.Parallel.parallelTaskTypedCollection(tasks);
    }

    private static Task<List<BookItem>> parseTask(List<Element> bookHtmlList) {
        return Tasks.newTask(() -> parseListElement(bookHtmlList));
    }

    private static List<BookItem> parseListElement(List<Element> bookHtmlList) {
        ArrayList<BookItem> bookItemList = new ArrayList<>();
        for (Element element : bookHtmlList) {
            BookItem item = parseElement(element);
            if (item != null)
                bookItemList.add(item);
        }
        return bookItemList;
    }

    private static BookItem parseElement(Element rawBookElement) {
        BookItem item = new BookItem();
        Element bookUrl = rawBookElement.getFirstElement(HTMLElementName.A);
        if (bookUrl != null) {
            item.url = bookUrl.getAttributeValue(HREF);
        }

        try {
            Element cover = rawBookElement.getFirstElementByClass(COVER).getFirstElement(IFRAME);
            if (cover != null)
                item.coverSrc = getImgSrc(cover.getAttributeValue(SRC));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Element title = rawBookElement.getFirstElementByClass("object");
        if (title != null) {
            item.title = title.getTextExtractor().toString();
        }

        List<Element> infoList = rawBookElement.getAllElementsByClass("info");
        if (infoList != null) {

            if (infoList.isEmpty())
                return null;

            Element writer = infoList.get(0);
            item.writer = writer.getTextExtractor().toString();

            Element publisher = infoList.get(1);
            item.bookInfo = publisher.getTextExtractor().toString();

            if (infoList.size() > 2) {
                Element stateAndLocation = infoList.get(2);
                String[] stateAndLocations = stateAndLocation.getTextExtractor().toString().split(StringUtil.SPACE);
                String bookState = stateAndLocations[1];

                item.site = stateAndLocations[0];
                item.bookState = bookState;
                if (bookState.contains("대출가능"))
                    item.bookStateInt = BookItem.BOOK_STATE_AVAILABLE;
                else
                    item.bookStateInt = BookItem.BOOK_STATE_NOT_AVAILABLE;

            } else {
                List<Element> aElements = rawBookElement.getAllElements(HTMLElementName.A);
                if (aElements != null && aElements.size() > 1) {
                    Element onlineUrl = aElements.get(1);

                    item.site = "http://mlibrary.uos.ac.kr" + onlineUrl.getAttributeValue(HREF);
                    item.bookState = "온라인 이용 가능";
                    item.bookStateInt = BookItem.BOOK_STATE_ONLINE;

                } else
                    item.bookStateInt = BookItem.BOOK_STATE_NOT_AVAILABLE;

            }
        }

        Element bookStateInfos = rawBookElement.getFirstElementByClass("downIcon");
        item.bookStateInfoList = null;

        if (bookStateInfos != null) {
            Element a = bookStateInfos.getFirstElement(HTMLElementName.A);

            if (a != null) {

                String functionCallLoc = a.getAttributeValue(HREF);
                String[] params = functionCallLoc.split("', '");

                String sysdCtrl = params[2], location = params[3].split("'")[0];

                item.infoUrl = "http://mlibrary.uos.ac.kr/search/prevLoc/" + sysdCtrl + "?loc=" + location;
            }

        }
        return item;
    }

    /**
     * 책 커버 이미지의 주소를 얻어온다.
     */
    private static String getImgSrc(String imgUrl) {
        String imgSrc = StringUtil.NULL;
        if (TextUtils.isEmpty(imgUrl))
            return imgSrc;

        try {
            imgSrc = HttpRequest.Builder.newStringRequestBuilder(imgUrl)
                    .build()
                    .wrap(imageHtmlParser)
                    .get();

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return imgSrc;
    }

    private static final Func<String, String> imageHtmlParser = new JerichoParser<String>() {
        @Override
        protected String parseHtmlBody(Source source) throws Exception {
            return source.getAllElements(HTMLElementName.IMG)
                    .get(0)
                    .getAttributeValue(SRC);
        }
    };


}
