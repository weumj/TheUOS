package com.uoscs09.theuos.http.parse;

public class ParseFactory {
	/* TODO enum type¿∏∑Œ πŸ≤‹ ∞Õ*/
	public static final int ANOUNCE = 111;
	public static final int BOOK = 112;
	public static final int REST = 113;
	public static final int SEAT = 114;
	public static final int PHONE = 115;
	public static final int TIMETABLE = 116;
	public static final int ETC_EMPTY_ROOM = 117;
	public static final int ETC_SUBJECT = 118;
	public static final int ETC_SUBJECT_INFO = 119;
	public static final int ETC_SUBJECT_LIST = 120;
	public static final int ETC_SUBJECT_SCORE = 121;

	/* TODO enum type¿∏∑Œ πŸ≤‹ ∞Õ*/
	public static final class Value {
		public final static int BASIC = 0;
		public final static int BOTTOM = 999;
		public final static int BODY = 777;
		public final static int SUBJECT = 666;
		public final static int CULTURE = 555;
	}

	public static IParseHttp create(int which, String body, int howTo) {
		switch (which) {
		case ANOUNCE:
			return new ParseAnounce(body, howTo);
		case BOOK:
			return new ParseBook(body);
		case PHONE:
			return new ParsePhone(body, howTo);
		case REST:
			return new ParseRest(body);
		case SEAT:
			return new ParseSeat(body);
		case TIMETABLE:
			return new ParseTimetable(body);
		case ETC_EMPTY_ROOM:
			return new ParseEmptyRoom(body);
		case ETC_SUBJECT:
			return new ParseSubject(body);
		case ETC_SUBJECT_INFO:
			return new ParseSubjectInfo(body);
		case ETC_SUBJECT_LIST:
			return new ParseSubjectList(body);
		case ETC_SUBJECT_SCORE:
			return new ParseSubjectScore(body);
		default:
			return null;
		}
	}
}
