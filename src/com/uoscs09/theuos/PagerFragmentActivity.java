package com.uoscs09.theuos;

import java.util.ArrayList;

import android.R.color;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.uoscs09.theuos.common.BackPressCloseHandler;
import com.uoscs09.theuos.common.impl.BaseFragmentActivity;
import com.uoscs09.theuos.common.impl.SimpleTextViewAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.setting.SettingActivity;

/*TODO textColortheme 관련 설정을 코드가 아닌 xml(/value/style_xxx)에서 변경할 수 있도록 할 것*/
/** Main Activity, ViewPager가 존재한다. */
public class PagerFragmentActivity extends BaseFragmentActivity implements
		PagerInterface {
	/** ViewPager */
	private ViewPager mViewPager;
	/** ViewPager Adapter */
	private FragmentStatePagerAdapter mPagerAdapter;
	/** 뒤로 두번눌러 종료 */
	private BackPressCloseHandler mBackCloseHandler;
	/** 화면 순서를 나타내는 리스트 */
	private ArrayList<Integer> mPageOrderList;

	/** DrawerLayout */
	protected DrawerLayout drawerLayout;
	/** Drawer ListView */
	private ListView mDrawerListView;
	/** Drawer Toggle */
	private ActionBarDrawerToggle mDrawerToggle;

	private static final int START_SETTING = 999;
	private static final String SAVED_TAB_NUM = "saved_tab_num";

	private void initValues() {
		PrefUtil pref = PrefUtil.getInstance(this);
		AppUtil.initStaticValues(pref);
		mPageOrderList = AppUtil.loadPageOrder(this);
		if (pref.get(PrefUtil.KEY_HOME, true)) {
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

		setContentView(R.layout.activity_pager_and_drawer);
		initPager();
		initDrawer();
		/* 호출 순서를 바꾸지 말 것 */

		int tabNumber = getIntent().getIntExtra(SAVED_TAB_NUM, 0);
		if (savedInstanceState != null) {
			navigateItem(mPageOrderList.indexOf(savedInstanceState
					.getInt(SAVED_TAB_NUM)), false);
		} else if (tabNumber != 0) {
			navigateItem(mPageOrderList.indexOf(tabNumber), false);
		} else {
			navigateItem(0, false);
		}
		AppUtil.startOrStopServiceAnounce(getApplicationContext());
		mBackCloseHandler = new BackPressCloseHandler();
		System.gc();
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_TAB_NUM, getCurrentPageId());
		super.onSaveInstanceState(outState);
	}

	/**
	 * ViewPager를 전환하는 메소드
	 * 
	 * @param position
	 *            전환될 위치
	 * @param isFromPager
	 *            메소드가 ViewPager로 부터 호출되었는지 여부 <br>
	 *            loop를 방지하는 역할을 한다.
	 */
	protected void navigateItem(int position, boolean isFromPager) {
		if (position < 0) {
			navigateItem(1, isFromPager);
			return;
		}
		if (!isFromPager) {
			mViewPager.setCurrentItem(position, true);
			drawerLayout.closeDrawer(mDrawerListView);
		}
		mDrawerListView.setItemChecked(position, true);
		getActionBar().setTitle(mPagerAdapter.getPageTitle(position));
	}

	private void initDrawer() {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> list = (ArrayList<Integer>) mPageOrderList.clone();
		list.add(R.string.setting);
		list.add(R.string.action_exit);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		drawerLayout = (DrawerLayout) findViewById(R.id.activity_pager_drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.activity_pager_left_drawer);
		int drawerLayoutId;
		switch (AppUtil.theme) {
		case Black:
			drawerLayoutId = R.layout.drawer_list_item_dark;
			break;
		case BlackAndWhite:
			drawerLayoutId = R.layout.drawer_list_item_dark;
			break;
		case White:
		default:
			drawerLayoutId = R.layout.drawer_list_item;
			mDrawerListView.setBackgroundColor(getResources().getColor(
					color.background_light));
			break;
		}

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		final float density = getResources().getDisplayMetrics().density;
		int width = Math.round(30 * density);
		int height = Math.round(28 * density);
		AppTheme theme = AppUtil.theme == AppTheme.White ? AppTheme.White
				: AppTheme.Black;
		AppTheme iconTheme = AppUtil.theme == AppTheme.White ? AppTheme.White
				: AppTheme.Black;
		mDrawerListView.setAdapter(new SimpleTextViewAdapter.Builder(this,
				drawerLayoutId, list).setTheme(theme)
				.setDrawableTheme(iconTheme)
				.setDrawableBounds(new Rect(0, 0, width, height)).create());
		mDrawerListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int pos, long arg3) {
						int size = mPagerAdapter.getCount();
						if (pos < size)
							navigateItem(pos, false);
						else if (pos == size) {
							startSettingActivity();
							drawerLayout.closeDrawer(mDrawerListView);
						} else if (pos == size + 1) {
							AppUtil.exit(getApplicationContext());
						}
					}
				});

		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		drawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.app_name, /* "open drawer" description for accessibility */
		R.string.app_name /* "close drawer" description for accessibility */
		);
		drawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void initPager() {
		mPagerAdapter = new IndexPagerAdapter(getSupportFragmentManager(),
				mPageOrderList, this);
		mViewPager = (ViewPager) findViewById(R.id.activity_pager_viewpager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						navigateItem(position, true);
					}
				});
		if (AppUtil.test) {
			switch (AppUtil.theme) {
			case BlackAndWhite:
				mViewPager.setPageTransformer(true,
						new ZoomOutPageTransformer());
				break;
			case White:
				mViewPager.setPageTransformer(true, new DepthPageTransformer());
				break;
			default:
				mViewPager.setPageTransformer(true, new PagerTransformer());
				break;
			}
		}

	}

	/** SettingActivity를 시작한다. */
	protected void startSettingActivity() {
		startActivityForResult(new Intent(this, SettingActivity.class),
				START_SETTING);
	}

	/** 현재 페이지의 인덱스를 얻는다. */
	protected int getCurrentPageIndex() {
		return mViewPager.getCurrentItem();
	}

	/** 현재 페이지의 id를 얻는다. */
	protected int getCurrentPageId() {
		return mPageOrderList.get(getCurrentPageIndex());
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getActionBar().setDisplayShowCustomEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_exit:
			AppUtil.exit(this);
			return true;
		case R.id.setting:
			startSettingActivity();
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// AppUtil.clearApplicationFile(getCacheDir());
		// AppUtil.clearApplicationFile(getExternalCacheDir());
		AppUtil.closeAllDatabase(getApplicationContext());
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
			AppUtil.closeAllDatabase(this);
			finish();
			overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
			// 지정 시간 후 모든 스레드 종료
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			}, 300);
		}
	}

	@Override
	public void onBackPressed() {
		if (PrefUtil.getInstance(getApplicationContext()).get(
				PrefUtil.KEY_HOME, true)) {
			if (getCurrentPageIndex() == 0) {
				doBack();
			}
			navigateItem(0, false);
		} else {
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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// activity를 재시작함
		// XXX potential memory leak!!!!!
		if (resultCode == AppUtil.RELAUNCH_ACTIVITY) {
			finish();
			overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
			startActivity(getIntent().putExtra(SAVED_TAB_NUM,
					getCurrentPageId()));
		}
		mDrawerListView.setItemChecked(getCurrentPageIndex(), true);
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (!PrefUtil.getInstance(this).get(PrefUtil.KEY_HOME, true)) {
				openOrCloseDrawer();
			} else if (getCurrentPageIndex() != 0) {
				openOrCloseDrawer();
			}
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	private void openOrCloseDrawer() {
		if (drawerLayout.isDrawerOpen(mDrawerListView))
			drawerLayout.closeDrawer(mDrawerListView);
		else
			drawerLayout.openDrawer(mDrawerListView);
	}

	@Override
	public void sendCommand(Type type, Object data) {
		switch (type) {
		case PAGE:
			try {
				navigateItem(mPageOrderList.indexOf(Integer.valueOf(data
						.toString())), false);
			} catch (Exception e) {
			}
			break;
		case SETTING:
			startSettingActivity();
			break;
		default:
			break;
		}
	}

	public class PagerTransformer implements ViewPager.PageTransformer {
		@Override
		public void transformPage(View arg0, float arg1) {
			arg0.setRotationY(arg1 * -30);
		}
	}

	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
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
	}

	public class DepthPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
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
}
