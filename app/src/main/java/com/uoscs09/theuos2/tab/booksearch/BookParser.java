package com.uoscs09.theuos2.tab.booksearch;

import android.text.TextUtils;

import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;

public class BookParser extends JerichoParser<List<BookItem>> {
    //private static final String LOG_TAG = "BookParser";

    private static final String HREF = "href";
    private static final String SRC = "src";
    private static final String IFRAME = "iframe";
    private static final String COVER = "cover";
    private static final String LI = "li";

    @Override
    protected List<BookItem> parseHtmlBody(Source source) throws Throwable {
        List<Element> briefList = source.getAllElementsByClass("briefList");
        List<Element> bookHtmlList = briefList.get(0).getAllElements(LI);

       return Observable.from(bookHtmlList)
                .map(BookParser::parseElement)
                .filter(bookItem -> bookItem != null)
                .toList()
                .toBlocking()
                .first();
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
                item.coverSrc = getBookImgSrc(cover.getAttributeValue(SRC));

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
                String[] stateAndLocations = stateAndLocation.getTextExtractor().toString().split(" ");
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
    private static String getBookImgSrc(String imgUrl) {
        String imgSrc = "";
        if (TextUtils.isEmpty(imgUrl))
            return imgSrc;

        try {
            Response response = okHttpClient().newCall(new Request.Builder()
                    .url(imgUrl)
                    .get()
                    .build())
                    .execute();

            if (response.isSuccessful()) {
                return imageHtmlParser.parse(new String(response.body().bytes(), StringUtil.ENCODE_UTF_8));
            }
            /*
            imgSrc = HttpTask.Builder.newStringRequestBuilder(imgUrl)
                    .build()
                    .wrap(imageHtmlParser)
                    .get();
*/
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return imgSrc;
    }

    private static final JerichoParser<String> imageHtmlParser = new JerichoParser<String>() {
        @Override
        protected String parseHtmlBody(Source source) throws Exception {
            return source.getAllElements(HTMLElementName.IMG)
                    .get(0)
                    .getAttributeValue(SRC);
        }
    };


    private static OkHttpClient okHttpClient;

    private static synchronized OkHttpClient okHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }


}
