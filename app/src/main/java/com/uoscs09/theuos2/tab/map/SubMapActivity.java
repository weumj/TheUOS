package com.uoscs09.theuos2.tab.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;

public class SubMapActivity extends BaseActivity implements LocationListener {
    @ReleaseWhenDestroy
    private GoogleMap googleMap;
    private boolean isInit = true;
    @ReleaseWhenDestroy
    private AlertDialog dialog, locationSelector;
    @ReleaseWhenDestroy
    private LocationManager locationManager;
    @ReleaseWhenDestroy
    private Location location;
    @ReleaseWhenDestroy
    private AppCompatSpinner mLocationSelectSpinner;

    private int buildingNo;

    private static final int REQUEST_LOCATION_SOURCE_SETTINGS = 9643;

    private enum CASE_WELFARE {
        CASH, BANK, COPY, PRINT, SEARCH, REST, ELEVATOR, HEALTH_CENTER, POST, RESTAURANT, FASTFOOD, STAND, EYE, BOOK, WRITING, SOUVENIR, HEALTH, TENNIS
    }

    private static class Adapter extends AbsArrayAdapter.SimpleAdapter<OApiUtil.UnivBuilding> {

        public Adapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, OApiUtil.UnivBuilding.values());
        }

        @Override
        public String getTextFromItem(OApiUtil.UnivBuilding item) {
            return item.getLocaleName();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_map_googlemap);

        LinearLayout toolbarParent = (LinearLayout) findViewById(R.id.toolbar_parent);
        Toolbar toolbar = (Toolbar) toolbarParent.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!initMap()) {
            AppUtil.showToast(getApplicationContext(), R.string.tab_map_submap_device_without_googlemap, true);
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.action_map);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

        View spinnerLayout = View.inflate(this, R.layout.view_tab_map_sub_spinner_layout, null);
        mLocationSelectSpinner = (AppCompatSpinner) spinnerLayout.findViewById(R.id.spinner);
        mLocationSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInit) {
                    isInit = false;
                }

                googleMap.clear();
                OApiUtil.UnivBuilding univBuilding = OApiUtil.UnivBuilding.fromNumber(position + 1);
                moveCamera(univBuilding);
                setMapMarker(univBuilding);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        toolbarParent.addView(spinnerLayout);

        ListView listView = new ListView(this);
        final Adapter adapter = new Adapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (location == null) {
                    AppUtil.showToast(getBaseContext(), R.string.tab_map_submap_do_not_locate, true);
                } else {

                    showRoute(new LatLng(location.getLatitude(), location.getLongitude()), adapter.getItem(arg2).latLng());
                    locationSelector.dismiss();
                }
            }
        });

        locationSelector = new AlertDialog.Builder(this)
                .setView(listView)
                .setTitle(R.string.tab_map_submap_select_dest)
                .setMessage(R.string.tab_map_submap_select_building)
                .create();

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                buildingNo = getIntent().getIntExtra("building", -1);
                if (buildingNo != -1) {
                    isInit = false;

                    //OApiUtil.UnivBuilding univBuilding = OApiUtil.UnivBuilding.fromNumber(buildingNo);
                    mLocationSelectSpinner.setSelection(buildingNo - 1, false);
                   // moveCamera(univBuilding);
                    //moveCameraPositionAt(buildingNo);
                    buildingNo = -1;

                } else {
                    moveCamera(OApiUtil.UnivBuilding.Univ);
                    setMapMarker(OApiUtil.UnivBuilding.Univ);
                    // moveCameraPositionAt(0);
                    //setCameraMapMarkerAt(0, getString(R.string.univ));
                }
            }
        }, 500);
    }

    @NonNull
    @Override
    protected String getScreenName() {
        return "SubMapActivity";
    }

    private Location getCurrentLocation() {
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(provider);
    }

    private void showRoute(LatLng start, LatLng dest) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + start.latitude + ',' + start.longitude + "&daddr=" + dest.latitude + ',' + dest.longitude));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    private boolean initMap() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.tab_map_submap)).getMap();
            if (googleMap == null) {
                return false;
            }

            googleMap.setBuildingsEnabled(true);
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    location = arg0;
                }
            });

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) {
                // 위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동
                new AlertDialog.Builder(this)
                        .setTitle("위치서비스 동의")
                        .setNeutralButton("이동", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SOURCE_SETTINGS);
                            }
                        })

                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .show();

            } else {
                // 위치 정보 설정이 되어 있으면 현재위치를 받아옴
                locationManager.requestLocationUpdates(provider, 1, 1, this);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION_SOURCE_SETTINGS:
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);

                // 사용자가 위치설정동의 안했을때 종료
                if (provider == null) {
                    finish();

                    // 사용자가 위치설정 동의 했을때
                } else {
                    locationManager.requestLocationUpdates(provider, 1L, 2F, this);
                    initMap();
                }
                break;

            default:
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tab_map_googlemap_menu, menu);
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
            /*case R.id.action_direction:
                if (location == null)
                    location = getCurrentLocation();
                locationSelector.show();
                return true;
                */
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        if (googleMap != null) {
            googleMap.clear();
            googleMap = null;
        }

        if(locationManager != null){
            locationManager.removeUpdates(this);
            locationManager = null;
        }
        super.onDestroy();
    }

    private void selectWelfareBuildingMenu() {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(this)
                    .setTitle("복지시설")
                    .setItems(R.array.tab_map_submap_buildings_welfare, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            googleMap.clear();
                            String locationName = getResources().getStringArray(R.array.tab_map_submap_buildings_welfare)[item];

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
                                    setCameraMapMarkerAt(32, locationName);
                                    break;
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
                    })
                  .create();

        }
        dialog.show();
    }


    @Deprecated
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

    @Deprecated
    private void moveCameraPositionAt(int itemPosition) {
        LatLng latLng = getLatLngByBuildingNumber(itemPosition);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Deprecated
    private void setCameraMapMarkerAt(int itemPosition, String locationName) {
        LatLng latLng = getLatLngByBuildingNumber(itemPosition);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.addMarker(new MarkerOptions().position(latLng)
                .title(locationName).visible(true));
    }

    private void moveCamera(OApiUtil.UnivBuilding building) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(building.latLng()));
    }

    private void setMapMarker(OApiUtil.UnivBuilding building) {
        setMapMarker(building, building.getLocaleName());
    }

    private void setMapMarker(OApiUtil.UnivBuilding building, String markerName) {
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.addMarker(new MarkerOptions().position(building.latLng()).title(markerName).visible(true));
    }

}
