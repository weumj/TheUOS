package com.uoscs09.theuos.http.parse;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.phonelist.PhoneItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsePhone extends JerichoParse<PhoneItem> {
    public static final int BODY = 1;
    public static final int SUBJECT = 2;
    public static final int CULTURE = 3;
    private int howTo;
    public static final int BOTTOM = 0;


    public ParsePhone(String body, int howTo) {
        super(body);
        this.howTo = howTo;
    }

    @Override
    protected ArrayList<PhoneItem> parseHttpBody(Source source)
            throws IOException {
        ArrayList<PhoneItem> itemList = new ArrayList<>();

        switch (howTo) {
            case BOTTOM: // 기타 주요시설 파싱(페이지 아랫부분의 전화번호 파싱)
            {
                Element div = source.getElementById("chargeBorder");
                List<Element> list = div.getAllElementsByClass("ml10");
                String name, tel;
                name = tel = StringUtil.NULL;
                int i = 0;
                for (Element li : list) {
                    String string = li.getContent().getTextExtractor().toString();
                    String LOCAL_NUMBER = "02)";
                    if (i == 0) {
                        name = string;
                    } else if (string.startsWith(LOCAL_NUMBER)) {
                        tel = string;
                        tel = tel.replace(LOCAL_NUMBER, "02-");
                    }
                    i++;
                }
                itemList.add(new PhoneItem(name, tel));
            }
            break;
            case BODY: // 각 편의시설 전화번호 파싱
            {
                List<Element> div = source.getAllElementsByClass("floatL ml16");
                for (Element element : div) {
                    // 장소 이름 얻어옴
                    Element img = element.getAllElements(HTMLElementName.IMG)
                            .get(0);
                    String site = img.getAttributeValue("title");
                    site = site.replace("(훼미리마트)", StringUtil.NULL);
                    if (site == null) {
                        throw new IOException();
                    }
                    // 전화번호 얻어옴
                    Element telNumber = element.getAllElementsByClass("d3").get(1);
                    String tel2 = telNumber.getContent().getTextExtractor()
                            .toString();
                    String telNumberString = tel2.replace("전화번호 : 02) ", "02-");
                    if (telNumberString.startsWith("전화번호")) {
                        telNumberString = telNumberString.replace(
                                "전화번호 : 도서관 02) ", "도서관 02-");
                        String[] array = telNumberString.split(StringUtil.SPACE);
                        telNumberString = array[0] + StringUtil.SPACE + array[1]
                                + StringUtil.NEW_LINE + array[2] + StringUtil.SPACE
                                + array[3];
                    }
                    PhoneItem item = new PhoneItem(site, telNumberString);
                    itemList.add(item);
                }
            }
            break;
            case SUBJECT: // 각 학과/학부 사무실 전화번호 파싱
            {
                List<Element> div_site = source.getAllElementsByClass("floatL");
                List<Element> div_number = source
                        .getAllElementsByClass("floatL mt3 ml10 mb14 cTel");
                ArrayList<String> list_site = new ArrayList<>();
                ArrayList<String> list_num = new ArrayList<>();
                for (Element element : div_site) {
                    // 장소 이름 얻어옴
                    try {
                        Element li = element.getAllElementsByClass("d1").get(0);
                        Element img = li.getAllElements(HTMLElementName.IMG).get(0);
                        // title 속성로 가져올 수도 있지만
                        // 홈페이지에 잘못 입력한 부분이 있어서
                        // alt로 가져옴
                        String site = img.getAttributeValue("alt");
                        list_site.add(site);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (Element element : div_number) {
                    // 전화번호 얻어옴
                    String telNumber = element.getContent().getTextExtractor().toString();
                    String[] array = telNumber.split(StringUtil.SPACE);
                    telNumber = StringUtil.NULL;
                    int i = 0;
                    for (String string : array) {
                        if (i == 2) {
                            telNumber += string.replace(')', '-');
                            telNumber = StringUtil.remove(telNumber,
                                    StringUtil.CODE_R_PRNTSIS);
                        } else if (i > 2) {
                            telNumber += string;
                        }
                        i++;
                    }
                    telNumber = StringUtil.remove(telNumber, "]");
                    list_num.add(telNumber);
                }
                int size = list_site.size() > list_num.size() ? list_num.size() : list_site.size();
                for (int i = 0; i < size; i++) {
                    String site = list_site.get(i);
                    if (itemList.indexOf(site) == -1) {
                        String telNumberString = list_num.get(i);
                        PhoneItem item = new PhoneItem(site, telNumberString);
                        itemList.add(item);
                    }
                }
            }
            break;
            case CULTURE: {
                ArrayList<String> list_site = new ArrayList<>();
                ArrayList<String> list_num = new ArrayList<>();
                List<Element> div_site = source.getAllElementsByClass("m0p0");
                for (Element element : div_site) {
                    // 장소 이름 얻어옴
                    try {
                        Element li = element.getAllElementsByClass("d1").get(0)
                                .getAllElements(HTMLElementName.IMG).get(0);
                        String site = li.getAttributeValue("alt");
                        list_site.add(site);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Pattern p = Pattern.compile("\\[학과사무실 :.+\\]");
                Matcher m = p.matcher(source);
                while (m.find()) {
                    list_num.add(StringUtil.removeRegex(m.group().split(":")[1],
                            "( |\\])").replace(")", "-"));
                }

                int size = list_site.size() > list_num.size() ? list_num.size()
                        : list_site.size();
                for (int i = 0; i < size; i++) {
                    String site = list_site.get(i);
                    if (itemList.indexOf(site) == -1) {
                        String telNumberString = list_num.get(i);
                        PhoneItem item = new PhoneItem(site, telNumberString);
                        itemList.add(item);
                    }
                }
            }
            break;
            default:
                break;
        }
        itemList.trimToSize();
        return itemList;
    }
}
