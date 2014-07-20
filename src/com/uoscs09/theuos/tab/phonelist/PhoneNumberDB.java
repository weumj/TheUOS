package com.uoscs09.theuos.tab.phonelist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;

public class PhoneNumberDB implements Runnable {
	private SQLiteDatabase db;
	private static final String TABLE_Name = "PhoneNumberList";
	private static final String TABLE_AttrSite = "Site";
	private static final String TABLE_AttrPhone = "PhoneNumber";
	private PrefUtil pref;
	private static PhoneNumberDB instance = null;

	public synchronized static PhoneNumberDB getInstance(Context context) {
		if (instance == null)
			instance = new PhoneNumberDB(context);
		return instance;
	}

	private PhoneNumberDB(Context context) {
		this.db = context.getApplicationContext().openOrCreateDatabase(
				AppUtil.DB_PHONE, 0, null);
		pref = PrefUtil.getInstance(context);
		createDB();
	}

	public boolean createDB() {
		if (!pref.get(TABLE_AttrPhone, false)) {
			try {
				db.execSQL("CREATE TABLE PhoneNumberList (Site text, PhoneNumber text, PRIMARY KEY(Site))");
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				init();
				pref.put(TABLE_AttrSite, true);
			}
		} else {
			return true;
		}
	}

	public static boolean isOpen() {
		return instance == null ? false : true;
	}

	public synchronized boolean insert(PhoneItem item) {
		try {
			ContentValues value = new ContentValues();
			value.put(TABLE_AttrSite, item.siteName);
			value.put(TABLE_AttrPhone, item.sitePhoneNumber);
			db.insert(TABLE_Name, null, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public synchronized int update(PhoneItem item) {
		ContentValues value = new ContentValues();
		value.put(TABLE_AttrSite, item.siteName);
		value.put(TABLE_AttrPhone, item.sitePhoneNumber);
		int count = db.update(TABLE_Name, value, "Site = '" + item.siteName
				+ "'", null);
		return count;
	}

	/**
	 * @return update : update된 row 갯수 <br>
	 *         insert : 성공 -> -1, 실패 -> -2
	 */
	public int insertOrUpdate(PhoneItem item) {
		if (read(item.siteName) != null) {
			return update(item);
		} else {
			return insert(item) ? -1 : -2;
		}
	}

	public synchronized int delete(PhoneItem item) {
		return db.delete(TABLE_Name, TABLE_AttrSite + " = '" + item.siteName
				+ "'", null);
	}

	public PhoneItem read(String siteName) {
		String query = " WHERE " + TABLE_AttrSite + " = '" + siteName + "'";
		try {
			return select(query).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public List<PhoneItem> readAll(String query) {
		return select(StringUtil.NULL);
	}

	private synchronized ArrayList<PhoneItem> select(String query) {
		ArrayList<PhoneItem> itemList = null;
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM " + TABLE_Name + " " + query, null);
			if (c.getCount() > 0) {
				itemList = new ArrayList<PhoneItem>();
				c.moveToFirst();
				do {
					String site = c.getString(0);
					String number = c.getString(1);
					PhoneItem item = new PhoneItem(site, number);
					itemList.add(item);
					c.moveToNext();
				} while (!c.isAfterLast());
			}
		} finally {
			if (c != null)
				c.close();
		}
		return itemList;
	}

	public synchronized void close() {
		AsyncLoader.excute(this);
	}

	@Override
	public void run() {
		db.close();
		pref = null;
		instance = null;
	}

	private synchronized void init() {
		if (!pref.get(TABLE_Name, false)) {
			insert(new PhoneItem("정경대학", "02-6490-2003"));
			insert(new PhoneItem("행정학과", "02-6490-2010"));
			insert(new PhoneItem("국제관계학과", "02-6490-2035"));
			insert(new PhoneItem("경제학부", "02-6490-2051"));
			insert(new PhoneItem("사회복지학과", "02-6490-2075"));
			insert(new PhoneItem("세무학과", "02-6490-2095"));
			insert(new PhoneItem("법학부", "02-6490-2110"));

			insert(new PhoneItem("경영대학", "02-6490-2201"));
			insert(new PhoneItem("경영학부", "02-6490-2210"));

			insert(new PhoneItem("공과대학", "02-6490-2304"));
			insert(new PhoneItem("전자전기컴퓨터공학부", "02-6490-2310"));
			insert(new PhoneItem("화학공학과", "02-6490-2360"));
			insert(new PhoneItem("기계정보공학과", "02-6490-2380"));
			insert(new PhoneItem("신소재공학과", "02-6490-2400"));
			insert(new PhoneItem("토목공학과", "02-6490-2420"));
			insert(new PhoneItem("컴퓨터과학부", "02-6490-2440"));

			insert(new PhoneItem("인문대학", "02-6490-2505"));
			insert(new PhoneItem("영어영문학과", "02-6490-2510"));
			insert(new PhoneItem("국어국문학과", "02-6490-2530"));
			insert(new PhoneItem("국사학과", "02-6490-2550"));
			insert(new PhoneItem("철학과", "02-6490-2570"));
			insert(new PhoneItem("중국어문화학과", "02-6490-2586"));

			insert(new PhoneItem("자연과학대학", "02-6490-2601"));
			insert(new PhoneItem("수학과", "02-6490-2606"));
			insert(new PhoneItem("통계학과", "02-6490-2625"));
			insert(new PhoneItem("물리학과", "02-6490-2640"));
			insert(new PhoneItem("생명과학과", "02-6490-2660"));
			insert(new PhoneItem("환경원예학과", "02-6490-2680"));

			insert(new PhoneItem("도시과학대학", "02-6490-2702"));
			insert(new PhoneItem("건축학부(건축공학)", "02-6490-2753"));
			insert(new PhoneItem("건축학부(건축학)", "02-6490-2751"));
			insert(new PhoneItem("도시공학과", "02-6490-2790"));
			insert(new PhoneItem("교통공학과", "02-6490-2815"));
			insert(new PhoneItem("조경학과", "02-6490-2835"));
			insert(new PhoneItem("도시행정학과", "02-6490-2710"));
			insert(new PhoneItem("도시사회학과", "02-6490-2730"));
			insert(new PhoneItem("공간정보공학과", "02-6490-2880"));
			insert(new PhoneItem("환경공학부", "02-6490-2853"));

			insert(new PhoneItem("예술체육대학", "02-6490-2902"));
			insert(new PhoneItem("음악학과", "02-6490-2930"));
			insert(new PhoneItem("산업디자인학과", "02-6490-2906"));
			insert(new PhoneItem("환경조각학과", "02-6490-2916"));
			insert(new PhoneItem("스포츠과학과", "02-6490-2945"));

			insert(new PhoneItem("자유전공학부", "02-6490-2126"));

			insert(new PhoneItem("교양교육부", "02-6490-5202"));
			insert(new PhoneItem("글쓰기센터", "02-6490-5274"));
			insert(new PhoneItem("대학영어센터", "02-6490-5206"));
			insert(new PhoneItem("교양수학", "02-6490-5235"));
			insert(new PhoneItem("교양컴퓨터", "02-6490-5265"));
			insert(new PhoneItem("교양물리", "02-6490-5245"));
			insert(new PhoneItem("교양화학", "02-6490-5255"));
			insert(new PhoneItem("교양생물", "02-6490-5262"));
			insert(new PhoneItem("교양체육", "02-6490-2945"));

			insert(new PhoneItem("생활관", "02-6490-5186"));
			insert(new PhoneItem("대학보건소", "02-6490-6590"));
			insert(new PhoneItem("학생과", "02-6490-6212"));
			insert(new PhoneItem("체육관", "02-6490-5165"));
			insert(new PhoneItem("학생회관 편의점", "02-6490-5861"));
			insert(new PhoneItem("중앙도서관 편의점", "02-6490-5862"));
			insert(new PhoneItem("생활관 편의점", "02-6490-5863"));
			insert(new PhoneItem("WEB", "02-6490-5865"));
			insert(new PhoneItem("Free Zone", "02-6490-5866"));
			insert(new PhoneItem("그리고...휴", "02-6490-5867"));
			insert(new PhoneItem("파리바게뜨", "02-6490-5864"));
			insert(new PhoneItem("자판기", "02-6490-5852"));
			insert(new PhoneItem("서점", "02-2210-2344"));
			insert(new PhoneItem("문구", "02-2210-2344"));
			insert(new PhoneItem("복사", "02-2210-2358"));
			insert(new PhoneItem("안경점", "02-2210-2360"));
			insert(new PhoneItem("기념품매장", "02-2210-2192"));
			insert(new PhoneItem("우체국", "02-2210-2293"));

			// DB가 초기화 되었다는 표시를 함.
			pref.put(TABLE_Name, true);
		}

	}
}
