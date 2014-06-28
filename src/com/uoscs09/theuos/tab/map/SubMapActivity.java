package com.uoscs09.theuos.tab.map;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.BaseFragmentActivity;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.StringUtil;

public class SubMapActivity extends BaseFragmentActivity implements
		LocationListener {
	private GoogleMap googleMap;
	private boolean isInit = true;
	private AlertDialog dialog, locationSelector;
	private LocationManager locationManager;
	private Location location;
	private int buildingNo;

	private enum CASE_WELFARE {
		CASH, BANK, COPY, PRINT, SEARCH, REST, ELEVATOR, HEALTH_CENTER, POST, RESTAURANT, FASTFOOD, STAND, EYE, BOOK, WRITING, SOUVENIR, HEALTH, TENNIS
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_map_googlemap);

		if (!initMap()) {
			AppUtil.showToast(getApplicationContext(),
					R.string.tab_map_submap_device_without_googlemap, true);
			finish();
			return;
		}

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.action_map);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		int spinnerLayout = android.R.layout.simple_spinner_item;
		if (AppUtil.theme == AppTheme.BlackAndWhite) {
			spinnerLayout = R.layout.spinner_simple_item_dark;
		}
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.buildings_univ, spinnerLayout);
		if (AppUtil.theme == AppTheme.BlackAndWhite) {
			spinnerAdapter
					.setDropDownViewResource(R.layout.spinner_simple_dropdown_item_dark);
		} else {
			spinnerAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(spinnerAdapter,
				new OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						if (isInit) {
							isInit = false;
							return true;
						}
						googleMap.clear();
						moveCameraPositionAt(itemPosition + 1);
						setCameraMapMarkerAt(
								itemPosition + 1,
								getResources().getStringArray(
										R.array.buildings_univ)[itemPosition]);
						return true;
					}
				});
		ListView listView = new ListView(this);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.buildings_univ)));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (location == null) {
					AppUtil.showToast(getBaseContext(),
							R.string.tab_map_submap_do_not_locate, true);
				} else {
					showRoute(
							new LatLng(location.getLatitude(), location
									.getLongitude()),
							getLatLngByBuildingNumber(arg2 + 1));
					locationSelector.dismiss();
				}
			}
		});
		locationSelector = new AlertDialog.Builder(this).setView(listView)
				.setTitle(R.string.tab_map_submap_select_dest)
				.setMessage(R.string.tab_map_submap_select_building).create();
		moveCameraPositionAt(0);
		setCameraMapMarkerAt(0, getString(R.string.univ));
	}

	private Location getCurrentLocation() {
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		return location;
	}

	private void showRoute(LatLng start, LatLng dest) {
		StringBuilder sb = new StringBuilder(
				"http://maps.google.com/maps?saddr=");
		sb.append(start.latitude).append(',').append(start.longitude)
				.append("&daddr=").append(dest.latitude).append(',')
				.append(dest.longitude);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(sb.toString()));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName("com.google.android.apps.maps",
				"com.google.android.maps.MapsActivity");
		startActivity(intent);
	}

	private boolean initMap() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.tab_map_submap)).getMap();
			if (googleMap == null) {
				return false;
			}
			googleMap.setBuildingsEnabled(true);
			googleMap.setMyLocationEnabled(true);
			googleMap
					.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

						@Override
						public void onMyLocationChange(Location arg0) {
							location = arg0;
						}
					});
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			if (provider == null) { // 위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동합니다
				new AlertDialog.Builder(this)
						.setTitle("위치서비스 동의")
						.setNeutralButton("이동",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										startActivityForResult(
												new Intent(
														android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
												0);
									}
								})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										finish();
									}
								}).show();

			} else { // 위치 정보 설정이 되어 있으면 현재위치를 받아옵니다
				locationManager.requestLocationUpdates(provider, 1, 1, this);
			}
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 후
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			if (provider == null) {// 사용자가 위치설정동의 안했을때 종료
				finish();
			} else {// 사용자가 위치설정 동의 했을때
				locationManager.requestLocationUpdates(provider, 1L, 2F, this);
				initMap();
			}
			break;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		buildingNo = getIntent().getIntExtra("building", -1);
		if (buildingNo != -1) {
			isInit = false;
			getActionBar().setSelectedNavigationItem(buildingNo - 1);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					moveCameraPositionAt(buildingNo);
					buildingNo = -1;
				}
			}, 1000);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			getMenuInflater().inflate(R.menu.tab_map_googlemap_menu_dark, menu);
			break;
		case White:
		default:
			getMenuInflater().inflate(R.menu.tab_map_googlemap_menu, menu);
			break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!super.onKeyDown(keyCode, event)) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				selectWelfareBuildingMenu();
				return true;
			default:
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_search:
			selectWelfareBuildingMenu();
			return true;
		case R.id.action_direction:
			if (location == null)
				location = getCurrentLocation();
			locationSelector.show();
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onDestroy() {
		googleMap.clear();
		googleMap = null;
		super.onDestroy();
	}

	private void selectWelfareBuildingMenu() {
		if (dialog == null) {
			dialog = new AlertDialog.Builder(this)
					.setTitle("복지시설")
					.setItems(R.array.tab_map_submap_buildings_welfare,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									googleMap.clear();
									String locationName = getResources()
											.getStringArray(
													R.array.tab_map_submap_buildings_welfare)[item];
									// TODO 리팩토링이 시급함 state
									switch (CASE_WELFARE.values()[item]) {
									case BANK:
										setCameraMapMarkerAt(7, locationName);
										break;
									case COPY:
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(21, locationName);
										break;
									case PRINT:
										setCameraMapMarkerAt(5, locationName
												+ StringUtil.NEW_LINE
												+ "2층 PC실(533호)");
										setCameraMapMarkerAt(15, locationName
												+ StringUtil.NEW_LINE
												+ "전자도서관(입금가능), 2층 227호");
										setCameraMapMarkerAt(20, locationName
												+ StringUtil.NEW_LINE
												+ "4층(입금가능), 5층, 6층(도서관)");
										setCameraMapMarkerAt(21, locationName
												+ StringUtil.NEW_LINE
												+ "1층, 2층, 3층(입금가능), 4층");
										setCameraMapMarkerAt(33, locationName
												+ StringUtil.NEW_LINE
												+ "3층 경영도서관, 4층 PC실(입금가능)");
										setCameraMapMarkerAt(34, locationName
												+ StringUtil.NEW_LINE + "1층 로비");
										break;
									case CASH:
										setCameraMapMarkerAt(7, locationName);
										setCameraMapMarkerAt(8, locationName);
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(15, locationName);
										setCameraMapMarkerAt(21, locationName);
										break;
									case ELEVATOR:
										setCameraMapMarkerAt(3, locationName);
										setCameraMapMarkerAt(5, locationName);
										setCameraMapMarkerAt(6, locationName);
										setCameraMapMarkerAt(7, locationName);
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(14, locationName);
										setCameraMapMarkerAt(15, locationName);
										setCameraMapMarkerAt(19, locationName);
										setCameraMapMarkerAt(20, locationName);
										setCameraMapMarkerAt(21, locationName);
										setCameraMapMarkerAt(22, locationName);
										setCameraMapMarkerAt(34, locationName);
										break;
									case HEALTH:
										setCameraMapMarkerAt(17, locationName);
										setCameraMapMarkerAt(22, locationName);
									case TENNIS:
										setCameraMapMarkerAt(32, locationName);
										break;
									case WRITING:
									case BOOK:
									case FASTFOOD:
									case POST:
									case SOUVENIR:
									case EYE:
									case HEALTH_CENTER:
										setCameraMapMarkerAt(12, locationName);
										break;
									case REST:
										setCameraMapMarkerAt(3, locationName);
										setCameraMapMarkerAt(5, locationName);
										setCameraMapMarkerAt(6, locationName);
										setCameraMapMarkerAt(8, locationName);
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(15, locationName);
										setCameraMapMarkerAt(16, locationName);
										setCameraMapMarkerAt(19, locationName);
										setCameraMapMarkerAt(20, locationName);
										setCameraMapMarkerAt(21, locationName);
										setCameraMapMarkerAt(22, locationName);
										setCameraMapMarkerAt(33, locationName);
										break;
									case RESTAURANT:
										setCameraMapMarkerAt(7, locationName);
										setCameraMapMarkerAt(8, locationName);
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(34, locationName);
										break;
									case SEARCH:
										setCameraMapMarkerAt(3, locationName);
										setCameraMapMarkerAt(4, locationName);
										setCameraMapMarkerAt(8, locationName);
										setCameraMapMarkerAt(14, locationName);
										setCameraMapMarkerAt(19, locationName);
										setCameraMapMarkerAt(21, locationName);
										setCameraMapMarkerAt(22, locationName);
										break;
									case STAND:
										setCameraMapMarkerAt(12, locationName);
										setCameraMapMarkerAt(21, locationName);
										setCameraMapMarkerAt(33, locationName);
										break;
									default:
										break;
									}
									dialog.dismiss();
								}
							}).create();
		}
		dialog.show();
	}

	private LatLng getLatLngByBuildingNumber(int item) {
		LatLng latLng;
		switch (item) {
		case 1:
			latLng = new LatLng(37.583594, 127.056568);
			break;
		case 2:
			latLng = new LatLng(37.58482, 127.058488);
			break;
		case 3:
			latLng = new LatLng(37.583817, 127.057877);
			break;
		case 4:
			latLng = new LatLng(37.584593, 127.06068);
			break;
		case 5:
			latLng = new LatLng(37.583759, 127.061098);
			break;
		case 6:
			latLng = new LatLng(37.58468, 127.059655);
			break;
		case 7:
			latLng = new LatLng(37.584765, 127.057708);
			break;
		case 8:
			latLng = new LatLng(37.582512, 127.059121);
			break;
		case 9:
			latLng = new LatLng(37.583953, 127.055685);
			break;
		case 10:
			latLng = new LatLng(37.582899, 127.056619);
			break;
		case 11:
			latLng = new LatLng(37.584703, 127.059041);
			break;
		case 12:
			latLng = new LatLng(37.583702, 127.060073);
			break;
		case 13:
			latLng = new LatLng(37.58492, 127.060736);
			break;
		case 14:
			latLng = new LatLng(37.585315, 127.057547);
			break;
		case 15:
			latLng = new LatLng(37.58312, 127.058681);
			break;
		case 16:
			latLng = new LatLng(37.584093, 127.056182);
			break;
		case 17:
			latLng = new LatLng(37.58442, 127.055543);
			break;
		case 18:
			latLng = new LatLng(37.582841, 127.057662);
			break;
		case 19:
			latLng = new LatLng(37.582896, 127.060771);
			break;
		case 20:
			latLng = new LatLng(37.582008, 127.05679);
			break;
		case 21:
			latLng = new LatLng(37.584809, 127.062131);
			break;
		case 22:
			latLng = new LatLng(37.585409, 127.062823);
			break;
		case 23:
			latLng = new LatLng(37.582295, 127.057818);
			break;
		case 24:
			latLng = new LatLng(37.582376, 127.057432);
			break;
		case 25:
			latLng = new LatLng(37.582531, 127.060124);
			break;
		case 26:
			latLng = new LatLng(37.582431, 127.060767);
			break;
		case 27:
			latLng = new LatLng(37.583009, 127.059765);
			break;
		case 28:
			latLng = new LatLng(37.585262, 127.056653);
			break;
		case 29:
			latLng = new LatLng(37.583124, 127.056932);
			break;
		case 30:
			latLng = new LatLng(37.583447, 127.054974);
			break;
		case 31:
			latLng = new LatLng(37.585215, 127.060939);
			break;
		case 32:
			latLng = new LatLng(37.582401, 127.056546);
			break;
		case 33:
			latLng = new LatLng(37.58441, 127.057145);
			break;
		case 34:
			latLng = new LatLng(37.584342, 127.063399);
			break;
		case 35:
			// 신본관
			// latLng = new LatLng(37.583462, 127.056927);
			latLng = new LatLng(37.584180, 127.060765);
			break;
		default:
			latLng = new LatLng(37.583921, 127.059011);
		}
		return latLng;
	}

	private void moveCameraPositionAt(int itemPosition) {
		LatLng latLng = getLatLngByBuildingNumber(itemPosition);
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
	}

	private void setCameraMapMarkerAt(int itemPosition, String locationName) {
		LatLng latLng = getLatLngByBuildingNumber(itemPosition);
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
		googleMap.addMarker(new MarkerOptions().position(latLng)
				.title(locationName).visible(true));
	}
}
