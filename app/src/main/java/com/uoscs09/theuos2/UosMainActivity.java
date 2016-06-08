package com.uoscs09.theuos2;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.common.BackPressCloseHandler;
import com.uoscs09.theuos2.setting.SettingActivity;
import com.uoscs09.theuos2.tab.map.GoogleMapActivity;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.PrefHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;

/**
 * Main Activity, ViewPager 가 존재한다.
 */
@SuppressWarnings("ConstantConditions")
public class UosMainActivity extends BaseActivity {

    @BindView(R.id.activity_pager_viewpager)
    ViewPager mViewPager;
    private IndexPagerAdapter mPagerAdapter;
    private BackPressCloseHandler mBackCloseHandler;

    /**
     * 화면 순서를 나타내는 리스트
     */
    private ArrayList<Integer> mPageOrderList;

    @BindView(R.id.activity_uos_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.left_drawer)
    View mLeftDrawerLayout;
    @BindView(R.id.drawer_listview)
    RecyclerView mDrawerListView;
    private DrawerAdapter mDrawerAdapter;

    /**
     * ActionBar Toggle
     */
    private ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.toolbar_parent)
    AppBarLayout mToolBarParent;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    CoordinatorLayout mCoordinatorLayout;
    private CoordinatorLayout.Behavior mAppBarBehavior;

    private OnBackPressListener onBackPressListener = null;

    private static final int START_SETTING = 999;
    private static final String SAVED_TAB_NUM = "saved_tab_num";


    private void initValues() {

        AppUtil.initStaticValues();
        mPageOrderList = AppUtil.loadEnabledPageOrder(this);
        if (PrefHelper.Screens.isHomeEnable() || mPageOrderList.isEmpty()) {
            mPageOrderList.add(0, R.string.title_section0_home);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        // .detectAll().penaltyLog().penaltyDialog().build());
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
        // .penaltyLog().penaltyDeath().build());
        /* 호출 순서를 바꾸지 말 것 */
        initValues();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_uosmain);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCoordinatorLayout = ButterKnife.findById(this, R.id.activity_uos_coordinator);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mToolBarParent.getLayoutParams();
            mAppBarBehavior = params.getBehavior();
        }

        setSupportActionBar(mToolbar);

        initPager();
        initDrawer();
        /* 호출 순서를 바꾸지 말 것 */

        int tabNumber = getIntent().getIntExtra(SAVED_TAB_NUM, 0);
        if (savedInstanceState != null) {
            navigateItem(mPageOrderList.indexOf(savedInstanceState.getInt(SAVED_TAB_NUM)), false);

        } else if (tabNumber != 0) {
            navigateItem(mPageOrderList.indexOf(tabNumber), false);

        } else {
            navigateItem(0, false);

        }

        //AppUtil.startOrStopServiceAnnounce(getApplicationContext());
        mBackCloseHandler = new BackPressCloseHandler();

        //TODO test
        //startActivity(new Intent(this, TestActivity.class));
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "UOSMainActivity";
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_TAB_NUM, getCurrentPageId());
        super.onSaveInstanceState(outState);
    }

    /**
     * ViewPager 를 전환하는 메소드
     *
     * @param position    전환될 위치
     * @param isFromPager 메소드가 ViewPager 로 부터 호출되었는지 여부 <br>
     *                    loop 를 방지하는 역할을 한다.
     */
    void navigateItem(int position, boolean isFromPager) {
        if (position < 0) {
            navigateItem(1, isFromPager);
            return;
        }

        /*
        Fragment f = mPagerAdapter.getCurrentFragment();
        BaseTabFragment tabFragment;
        if (f != null && f instanceof BaseTabFragment) {
            tabFragment = (BaseTabFragment) f;
            //tabFragment.removeTabMenu();
            tabFragment.resetNestedScrollPosition();
        }
        */

        resetAppBar();

        if (!isFromPager) {
            mViewPager.setCurrentItem(position, true);
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        }

        /*
        f = mPagerAdapter.getCurrentFragment();
        if (f != null && f instanceof BaseTabFragment && f != tabFragment) {
            tabFragment = (BaseTabFragment) f;
            tabFragment.addTabMenu();
        }
        */

        int res = mPageOrderList.get(position);
        if (res != -1) {
            getSupportActionBar().setTitle(res);
            // getSupportActionBar().setIcon(AppUtil.getPageIcon(res));
        }

        mDrawerAdapter.putSelected(position);

        mDrawerListView.scrollToPosition(position);

    }

    private void initDrawer() {
        @SuppressWarnings("unchecked")
        ArrayList<Integer> list = (ArrayList<Integer>) mPageOrderList.clone();
        list.add(R.string.setting);
        list.add(R.string.action_exit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        initDrawerDetail();

        // AppUtil.getStyledValue(this,R.attr.menu_ic_navigation_drawer)
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerToggle.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerToggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mDrawerToggle.onDrawerStateChanged(newState);
            }
        });
    }

    private void initDrawerDetail() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDrawerListView.setLayoutManager(layoutManager);
        mDrawerListView.setAdapter(mDrawerAdapter = new DrawerAdapter());
    }

    @OnClick(R.id.drawer_btn_setting)
    void toSetting() {
        mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        startSettingActivity();
    }

    @OnClick(R.id.drawer_btn_map)
    void toMap(View v) {
        AppUtil.startActivityWithScaleUp(UosMainActivity.this, new Intent(this, GoogleMapActivity.class), v);
    }

    @OnClick(R.id.drawer_btn_exit)
    void goExit() {
        mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        AppUtil.exit(this);
    }

    private void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(mLeftDrawerLayout))
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        else
            mDrawerLayout.openDrawer(mLeftDrawerLayout);
    }

    private void initPager() {
        mPagerAdapter = new IndexPagerAdapter(getSupportFragmentManager(), mPageOrderList, this);

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                navigateItem(position, true);
            }
        });
        //mViewPager.setOffscreenPageLimit(mPageOrderList.size() >= 6 ? 6 : mPageOrderList.size());
        /*
        switch (AppUtil.theme) {
            case BlackAndWhite:
                mViewPager.setPageTransformer(true, new PagerTransformer(2));
                break;
            case White:
                mViewPager.setPageTransformer(true, new PagerTransformer(1));
                break;
            case LightBlue:
                break;
            case Black:
            default:
                mViewPager.setPageTransformer(true, new PagerTransformer(0));
                break;
        }
        */

    }

    public ViewGroup getToolbarParent() {
        return mToolBarParent;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


    private void resetAppBar() {
        if (mAppBarBehavior != null) {
            //noinspection unchecked
            mAppBarBehavior.onNestedFling(mCoordinatorLayout, mToolBarParent, null, 0, -1000, true);
        }
    }

    /**
     * SettingActivity 를 시작한다.
     */
    void startSettingActivity() {
        startActivityForResult(new Intent(this, SettingActivity.class), START_SETTING);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 현재 페이지의 인덱스를 얻는다.
     */
    private int getCurrentPageIndex() {
        return mViewPager.getCurrentItem();
    }

    /**
     * 현재 페이지의 id를 얻는다.
     */
    private int getCurrentPageId() {
        return mPageOrderList.get(getCurrentPageIndex());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDrawerToggle.isDrawerIndicatorEnabled());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDrawerToggle.isDrawerIndicatorEnabled());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawerLayout);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!isDrawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                openOrCloseDrawer();
                return true;
            default:
                return false;
        }
    }

    /**
     * 새로운 인텐트를 받을때 그것을 검사해서 <br>
     * FinishSelf = true 이면 어플리케이션 종료
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("FinishSelf", false)) {
            // TODO 이곳에서 종료 전 처리를 한다.
            // AppUtil.clearApplicationFile(getCacheDir());
            // AppUtil.clearApplicationFile(getExternalCacheDir());
            finish();
            overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
            // 지정 시간 후 모든 스레드 종료

            new Handler().postDelayed(() -> android.os.Process.killProcess(android.os.Process.myPid()), 300);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        mBackCloseHandler = null;
        mDrawerListView = null;
        mDrawerToggle = null;
        mPageOrderList = null;
        mPagerAdapter = null;
        mViewPager = null;
        mLeftDrawerLayout = null;
        mDrawerLayout = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void onBackPressed() {
        if (onBackPressListener != null && onBackPressListener.onBackPress())
            return;

        if (AppUtil.isScreenSizeSmall(this) && mDrawerLayout.isDrawerOpen(mLeftDrawerLayout)) {
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);

        }/* else if (PrefUtil.getInstance(getApplicationContext()).get(PrefUtil.KEY_HOME, true)) {
            if (getCurrentPageIndex() == 0) {
                doBack();
            }

            navigateItem(0, false);

        } */ else {
            doBack();
        }
    }

    private void doBack() {
        if (mBackCloseHandler.onBackPressed()) {
            AppUtil.exit(this);
        } else {
            AppUtil.showToast(this, R.string.before_finish, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // activity 를 재시작함
        // XXX potential memory leak!!!!!
        if (resultCode == AppUtil.RELAUNCH_ACTIVITY) {
            //mDrawerLayout.closeDrawer(mLeftDrawerLayout);
            //recreate();

            finish();
            overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);

            startActivity(getIntent().putExtra(SAVED_TAB_NUM, getCurrentPageId()));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (!PrefHelper.Screens.isHomeEnable()) {
                    openOrCloseDrawer();
                } else if (getCurrentPageIndex() != 0) {
                    openOrCloseDrawer();
                }
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }

    }

    public interface OnBackPressListener {
        boolean onBackPress();
    }

    public void setOnBackPressListener(OnBackPressListener l) {
        this.onBackPressListener = l;
    }
/*
    public class PagerTransformer implements ViewPager.PageTransformer {
        private final int i;

        public PagerTransformer(int i) {
            this.i = i;
        }

        @Override
        public void transformPage(View arg0, float arg1) {
            if(getCurrentPageIndex() < 1)
                transformDepth(arg0, arg1);

            switch (i) {
                case 1:
                    transfromZoom(arg0, arg1);
                    break;
                case 2:
                    transformDepth(arg0, arg1);
                    break;
                case 3:
                    break;
                default:
                    arg0.setRotationY(arg1 * -30);
                    break;
            }

        }

        private void transformZoom(View view, float position) {
            final float MIN_SCALE = 0.85f;
            final float MIN_ALPHA = 0.5f;
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as
                // well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }

        private void transformDepth(View view, float position) {
            final float MIN_SCALE = 0.75f;
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
                        * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
*/

    private class DrawerAdapter extends ListRecyclerAdapter<Integer, DrawerViewHolder> {
        private final int mDefaultTextColor, mSelectedTextColor;
        private final PorterDuffColorFilter mColorFilter;
        private int mCurrentSelection = 0;
        //private final boolean isHomeEnable;

        public DrawerAdapter() {
            super(mPageOrderList, new ListRecyclerUtil.InnerClassViewHolderFactory<>(UosMainActivity.this, DrawerViewHolder.class, R.layout.list_layout_drawer));
            mDefaultTextColor = AppUtil.getAttrColor(UosMainActivity.this, R.attr.colorControlNormal);
            mSelectedTextColor = AppUtil.getAttrColor(UosMainActivity.this, R.attr.color_primary_text);
            mColorFilter = new PorterDuffColorFilter(mSelectedTextColor, PorterDuff.Mode.SRC_IN);
            // isHomeEnable = PrefHelper.isHomeEnable();
        }

        @Override
        public void onBindViewHolder(DrawerViewHolder holder, int position) {
            int titleStringId = mPageOrderList.get(position);

            Drawable drawable = ImageUtil.getPageIcon(holder.itemView.getContext(), titleStringId);
            holder.img.setImageDrawable(drawable);
            holder.text.setText(titleStringId);

            // 표시해야할 아이템이 선택된 상태라면 특별한 컬러로 하이라이트 시켜주고
            // 아니면 일반 상태로 표시한다.
            if (mCurrentSelection == position) {
                if (titleStringId != R.string.title_section0_home)
                    holder.img.setColorFilter(mColorFilter);
                else
                    holder.img.setColorFilter(null);
                holder.text.setTextColor(mSelectedTextColor);

            } else {
                holder.img.setColorFilter(null);
                holder.text.setTextColor(mDefaultTextColor);

            }
        }

        @Override
        public int getItemCount() {
            return mPageOrderList.size();
        }

        /**
         * position 의 아이템을 선택된 상태로 만든다.
         */
        void putSelected(int position) {
            notifyItemChanged(mCurrentSelection);

            mCurrentSelection = position;

            notifyItemChanged(position);

        }

    }

    class DrawerViewHolder extends com.uoscs09.theuos2.base.ViewHolder<Integer> {
        @BindView(R.id.drawer_list_img)
        ImageView img;
        @BindView(R.id.drawer_list_text)
        TextView text;

        public DrawerViewHolder(View itemView) {
            super(itemView);
        }

        @OnClick(R.id.drawer_list_ripple)
        void onViewClick() {
            navigateItem(getLayoutPosition(), false);
        }
    }
}
