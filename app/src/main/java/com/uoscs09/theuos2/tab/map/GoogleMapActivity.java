package com.uoscs09.theuos2.tab.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil.UnivBuilding;
import com.uoscs09.theuos2.util.StringUtil;

public class GoogleMapActivity extends BaseActivity implements LocationListener {
    private GoogleMap googleMap;
    private boolean isInit = true;
    private AlertDialog mWelfareBuildingDialog;
    private LocationManager locationManager;
    //private Location location;
    private Spinner mLocationSelectSpinner;

    private int buildingNo;

    private static final int REQUEST_LOCATION_SOURCE_SETTINGS = 9643;

    private enum WelfareCategory {
        CASH, BANK, COPY, PRINT, SEARCH, REST, ELEVATOR, HEALTH_CENTER, POST, RESTAURANT, FASTFOOD, STAND, EYE, BOOK, WRITING, SOUVENIR, HEALTH, TENNIS
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
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_map);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }
        View spinnerLayout = View.inflate(this, R.layout.view_tab_map_sub_spinner_layout, null);
        mLocationSelectSpinner = (Spinner) spinnerLayout.findViewById(R.id.spinner);
        mLocationSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInit) {
                    isInit = false;
                }

                googleMap.clear();
                UnivBuilding univBuilding = UnivBuilding.fromNumber(position + 1);
                moveCamera(univBuilding);
                setMapMarker(univBuilding);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        toolbarParent.addView(spinnerLayout);

        mLocationSelectSpinner.postDelayed(new Runnable() {
            @Override
            public void run() {
                buildingNo = getIntent().getIntExtra("building", -1);
                if (buildingNo != -1) {
                    isInit = false;

                    // UnivBuilding univBuilding =  UnivBuilding.fromNumber(buildingNo);
                    mLocationSelectSpinner.setSelection(buildingNo - 1, false);
                    // moveCamera(univBuilding);
                    //moveCameraPositionAt(buildingNo);
                    buildingNo = -1;

                } else {
                    moveCamera(UnivBuilding.Univ);
                    setMapMarker(UnivBuilding.Univ);
                    // moveCameraPositionAt(0);
                    //setMapMarker( UnivBuilding.fromNumber(0, getString(R.string.univ));
                }
            }
        }, 500);
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "GoogleMapActivity";
    }

    /*
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
    */

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
                    // location = arg0;
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
        // this.location = location;
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

        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager = null;
        }
        super.onDestroy();
    }

    private void selectWelfareBuildingMenu() {
        if (mWelfareBuildingDialog == null) {
            mWelfareBuildingDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.tab_map_submap_welfare)
                    .setItems(R.array.tab_map_submap_buildings_welfare, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            googleMap.clear();

                            //String locationName = getResources().getStringArray(R.array.tab_map_submap_buildings_welfare)[item];
                            String locationName = mWelfareBuildingDialog.getListView().getAdapter().getItem(item).toString();

                            WelfareCategory welfareCategory = WelfareCategory.values()[item];
                            sendTrackerEvent("welfare", welfareCategory.name());
                            // TODO 리팩토링이  필요함.
                            switch (welfareCategory) {
                                case BANK:
                                    setMapMarker(UnivBuilding.University_Center, locationName);
                                    break;
                                case COPY:
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    break;
                                case PRINT:
                                    setMapMarker(UnivBuilding.Liberal_Arts, locationName + StringUtil.NEW_LINE + "2층 PC실(533호)");
                                    setMapMarker(UnivBuilding.The_21st_Century, locationName + StringUtil.NEW_LINE + "전자도서관(입금가능), 2층 227호");
                                    setMapMarker(UnivBuilding.Law, locationName + StringUtil.NEW_LINE + "4층(입금가능), 5층, 6층(도서관)");
                                    setMapMarker(UnivBuilding.Library, locationName + StringUtil.NEW_LINE + "1층, 2층, 3층(입금가능), 4층");
                                    setMapMarker(UnivBuilding.Mirae, locationName + StringUtil.NEW_LINE + "3층 경영도서관, 4층 PC실(입금가능)");
                                    setMapMarker(UnivBuilding.International, locationName + StringUtil.NEW_LINE + "1층 로비");
                                    break;
                                case CASH:
                                    setMapMarker(UnivBuilding.University_Center, locationName);
                                    setMapMarker(UnivBuilding.Natural_Science, locationName);
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.The_21st_Century, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    break;
                                case ELEVATOR:
                                    setMapMarker(UnivBuilding.Architecture_and_CivilEngineering, locationName);
                                    setMapMarker(UnivBuilding.Liberal_Arts, locationName);
                                    setMapMarker(UnivBuilding.Baebong, locationName);
                                    setMapMarker(UnivBuilding.University_Center, locationName);
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.Science_and_Technology, locationName);
                                    setMapMarker(UnivBuilding.The_21st_Century, locationName);
                                    setMapMarker(UnivBuilding.IT, locationName);
                                    setMapMarker(UnivBuilding.Law, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    setMapMarker(UnivBuilding.Dormitory, locationName);
                                    setMapMarker(UnivBuilding.International, locationName);
                                    break;
                                case HEALTH:
                                    setMapMarker(UnivBuilding.Gymnaseum, locationName);
                                    setMapMarker(UnivBuilding.Dormitory, locationName);
                                    setMapMarker(UnivBuilding.Wellness, locationName);
                                    break;
                                case TENNIS:
                                    setMapMarker(UnivBuilding.Wellness, locationName);
                                    break;
                                case WRITING:
                                case BOOK:
                                case FASTFOOD:
                                case POST:
                                case SOUVENIR:
                                case EYE:
                                case HEALTH_CENTER:
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    break;
                                case REST:
                                    setMapMarker(UnivBuilding.Architecture_and_CivilEngineering, locationName);
                                    setMapMarker(UnivBuilding.Liberal_Arts, locationName);
                                    setMapMarker(UnivBuilding.Baebong, locationName);
                                    setMapMarker(UnivBuilding.Natural_Science, locationName);
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.The_21st_Century, locationName);
                                    setMapMarker(UnivBuilding.Design_and_Sculpture, locationName);
                                    setMapMarker(UnivBuilding.IT, locationName);
                                    setMapMarker(UnivBuilding.Law, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    setMapMarker(UnivBuilding.Dormitory, locationName);
                                    setMapMarker(UnivBuilding.Mirae, locationName);
                                    break;
                                case RESTAURANT:
                                    setMapMarker(UnivBuilding.University_Center, locationName);
                                    setMapMarker(UnivBuilding.Natural_Science, locationName);
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.International, locationName);
                                    break;
                                case SEARCH:
                                    setMapMarker(UnivBuilding.Architecture_and_CivilEngineering, locationName);
                                    setMapMarker(UnivBuilding.Changgong, locationName);
                                    setMapMarker(UnivBuilding.Natural_Science, locationName);
                                    setMapMarker(UnivBuilding.Science_and_Technology, locationName);
                                    setMapMarker(UnivBuilding.IT, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    setMapMarker(UnivBuilding.Dormitory, locationName);
                                    break;
                                case STAND:
                                    setMapMarker(UnivBuilding.Student_Hall, locationName);
                                    setMapMarker(UnivBuilding.Library, locationName);
                                    setMapMarker(UnivBuilding.Mirae, locationName);
                                    break;
                                default:
                                    break;
                            }
                            dialog.dismiss();
                        }
                    })
                    .create();

        }
        mWelfareBuildingDialog.show();
    }

    private void moveCamera(UnivBuilding building) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(building.latLng()));
    }

    private void setMapMarker(UnivBuilding building) {
        setMapMarker(building, building.getLocaleName(), null);
    }

    private void setMapMarker(UnivBuilding building, String snippet) {
        setMapMarker(building, building.getLocaleName(), snippet);
    }

    private void setMapMarker(UnivBuilding building, String title, String snippet) {
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        googleMap.addMarker(
                new MarkerOptions()
                        .position(building.latLng())
                        .title(title)
                        .snippet(snippet)
                        .visible(true)
        );
    }

}
