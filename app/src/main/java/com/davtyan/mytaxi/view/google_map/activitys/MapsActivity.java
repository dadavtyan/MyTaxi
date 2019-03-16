package com.davtyan.mytaxi.view.google_map.activitys;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davtyan.mytaxi.App;
import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.R;

import com.davtyan.mytaxi.model.Driver;
import com.davtyan.mytaxi.model.Geo;
import com.davtyan.mytaxi.view.activitys.AuthActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 774;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    private GoogleMap mMap;
    private Location mLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;


    private DrawerLayout mDrawerLayout;
    private TextView numberTextView;
    private String phoneNumber;
    private Driver driver;

    private LinearLayout transactionTab;
    private Geo geo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initView();
        initMapData();
    }

    private void initMapData() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initView() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        transactionTab = findViewById(R.id.tab_menu);

        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.call_taxi).setOnClickListener(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        numberTextView = headerView.findViewById(R.id.phone_number);
        if (getIntent().getExtras() != null) {
            phoneNumber = getIntent().getStringExtra("Number");
            numberTextView.setText(phoneNumber);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (checkPermission()) {
                onLocationPermissionGranted();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void createLocationRequest() {

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);


        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                // All required changes were successfully made
                if (checkPermission())
                    onLocationPermissionGranted();
                Toast.makeText(MapsActivity.this,states.isLocationPresent()+"",Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                // The user was asked to change settings, but chose not to
                Toast.makeText(MapsActivity.this,"Canceled",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        if (checkPermission()) onLocationPermissionGranted();

    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void onLocationPermissionGranted() {
        if (!checkPermission()) {
            return;
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        Log.i("location1", "my : " + mLocation);
                        Log.i("location1", "p : " + location);
                        if (location != null) {
                            mLocation = location;
                            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                            String title = getAddress(geocoder);
                            setPlacesOnMap(latLng, title);

                        } else {
                            createLocationRequest();
                            mLocation = null;
                        }
                    }
                });
    }

    private void setPlacesOnMap(LatLng latLng, String title) {

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();

        //mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private String getAddress(Geocoder geocoder) {
        String result;
        try {
            List<Address> addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
            result = addresses.get(0).getLocality() + ":" + addresses.get(0).getCountryName() + ":" +
                    addresses.get(0).getFeatureName() + ":" + addresses.get(0).getAdminArea();
        } catch (IOException e) {
            result = "";
            e.printStackTrace();
        }
        return result;
    }

    private boolean checkPermission() {
        int pFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int pCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (pFineLocation != PackageManager.PERMISSION_GRANTED && pCoarseLocation != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.call_taxi:
                getDrivers();
                showTransactionMenu();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.logout:
                Intent intent = new Intent(MapsActivity.this, AuthActivity.class);
                intent.putExtra("Log Out", "Log Out");
                startActivity(intent);
                return true;
            case R.id.home:
                drawRoute();
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.test:
                mDrawerLayout.closeDrawers();
                return true;
        }

        return true;
    }

    private void showTransactionMenu() {
        int[] location = new int[2];
        transactionTab.getLocationOnScreen(location);
//        TransactionMenu popupFigure = new TransactionMenu(this,geo);
//        popupFigure.showAtLocation(transactionTab, Gravity.NO_GRAVITY, location[0], location[1] + popupFigure.getHeight());
    }



    private void drawRoute(){
        LatLng origin = new LatLng(40.1910778, 44.513604);
        mMap.addMarker(new MarkerOptions().position(origin).title("AOD"));

        LatLng destination  = new LatLng(40.171381, 44.5037763);
        mMap.addMarker(new MarkerOptions().position(destination).title("Driver"));


    }

    public String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" +
                output + "?" + parameters + "&key=" + Constants.MAPS_KEY;

        return url;

    }

    private void getDrivers() {
        App.getApi().getDrivers().enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {

                driver = response.body().get(0);

                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                Log.i("myLog", "onResponse: " + t.getMessage());
            }
        });
    }

}