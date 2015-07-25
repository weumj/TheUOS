package com.uoscs09.theuos2.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.TabHomeFragment;
import com.uoscs09.theuos2.UosMainActivity;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.tab.announce.TabAnnounceFragment;
import com.uoscs09.theuos2.tab.booksearch.TabBookSearchFragment;
import com.uoscs09.theuos2.tab.emptyroom.TabSearchEmptyRoomFragment;
import com.uoscs09.theuos2.tab.libraryseat.TabLibrarySeatFragment;
import com.uoscs09.theuos2.tab.map.TabMapFragment;
import com.uoscs09.theuos2.tab.phonelist.TabPhoneFragment;
import com.uoscs09.theuos2.tab.restaurant.TabRestaurantFragment;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleFragment;
import com.uoscs09.theuos2.tab.score.ScoreFragment;
import com.uoscs09.theuos2.tab.subject.TabSearchSubjectFragment2;
import com.uoscs09.theuos2.tab.timetable.TabTimeTableFragment2;
import com.uoscs09.theuos2.tab.transport.TabTransportFragment;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class AppUtil {
    public static final int RESOURCE_NOT_EXIST = -1;
    public static final String DB_PHONE = "PhoneNumberDB.db";
    public static final int RELAUNCH_ACTIVITY = 6565;

    private static final int MAX_PAGE_SIZE_NORMAL = 10;

    private static int PAGE_SIZE = MAX_PAGE_SIZE_NORMAL;
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
        White(R.style.AppTheme_Style_White),
        /**
         * android default, 액션바는 검은색, 일반 배경은 하얀색
         */
        BlackAndWhite(R.style.AppTheme_Style_BlackAndWhite),
        /**
         * black textColortheme
         */
        Black(R.style.AppTheme_Style_Black),
        /**
         * Light Blue Theme
         */
        LightBlue(R.style.AppTheme_Style_Light_Blue);

        final int styleId;

        AppTheme(int styleId) {
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

        PAGE_SIZE = test ? 13 : MAX_PAGE_SIZE_NORMAL;
    }

/*
    public enum TabInfo {
        Home(TabHomeFragment.class, R.string.title_section0_home, R.drawable.ic_launcher),
        Announce(TabAnnounceFragment.class, R.string.title_tab_announce, R.attr.theme_ic_action_action_view_list),
        Schedule(UnivScheduleFragment.class, R.string.title_tab_schedule, R.attr.theme_ic_action_calendar),
        Restaurant(TabRestaurantFragment.class, R.string.title_tab_restaurant, R.attr.theme_ic_action_maps_local_restaurant),
        BookSearch(TabBookSearchFragment.class, R.string.title_tab_book_search, R.attr.theme_ic_action_book_search),
        LibrarySeat(TabLibrarySeatFragment.class, R.string.title_tab_library_seat, R.attr.theme_ic_action_book_opened),
        TimeTable(TabTimeTableFragment2.class, R.string.title_tab_timetable, R.attr.theme_ic_action_timetable),
        Map(TabMapFragment.class, R.string.title_tab_map, R.attr.theme_ic_action_maps_place),
        EmptyRoom(TabSearchEmptyRoomFragment.class, R.string.title_tab_search_empty_room, R.attr.theme_ic_action_action_search),
        SearchSubject(TabSearchSubjectFragment2.class, R.string.title_tab_search_subject, R.attr.theme_ic_action_content_content_paste),

        // disable
        Phone(TabPhoneFragment.class, R.string.title_tab_phone, R.attr.theme_ic_action_communication_call),
        CheckCourseEval(ScoreFragment.class, R.string.title_tab_score, R.attr.theme_ic_action_content_content_copy),
        Transport(TabTransportFragment.class, R.string.title_tab_transport, R.attr.theme_ic_action_maps_directions);

        public final Class<? extends Fragment> tabClass;
        @StringRes
        public final int title;
        private CharSequence titleText;
        final int iconRes;

        TabInfo(Class<? extends Fragment> tabClass, @StringRes int titleId, int iconRes) {
            this.tabClass = tabClass;
            this.title = titleId;
            this.iconRes = iconRes;
        }

        public int getIcon(Context context) {
            if (this.equals(Home))
                return iconRes;
            else
                return getAttrValue(context, iconRes);
        }

        public CharSequence getTitle(Context context){
            if(titleText == null)
                titleText = context.getText(title);

            return titleText;
        }

        public Fragment getFragment(){
            try {
                return tabClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        public static TabInfo find(@StringRes int titleId){
            switch (titleId) {
                case R.string.title_section0_home:
                    return Home;

                case R.string.title_tab_announce:
                    return Announce;

                case R.string.title_tab_restaurant:
                    return Restaurant;

                case R.string.title_tab_book_search:
                    return BookSearch;

                case R.string.title_tab_library_seat:
                    return LibrarySeat;

                case R.string.title_tab_map:
                    return Map;

                case R.string.title_tab_phone:
                    return Phone;

                case R.string.title_tab_timetable:
                    return TimeTable;

                case R.string.title_tab_search_empty_room:
                    return EmptyRoom;

                case R.string.title_tab_search_subject:
                    return SearchSubject;

                case R.string.title_tab_schedule:
                    return Schedule;

                case R.string.title_tab_score:
                    return CheckCourseEval;

                case R.string.title_tab_transport:
                    return Transport;

                default:
                    return null;
            }
        }

    }
*/

    public static class Page {
        public int stringId;
        public int order;
        //public final Class<? extends Fragment>
        public boolean isEnable;

        private static final String PAGE_ORDER_ = "page_order_";
        private static final String PAGE_ENABLE_ = "page_enable_";

        Page(int order) {
            this.order = order;
            stringId = AppUtil.getTitleResId(order);
            isEnable = true;
        }

        Page(int order, boolean isEnable) {
            this(order);
            this.isEnable = isEnable;
        }

        static Page read(PrefUtil pref, int order) {
            int tabOrder = pref.get(PAGE_ORDER_ + order, order);
            boolean isEnable = pref.get(PAGE_ENABLE_ + order, true);

            return new Page(tabOrder, isEnable);
        }

        void write(PrefUtil pref, int order) {
            pref.put(PAGE_ORDER_ + order, this.order);
            pref.put(PAGE_ENABLE_ + order, isEnable);
        }


        static void clearAll(PrefUtil pref) {
            String[] array = new String[PAGE_SIZE];
            String[] array2 = new String[PAGE_SIZE];
            for (int i = 1; i < array.length; i++) {
                array[i - 1] = PAGE_ORDER_ + i;
                array2[i - 1] = PAGE_ENABLE_ + i;
            }
            pref.remove(array);
            pref.remove(array2);
        }


        public void swap(Page another) {
            int stringId = this.stringId;
            int order = this.order;
            boolean isEnable = this.isEnable;

            this.stringId = another.stringId;
            this.order = another.order;
            this.isEnable = another.isEnable;

            another.stringId = stringId;
            another.order = order;
            another.isEnable = isEnable;
        }

        @Override
        public String toString() {
            return "Page : " + order + " / " + isEnable + " : " + stringId;
        }
    }

    public static ArrayList<Page> loadDefaultOrder2() {
        ArrayList<Page> list = new ArrayList<>();
        for (int i = 1; i < PAGE_SIZE; i++) {
            list.add(new Page(i));
        }

        return list;
    }

    public static ArrayList<Page> loadPageOrder2(Context context) {
        ArrayList<Page> list = new ArrayList<>();
        PrefUtil pref = PrefUtil.getInstance(context);
        for (int i = 1; i < PAGE_SIZE; i++) {
            list.add(Page.read(pref, i));
        }

        return list;
    }

    public static ArrayList<Integer> loadEnabledPageOrder(Context context) {
        PrefUtil pref = PrefUtil.getInstance(context);
        ArrayList<Integer> tabList = new ArrayList<>();
        for (int i = 1; i < PAGE_SIZE; i++) {
            Page page = Page.read(pref, i);

            if (page.isEnable)
                tabList.add(page.stringId);
        }

        return tabList;
    }

    public static void savePageOrder2(ArrayList<Page> list, Context context) {
        PrefUtil pref = PrefUtil.getInstance(context);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            list.get(i).write(pref, i + 1);
        }

    }

    /**
     * 기본 page title의 resource id에 따른 page순서를 반환한다.
     */
    public static int titleResIdToOrder(@StringRes int titleResId) {
        switch (titleResId) {
            case R.string.title_section0_home:
                return 0;
            case R.string.title_tab_announce:
                return 1;
            case R.string.title_tab_schedule:
                return 2;
            case R.string.title_tab_restaurant:
                return 3;
            case R.string.title_tab_book_search:
                return 4;
            case R.string.title_tab_library_seat:
                return 5;
            case R.string.title_tab_timetable:
                return 6;
            case R.string.title_tab_map:
                return 7;
            case R.string.title_tab_search_empty_room:
                return 8;
            case R.string.title_tab_search_subject:
                return 9;

            case R.string.title_tab_phone:
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
                return RESOURCE_NOT_EXIST;
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
                return R.string.title_tab_announce;
            case 2:
                return R.string.title_tab_schedule;
            case 3:
                return R.string.title_tab_restaurant;
            case 4:
                return R.string.title_tab_book_search;
            case 5:
                return R.string.title_tab_library_seat;
            case 6:
                return R.string.title_tab_timetable;
            case 7:
                return R.string.title_tab_map;
            case 8:
                return R.string.title_tab_search_empty_room;
            case 9:
                return R.string.title_tab_search_subject;

            case 10:
                return R.string.title_tab_phone;
            case 11:
                return R.string.title_tab_score;
            case 12:
                return R.string.title_tab_transport;
            case 98:
                return R.string.setting;
            case 99:
                return R.string.title_section_etc;
            default:
                return RESOURCE_NOT_EXIST;
        }
    }

    public static int getPageIcon(Context context, @StringRes int pageStringResId) {
        int id;
        switch (pageStringResId) {
            case R.string.title_section0_home:
                return R.drawable.ic_launcher;

            case R.string.title_tab_announce:
                id = R.attr.theme_ic_action_action_view_list;
                break;

            case R.string.title_tab_restaurant:
                id = R.attr.theme_ic_action_maps_local_restaurant;
                break;

            case R.string.title_tab_book_search:
                id = R.attr.theme_ic_action_book_search;
                break;

            case R.string.title_tab_library_seat:
                id = R.attr.theme_ic_action_book_opened;
                break;

            case R.string.title_tab_map:
                id = R.attr.theme_ic_action_maps_place;
                break;

            case R.string.title_tab_phone:
                id = R.attr.theme_ic_action_communication_call;
                break;

            case R.string.title_tab_timetable:
                id = R.attr.theme_ic_action_timetable;
                break;

            case R.string.title_tab_search_empty_room:
                id = R.attr.theme_ic_action_action_search;
                break;

            case R.string.title_tab_search_subject:
                id = R.attr.theme_ic_action_content_content_paste;
                break;

            case R.string.title_tab_schedule:
                id = R.attr.theme_ic_action_calendar;
                break;

            case R.string.title_tab_score:
                id = R.attr.theme_ic_action_content_content_copy;
                break;

            case R.string.title_tab_transport:
                id = R.attr.theme_ic_action_maps_directions;
                break;

            case R.string.title_section_etc:
                id = R.attr.theme_ic_action_navigation_check;
                break;

            case R.string.setting:
                id = R.attr.theme_ic_action_action_settings;
                break;

            case R.string.action_exit:
                id = R.attr.theme_ic_action_navigation_close;
                break;

            default:
                return RESOURCE_NOT_EXIST;
        }

        return getAttrValue(context, id);

    }

    public static int getPageIconWhite(@StringRes int id) {
        switch (id) {
            case R.string.title_section0_home:
                return R.drawable.ic_launcher;

            case R.string.title_tab_announce:
                return R.drawable.ic_action_action_view_list_white;

            case R.string.title_tab_restaurant:
                return R.drawable.ic_action_maps_local_restaurant_white;

            case R.string.title_tab_book_search:
                return R.drawable.ic_action_book_search_white;

            case R.string.title_tab_library_seat:
                return R.drawable.ic_action_book_opened_white;

            case R.string.title_tab_map:
                return R.drawable.ic_action_maps_place_white;

            case R.string.title_tab_phone:
                return R.drawable.ic_action_communication_call_white;

            case R.string.title_tab_timetable:
                return R.drawable.ic_action_timetable_white;

            case R.string.title_tab_search_empty_room:
                return R.drawable.ic_action_action_search_white;

            case R.string.title_tab_search_subject:
                return R.drawable.ic_action_content_content_paste_white;

            case R.string.title_tab_schedule:
                return R.drawable.ic_action_calendar_white;

            case R.string.title_tab_score:
                return R.drawable.ic_action_content_content_copy_white;

            case R.string.title_tab_transport:
                return R.drawable.ic_action_maps_directions_white;

            case R.string.title_section_etc:
                return R.drawable.ic_action_navigation_check_white;

            case R.string.setting:
                return R.drawable.ic_action_action_settings_white;

            case R.string.action_exit:
                return R.drawable.ic_action_navigation_close_white;

            default:
                return RESOURCE_NOT_EXIST;
        }
    }

    /**
     * 현재 테마에 정의된, {@link R.attr}에 선언된 값을 가져온다.
     *
     * @param attrId 가져올 값의 Id
     * @return 현재 테마에서 정의한 해당 값의 id
     */
    public static int getAttrValue(Context context, @AttrRes int attrId) {
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(attrId, out, true);

        return out.resourceId;
    }

    public static int getAttrColor(Context context, @AttrRes int attrColorId){
        return context.getResources().getColor(getAttrValue(context, attrColorId));
    }

    /**
     * 메인 액티비티에 종료 인텐트를 보낸다.
     */
    public static void exit(Context context) {
        Intent clearTop = new Intent(context, UosMainActivity.class);
        clearTop.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
    public static Class<? extends Fragment> getPageClass(@StringRes int pageTitleResId) {
        switch (pageTitleResId) {
            case R.string.title_section0_home:
                return TabHomeFragment.class;

            case R.string.title_tab_announce:
                return TabAnnounceFragment.class;

            case R.string.title_tab_restaurant:
                return TabRestaurantFragment.class;

            case R.string.title_tab_book_search:
                return TabBookSearchFragment.class;

            case R.string.title_tab_library_seat:
                return TabLibrarySeatFragment.class;

            case R.string.title_tab_map:
                return TabMapFragment.class;

            case R.string.title_tab_phone:
                return TabPhoneFragment.class;

            case R.string.title_tab_timetable:
                return TabTimeTableFragment2.class;

            case R.string.title_tab_search_empty_room:
                return TabSearchEmptyRoomFragment.class;

            case R.string.title_tab_search_subject:
                return TabSearchSubjectFragment2.class;

            case R.string.title_tab_schedule:
                return UnivScheduleFragment.class;

            case R.string.title_tab_score:
                return ScoreFragment.class;

            case R.string.title_tab_transport:
                return TabTransportFragment.class;

            default:
                return null;
        }
    }

    public static int getPageResByClass(Class<? extends Fragment> fragmentClass) {

        /*
        switch (fragmentClass.getSimpleName()) {
            case "TabHomeFragment":
                return R.string.title_section0_home;
        }
        */

        if (fragmentClass.equals(TabHomeFragment.class))
            return R.string.title_section0_home;

        else if (fragmentClass.equals(TabAnnounceFragment.class))
            return R.string.title_tab_announce;

        else if (fragmentClass.equals(TabRestaurantFragment.class))
            return R.string.title_tab_restaurant;

        else if (fragmentClass.equals(TabBookSearchFragment.class))
            return R.string.title_tab_book_search;

        else if (fragmentClass.equals(TabLibrarySeatFragment.class))
            return R.string.title_tab_library_seat;

        else if (fragmentClass.equals(TabMapFragment.class))
            return R.string.title_tab_map;
        else if (fragmentClass.equals(TabPhoneFragment.class))
            return R.string.title_tab_phone;

        else if (fragmentClass.equals(TabTimeTableFragment2.class))
            return R.string.title_tab_timetable;

        else if (fragmentClass.equals(TabSearchEmptyRoomFragment.class))
            return R.string.title_tab_search_empty_room;

        else if (fragmentClass.equals(TabSearchSubjectFragment2.class))
            return R.string.title_tab_search_subject;

        else if (fragmentClass.equals(UnivScheduleFragment.class))
            return R.string.title_tab_schedule;

        else if (fragmentClass.equals(ScoreFragment.class))
            return R.string.title_tab_score;

        else if (fragmentClass.equals(TabTransportFragment.class))
            return R.string.title_tab_transport;

        else
            return RESOURCE_NOT_EXIST;
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

    public static void startActivityWithScaleUp(Activity activity, Intent intent, View v){
        ActivityCompat.startActivity(activity, intent, ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
    }

    public static void showInternetConnectionErrorToast(Context context, boolean isVisible) {
        showToast(context, R.string.error_internet, isVisible);
    }

    public static void showToast(Context context, CharSequence msg, boolean isVisible) {
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

    public static void showErrorToast(Context context, Exception e, boolean isVisible) {
        if (context != null) {
            showToast(context, context.getText(R.string.error_occur) + " : " + e.getMessage(), isVisible);
        }
    }

    /**
     * 공지사항 알리미 서비스를 시작/정지한다.
     */
    public static void startOrStopServiceAnnounce(Context context) {
        /*boolean isServiceEnable = PrefUtil.getInstance(context).get(context.getString(R.string.pref_key_check_anounce_service), true);
        Intent service = new Intent(context, ServiceForAnnounce.class);

        if (isServiceEnable) {
            context.startService(service);
        } else {
            context.stopService(service);
        }

        return isServiceEnable;
        */
       // return true;
    }

    /**
     * 기본 메시지가 <b>R.string.progress_while_updating</b>인 <br>
     * ProgressDialog 를 생성한다.
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
     * @param msg                  dialog 에 나타날 message
     * @param cancelButtonListener 취소 버튼을 눌렀을 때, 불릴 callback
     */
    public static MaterialDialog getProgressDialog(Context context, boolean isHorizontal, CharSequence msg, final DialogInterface.OnClickListener cancelButtonListener) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .content(msg)
                .progress(!isHorizontal, 100, true)
                .negativeText(android.R.string.cancel);

        if (cancelButtonListener != null)
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    cancelButtonListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                }
            });


        return builder.build();
    }

    /**
     * 열려진 DB를 모두 닫는다.
     */
    public static void closeAllDatabase(Context context) {
        /*
        if (PhoneNumberDB.isOpen())
            PhoneNumberDB.getInstance(context).close();
            */
    }

    /**
     * 현재 설정된 {@link AppUtil.AppTheme} 값에 따라 테마를 적용함. <br>
     * 반드시 activity의 onCreate()실행 처음에 불려야 한다.
     */
    public static void applyTheme(Context appContext) {
        if (theme == null) {
            theme = AppTheme.values()[PrefUtil.getInstance(appContext).get(PrefUtil.KEY_THEME, 0)];
        }

        appContext.setTheme(theme.styleId);
    }

    public static int getColor(int index) {
        switch (index % 17) {
            case 0:
            default:
                return R.color.red_yellow;
            case 1:
                return R.color.light_blue;
            case 2:
                return R.color.material_light_blue_700;
            case 3:
                return R.color.purple;
            case 4:
                return R.color.green;
            case 5:
                return R.color.gray_blue;
            case 6:
                return R.color.material_blue_grey_400;
            case 7:
                return R.color.material_green_700;
            case 8:
                return R.color.material_deep_teal_500;
            case 9:
                return R.color.material_blue_grey_200;
            case 10:
                return R.color.material_deep_teal_200;
            case 11:
                return R.color.material_grey_600;
            case 12:
                return R.color.material_red_200;
            case 13:
                return R.color.material_light_blue_200;
            case 14:
                return R.color.material_indigo_200;
            case 15:
                return R.color.material_green_200;
            case 16:
                return R.color.material_green_300;
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
            ViewGroup viewGroup = (ViewGroup) view;

            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                unbindDrawables(viewGroup.getChildAt(i));
            }

            if (view instanceof AdapterView || view instanceof AdapterViewCompat)
                return;

            viewGroup.removeAllViews();
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
        int sizeInfoMasked = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (sizeInfoMasked) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return true;
            default:
                return false;
        }
    }

}
