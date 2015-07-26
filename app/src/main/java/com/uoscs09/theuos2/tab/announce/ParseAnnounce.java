package com.uoscs09.theuos2.tab.announce;

import com.uoscs09.theuos2.parse.JerichoParser;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public abstract class ParseAnnounce extends JerichoParser<ArrayList<AnnounceItem>> {

    public static ParseAnnounce getParser() {
        return new Normal();
    }

    public static ParseAnnounce getScholarshipParser() {
        return new Scholarship();
    }

    @Override
    protected ArrayList<AnnounceItem> parseHtmlBody(Source source) throws Exception {
        Element noticeTable = source.getFirstElementByClass(getTableClassName());

        Element tbody = noticeTable.getFirstElement(HTMLElementName.TBODY);

        List<Element> noticeList = tbody.getAllElements(HTMLElementName.TR);

        ArrayList<AnnounceItem> list = new ArrayList<>();
        for (Element notice : noticeList) {
            AnnounceItem item = parseElement(notice);
            if (item != null)
                list.add(item);
        }

        return list;
    }

    private AnnounceItem parseElement(Element element) {
        try {
            AnnounceItem item = new AnnounceItem();

            List<Element> childElementList = element.getAllElements(HTMLElementName.TD);
            if (checkElementIsNoticeType(element))
                item.type = AnnounceItem.TYPE_NOTICE;
            else
                item.number = Integer.parseInt(childElementList.get(0).getTextExtractor().toString());

            Element titleElement = childElementList.get(1);
            item.title = titleElement.getTextExtractor().toString();
            item.pageURL = extractPageURL(titleElement.getFirstElement(HTMLElementName.A)
                    .getAttributeValue(getPageURLElementAttrName()));

            item.date = childElementList.get(3).getTextExtractor().toString();

            Element fileDownElement = childElementList.get(5).getFirstElement(HTMLElementName.A);
            if (fileDownElement != null) {
                item.attachedFileUrl = extractFileDownURL(fileDownElement.getAttributeValue("onclick"));
            }

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract String getTableClassName();


    abstract boolean checkElementIsNoticeType(Element e);

    abstract String getPageURLElementAttrName();

    abstract String extractPageURL(String str);

    abstract String extractFileDownURL(String str);

    private static class Normal extends ParseAnnounce {
        @Override
        protected String getTableClassName() {
            return "listType01";
        }

        @Override
        boolean checkElementIsNoticeType(Element e) {
            return "on".equals(e.getAttributeValue("class"));
        }

        @Override
        String getPageURLElementAttrName() {
            return "onclick";
        }

        @Override
        String extractPageURL(String onClick) {
            int first = onClick.indexOf('\'') + 1;
            int second = onClick.indexOf('\'', first);
            int third = onClick.indexOf('\'', second + 1) + 1;
            int fourth = onClick.indexOf('\'', third);

            return "http://www.uos.ac.kr/korNotice/view.do?sort=" + onClick.substring(first, second) +
                    "&seq=" + onClick.substring(third, fourth)
                    + "&viewAuth=Y&writeAuth=N"
                    + "&list_id=";
        }

        @Override
        String extractFileDownURL(String href) {
            int first = href.indexOf('\'') + 1;
            int second = href.indexOf('\'', first);
            int third = href.indexOf('\'', second + 1) + 1;
            int fourth = href.indexOf('\'', third);


            return "http://www.uos.ac.kr/common/FileDown.do?seq=" + href.substring(first, second)
                    + "&f_seq=" + href.substring(third, fourth)
                    + "&list_id=";
        }


    }

    private static class Scholarship extends ParseAnnounce {

        @Override
        protected String getTableClassName() {
            return "notice_tb ";
        }

        @Override
        boolean checkElementIsNoticeType(Element e) {
            return "left_C fontBold line_h_41".equals(e.getFirstElement(HTMLElementName.TD).getAttributeValue("class"));
        }

        @Override
        String getPageURLElementAttrName() {
            return "href";
        }


        @Override
        String extractPageURL(String href) {
            int first = href.indexOf('\'') + 1;
            int second = href.indexOf('\'', first);
            int third = href.indexOf('\'', second + 1) + 1;
            int fourth = href.indexOf('\'', third);

            return "http://scholarship.uos.ac.kr/scholarship/notice/notice/view.do?brdDate=" + href.substring(first, second)
                    + "&brdSeq=" + href.substring(third, fourth)
                    + "&brdBbsseq=1";

        }

        @Override
        String extractFileDownURL(String href) {
            int first = href.indexOf('\'') + 1;
            int second = href.indexOf('\'', first);
            int third = href.indexOf('\'', second + 1) + 1;
            int fourth = href.indexOf('\'', third);
            int fifth = href.indexOf('\'', fourth + 1) + 1;
            int sixth = href.indexOf('\'', fifth);

            return "http://scholarship.uos.ac.kr/scholarship/download.do?brdDate=" + href.substring(first, second)
                    + "&brdSeq=" + href.substring(third, fourth)
                    + "&filSeq=" + href.substring(fifth, sixth)
                    + "&brdBbsseq=1";
        }

    }

}
