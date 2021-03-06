package com.uoscs09.theuos2.tab.announce;

import android.support.v4.util.Pair;

import com.uoscs09.theuos2.parse.JerichoParser;
import com.uoscs09.theuos2.util.CollectionUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

public abstract class AnnounceParser extends JerichoParser<List<AnnounceItem>> {
    public static AnnounceParser mobileWeb() {
        return new MobileWeb();
    }

    public static AnnounceParser normalWeb() {
        return NormalWeb.getParser();
    }

    public static AnnounceParser scholarship() {
        return NormalWeb.getScholarshipParser();
    }

    private abstract static class NormalWeb extends AnnounceParser {
        public static AnnounceParser getParser() {
            return new Normal();
        }

        public static AnnounceParser getScholarshipParser() {
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
                /*
                if (checkElementIsNoticeType(element))
                    item.type = AnnounceItem.TYPE_NOTICE;
                else {
                    String numberString = childElementList.get(0).getTextExtractor().toString();
                    if ("글이 없습니다.".equals(numberString))
                        return null;
                    else
                        item.number = Integer.parseInt(childElementList.get(0).getTextExtractor().toString());
                }
                */

                Element titleElement = childElementList.get(1);
                item.title = titleElement.getTextExtractor().toString();
                item.pageURL = extractPageURL(titleElement.getFirstElement(HTMLElementName.A)
                        .getAttributeValue(getPageURLElementAttrName()));

                item.date = childElementList.get(3).getTextExtractor().toString();

                /*
                Element fileDownElement = childElementList.get(5).getFirstElement(HTMLElementName.A);
                if (fileDownElement != null) {
                    // item.attachedFileUrl = extractFileDownURL(fileDownElement.getAttributeValue("onclick"));
                }
                */

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

        private static class Normal extends NormalWeb {
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

        private static class Scholarship extends NormalWeb {

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

    private static class MobileWeb extends AnnounceParser {
        private static final Pattern p = Pattern.compile("\'\\d*\'");

        @Override
        protected List<AnnounceItem> parseHtmlBody(Source source) throws Throwable {
            Element boardList = source.getElementById("board_list");
            List<Element> elementList = boardList.getChildElements();

            return Observable.from(elementList)
                    .map(MobileWeb::parseElement)
                    .filter(announceItem -> announceItem != null)
                    .toList()
                    .toBlocking()
                    .first();
        }

        private static AnnounceItem parseElement(Element element) {
            try {
                Element linkElement = element.getFirstElementByClass("list_warp");
                if (linkElement == null)
                    return null;


                String[] arr = extractPageUrl(linkElement);

                String viewUrl = arr.length < 2 ? "" :
                        arr.length == 3 ? String.format("http://m.uos.ac.kr/mkor/schBoard/view.do?sort=%s&seq=%s&board_id=%s", arr[0], arr[1], arr[2]) // scholarship
                                : String.format("http://www.uos.ac.kr/korNotice/view.do?sort=%s&seq=%s&list_id=", arr[0], arr[1]);
                Element contentElement = element.getFirstElementByClass("list_text");
                if (contentElement == null) {
                    return null;
                }

                Element titleElement = contentElement.getFirstElementByClass("list_title");
                String title = titleElement == null ? "" : titleElement.getTextExtractor().toString();

                Element dateElement = contentElement.getFirstElementByClass("date");
                String date = dateElement == null ? "" : dateElement.getTextExtractor().toString();

                AnnounceItem item = new AnnounceItem();

                item.pageURL = viewUrl;
                item.date = date;
                item.title = title;
                return item;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static String[] extractPageUrl(Element linkElement) {
            String onclickValue = linkElement.getAttributeValue("onclick"); // javascript:View('10','17030') , javascript:schView('2','1','20160630')
            Matcher m = p.matcher(onclickValue);

            ArrayList<String> list = new ArrayList<>(3);
            while (m.find()) {
                list.add(m.group().replaceAll("'", ""));
            }

            return list.toArray(new String[list.size()]);
        }
    }


    public static JerichoParser<AnnounceDetailItem> announcePageParser(AnnounceItem.Category category) {
        if (category == AnnounceItem.Category.SCHOLARSHIP)
            return new ScholarshipAnnouncePageParser();
        else return new AnnouncePageParser();
    }

    private static class AnnouncePageParser extends JerichoParser<AnnounceDetailItem> {

        @Override
        protected AnnounceDetailItem parseHtmlBody(Source source) throws Throwable {
            AnnounceDetailItem announceDetailItem = new AnnounceDetailItem();
            Element listTypeView = CollectionUtil.elementAt(source.getAllElementsByClass("listType view"), 0);

            if (listTypeView != null) {
                Element file = CollectionUtil.elementAt(listTypeView.getAllElementsByClass("file"), 0);

                String html = listTypeView.toString();
                if (file != null) {
                    html = html.replace(file.toString(), "");
                }

                Pattern p = Pattern.compile("<img.+?>");
                Matcher m = p.matcher(html);
                while (m.find()) {
                    html = html.replace(m.group(), m.group()
                            //.replaceAll("width=\".+?\"", "")
                            //.replaceAll("height=\".+?\"", "")
                            .replaceAll("style=\".+?\"", "")
                            .replace(">", " style=\"max-width: 96%; height: auto; >"));
                }

                announceDetailItem.page = "<meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, target-densitydpi=device-dpi, user-scalable=yes\" />\n" +
                        "<link rel=\"stylesheet\" href=\"/css/kor2016/style.css\" type=\"text/css\" />\n" + html;
            }


            Element fileLayer = CollectionUtil.elementAt(source.getAllElementsByClass("fileLyaer") /*uos 웹페이지 오타*/, 0);
            if (fileLayer != null) {
                List<Element> fileAElement = fileLayer.getAllElementsByClass("dbtn");

                Pattern p = Pattern.compile("\'.+?\'");
                ArrayList<Pair<String, String>> fileUrlPairList = new ArrayList<>(fileAElement.size());
                for (Element e : fileAElement) {
                    String onclick = e.getAttributeValue("onclick");
                    Matcher m = p.matcher(onclick);

                    ArrayList<String> list = new ArrayList<>(3);
                    while (m.find()) {
                        list.add(m.group().replaceAll("'", ""));
                    }

                    if (list.size() > 2) {
                        String fileUrl = String.format("http://m.uos.ac.kr/common/view/FileDown.do?file_path=%s&file_upNm=%s&file_orgNm=%s", list.get(0), list.get(1), list.get(2));
                        fileUrlPairList.add(new Pair<>(list.get(2), fileUrl));
                    }
                }

                announceDetailItem.fileNameUrlPairList = fileUrlPairList;
            }
            return announceDetailItem;
        }
    }

    private static class ScholarshipAnnouncePageParser extends JerichoParser<AnnounceDetailItem> {

        @Override
        protected AnnounceDetailItem parseHtmlBody(Source source) throws Throwable {
            AnnounceDetailItem announceDetailItem = new AnnounceDetailItem();
            Element conTextBoard = CollectionUtil.elementAt(source.getAllElementsByClass("con_text_board"), 0);

            if (conTextBoard != null) {
                Element file = CollectionUtil.elementAt(conTextBoard.getAllElementsByClass("file"), 0);

                String html = conTextBoard.toString();
                if (file != null) {
                    html = html.replace(file.toString(), "");
                }

                Element boardNavigator = CollectionUtil.elementAt(conTextBoard.getAllElementsByClass("board_num"), 0);
                if (boardNavigator != null) {
                    html = html.replace(boardNavigator.toString(), "");
                }

                Pattern p = Pattern.compile("<img.+?>");
                Matcher m = p.matcher(html);
                while (m.find()) {
                    html = html.replace(m.group(), m.group()
                            //.replaceAll("width=\".+?\"", "")
                            //.replaceAll("height=\".+?\"", "")
                            .replaceAll("style=\".+?\"", "")
                            .replace(">", " style=\"max-width: 96%; height: auto; >"));
                }

                announceDetailItem.page = "<meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, target-densitydpi=device-dpi, user-scalable=yes\" />\n" +
                        "<link rel=\"stylesheet\" href=\"/css/mkor/base.css\" type=\"text/css\" />\n" + html;
            }


            List<Element> fileDivElement = source.getAllElementsByClass("pb4 hwp");

            Pattern p = Pattern.compile("\'.+?\'");
            ArrayList<Pair<String, String>> fileUrlPairList = new ArrayList<>(fileDivElement.size());
            for (Element e : fileDivElement) {
                String href = e.getChildElements().get(0).getAttributeValue("href");
                Matcher m = p.matcher(href);

                ArrayList<String> list = new ArrayList<>(3);
                while (m.find()) {
                    list.add(m.group().replaceAll("'", ""));
                }

                String fileUrl = String.format("http://m.uos.ac.kr/common/view/FileDown.do?file_path=%s&file_upNm=%s&file_orgNm=%s", list.get(0), list.get(1), list.get(2));
                fileUrlPairList.add(new Pair<>(list.get(2), fileUrl));
            }

            announceDetailItem.fileNameUrlPairList = fileUrlPairList;

            return announceDetailItem;
        }
    }
}
