package com.uoscs09.theuos.util;

import java.util.Calendar;

public class SeoulOApiUtil {
	public final static String KEY = "KEY";
	public final static String OAPI_KEY = OApiKey.SEOUL_OPAI_KEY.toString();
	public final static String TYPE = "TYPE";
	public final static String TYPE_XML = "xml";
	public final static String TYPE_JSON = "json";
	public final static String SERVICE = "SERVICE";
	public final static String START_INDEX = "START_INDEX";
	public final static String END_INDEX = "END_INDEX";
	public final static String STATION_CD = "STATION_CD";
	public final static String INOUT_TAG = "INOUT_TAG";
	public final static String WEEK_TAG = "WEEK_TAG";
	public final static String HOST = "http://openAPI.seoul.go.kr:8088/";

	public final static String METRO_ARRIVAL = "SearchArrivalTimeOfLine2SubwayByIDService";

	public static class Metro {
		public static final String Chung = "0158";
		public static final String JChung = "1014";
		public static final String HOE = "1015";
		public static final String JHOE = "1200";
		public static final String DAP = "2543";

		public static  String[] getValues() {
			return new String[] { Chung, JChung, HOE, JHOE, DAP };
		}
	}

	public static String getStationName(String code) {
        switch (code) {
            case Metro.Chung:
                return "청량리 (1호선)";
            case Metro.DAP:
                return "답십리";
            case Metro.HOE:
                return "회기 (1호선)";
            case Metro.JChung:
                return "청량리 (중앙선)";
            case Metro.JHOE:
                return "회기 (중앙선)";
            default:
                return null;
        }
	}

	public static String getWeekTag() {
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return "3";
		case Calendar.SATURDAY:
			return "2";
		default:
			return "1";
		}
	}
}
