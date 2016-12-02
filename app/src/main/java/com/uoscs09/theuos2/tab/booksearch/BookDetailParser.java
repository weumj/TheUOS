package com.uoscs09.theuos2.tab.booksearch;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.uoscs09.theuos2.parse.JerichoParser;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class BookDetailParser extends JerichoParser<BookDetailItem> {


    @Override
    protected BookDetailItem parseHtmlBody(Source s) throws Throwable {

        List<Element> searchDetailElements = s.getAllElementsByClass("searchDetail");

        if (checkNull(searchDetailElements)) {
            return null;
        }

        Element info = searchDetailElements.get(0).getFirstElementByClass("info");


        if (info == null) {
            return null;
        }

        BookDetailItem bookDetailItem = new BookDetailItem();

        recordDetail(info, bookDetailItem);


        Element relationInfo = searchDetailElements.get(0).getFirstElementByClass("relationInfo");

        if (relationInfo != null) // nullable
            recordRelationInfo(relationInfo, bookDetailItem);

        return bookDetailItem;
    }


    private void recordDetail(Element info, BookDetailItem item) {
        Element itemElement = info.getFirstElementByClass("item");
        if (itemElement == null)
            return;

        List<Element> bookDetailInfoElementList = itemElement.getChildElements();

        if (checkNull(bookDetailInfoElementList))
            return;

        for (Element element : bookDetailInfoElementList) {
            Element titleElement = element.getFirstElementByClass("title");
            String title = (String) extractContent(titleElement);

            if (title == null)
                continue;

            Element contentsElement = element.getFirstElementByClass("content");
            Object contents = extractContent(contentsElement);

            Pair<String, Object> pair = new Pair<>(title, contents);
            item.detailInfoList.add(pair);
        }

    }

    private Object extractContent(Element element) {
        if (element != null) {
            Element e = element.getFirstElement(HTMLElementName.SPAN); // <span>
            if (e != null) {
                List<Element> childE = e.getAllElements(HTMLElementName.A);
                if (checkNull(childE))
                    return e.getTextExtractor().toString();
                else { // <a>
                    ArrayList<BookDetailItem.UrlObject> urlObjects = new ArrayList<>(childE.size());
                    for (Element ee : childE) {
                        String info = ee.getTextExtractor().toString();
                        if (TextUtils.isEmpty(info))
                            info = "URL";
                        String url = "http://mlibrary.uos.ac.kr" + ee.getAttributeValue("href");

                        BookDetailItem.UrlObject urlObject = new BookDetailItem.UrlObject(info, url);
                        urlObjects.add(urlObject);
                    }

                    return urlObjects;
                }
            }
        }

        return null;
    }

    private void recordRelationInfo(Element element, BookDetailItem bookDetailItem) {
        BookDetailItem.RelationInfo relationInfo = new BookDetailItem.RelationInfo();

        {
            Element e1 = element.getFirstElement(HTMLElementName.P);
            if (e1 != null) {
                relationInfo.title = e1.getTextExtractor().toString();
            }
        }

        Element contentsElement = element.getFirstElementByClass("content");
        if (contentsElement != null) {


            // todo 보통 도서와 레이아웃이 다른경우 (논문 등등..) 처리
            // if(contentsElement.getFirstElementByClass("divLocationList")){
            //
            //  }else{
            //

            List<Element> elementList = element.getAllElementsByClass("item");
            for (Element ee : elementList) {
                Element e1 = ee.getFirstElement(HTMLElementName.P);
                BookDetailItem.LocationInfo locationInfo = new BookDetailItem.LocationInfo();
                if (e1 != null) {
                    locationInfo.title = e1.getTextExtractor().toString();
                }

                Element e2 = element.getFirstElement(HTMLElementName.UL);
                if (e2 != null) {
                    List<Element> allElements = e2.getAllElements(HTMLElementName.LI);
                    for (Element e : allElements) {
                        locationInfo.infoList.add(e.getTextExtractor().toString()); // 청구기호, 등록번호, ....
                    }
                }

                Element e3 = element.getFirstElementByClass("link");

                if (e3 != null) {
                    Element e4 = element.getFirstElementByClass("current");
                    if (e4 != null) {
                        String state = e4.getTextExtractor().toString();
                        locationInfo.state = BookItem.checkLocationState(state);
                    }

                    Element e5 = element.getFirstElementByClass("reservation");
                    if (e5 != null) {
                        Element e6 = e5.getFirstElement(HTMLElementName.A);
                        if (e6 != null) {
                            locationInfo.link = "http://mlibrary.uos.ac.kr" + e6.getAttributeValue("href");
                        }
                    }
                }


                relationInfo.subRelationInfoList.add(locationInfo);
            }
            //}
        } else {
            return;
        }

        bookDetailItem.relationInfo = relationInfo;
    }

}
