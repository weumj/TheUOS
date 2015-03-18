package com.uoscs09.theuos.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.TabHomeFragment;
import com.uoscs09.theuos.UosMainActivity;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.tab.anounce.ServiceForAnounce;
import com.uoscs09.theuos.tab.anounce.TabAnounceFragment;
import com.uoscs09.theuos.tab.booksearch.TabBookSearchFragment;
import com.uoscs09.theuos.tab.emptyroom.TabSearchEmptyRoomFragment;
import com.uoscs09.theuos.tab.libraryseat.TabLibrarySeatFragment;
import com.uoscs09.theuos.tab.map.TabMapFragment;
import com.uoscs09.theuos.tab.phonelist.PhoneNumberDB;
import com.uoscs09.theuos.tab.phonelist.TabPhoneFragment;
import com.uoscs09.theuos.tab.restaurant.TabRestaurantFragment;
import com.uoscs09.theuos.tab.schedule.TabScheduleFragment;
import com.uoscs09.theuos.tab.score.ScoreFragment;
import com.uoscs09.theuos.tab.subject.TabSearchSubjectFragment;
import com.uoscs09.theuos.tab.timetable.TabTimeTableFragment;
import com.uoscs09.theuos.tab.transport.TabTransportFragment;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    public static final String DB_PHONE = "PhoneNumberDB.db";
    public static final int RELAUNCH_ACTIVITY = 6565;
    private static int PAGE_SIZE = 10;
    public static boolean test;
    public static AppTheme theme;

    /**
     * 어플리케이션의 테마를 나타내는 enum<br>
     * <li><b>White</b> : 흰색/회색 계통으로 구성된 테마</li><br>
     * <li><b>BlackAndWhite</b> : 안드로이드 기본 액션바 테마, 액션바는 검은색, 일반 배경은 하얀색 계통</li><br>
     * <li><b>Black</b> : 검은색 계열로 구성된 테마</li><br>
     */
    public enum AppTheme {
        /**
         * white textColortheme
         */
        White(R.style.Mystyle_White),
        /**
         * android default, 액션바는 검은색, 일반 배경은 하얀색
         */
        BlackAndWhite(R.style.Mystyle_BlackAndWhite),
        /**
         * black textColortheme
         */
        Black(R.style.Mystyle_Black),
        /**
         * Light Blue Theme
         */
        LightBlue(R.style.Mystyle_Light_Blue)
        ;

        final int styleId;
        AppTheme(int styleId){
            this.styleId = styleId;
        }
    }

    public static void initStaticValues(PrefUtil pref) {
        int v = pref.get(PrefUtil.KEY_THEME, 0);
        AppTheme[] vals = AppTheme.values();
        if (v >= vals.length) {
            v = 0;
            pref.put(PrefUtil.KEY_THEME, v);
        }
        AppUtil.theme = vals[v];
        AppUtil.test = pref.get("test", false);
        PAGE_SIZE = test ? 13 : 10;
    }

    /**
     * 저장된 탭 순서를 불러옴
     */
    public static ArrayList<Integer> loadPageOrder(Context context) {
        PrefUtil pref = PrefUtil.getInstance(context);
        ArrayList<Integer> tabList = new ArrayList<>();
        String page = "page";
        for (int i = 1; i < PAGE_SIZE; i++) {
            tabList.add(getTitleResId(pref.get(page + i, i)));
        }
        // tabList.add(getTitleResId(mPrefUtil.get("page99", 99)));

        return tabList;
    }

    // TODO page의 순서 뿐만 아니라 사용 여부도 결정할 수 있게 하는것을 구현
    private static int[] getPages() {
        return new int[]{R.string.title_section1_announce, /* 공지사항 */
                R.string.title_section2_rest, /* 식당메뉴 */
                R.string.title_section3_book, /* 도서검색 */
                R.string.title_section4_lib, /* 도서관좌석 */
                R.string.title_section5_map, /* 학교지도 */
                R.string.title_section6_tel, /* 전화번호 */
                R.string.title_section7_time, /* 시간표 */
                R.string.title_tab_search_empty_room, /* 빈강의실 */
                R.string.title_tab_search_subject, /* 교과목 */
                R.string.title_tab_transport, /* 교통정보 */
                R.string.title_tab_score /* 수업평가 */};
    }

    /**
     * 기본 탭 순서를 불러옴
     */
    public static ArrayList<Integer> loadDefaultPageOrder() {
        ArrayList<Integer> list = new ArrayList<>();
        int[] pages = getPages();
        for (int i = 0; i < PAGE_SIZE - 1; i++) {
            list.add(pages[i]);
        }
        return list;
    }

    /**
     * 기본 page title의 resource id에 따른 page순서를 반환한다.
     */
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
            case R.string.title_tab_schedule:
                return 10;
            case R.string.title_tab_score:
                return 11;
            case R.string.title_tab_transport:
                return 12;
            case R.string.setting:
                return 98;
            case R.string.title_section_etc:
                return 99;
            default:
                return -1;
        }
    }

    /**
     * 기본 page순서에 따른 page title의 resource id를 반환한다.
     */
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
            case 10:
                return R.string.title_tab_schedule;
            case 11:
                return R.string.title_tab_score;
            case 12:
                return R.string.title_tab_transport;
            case 98:
                return R.string.setting;
            case 99:
                return R.string.title_section_etc;
            default:
                return -1;
        }
    }

    /**
     * page 순서를 저장한다.
     */
    public static void savePageOrder(List<Integer> list, Context context) {
        PrefUtil pref = PrefUtil.getInstance(context);
        String PAGE = "page";
        for (int i = 1; i < PAGE_SIZE; i++) {
            pref.put(PAGE + i, titleResIdToOrder(list.get(i - 1)));
        }
    }

    /**
     * 탭 아이콘을 불러옴<br>
     * <i>추후에 이 메소드를 삭제할 예정</i>
     */
    @Deprecated
    public static int getPageIcon(int pageStringResourceId, AppTheme theme) {
        switch (theme) {
            case BlackAndWhite:
            case Black:
                return getPageIconWhite(pageStringResourceId);
            case White:
            default:
                return getPageIconGray(pageStringResourceId);
        }
    }

    /**
     * 탭 아이콘을 불러옴
     */
    @Deprecated
    public static int getPageIcon(int pageStringResourceId) {
        switch (theme) {
            case BlackAndWhite:
            case Black:
                return getPageIconWhite(pageStringResourceId);
            case White:
            default:
                return getPageIconGray(pageStringResourceId);
        }
    }

    public static int getPageIcon(Context context, int pageStringResId) {
        int iconId;
        switch (pageStringResId) {
            case R.string.title_section0_home:
                return R.drawable.ic_launcher;
            case R.string.title_section1_announce:
                iconId = R.attr.ic_collections_view_as_list;
                break;
            case R.string.title_section2_rest:
                iconId = R.attr.ic_restaurant;
                break;
            case R.string.title_section3_book:
                iconId = R.attr.ic_book;
                break;
            case R.string.title_section4_lib:
                iconId = R.attr.ic_chair;
                break;
            case R.string.title_section5_map:
                iconId = R.attr.ic_location_place;
                break;
            case R.string.title_section6_tel:
                iconId = R.attr.ic_device_access_call;
                break;
            case R.string.title_section7_time:
                iconId = R.attr.ic_content_timetable;
                break;
            case R.string.title_tab_search_empty_room:
                iconId = R.attr.ic_action_search;
                break;
            case R.string.title_tab_search_subject:
                iconId = R.attr.ic_content_paste;
                break;
            case R.string.title_tab_schedule:
                iconId = R.attr.ic_content_timetable;
                break;
            case R.string.title_tab_score:
                iconId = R.attr.ic_content_copy;
                break;
            case R.string.title_tab_transport:
                iconId = R.attr.ic_location_directions;
                break;
            case R.string.title_section_etc:
                iconId = R.attr.ic_navigation_accept;
                break;
            case R.string.setting:
                iconId = R.attr.ic_action_settings;
                break;
            case R.string.action_exit:
                iconId = R.attr.ic_content_remove;
                break;
            default:
                return -1;
        }

        return getStyledValue(context, iconId);
    }

    public static int getPageIconForMenu(Context context, int pageStringResId) {
        int iconId;
        switch (pageStringResId) {
            case R.string.title_section0_home:
                return R.drawable.ic_launcher;
            case R.string.title_section1_announce:
                iconId = R.attr.menu_ic_collections_view_as_list;
                break;
            case R.string.title_section2_rest:
                iconId = R.attr.menu_ic_restaurant;
                break;
            case R.string.title_section3_book:
                iconId = R.attr.menu_ic_book;
                break;
            case R.string.title_section4_lib:
                iconId = R.attr.menu_ic_chair;
                break;
            case R.string.title_section5_map:
                iconId = R.attr.menu_ic_location_place;
                break;
            case R.string.title_section6_tel:
                iconId = R.attr.menu_ic_device_access_call;
                break;
            case R.string.title_section7_time:
                iconId = R.attr.menu_ic_content_timetable;
                break;
            case R.string.title_tab_search_empty_room:
                iconId = R.attr.menu_ic_action_search;
                break;
            case R.string.title_tab_search_subject:
                iconId = R.attr.menu_ic_content_paste;
                break;
            case R.string.title_tab_schedule:
                iconId = R.attr.menu_ic_content_timetable;
                break;
            case R.string.title_tab_score:
                iconId = R.attr.menu_ic_content_copy;
                break;
            case R.string.title_tab_transport:
                iconId = R.attr.menu_ic_location_directions;
                break;
            case R.string.title_section_etc:
                iconId = R.attr.menu_ic_navigation_accept;
                break;
            case R.string.setting:
                iconId = R.attr.menu_ic_action_settings;
                break;
            case R.string.action_exit:
                iconId = R.attr.menu_ic_content_remove;
                break;
            default:
                return -1;
        }

        return getStyledValue(context, iconId);
    }

    private static int getPageIconGray(int id) {
        switch (id) {
            case R.string.title_section0_home:
                return R.drawable.ic_launcher;
            case R.string.title_section1_announce:
                return R.drawable.ic_action_action_view_list;
            case R.string.title_section2_rest:
                return R.drawable.ic_restaurant;
            case R.string.title_section3_book:
                return R.drawable.ic_book_text;
            case R.string.title_section4_lib:
                return R.drawable.ic_action_book_with_pen;
            case R.string.title_section5_map:
                return R.drawable.ic_action_maps_place;
            case R.string.title_section6_tel:
                return R.drawable.ic_action_communication_call;
            case R.string.title_section7_time:
                return R.drawable.ic_action_content_timetable;
            case R.string.title_tab_search_empty_room:
                return R.drawable.ic_action_action_search;
            case R.string.title_tab_search_subject:
                return R.drawable.ic_action_content_content_paste;
            case R.string.title_tab_schedule:
                return R.drawable.ic_action_content_timetable;
            case R.string.title_tab_score:
                return R.drawable.ic_action_content_content_copy;
            case R.string.title_tab_transport:
                return R.drawable.ic_action_maps_directions;
            case R.string.title_section_etc:
                return R.drawable.ic_action_navigation_check;
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
                return R.drawable.ic_action_action_view_list_dark;
            case R.string.title_section2_rest:
                return R.drawable.ic_restaurant_dark;
            case R.string.title_section3_book:
                return R.drawable.ic_book_text_dark;
            case R.string.title_section4_lib:
                return R.drawable.ic_action_book_with_pen_dark;
            case R.string.title_section5_map:
                return R.drawable.ic_action_maps_place_dark;
            case R.string.title_section6_tel:
                return R.drawable.ic_action_communication_call_dark;
            case R.string.title_section7_time:
                return R.drawable.ic_action_content_timetable_dark;
            case R.string.title_tab_search_empty_room:
                return R.drawable.ic_action_action_search_dark;
            case R.string.title_tab_search_subject:
                return R.drawable.ic_action_content_content_paste_dark;
            case R.string.title_tab_schedule:
                return R.drawable.ic_action_content_timetable_dark;
            case R.string.title_tab_score:
                return R.drawable.ic_action_content_content_copy_dark;
            case R.string.title_tab_transport:
                return R.drawable.ic_action_maps_directions_dark;
            case R.string.title_section_etc:
                return R.drawable.ic_action_navigation_check_dark;
            case R.string.setting:
                return R.drawable.ic_action_action_settings_dark;
            case R.string.action_exit:
                return R.drawable.ic_action_content_remove_dark;
            default:
                return -1;
        }
    }

    /**
     * 현재 테마에 정의된, {@link R.attr}에 선언된 값을 가져온다.
     *
     * @param attrId 가져올 값의 Id
     * @return 현재 테마에서 정의한 해당 값의 id
     * @since 2.31
     */
    public static int getStyledValue(Context context, int attrId) {
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(attrId, out, true);

        return out.resourceId;
    }

    /**
     * 메인 액티비티에 종료 인텐트를 보낸다.
     */
    public static void exit(Context context) {
        Intent clearTop = new Intent(context, UosMainActivity.class);
        clearTop.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        clearTop.putExtra("FinishSelf", true);
        context.startActivity(clearTop);
    }

    public static void clearCache(Context context) {
        IOUtil.clearApplicationFile(context.getCacheDir());
        IOUtil.clearApplicationFile(context.getExternalCacheDir());
    }

    /**
     * @param pageTitleResId 얻으려는 page에 알맞는 string 리소스 id
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
            case R.string.title_tab_search_empty_room:
                return TabSearchEmptyRoomFragment.class;
            case R.string.title_tab_search_subject:
                return TabSearchSubjectFragment.class;
            case R.string.title_tab_schedule:
                return TabScheduleFragment.class;
            case R.string.title_tab_score:
                return ScoreFragment.class;
            case R.string.title_tab_transport:
                return TabTransportFragment.class;
            default:
                return null;
        }
    }

    public static int getPageResByClass(Class<? extends Fragment> fragmentClass) {
        if (fragmentClass.equals(TabHomeFragment.class))
            return R.string.title_section0_home;
        else if (fragmentClass.equals(TabAnounceFragment.class))
            return R.string.title_section1_announce;
        else if (fragmentClass.equals(TabRestaurantFragment.class))
            return R.string.title_section2_rest;
        else if (fragmentClass.equals(TabBookSearchFragment.class))
            return R.string.title_section3_book;
        else if (fragmentClass.equals(TabLibrarySeatFragment.class))
            return R.string.title_section4_lib;
        else if (fragmentClass.equals(TabMapFragment.class))
            return R.string.title_section5_map;
        else if (fragmentClass.equals(TabPhoneFragment.class))
            return R.string.title_section6_tel;
        else if (fragmentClass.equals(TabTimeTableFragment.class))
            return R.string.title_section7_time;
        else if (fragmentClass.equals(TabSearchEmptyRoomFragment.class))
            return R.string.title_tab_search_empty_room;
        else if (fragmentClass.equals(TabSearchSubjectFragment.class))
            return R.string.title_tab_search_subject;
        else if (fragmentClass.equals(TabScheduleFragment.class))
            return R.string.title_tab_schedule;
        else if (fragmentClass.equals(ScoreFragment.class))
            return R.string.title_tab_score;
        else if (fragmentClass.equals(TabTransportFragment.class))
            return R.string.title_tab_transport;
        else
            return -1;
    }

    /**
     * 인텐트를 통해 인터넷 페이지를 띄운다.
     *
     * @param webURL 접속하려는 페이지의 url
     * @return url이 설정된 intent
     */
    public static Intent setWebPageIntent(String webURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(webURL));
        return intent;
    }

    public static void showInternetConnectionErrorToast(Context context, boolean isVisible) {
        showToast(context, R.string.error_internet, isVisible);
    }

    public static void showToast(Context context, CharSequence msg,  boolean isVisible) {
        if (context != null && isVisible) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, int resId, boolean isVisible) {
        if (context != null && isVisible) {
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, int resId) {
        showToast(context, resId, true);
    }

    public static void showToast(Context context, CharSequence msg) {
        showToast(context, msg, true);
    }

    public static void showCanceledToast(Context context, boolean isVisible) {
        showToast(context, R.string.canceled, isVisible);
    }

    public static void showErrorToast(Context context, Exception e,
                                      boolean isVisible) {
        if (context != null) {
            showToast(context, context.getText(R.string.error_occur) + " : " + e.getMessage(), isVisible);
        }
    }

    /**
     * 공지사항 알리미 서비스를 시작/정지한다.
     */
    public static boolean startOrStopServiceAnounce(Context context) {
        boolean isServiceEnable = PrefUtil.getInstance(context).get(  context.getString(R.string.pref_key_check_anounce_service),  true);
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
     * @param isHorizontal         진행바가 막대 모양인지 원 모양인지 결정한다.
     * @param cancelButtonListener 취소 버튼을 눌렀을 때, 불릴 callback
     */
    public static MaterialDialog getProgressDialog(Context context, boolean isHorizontal, OnClickListener cancelButtonListener) {
        return getProgressDialog(context, isHorizontal, context.getString(R.string.progress_while_updating), cancelButtonListener);
    }

    /**
     * ProgressDialog를 생성한다.
     *
     * @param isHorizontal         진행바가 막대 모양인지 원 모양인지 결정한다.
     * @param msg                  dialog에 나타날 message
     * @param cancelButtonListener 취소 버튼을 눌렀을 때, 불릴 callback
     */
    public static MaterialDialog getProgressDialog(Context context, boolean isHorizontal, CharSequence msg, final DialogInterface.OnClickListener cancelButtonListener) {

        return new MaterialDialog.Builder(context)
                .content(msg)
                .progress(!isHorizontal, 100, true)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        if (cancelButtonListener != null)
                            cancelButtonListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                })
                .build();
    }

    /**
     * 액티비티 전환 애니메이션을 바꾼다.<br>
     * <li>1 : 화면이 fade된다.</li><br>
     * <li>2 : 화면이 확대/축소 된다.</li>
     */
    public static void overridePendingTransition(Activity activity, int how) {
        switch (how) {
            case 0:
                activity.overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
                break;
            case 1:
                activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
            default:
                break;
        }
    }

    /**
     * 열려진 DB를 모두 닫는다.
     */
    public static void closeAllDatabase(Context context) {
        if (PhoneNumberDB.isOpen())
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

        appContext.setTheme(theme.styleId);
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
     * <b>{@code @ReleaseWhenDestroy}</b> annotation이 붙은 Field를 null로 설정한다.
     *
     * @param receiver 해제 될 field가 있는 object
     */
    public static void releaseResource(Object receiver) {
        Field[] fields = receiver.getClass().getDeclaredFields();

        for (Field f : fields) {
            if (f.getAnnotation(ReleaseWhenDestroy.class) != null) {
                try {
                    f.setAccessible(true);
                    if (f.getType().isArray()) {
                        Object array = f.get(receiver);
                        int size = Array.getLength(array);
                        for (int i = 0; i < size; i++) {
                            Object o = Array.get(array, i);
                            if (o instanceof View)
                                unbindDrawables((View) o);
                            Array.set(array, i, null);
                        }
                    }
                    Object o = f.get(receiver);
                    if (o instanceof View)
                        unbindDrawables((View) o);
                    f.set(receiver, null);
                    f.setAccessible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 주어진 View와 View의 child의 drawable을 제거한다.
     *
     * @param view - 해제될 View
     */
    public static void unbindDrawables(View view) {
        view.destroyDrawingCache();
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if (view instanceof AdapterView
                    || view instanceof AdapterViewCompat)
                return;
            ((ViewGroup) view).removeAllViews();
        }
    }

    /**
     * 화면 크기를 판단한다.
     *
     * @return {@code true} - 화면 크기가
     * {@link Configuration#SCREENLAYOUT_SIZE_NORMAL} 이하 일 경우<br>
     * <br>
     * {@code false} - 그 외
     */
    public static boolean isScreenSizeSmall(Context context) {
        int sizeInfoMasked = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (sizeInfoMasked) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return true;
            default:
                return false;
        }
    }

}
