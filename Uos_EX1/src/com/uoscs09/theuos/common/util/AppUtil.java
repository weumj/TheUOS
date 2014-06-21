package com.uoscs09.theuos.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.uoscs09.theuos.PagerFragmentActivity;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.TabHomeFragment;
import com.uoscs09.theuos.tab.anounce.ServiceForAnounce;
import com.uoscs09.theuos.tab.anounce.TabAnounceFragment;
import com.uoscs09.theuos.tab.booksearch.TabBookSearchFragment;
import com.uoscs09.theuos.tab.emptyroom.TabSearchEmptyRoomFragment;
import com.uoscs09.theuos.tab.etc.TabEtcFragment;
import com.uoscs09.theuos.tab.libraryseat.TabLibrarySeatFragment;
import com.uoscs09.theuos.tab.map.TabMapFragment;
import com.uoscs09.theuos.tab.phonelist.PhoneNumberDB;
import com.uoscs09.theuos.tab.phonelist.TabPhoneFragment;
import com.uoscs09.theuos.tab.restaurant.TabRestaurantFragment;
import com.uoscs09.theuos.tab.subject.TabSearchSubjectFragment;
import com.uoscs09.theuos.tab.timetable.TabTimeTableFragment;

public class AppUtil {
	public static final String FILE_TIMETABLE = "timetable_file";
	public static final String DB_PHONE = "PhoneNumberDB.db";
	public static final String FILE_COLOR_TABLE = "color_table_file";
	public static final String FILE_REST = "rest_file";
	public static final int RELAUNCH_ACTIVITY = 6565;
	public static boolean test;
	public static AppTheme theme;
	public static int timetable_limit;

	/**
	 * 어플리케이션의 테마를 나타내는 enum<br>
	 * <li><b>White</b> : 흰색/회색 계통으로 구성된 테마</li><br>
	 * <li><b>BlackAndWhite</b> : 안드로이드 기본 액션바 테마, 액션바는 검은색, 일반 배경은 하얀색 계통</li><br>
	 * <li><b>Black</b> : 검은색 계열로 구성된 테마</li><br>
	 */
	public enum AppTheme {
		/** white theme */
		White,
		/** android default, 액션바는 검은색, 일반 배경은 하얀색 */
		BlackAndWhite,
		/** black theme */
		Black;
	}

	public static void initStaticValues(PrefUtil pref) {
		AppUtil.theme = AppTheme.values()[pref.get(PrefUtil.KEY_THEME, 0)];
		AppUtil.timetable_limit = pref.get(PrefUtil.KEY_TIMETABLE_LIMIT,
				PrefUtil.TIMETABLE_LIMIT_MAX);
		AppUtil.test = pref.get("test", false);
	}

	/** 저장된 탭 순서를 불러옴 */
	public static ArrayList<Integer> loadPageOrder(Context context) {
		PrefUtil pref = PrefUtil.getInstance(context);
		ArrayList<Integer> tabList = new ArrayList<Integer>();
		tabList.add(getTitleResId(pref.get("page1", 1)));
		tabList.add(getTitleResId(pref.get("page2", 2)));
		tabList.add(getTitleResId(pref.get("page3", 3)));
		tabList.add(getTitleResId(pref.get("page4", 4)));
		tabList.add(getTitleResId(pref.get("page5", 5)));
		tabList.add(getTitleResId(pref.get("page6", 6)));
		tabList.add(getTitleResId(pref.get("page7", 7)));
		tabList.add(getTitleResId(pref.get("page8", 8)));
		tabList.add(getTitleResId(pref.get("page9", 9)));
		// tabList.add(getTitleResId(pref.get("page99", 99)));

		return tabList;
	}

	/** 기본 탭 순서를 불러옴 */
	public static ArrayList<Integer> loadDefaultPageOrder() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(R.string.title_section1_announce);
		list.add(R.string.title_section2_rest);
		list.add(R.string.title_section3_book);
		list.add(R.string.title_section4_lib);
		list.add(R.string.title_section5_map);
		list.add(R.string.title_section6_tel);
		list.add(R.string.title_section7_time);
		list.add(R.string.title_tab_search_empty_room);
		list.add(R.string.title_tab_search_subject);
		// list.add(R.string.title_section_etc);
		return list;
	}

	/** 기본 page title의 resource id에 따른 page순서를 반환한다. */
	public static int titleResIdToOrder(int titleResId) {
		switch (titleResId) {
		case R.string.title_section0_home:
			return 0;
		case R.string.title_section1_announce:
			return 1;
		case R.string.title_section2_rest:
			return 2;
		case R.string.title_section3_book:
			return 3;
		case R.string.title_section4_lib:
			return 4;
		case R.string.title_section5_map:
			return 5;
		case R.string.title_section6_tel:
			return 6;
		case R.string.title_section7_time:
			return 7;
		case R.string.title_tab_search_empty_room:
			return 8;
		case R.string.title_tab_search_subject:
			return 9;
		case R.string.setting:
			return 98;
		case R.string.title_section_etc:
			return 99;
		default:
			return -1;
		}
	}

	/** 기본 page순서에 따른 page title의 resource id를 반환한다. */
	public static int getTitleResId(int order) {
		switch (order) {
		case 0:
			return R.string.title_section0_home;
		case 1:
			return R.string.title_section1_announce;
		case 2:
			return R.string.title_section2_rest;
		case 3:
			return R.string.title_section3_book;
		case 4:
			return R.string.title_section4_lib;
		case 5:
			return R.string.title_section5_map;
		case 6:
			return R.string.title_section6_tel;
		case 7:
			return R.string.title_section7_time;
		case 8:
			return R.string.title_tab_search_empty_room;
		case 9:
			return R.string.title_tab_search_subject;
		case 98:
			return R.string.setting;
		case 99:
			return R.string.title_section_etc;
		default:
			return -1;
		}
	}

	/** page 순서를 저장한다. */
	public static void savePageOrder(List<Integer> list, Context context) {
		PrefUtil pref = PrefUtil.getInstance(context);
		String PAGE = "page";
		for (int i = 1; i < 10; i++) {
			pref.put(PAGE + i, titleResIdToOrder(list.get(i - 1)));
		}
	}

	/** 탭 아이콘을 불러옴 */
	public static int getPageIcon(int pageStringResourceId, AppTheme theme) {
		switch (theme) {
		case BlackAndWhite:
		case Black:
			return getPageIconWhite(pageStringResourceId);
		case White:
			return getPageIconGray(pageStringResourceId);
		default:
			return -1;
		}
	}

	/** 탭 아이콘을 불러옴 */
	public static int getPageIcon(int pageStringResourceId) {
		switch (theme) {
		case BlackAndWhite:
		case Black:
			return getPageIconWhite(pageStringResourceId);
		case White:
			return getPageIconGray(pageStringResourceId);
		default:
			return -1;
		}
	}

	private static int getPageIconGray(int id) {
		switch (id) {
		case R.string.title_section0_home:
			return R.drawable.ic_launcher;
		case R.string.title_section1_announce:
			return R.drawable.ic_action_collections_view_as_list;
		case R.string.title_section2_rest:
			return R.drawable.ic_restaurant;
		case R.string.title_section3_book:
			return R.drawable.ic_book_text;
		case R.string.title_section4_lib:
			return R.drawable.ic_chair;
		case R.string.title_section5_map:
			return R.drawable.ic_action_location_place;
		case R.string.title_section6_tel:
			return R.drawable.ic_action_device_access_call;
		case R.string.title_section7_time:
			return R.drawable.ic_action_device_access_storage_1;
		case R.string.title_tab_search_empty_room:
			return R.drawable.ic_action_action_search;
		case R.string.title_tab_search_subject:
			return R.drawable.ic_action_content_paste;
		case R.string.title_section_etc:
			return R.drawable.ic_action_navigation_accept;
		case R.string.setting:
			return R.drawable.ic_action_action_settings;
		case R.string.action_exit:
			return R.drawable.ic_action_content_remove;
		default:
			return -1;
		}
	}

	private static int getPageIconWhite(int id) {
		switch (id) {
		case R.string.title_section0_home:
			return R.drawable.ic_launcher;
		case R.string.title_section1_announce:
			return R.drawable.ic_action_collections_view_as_list_dark;
		case R.string.title_section2_rest:
			return R.drawable.ic_restaurant_dark;
		case R.string.title_section3_book:
			return R.drawable.ic_book_text_dark;
		case R.string.title_section4_lib:
			return R.drawable.ic_chair_dark;
		case R.string.title_section5_map:
			return R.drawable.ic_action_location_place_dark;
		case R.string.title_section6_tel:
			return R.drawable.ic_action_device_access_call_dark;
		case R.string.title_section7_time:
			return R.drawable.ic_action_device_access_storage_1_dark;
		case R.string.title_tab_search_empty_room:
			return R.drawable.ic_action_action_search_dark;
		case R.string.title_tab_search_subject:
			return R.drawable.ic_action_content_paste_dark;
		case R.string.title_section_etc:
			return R.drawable.ic_action_navigation_accept_dark;
		case R.string.setting:
			return R.drawable.ic_action_action_settings_dark;
		case R.string.action_exit:
			return R.drawable.ic_action_content_remove_dark;
		default:
			return -1;
		}
	}

	/** 메인 액티비티에 종료 인텐트를 보낸다. */
	public static void exit(Context context) {
		Intent clearTop = new Intent(context, PagerFragmentActivity.class);
		clearTop.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		clearTop.putExtra("FinishSelf", true);
		context.startActivity(clearTop);
	}

	/** 어플리케이션의 파일 또는 폴더를 삭제한다. */
	public static void clearApplicationFile(File dir) {
		if (dir == null || !dir.isDirectory())
			return;
		File[] children = dir.listFiles();
		try {
			for (File file : children) {
				if (file.isDirectory())
					clearApplicationFile(file);
				else
					file.delete();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * @param pageTitleResId
	 *            얻으려는 page에 알맞는 string 리소스 id
	 * @return fragment 클래스
	 */
	public static Class<? extends Fragment> getPageClass(int pageTitleResId) {
		switch (pageTitleResId) {
		case R.string.title_section0_home:
			return TabHomeFragment.class;
		case R.string.title_section1_announce:
			return TabAnounceFragment.class;
		case R.string.title_section2_rest:
			return TabRestaurantFragment.class;
		case R.string.title_section3_book:
			return TabBookSearchFragment.class;
		case R.string.title_section4_lib:
			return TabLibrarySeatFragment.class;
		case R.string.title_section5_map:
			return TabMapFragment.class;
		case R.string.title_section6_tel:
			return TabPhoneFragment.class;
		case R.string.title_section7_time:
			return TabTimeTableFragment.class;
		case R.string.title_section_etc:
			return TabEtcFragment.class;
		case R.string.title_tab_search_empty_room:
			return TabSearchEmptyRoomFragment.class;
		case R.string.title_tab_search_subject:
			return TabSearchSubjectFragment.class;
		default:
			return null;
		}
	}

	/**
	 * 인텐트를 통해 인터넷 페이지를 띄운다.
	 * 
	 * @param webURL
	 *            접속하려는 페이지의 url
	 * @return url이 설정된 intent
	 */
	public static Intent setWebPageIntent(String webURL) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(webURL));
		return intent;
	}

	public static void showInternetConnectionErrorToast(Context context,
			boolean isVisible) {
		showToast(context, R.string.error_internet, isVisible);
	}

	public static void showToast(Context context, CharSequence msg,
			boolean isVisible) {
		if (context != null && isVisible) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}

	public static void showToast(Context context, int resId, boolean isVisible) {
		if (context != null && isVisible) {
			Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
		}
	}

	public static void showCanceledToast(Context context, boolean isVisible) {
		showToast(context, R.string.canceled, isVisible);
	}

	public static void showErrorToast(Context context, Exception e,
			boolean isVisible) {
		if (context != null) {
			showToast(context, context.getText(R.string.error_occur) + " : "
					+ e.getMessage(), isVisible);
		}
	}

	/** 공지사항 알리미 서비스를 시작/정지한다. */
	public static boolean startOrStopServiceAnounce(Context context) {
		boolean isServiceEnable = PrefUtil.getInstance(context).get(
				context.getString(R.string.pref_key_check_anounce_service),
				true);
		Intent service = new Intent(context, ServiceForAnounce.class);
		if (isServiceEnable) {
			context.startService(service);
		} else {
			context.stopService(service);
		}
		return isServiceEnable;
	}

	/**
	 * 기본 메시지가 <b>R.string.progress_while_updating</b>인 <br>
	 * ProgressDialog를 생성한다.
	 * 
	 * @param context
	 * @param isHorizontal
	 *            진행바가 막대 모양인지 원 모양인지 결정한다.
	 * @param cancelButtonListener
	 *            취소 버튼을 눌렀을 때, 불릴 callback
	 */
	public static ProgressDialog getProgressDialog(Context context,
			boolean isHorizontal, OnClickListener cancelButtonListener) {
		return getProgressDialog(context, isHorizontal,
				context.getString(R.string.progress_while_updating),
				cancelButtonListener);
	}

	/**
	 * ProgressDialog를 생성한다.
	 * 
	 * @param context
	 * @param isHorizontal
	 *            진행바가 막대 모양인지 원 모양인지 결정한다.
	 * @param msg
	 *            dialog에 나타날 message
	 * @param cancelButtonListener
	 *            취소 버튼을 눌렀을 때, 불릴 callback
	 */
	public static ProgressDialog getProgressDialog(Context context,
			boolean isHorizontal, CharSequence msg,
			DialogInterface.OnClickListener cancelButtonListener) {
		ProgressDialog progress = new ProgressDialog(context);
		progress.setMessage(msg);
		progress.setCancelable(false);
		progress.setButton(DialogInterface.BUTTON_NEGATIVE,
				context.getText(android.R.string.cancel), cancelButtonListener);
		if (isHorizontal) {
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		return progress;
	}

	/**
	 * 액티비티 전환 애니메이션을 바꾼다.<br>
	 * <li>1 : 화면이 fade된다.</li><br>
	 * <li>2 : 화면이 확대/축소 된다.</li>
	 */
	public static void overridePendingTransition(Activity activity, int how) {
		switch (how) {
		case 0:
			activity.overridePendingTransition(R.anim.enter_fade,
					R.anim.exit_hold);
			break;
		case 1:
			activity.overridePendingTransition(R.anim.zoom_enter,
					R.anim.zoom_exit);
		default:
			break;
		}
	}

	/** 열려진 DB를 모두 닫는다. */
	public static void closeAllDatabase(Context context) {
		PhoneNumberDB.getInstance(context).close();
	}

	/**
	 * 현재 설정된 {@link AppUtil.AppTheme} 값에 따라 테마를 적용함. <br>
	 * 반드시 activity의 onCreate()실행 처음에 불려야 한다.
	 */
	public static void applyTheme(Context appContext) {
		if (theme == null) {
			theme = AppTheme.values()[PrefUtil.getInstance(appContext).get(
					PrefUtil.KEY_THEME, 0)];
		}
		switch (theme) {
		case BlackAndWhite:
			appContext.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
			break;
		case Black:
			appContext.setTheme(R.style.Theme_Mystyledark);
			break;
		case White:
		default:
			appContext.setTheme(R.style.Theme_Mystyle);
			break;
		}
	}

	/**
	 * 주어진 idx에 따라 color를 얻음, 범위는 0~9<br>
	 * 시간표 과목마다 색을 달리하는데 사용됨
	 */
	public static int getColor(int idx) {
		switch (idx) {
		case 0:
			return R.drawable.layout_color_red_yellow;
		case 1:
			return R.drawable.layout_color_light_blue;
		case 2:
			return R.drawable.layout_color_yellow;
		case 3:
			return R.drawable.layout_color_violet;
		case 4:
			return R.drawable.layout_color_green;
		case 5:
			return R.drawable.layout_color_gray_blue;
		case 6:
			return R.drawable.layout_color_purple;
		case 7:
			return R.drawable.layout_color_yellow_green;
		case 8:
			return R.drawable.layout_color_blue_green;
		case 9:
			return R.drawable.layout_color_gray_red;
		default:
			return -1;
		}
	}

	/**
	 * 주어진 이름의 파일을 읽어온다.
	 * 
	 * @return 파일이 존재하고, 성공적으로 읽어왔을 경우 : 해당 객체 <br>
	 *         파일이 없거나 예외가 발생할 경우 : null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readFromFile(Context context, String fileName) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;
		T object;
		try {
			fis = context.openFileInput(fileName);
			bis = new BufferedInputStream(fis);
			ois = new ObjectInputStream(bis);
			object = (T) ois.readObject();
		} catch (FileNotFoundException e) {
			object = null;
		} catch (Exception e) {
			e.printStackTrace();
			object = null;
		} finally {
			closeStream(ois);
			closeStream(bis);
			closeStream(fis);
		}
		return object;
	}

	/**
	 * 주어진 이름으로 파일을 저장한다.
	 * 
	 * @param mode
	 *            Context 클래스의 mode 변수
	 * @param obj
	 *            저장할 객체
	 * @return 성공 여부
	 */
	public static boolean saveToFile(Context context, String fileName,
			int mode, Object obj) {
		boolean state = false;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(fileName, mode);
			bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			state = true;
		} catch (Exception e) {
			AppUtil.showToast(context, R.string.error_file_save, true);
			e.printStackTrace();
		} finally {
			closeStream(oos);
			closeStream(bos);
			closeStream(fos);
		}
		return state;
	}

	private static void closeStream(Closeable close) {
		if (close != null) {
			try {
				close.close();
			} catch (IOException e) {
			}
		}
	}
}
