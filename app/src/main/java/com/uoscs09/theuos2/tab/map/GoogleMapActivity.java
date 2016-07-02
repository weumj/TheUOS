package com.uoscs09.theuos2.tab.map;

import android.Manifest;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil.UnivBuilding;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoogleMapActivity extends BaseActivity implements LocationListener {

    private static final int REQUEST_LOCATION_SOURCE_SETTINGS = 100;
    private static final int REQUEST_PERMISSION_IN_INIT = 120;
    private static final int REQUEST_PERMISSION_IN_RESUME = 140;

    private static final int PERMISSION_REQUEST_LOCATION = 4826;

    @Nullable
    private GoogleMap googleMap;
    @BindView(R.id.tab_map_fab)
    FloatingActionButton button;

    private AlertDialog mMenuDialog;
    private LocationManager locationManager;
    private boolean isLocationUpdates = false;

    private int selectedBuilding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_map_googlemap);

        ButterKnife.bind(this);

        int googlePlayAvailabilityResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googlePlayAvailabilityResult != ConnectionResult.SUCCESS) {
            AppUtil.showToast(getApplicationContext(), R.string.tab_map_device_without_googlemap, true);
            finish();
            return;
        }

        initMap();

        selectedBuilding = getIntent().getIntExtra("building", -1);

    }

    @OnClick(R.id.action_backward)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.tab_map_fab)
    void fabClick() {
        selectWelfareBuildingMenu();
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "GoogleMapActivity";
    }

    private void initMap() {
        if (googleMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.tab_map_submap)).getMapAsync(this::initGoogleMapSetting);
        } else {
            loadDefaultMapLocation();
        }
    }

    void initGoogleMapSetting(GoogleMap googleMap) {
        this.googleMap = googleMap;

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!checkSelfPermissionCompat(permissions)) {
            requestPermissionsCompat(REQUEST_PERMISSION_IN_INIT, permissions);
        } else {
            googleMap.setBuildingsEnabled(true);
            //noinspection MissingPermission
            googleMap.setMyLocationEnabled(true);
            ButtonMover bm = new ButtonMover();
            googleMap.setOnMarkerClickListener(bm);
            googleMap.setOnMapClickListener(bm);
            loadDefaultMapLocation();
        }
    }

    private class ButtonMover implements GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
        private boolean isButtonMove = false;
        private final float distance;

        public ButtonMover() {
            distance = getResources().getDimension(R.dimen.dp16);
        }

        @Override
        public void onMapClick(LatLng latLng) {
            if (isButtonMove) {
                button.animate().yBy(distance).start();
                isButtonMove = false;
            }
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (!isButtonMove) {
                button.animate().yBy(-distance).start();
                isButtonMove = true;
            }
            return false;
        }
    }


    private void loadDefaultMapLocation() {
        if (selectedBuilding != -1) {
            showBuildingItem(selectedBuilding);
            selectedBuilding = -1;
        } else {
            moveCamera(UnivBuilding.Univ);
            setMapMarker(UnivBuilding.Univ);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!isLocationUpdates && googleMap != null) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) {
                // 위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동
                new AlertDialog.Builder(this)
                        .setTitle("위치서비스 동의")
                        .setNeutralButton("이동", (dialog, which) -> {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_SOURCE_SETTINGS);
                        })
                        .setOnCancelListener(dialog -> finish())
                        .show();
            } else {
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                if (checkSelfPermissionCompat(permissions)) {
                    // 위치 정보 설정이 되어 있으면 현재위치를 받아옴
                    //noinspection MissingPermission
                    locationManager.requestLocationUpdates(provider, 1L, 2F, this);
                    isLocationUpdates = true;
                } else {
                    requestPermissionsCompat(REQUEST_PERMISSION_IN_RESUME, permissions);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        removeLocationUpdate();
    }

    @Override
    protected void onDestroy() {
        if (googleMap != null) {
            googleMap.clear();
            googleMap = null;
        }

        removeLocationUpdate();
        locationManager = null;
        super.onDestroy();
    }

    private void removeLocationUpdate() {
        if (locationManager != null) {
            if (checkSelfPermissionCompat(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //noinspection ResourceType
                locationManager.removeUpdates(this);
                isLocationUpdates = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_IN_INIT:
            case PERMISSION_REQUEST_LOCATION:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, R.string.tab_map_permission_denied)) {
                    initMap();
                } else {
                    finish();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION_SOURCE_SETTINGS:
                if (locationManager == null)
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (!isLocationUpdates) {
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, true);

                    // 사용자가 위치설정동의 안했을때 종료
                    if (provider == null) {
                        finish();
                        return;
                    }

                    // 사용자가 위치설정 동의 했을때
                    if (checkSelfPermissionCompat(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //noinspection MissingPermission
                        locationManager.requestLocationUpdates(provider, 1L, 2F, this);
                        isLocationUpdates = true;
                        initMap();
                    } else {
                        requestPermissionsCompat(PERMISSION_REQUEST_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
                    }
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

    private void selectWelfareBuildingMenu() {
        if (mMenuDialog == null) {
            View v = LayoutInflater.from(this).inflate(R.layout.dialog_map_menu, null, false);

            Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.tab_map_menu_title);

            final ViewGroup viewGroup = (ViewGroup) v.findViewById(R.id.tab_map_bar_parent);
            viewGroup.getChildAt(0).setBackgroundColor(AppUtil.getAttrColor(this, R.attr.color_actionbar_title));

            ViewPager pager = (ViewPager) v.findViewById(R.id.viewpager);

            v.findViewById(R.id.tab_map_menu_select_1).setOnClickListener(v1 -> pager.setCurrentItem(0, true));

            v.findViewById(R.id.tab_map_menu_select_2).setOnClickListener(v1 -> pager.setCurrentItem(1, true));

            pager.setAdapter(new PagerAdapter() {
                @Override
                public int getCount() {
                    return 2;
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return object.equals(view);
                }

                @Override
                public Object instantiateItem(ViewGroup container, final int position) {
                    View v = LayoutInflater.from(GoogleMapActivity.this).inflate(R.layout.view_map_menu_1, container, false);
                    ListView listView = (ListView) v.findViewById(R.id.list);
                    listView.setAdapter(ArrayAdapter.createFromResource(GoogleMapActivity.this, position == 0 ? R.array.buildings_univ : R.array.tab_map_buildings_welfare, android.R.layout.simple_list_item_1));
                    listView.setOnItemClickListener((parent, view, position1, id) -> {
                        if (position == 0) {
                            showBuildingItem(position1 + 1);
                        } else {
                            showWelfareItem(position1);
                        }
                        mMenuDialog.dismiss();
                    });

                    container.addView(v);
                    return v;
                }
            });
            pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    viewGroup.getChildAt(position).setBackgroundColor(AppUtil.getAttrColor(GoogleMapActivity.this, R.attr.color_actionbar_title));
                    viewGroup.getChildAt(1 - position).setBackgroundColor(0);
                }
            });

            mMenuDialog = new AlertDialog.Builder(this)
                    .setView(v)
                    .create();
            mMenuDialog.setOnShowListener(dialog1 -> AnimUtil.revealShow(v, null));
            //mMenuDialog.setOnDismissListener(dialog1 -> AnimUtil.revealShow(v, false, mMenuDialog));
            //mMenuDialog.setOnCancelListener(dialog1 -> AnimUtil.revealShow(v, false, mMenuDialog));
        }
        mMenuDialog.show();
    }

    private void showBuildingItem(int position) {
        if (googleMap != null) {
            googleMap.clear();
        }

        UnivBuilding univBuilding = UnivBuilding.fromNumber(position);
        setMapMarker(univBuilding);
        moveCamera(univBuilding);
    }

    private void showWelfareItem(int position) {
        if (googleMap != null) {
            googleMap.clear();
        }

        Welfare welfare = Welfare.values()[position];
        if (welfare == null) {
            return;
        }

        sendTrackerEvent("welfare", welfare.name());

        if (welfare.isArrayRes) {
            String[] descArray = getResources().getStringArray(welfare.descriptionRes);
            for (int i = 0; i < welfare.univBuildings.length; i++) {
                setMapMarker(welfare.univBuildings[i], descArray[i]);
            }
        } else {
            String desc = getString(welfare.descriptionRes);
            for (UnivBuilding building : welfare.univBuildings) {
                setMapMarker(building, desc);
            }
        }
    }

    private void moveCamera(UnivBuilding building) {
        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(building.latLng()));
        }
    }

    private void setMapMarker(UnivBuilding building) {
        setMapMarker(building, building.getLocaleName(), null);
    }

    private void setMapMarker(UnivBuilding building, String snippet) {
        setMapMarker(building, building.getLocaleName(), snippet);
    }

    private void setMapMarker(UnivBuilding building, String title, String snippet) {
        if (googleMap != null) {
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

}
