package com.uoscs09.theuos.common.util;

import java.util.Calendar;

/** WISE OPEN API관련 변수와 메소드를 가지는 클래스 */
public class OApiUtil {
	private static String year;
	public static final String UOS_API_KEY = OApiKey.WISE_OAPI_KEY;
	public static final String API_KEY = "apiKey";
	public static final String TERM = "term";
	public static final String YEAR = "year";
	public static final String SUBJECT_NAME = "subjectNm";
	public static final String SUBJECT_NO = "subjectNo";
	public static final String CLASS_DIV = "classDiv";

	public enum Term {
		SPRING, AUTUMN, SUMMER, WINTER
	}

	public static synchronized String getYear() {
		if (year == null) {
			year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		}
		return year;
	}

	public static String getSemesterYear(Term term) {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;

		if (term == Term.WINTER && (month < 3 || month == 12)) {
			year--;
		}

		return String.valueOf(year);
	}

	public static Term getTerm() {
		Calendar c = Calendar.getInstance();
		int m = c.get(Calendar.MONTH) + 1;

		switch (m) {
		case 1:
		case 2:
			return Term.WINTER;
		case 3:
		case 4:
		case 5:
		case 6:
			return Term.SPRING;
		case 7:
		case 8:
			return Term.SUMMER;
		default:
			return Term.AUTUMN;
		}
	}

	public static String getTermCode(Term term) {
		switch (term) {
		default:
		case SPRING:
			return "A10";
		case SUMMER:
			return "A11";
		case AUTUMN:
			return "A20";
		case WINTER:
			return "A21";
		}
	}

	/**
	 * @return 날짜를 나타내는 숫자 <br>
	 *         6월 1일 : 601<br>
	 *         12월 12일 : 1212
	 */
	public static int getDate() {
		Calendar c = Calendar.getInstance();
		return (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DATE);
	}

	/**
	 * @return 날짜와 시간을 나타내는 숫자 <br>
	 *         6월 1일 10시: 60110<br>
	 *         6월 1일 23시: 60123<br>
	 */
	public static int getDateTime() {
		Calendar c = Calendar.getInstance();
		return (c.get(Calendar.MONTH) + 1) * 10000 + c.get(Calendar.DATE) * 100
				+ c.get(Calendar.HOUR_OF_DAY);
	}

	public static String getBuildingName(String buildingNo) {
		StringBuilder sb = new StringBuilder();
		switch (Integer.valueOf(buildingNo)) {
		case 1:
			sb.append("전농관");
			break;
		case 2:
			sb.append("제1공학관");
			break;
		case 3:
			sb.append("건설공학관");
			break;
		case 4:
			sb.append("창공관");
			break;
		case 5:
			sb.append("인문학관");
			break;
		case 6:
			sb.append("배봉관");
			break;
		case 8:
			sb.append("자연과학관");
			break;
		case 9:
			sb.append("음악관");
			break;
		case 10:
			sb.append("경농관");
			break;
		case 11:
			sb.append("제2공학관");
			break;
		case 13:
			sb.append("언무관");
			break;
		case 14:
			sb.append("과학기술관");
			break;
		case 15:
			sb.append("21세기관");
			break;
		case 16:
			sb.append("조형관");
			break;
		case 17:
			sb.append("체육관");
			break;
		case 19:
			sb.append("정보기술관");
			break;
		case 20:
			sb.append("법학관");
			break;
		case 23:
			sb.append("건축구조실험동");
			break;
		case 24:
			sb.append("토목구조실험동");
			break;
		case 25:
			sb.append("미디어관");
			break;
		case 26:
			sb.append("자동화온실");
			break;
		case 28:
			sb.append("운동장");
			break;
		case 33:
			sb.append("미래관");
			break;
		default:
			return null;
		}
		return sb.append('\n').append(buildingNo).append('-').toString();
	}

	public static String getSubjectName(String timetable) {
		try {
			return timetable.trim().split(StringUtil.NEW_LINE)[0];
		} catch (Exception e) {
			return timetable.trim();
		}
	}

	public static String getCompressedString(String timetable) {
		String[] arr = timetable.trim().split(StringUtil.NEW_LINE);
		if (arr.length > 3) {
			String str = arr[0];
			if (str.length() > 6) {
				str = str.substring(0, 5) + "..";
			}
			return str + StringUtil.NEW_LINE + arr[2] + StringUtil.NEW_LINE
					+ arr[3];
		} else {
			return timetable;
		}
	}
}
