package com.uoscs09.theuos2.parse;

import android.util.Log;

import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.tab.booksearch.BookItem;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ParseBook extends JerichoParser<List<BookItem>> {
    private static final String LOG_TAG = "ParseBook";

    private static final String HREF = "href";
    private static final String SRC = "src";
    private static final String IFRAME = "iframe";
    private static final String COVER = "cover";
    private static final String LI = "li";

    private static class TaskCallable implements Callable<List<BookItem>> {
        final List<Element> bookHtmlList;
        final int start, end;

        TaskCallable(List<Element> bookHtmlList, int start, int end) {
            this.bookHtmlList = bookHtmlList;
            this.start = start;
            this.end = end;
        }

        @Override
        public List<BookItem> call() throws Exception {
            return parseListElement(bookHtmlList, start, end);
        }
    }

    @Override
    protected List<BookItem> parseHttpBody(Source source) throws IOException {
        List<Element> briefList = source.getAllElementsByClass("briefList");
        List<Element> bookHtmlList = briefList.get(0).getAllElements(LI);

        int size = bookHtmlList.size();
        if (size > 7 && Runtime.getRuntime().availableProcessors() > 2) {
            return parseListElementUsing2Thread(bookHtmlList, size);

        } else {
            return parseListElement(bookHtmlList, 0, size);

        }
    }

    private static List<BookItem> parseListElementUsing2Thread(List<Element> bookHtmlList, int size) {
        int halfSize = size / 2;
        FutureTask<List<BookItem>> task1 = new FutureTask<>(new TaskCallable(bookHtmlList, 0, halfSize)),
                task2 = new FutureTask<>(new TaskCallable(bookHtmlList, halfSize, size));

        AsyncUtil.executeFor(task1);
        AsyncUtil.executeFor(task2);

        ArrayList<BookItem> bookItemList = new ArrayList<>();

        // AsyncTask를 사용한다면 현재 Thread가 interrupt될 가능성이 존재함.
        for (; ; ) {
            try {
                bookItemList.addAll(task1.get());
                break;
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "interrupted TASK #1", e);

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        for (; ; ) {
            try {
                bookItemList.addAll(task2.get());
                break;
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "interrupted TASK #2", e);

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return bookItemList;
    }

    private static List<BookItem> parseListElement(List<Element> bookHtmlList, int start, int end) {
        if (start >= end)
            return Collections.emptyList();

        ArrayList<BookItem> bookItemList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Element rawBookHtml = bookHtmlList.get(i);
            BookItem item = parseElement(rawBookHtml);
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
        if (imgUrl == null || imgUrl.equals(StringUtil.NULL))
            return imgSrc;

        try {
            imgSrc = HttpRequest.Builder.newStringRequestBuilder(imgUrl)
                    .build()
                    .wrap(new JerichoParser<String>() {
                        @Override
                        protected String parseHttpBody(Source source) throws Exception {
                            return source.getAllElements(HTMLElementName.IMG)
                                    .get(0)
                                    .getAttributeValue(SRC);
                        }
                    })
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgSrc;
    }

}
