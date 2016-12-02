package com.uoscs09.theuos2.tab.libraryseat;

import com.uoscs09.theuos2.parse.JerichoParser;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibararySeatParser extends JerichoParser<SeatTotalInfo> {

    @Override
    protected SeatTotalInfo parseHtmlBody(Source src) throws IOException {
        List<Element> tableList = src.getAllElements(HTMLElementName.TABLE);

        // 페이지 에러
        if (tableList.size() < 4) {
            throw new IOException("Library Web Page Error");
        }

        Element seatListHtmlTable = tableList.get(1);
        ArrayList<SeatInfo> resultList = parseHtmlToSeatList(seatListHtmlTable);

        Element dismissTable = tableList.get(3);
        List<SeatDismissInfo> seatDismissInfoList = parseHtmlToDismissInfoList(dismissTable);

        SeatTotalInfo info = new SeatTotalInfo();
        info.seatInfoList = resultList;
        info.seatDismissInfoList = seatDismissInfoList;

        return info;
    }

    private ArrayList<SeatInfo> parseHtmlToSeatList(Element seatListHtmlTable) {
        ArrayList<SeatInfo> resultList = new ArrayList<>();
        int index;

        List<Element> trList = seatListHtmlTable.getAllElements(HTMLElementName.TR);

        // 0-12 스터디룸
        for (index = 16; index <= 28; index++) {
            resultList.add(parseWeb(trList.get(index), index));
        }

        // ! 13 중앙도서관 스터디룸 노트북 코너
        resultList.add(parseWeb(trList.get(10), 10));

        // !14-21 중앙도서관 제1 열람실 ~ 제4 열람실, 전자 정보실
        for (index = 2; index <= 9; index++) {
            resultList.add(parseWeb(trList.get(index), index));
        }

        // ! 22 경영경제전문도서관 자유열람석
        resultList.add(parseWeb(trList.get(15), 15));

        // 23-28 경영경제전문도서관 그룹 스터디룸 1 ~ 5, 세미나실
        for (index = 29; index <= 34; index++) {
            resultList.add(parseWeb(trList.get(index), index));
        }
        // !29-32 법학전문도서관
        for (index = 11; index <= 14; index++) {
            resultList.add(parseWeb(trList.get(index), index));
        }

        return resultList;
    }

    private List<SeatDismissInfo> parseHtmlToDismissInfoList(Element dismissTable) {
        List<Element> dismissTR = dismissTable.getAllElements(HTMLElementName.TR);

        if (dismissTR.size() < 4)
            return Collections.emptyList();

        ArrayList<SeatDismissInfo> list = new ArrayList<>(6);
        final int N = dismissTR.size();
        for (int i = 2; i < N; i++) {
            List<Element> tds = dismissTR.get(i).getAllElements(HTMLElementName.TD);

            if (tds.size() < 2)
                continue;

            SeatDismissInfo info = new SeatDismissInfo();
            info.time = Integer.parseInt(tds.get(0).getTextExtractor().toString().split(" ")[0]);
            info.seatCount = Integer.parseInt(tds.get(1).getTextExtractor().toString());

            list.add(info);
        }
        return list;
    }

    private SeatInfo parseWeb(Element tr, int i) {
        // roomName 열람실명 얻기. 예) 중앙도서관 제 1 열람실
        Element td = tr.getAllElements(HTMLElementName.TD).get(1);
        Element a = td.getFirstElement(HTMLElementName.A);

        SeatInfo item = new SeatInfo();
        String tmp = a.getTextExtractor().toString().substring(1);
        // 원문 앞에" " 빈 문자가 들어가 있음
        // tmp = new String(tmp.getBytes(), "EUC-KR"); //글자 깨짐 방지 인코딩 변경!
        item.roomName = tmp;

        // occupySeat 사용 좌석수 얻기. 예) 81
        td = tr.getAllElements(HTMLElementName.TD).get(3);
        Element font = td.getAllElements(HTMLElementName.FONT).get(0);
        tmp = font.getTextExtractor().toString();
        item.occupySeat = tmp;

        // vacancySeat 잔여 좌석수 얻기. 예) 317
        td = tr.getAllElements(HTMLElementName.TD).get(4);
        font = td.getAllElements(HTMLElementName.FONT).get(0);
        tmp = font.getTextExtractor().toString();
        item.vacancySeat = tmp;

        // utilizationRateStr 이용율 얻기. 예) 20.35
        td = tr.getAllElements(HTMLElementName.TD).get(5);
        font = td.getAllElements(HTMLElementName.FONT).get(0);
        tmp = font.getTextExtractor().toString();

        // 원문 끝에 " %"가 들어가 있음.
        item.utilizationRateStr = tmp.substring(0, tmp.length() - 2);
        try {
            item.utilizationRate = Float.parseFloat(item.utilizationRateStr);
        } catch (Exception e) {
            item.utilizationRate = 0;
        }

        item.index = indexToRoomNumber(i);

        return item;
    }

    private static int indexToRoomNumber(int idx) {
        if (idx < 10)
            return idx - 1;
        else if (idx < 16)
            return idx + 1;
        else
            return idx + 5;
    }
}
