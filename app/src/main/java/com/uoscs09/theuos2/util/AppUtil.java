package com.uoscs09.theuos2.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.TypedValue;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.TabHomeFragment;
import com.uoscs09.theuos2.UosMainActivity;
import com.uoscs09.theuos2.tab.announce.TabAnnounceFragment;
import com.uoscs09.theuos2.tab.booksearch.TabBookSearchFragment;
import com.uoscs09.theuos2.tab.emptyroom.TabSearchEmptyRoomFragment;
import com.uoscs09.theuos2.tab.libraryseat.TabLibrarySeatFragment;
import com.uoscs09.theuos2.tab.restaurant.TabRestaurantFragment;
import com.uoscs09.theuos2.tab.schedule.UnivScheduleFragment;
import com.uoscs09.theuos2.tab.score.TabWiseScoreFragment;
import com.uoscs09.theuos2.tab.subject.TabSearchSubjectFragment2;
import com.uoscs09.theuos2.tab.timetable.TabTimeTableFragment;

import java.util.ArrayList;
import java.util.Collections;

import mj.android.utils.common.CommonUtils;

public class AppUtil {
    public static final int RELAUNCH_ACTIVITY = 6565;

    private static AppTheme theme;

    private static Context context;

    public static void init(Context context) {
        if (context != null)
            AppUtil.context = context.getApplicationContext();
    }

    public static Context context() {
        return context;
    }

    public static AppTheme theme() {
        return theme;
    }

    public static void setTheme(AppTheme theme) {
        AppUtil.theme = theme;
    }

    /**
     * 어플리케이션의 테마를 나타내는 enum<br>
     * <li><b>White</b> : 흰색/회색 계통으로 구성된 테마</li><br>
     * <li><b>BlackAndWhite</b> : 안드로이드 기본 액션바 테마, 액션바는 검은색, 일반 배경은 하얀색 계통</li><br>
     * <li><b>Black</b> : 검은색 계열로 구성된 테마</li><br>
     */
    public enum AppTheme {
        /**
         * white textColorTheme
         */
        White(R.style.AppTheme_Style_White),
        /**
         * android default, 액션바는 검은색, 일반 배경은 하얀색
         */
        BlackAndWhite(R.style.AppTheme_Style_BlackAndWhite),
        /**
         * black textColorTheme
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

    public static void initStaticValues() {
        int v = PrefHelper.Screens.getAppTheme().ordinal();
        AppTheme[] vals = AppTheme.values();
        if (v >= vals.length) {
            v = 0;
            PrefHelper.Screens.putAppTheme(v);
        }
        AppUtil.theme = vals[v];
    }

    // todo Page -> TabInfo
    public enum TabInfo {
        Home(0, TabHomeFragment.class, R.string.title_section0_home, R.drawable.ic_launcher, R.drawable.ic_launcher),
        Announce(1, TabAnnounceFragment.class, R.string.title_tab_announce, R.attr.theme_ic_action_action_view_list, R.drawable.ic_action_action_view_list_white),
        Schedule(2, UnivScheduleFragment.class, R.string.title_tab_schedule, R.attr.theme_ic_action_calendar, R.drawable.ic_action_calendar_white),
        Restaurant(3, TabRestaurantFragment.class, R.string.title_tab_restaurant, R.attr.theme_ic_action_maps_local_restaurant, R.drawable.ic_action_maps_local_restaurant_white),
        BookSearch(4, TabBookSearchFragment.class, R.string.title_tab_book_search, R.attr.theme_ic_action_book_search, R.drawable.ic_action_book_search_white),
        LibrarySeat(5, TabLibrarySeatFragment.class, R.string.title_tab_library_seat, R.attr.theme_ic_action_book_opened, R.drawable.ic_action_book_opened_white),
        TimeTable(6, TabTimeTableFragment.class, R.string.title_tab_timetable, R.attr.theme_ic_action_timetable, R.drawable.ic_action_timetable_white),

        EmptyRoom(7, TabSearchEmptyRoomFragment.class, R.string.title_tab_search_empty_room, R.attr.theme_ic_action_action_search, R.drawable.ic_action_action_search_white),
        SearchSubject(8, TabSearchSubjectFragment2.class, R.string.title_tab_search_subject, R.attr.theme_ic_action_content_content_paste, R.drawable.ic_action_content_content_paste_white),
        WiseScore(9, TabWiseScoreFragment.class, R.string.title_tab_wise_score, R.attr.theme_ic_action_content_content_copy, R.drawable.ic_action_content_content_copy_white),
        //BuildingRoom(10, TabBuildingRoomFragment.class, R.string.title_tab_building_classroom, R.attr.theme_ic_action_action_about, R.drawable.ic_action_action_about_white),

        /* disable
        Phone(TabPhoneFragment.class, R.string.title_tab_phone, R.attr.theme_ic_action_communication_call,R.drawable.ic_action_communication_call_white),
        CheckCourseEval(ScoreFragment.class, R.string.title_tab_score, R.attr.theme_ic_action_content_content_copy),
        Transport(TabTransportFragment.class, R.string.title_tab_transport, R.attr.theme_ic_action_maps_directions,R.drawable.ic_action_maps_directions_white);
        Map(TabMapFragment.class, R.string.title_tab_map, R.attr.theme_ic_action_maps_place,R.drawable.ic_action_maps_place_white),

        */

        //ETC(,R.drawable.ic_action_navigation_check_white)
        //Exit(,R.drawable.ic_action_navigation_close_white)
        Setting(98, null, R.string.setting, R.attr.theme_ic_action_action_settings, R.drawable.ic_action_action_settings_white);


        private static final String TAB_ORDER_ = "tab_order_";
        private static final String TAB_ENABLE_ = "tab_enable_";

        public final int defaultOrder;
        public final Class<? extends Fragment> tabClass;
        @StringRes
        public final int titleResId;
        final int iconRes;
        @DrawableRes
        final int lightIconRes;

        private boolean isEnable = true;


        TabInfo(int defaultOrder, Class<? extends Fragment> tabClass, @StringRes int titleId, int iconRes, int lightIconRes) {
            this.defaultOrder = defaultOrder;
            this.tabClass = tabClass;
            this.titleResId = titleId;
            this.iconRes = iconRes;
            this.lightIconRes = lightIconRes;
        }


        @Nullable
        public Fragment getFragment() {
            try {
                return tabClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        @DrawableRes
        public int getIcon(Context context) {
            if (this.equals(Home))
                return iconRes;
            else
                return getAttrValue(context, iconRes);
        }

        @DrawableRes
        public int getLightIcon() {
            return lightIconRes;
        }

        public boolean recordedEnable() {
            PrefUtil pref = PrefUtil.getInstance(context);

            return pref.get(TAB_ENABLE_ + defaultOrder, true);
        }

        public int order() {
            PrefUtil pref = PrefUtil.getInstance(context);
            return pref.get(TAB_ORDER_ + defaultOrder, defaultOrder);
        }

        public void setEnable(boolean enable) {
            isEnable = enable;
        }

        public boolean isEnable() {
            return isEnable;
        }

        public static TabInfo find(@StringRes int titleId) {

            for (TabInfo tabInfo : TabInfo.values()) {
                if (tabInfo.titleResId == titleId) return tabInfo;
            }

            return null;
        }

        /**
         * load every TabInfo except Home(the uos, no. 0,), setting(no.98)
         */
        public static ArrayList<TabInfo> loadDefaultOrder() {
            ArrayList<TabInfo> list = new ArrayList<>();
            Collections.addAll(list, values());

            for (TabInfo tabInfo : list) {
                tabInfo.isEnable = true;
            }

            list.remove(0); // remove main(the uos)
            list.remove(list.size() - 1); //  remove setting

            return list;
        }


        /**
         * load ordered TabInfo list
         */
        public static ArrayList<TabInfo> loadEnabledTabOrderForSetting() {
            ArrayList<TabInfo> tabList = loadDefaultOrder();

            for (TabInfo tabInfo : tabList) {
                tabInfo.isEnable = tabInfo.recordedEnable();
            }
            Collections.sort(tabList, (o1, o2) -> intCompare(o1.order(), o2.order()));

            return tabList;
        }

        private static ArrayList<TabInfo> loadEnabledTabOrderInternal() {
            ArrayList<TabInfo> tabList = loadEnabledTabOrderForSetting();


            for (int i = tabList.size() - 1; i >= 0; i--) {
                if (!tabList.get(i).isEnable) tabList.remove(i);
            }

            return tabList;
        }
        /**
         * load ordered TabInfo list, with Home(the uos, no. 0,) if PrefHelper.Screens.isHomeEnable() == true
         */
        public static ArrayList<TabInfo> loadEnabledTabOrderForMain() {
            ArrayList<TabInfo> tabList = loadEnabledTabOrderInternal();

            if (PrefHelper.Screens.isHomeEnable()) tabList.add(0, Home);

            return tabList;
        }

        /**
         * load ordered TabInfo list, with Setting, Home(the uos, no. 0,) if PrefHelper.Screens.isHomeEnable() == true
         */
        public static ArrayList<TabInfo> loadEnabledTabOrderForHome() {
            ArrayList<TabInfo> tabList = loadEnabledTabOrderInternal();
            tabList.add(Setting);

            return tabList;
        }

        public static void saveTabOrderList(ArrayList<TabInfo> list) {
            PrefUtil pref = PrefUtil.getInstance(context);
            for (int i = 0; i < list.size(); i++) {
                TabInfo tabInfo = list.get(i);

                pref.put(TAB_ORDER_ + tabInfo.defaultOrder, i);
                pref.put(TAB_ENABLE_ + tabInfo.defaultOrder, tabInfo.isEnable);
            }
        }

        private static int intCompare(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
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

    @ColorInt
    public static int getAttrColor(Context context, @AttrRes int attrColorId) {
        return ContextCompat.getColor(context, getAttrValue(context, attrColorId));
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
     * 인텐트를 통해 인터넷 페이지를 띄운다.
     *
     * @param webURL 접속하려는 페이지의 url
     * @return url이 설정된 intent
     */
    public static Intent getWebPageIntent(String webURL) {
        return CommonUtils.getWebPageIntent(webURL);
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

    public static void showErrorToast(Context context, Throwable e, boolean isVisible) {
        if (context != null) {
            showToast(context, context.getText(R.string.error_occur) + " : " + e.getMessage(), isVisible);
        }
        e.printStackTrace();
    }

    /**
     * 기본 메시지가 <b>R.string.progress_while_updating</b>인 <br>
     * 진행바가 원 모양인 ProgressDialog 를 생성한다.
     */
    public static MaterialDialog getProgressDialog(Context context) {
        return getProgressDialog(context, false, context.getString(R.string.progress_while_updating), null);
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
            builder.onNegative((dialog, which) -> cancelButtonListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE));

        return builder.build();
    }

    /**
     * 현재 설정된 {@link AppUtil.AppTheme} 값에 따라 테마를 적용함. <br>
     * 반드시 activity의 onCreate()실행 처음에 불려야 한다.
     */
    public static void applyTheme(Context appContext) {
        if (theme == null) {
            theme = PrefHelper.Screens.getAppTheme();
        }

        appContext.setTheme(theme.styleId);
    }

    public static int getOrderedColor(Context context, int index) {
        return ContextCompat.getColor(context, getOrderedColorRes(index));
    }

    @ColorRes
    public static int getOrderedColorRes(int index) {
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
                return R.color.material_deep_teal_500_1;
            case 9:
                return R.color.material_blue_grey_200;
            case 10:
                return R.color.material_deep_teal_200_1;
            case 11:
                return R.color.material_grey_600_;
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
     * 화면 크기를 판단한다.
     *
     * @return {@code true} - 화면 크기가
     * {@link Configuration#SCREENLAYOUT_SIZE_NORMAL} 이하 일 경우<br>
     * <br>
     * {@code false} - 그 외
     */
    public static boolean isScreenSizeSmall() {
        int sizeInfoMasked = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (sizeInfoMasked) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return true;
            default:
                return false;
        }
    }


    @PermissionChecker.PermissionResult
    public static boolean checkSelfPermissionCompat(Context context, @NonNull String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    // String...
    @PermissionChecker.PermissionResult
    public static boolean checkSelfPermissionCompat(Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
            return true;
        }
    }

    public static void requestPermissionsCompat(Activity activity, int requestCode, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            activity.requestPermissions(permissions, requestCode);
    }

    public static boolean checkPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkPermissionResultAndShowToastIfFailed(Context context, @NonNull String[] permissions, @NonNull int[] grantResults, String message) {
        boolean result = checkPermissionResult(permissions, grantResults);

        //"권한이 거절되어 계속 진행 할 수 없습니다."
        if (!result)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        return result;
    }

    public static boolean checkPermissionResultAndShowToastIfFailed(Context context, @NonNull String[] permissions, @NonNull int[] grantResults, @StringRes int res) {
        boolean result = checkPermissionResult(permissions, grantResults);

        //"권한이 거절되어 계속 진행 할 수 없습니다."
        if (!result)
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show();

        return result;
    }

}
