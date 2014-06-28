package com.uoscs09.theuos.http.parse;

public class ParseFactory {
	/* TODO enum type¿∏∑Œ πŸ≤‹ ∞Õ*/
	public enum What{
		Anounce, Book, Rest, Seat, Phone, TimeTable, EmptyRoom,
		Subject, SubjectInfo, SubjectList, SubjectScore, Transport
	}
	/* TODO enum type¿∏∑Œ πŸ≤‹ ∞Õ*/
	public static final class Value {
		public final static int BASIC = 0;
		public final static int BOTTOM = 999;
		public final static int BODY = 777;
		public final static int SUBJECT = 666;
		public final static int CULTURE = 555;
	}

	public static IParseHttp create(What what, String body, int howTo) {
		switch (what) {
		case Anounce:
			return new ParseAnounce(body, howTo);
		case Book:
			return new ParseBook(body);
		case Phone:
			return new ParsePhone(body, howTo);
		case Rest:
			return new ParseRest(body);
		case Seat:
			return new ParseSeat(body);
		case TimeTable:
			return new ParseTimetable(body);
		case EmptyRoom:
			return new ParseEmptyRoom(body);
		case Subject:
			return new ParseSubject(body);
		case SubjectInfo:
			return new ParseSubjectInfo(body);
		case SubjectList:
			return new ParseSubjectList(body);
		case SubjectScore:
			return new ParseSubjectScore(body);
		case Transport:
			return new ParseTransport(body, howTo);
		default:
			return null;
		}
	}
}
