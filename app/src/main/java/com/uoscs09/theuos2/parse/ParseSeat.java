package com.uoscs09.theuos2.parse;

import com.uoscs09.theuos2.tab.libraryseat.SeatItem;
import com.uoscs09.theuos2.util.StringUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseSeat extends JerichoParser<ArrayList<SeatItem>> {
    private List<Element> trList;

	@Override
	protected ArrayList<SeatItem> parseHttpBody(Source src) throws IOException {
		ArrayList<SeatItem> resultList = new ArrayList<>();
		int index;

		List<Element> tableList = src.getAllElements(HTMLElementName.TABLE);
		Element table = tableList.get(1);
		Element dismissTable;
		
		// 페이지 에러
		if(tableList.size() < 4){
			throw new IOException("Library Web Page Error");
		}
		dismissTable = tableList.get(3);
		trList = table.getAllElements(HTMLElementName.TR);
		// 0-12 스터디룸
		for (index = 16; index <= 28; index++) {
			resultList.add(parseWeb(index));
		}

		// ! 13 중앙도서관 스터디룸 노트북 코너
		resultList.add(parseWeb(10));

		// !14-21 중앙도서관 제1 열람실 ~ 제4 열람실, 전자 정보실
		for (index = 2; index <= 9; index++) {
			resultList.add(parseWeb(index));
		}

		// ! 22 경영경제전문도서관 자유열람석
		resultList.add(parseWeb(15));

		// 23-28 경영경제전문도서관 그룹 스터디룸 1 ~ 5, 세미나실
		for (index = 29; index <= 34; index++) {
			resultList.add(parseWeb(index));
		}
		// !29-32 법학전문도서관
		for (index = 11; index <= 14; index++) {
			resultList.add(parseWeb(index));
		}

		List<Element> dismissTR = dismissTable.getAllElements(HTMLElementName.TR);

		StringBuilder sb = new StringBuilder();
		for (Element e : dismissTR) {
			List<Element> tds = e.getAllElements(HTMLElementName.TD);

			if (tds.size() < 2)
				continue;

			sb.append(tds.get(0).getTextExtractor().toString())
                    .append(StringUtil.NEW_LINE)
                    .append(tds.get(1).getTextExtractor().toString())
                    .append(StringUtil.NEW_LINE);
		}

		resultList.add(new SeatItem("좌석 해지 예상 시간표", sb.toString(),StringUtil.NULL, StringUtil.NULL, -1));
		return resultList;
	}

	private SeatItem parseWeb(int i) {
        String roomName;
        String occupySeat;
        String vacancySeat;
        String utilizationRate;
        String tmp;

        Element tr = trList.get(i);

		// roomName 열람실명 얻기. 예) 중앙도서관 제 1 열람실
        Element td = tr.getAllElements(HTMLElementName.TD).get(1);
        Element a = td.getFirstElement(HTMLElementName.A);
		tmp = a.getTextExtractor().toString().substring(1);
		// 원문 앞에" " 빈 문자가 들어가 있음
		// tmp = new String(tmp.getBytes(), "EUC-KR"); //글자 깨짐 방지 인코딩 변경!
		roomName = tmp;

		// occupySeat 사용 좌석수 얻기. 예) 81
		td = tr.getAllElements(HTMLElementName.TD).get(3);
        Element font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();
		occupySeat = tmp;

		// vacancySeat 잔여 좌석수 얻기. 예) 317
		td = tr.getAllElements(HTMLElementName.TD).get(4);
		font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();
		vacancySeat = tmp;

		// utilizationRate 이용율 얻기. 예) 20.35
		td = tr.getAllElements(HTMLElementName.TD).get(5);
		font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();

		// 원문 끝에 " %"가 들어가 있음.
		utilizationRate = tmp.substring(0, tmp.length() - 2);

		return new SeatItem(roomName, occupySeat, vacancySeat, utilizationRate, indexToRoomNumber(i));
	}

	private int indexToRoomNumber(int idx) {
		if (idx < 10)
			return idx - 1;
		else if (idx < 16)
			return idx + 1;
		else
			return idx + 5;
	}
}
