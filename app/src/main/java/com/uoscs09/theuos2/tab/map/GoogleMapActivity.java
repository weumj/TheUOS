package com.uoscs09.theuos2.tab.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.UnivBuilding;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoogleMapActivity extends BaseActivity implements LocationListener {
    @Nullable
    public static Intent startIntentWithErrorToast(Activity activity, OApiUtil.UnivBuilding univBuilding) {
        try {
            return startIntent(activity, univBuilding);
        } catch (GooglePlayServicesNotAvailableException e) {
            AppUtil.showToast(activity, R.string.tab_map_device_cannot_init_googlemap, true);
        }

        return null;
    }

    @Nullable
    public static Intent startIntentWithErrorToast(Activity activity) {
        try {
            return startIntent(activity);
        } catch (GooglePlayServicesNotAvailableException e) {
            AppUtil.showToast(activity, R.string.tab_map_device_cannot_init_googlemap, true);
        }

        return null;
    }

    public static Intent startIntent(Context context) throws GooglePlayServicesNotAvailableException {
        int googlePlayAvailabilityResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (googlePlayAvailabilityResult != ConnectionResult.SUCCESS) {
            throw new GooglePlayServicesNotAvailableException(googlePlayAvailabilityResult);
        }

        return new Intent(context, GoogleMapActivity.class);
    }

    public static Intent startIntent(Context context, OApiUtil.UnivBuilding univBuilding) throws GooglePlayServicesNotAvailableException {
        if (univBuilding.code < 0) {
            return null;
        }
        return startIntent(context).putExtra("building", univBuilding.code);
    }


    private static final int REQUEST_LOCATION_SOURCE_SETTINGS = 100;
    private static final int REQUEST_PERMISSION_IN_INIT = 120;
    private static final int REQUEST_PERMISSION_IN_INIT_MAP = 130;
    private static final int REQUEST_PERMISSION_IN_RESUME = 140;

    private static final int PERMISSION_REQUEST_LOCATION = 4826;

    @Nullable
    private GoogleMap googleMap;
    @BindView(R.id.tab_map_fab)
    FloatingActionButton button;

    private LocationManager locationManager;
    private boolean isLocationUpdates = false;

    private int selectedBuilding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!checkSelfPermissionCompat(permissions)) {
            requestPermissionsCompat(REQUEST_PERMISSION_IN_INIT, permissions);
            //AppUtil.showToast(this, R.string.tab_map_permission_denied);
            //finish();
            return;
        }

        selectedBuilding = getIntent().getIntExtra("building", -1);

        setContentView(R.layout.tab_map_googlemap);
        ButterKnife.bind(this);

        initMap();
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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!checkSelfPermissionCompat(permissions)) {
            requestPermissionsCompat(REQUEST_PERMISSION_IN_INIT_MAP, permissions);
        } else {
            googleMap.setBuildingsEnabled(true);
            //noinspection MissingPermission
            googleMap.setMyLocationEnabled(true);
            ButtonMover bm = new ButtonMover(button);
            googleMap.setOnMarkerClickListener(bm);
            googleMap.setOnMapClickListener(bm);
            loadDefaultMapLocation();
        }
    }

    private void loadDefaultMapLocation() {
        if (selectedBuilding != -1) {
            new Handler().postDelayed(() -> {
                showBuildingItem(selectedBuilding);
                selectedBuilding = -1;
            }, 500);
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
            googleMap.setOnMarkerClickListener(null);
            googleMap.setOnMapClickListener(null);
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
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, R.string.tab_map_permission_denied)) {
                    setContentView(R.layout.tab_map_googlemap);
                    ButterKnife.bind(this);
                    initMap();
                } else {
                    finish();
                }
                break;

            case REQUEST_PERMISSION_IN_INIT_MAP:
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
        MapBuildingsDialogFragment.showDialog(this, button, (pagerIndex, listIndex) -> {
            if (pagerIndex == 0) {
                showBuildingItem(listIndex + 1);
            } else {
                showWelfareItem(listIndex);
            }

            return true;
        });
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



    private static class ButtonMover implements GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
        private boolean isButtonMove = false;
        private final float distance;
        private final View button;
        public ButtonMover(View v) {
            this.button = v;
            distance = v.getContext().getResources().getDimension(R.dimen.dp28);
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
}
