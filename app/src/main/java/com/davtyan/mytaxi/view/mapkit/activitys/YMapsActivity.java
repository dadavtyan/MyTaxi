package com.davtyan.mytaxi.view.mapkit.activitys;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davtyan.mytaxi.App;
import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.R;


import com.davtyan.mytaxi.session.MySession;
import com.davtyan.mytaxi.model.Driver;
import com.davtyan.mytaxi.model.Geo;
import com.davtyan.mytaxi.model.Message;
import com.davtyan.mytaxi.util.Counter;
import com.davtyan.mytaxi.util.Distance;
import com.davtyan.mytaxi.view.InputFilterMinMax;
import com.davtyan.mytaxi.view.activitys.LoginActivity;
import com.davtyan.mytaxi.view.activitys.SettingsActivity;
import com.davtyan.mytaxi.view.mapkit.fragments.SuggestFragment;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSection;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.user_location.UserLocationTapListener;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YMapsActivity extends AppCompatActivity implements
        View.OnClickListener, NavigationView.OnNavigationItemSelectedListener,
        DrivingSession.DrivingRouteListener{

    private final SearchOptions SEARCH_OPTIONS = new SearchOptions().setSearchTypes(SearchType.GEO.value);

    private static final int PERMISSIONS_CODE = 109;
    private static final double DESIRED_ACCURACY = 0;
    private static final long MINIMAL_TIME = 1500;
    private static final double MINIMAL_DISTANCE = 50;
    private static final boolean USE_IN_BACKGROUND = false;

    private static final int COMFORTABLE_ZOOM_LEVEL = 14;


    private MapView mapView;

    private LocationManager locationManager;
    private LocationListener myLocationListener;

    private SearchManager searchManager;
    private Session.SearchListener mSearchPointListener;

    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    private MapObjectCollection mapObjects;
    private MapObjectCollection cMapObjects;


    private ViewGroup TransactionArea;
    private ViewGroup Transaction;
    private TextView TransactionBtn;


    private ArrayList<String> dNumbers;
    private ViewGroup driverInfoView, dialogView, confirmedView, successfullyView, ratingAreaView;
    private TextView mModelTextView, mColorTextView, mRatingTextView, mPhoneTextView, mPriceTextView, mDistanceTextView, mTimeTextView;
    private EditText ratingEditText; 

    private DrawerLayout mDrawerLayout;
    private Point START_LOCATION;
    private Point END_LOCATION;
    private boolean isStart,isOpen;
    private Geo geo;

    private int mDistance;
    private String driverId;
    private int TimeWithTraffic;
    private int Time;
    private String title;
    private MySession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitFactory.setApiKey(Constants.MAP_KIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        SearchFactory.initialize(this);

        locationManager = MapKitFactory.getInstance().createLocationManager();
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);

        setContentView(R.layout.activity_ymaps);
        super.onCreate(savedInstanceState);

        initView();
        initMapData();
        initLocationListener();
        initSearchPointListener();
        mapView.getMap().addCameraListener(cameraListener);
        App.logClassName(this);


    }

    private final CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraPositionChanged(@NonNull Map map, CameraPosition cameraPosition,
                                            @NonNull CameraUpdateSource cameraUpdateSource, boolean b) {
            Log.i("CameraListener", "getZoom: " + cameraPosition.getZoom());
           // Log.i("CameraListener", "getZoom: " +  cameraPosition.getTarget().getLatitude());
        }
    };

    private void initView() {
        mapView = findViewById(R.id.mapview);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        findViewById(R.id.btn_menu).setOnClickListener(this);
        findViewById(R.id.call_taxi).setOnClickListener(this);
        findViewById(R.id.fab_current_location).setOnClickListener(this);
        findViewById(R.id.search_tab).setOnClickListener(this);

        //init transaction elements

        mModelTextView = findViewById(R.id.car_model);
        mColorTextView = findViewById(R.id.car_color);
        mDistanceTextView = findViewById(R.id.distance);
        mPhoneTextView = findViewById(R.id.driver_phone);
        mPriceTextView = findViewById(R.id.trip_price);
        mTimeTextView = findViewById(R.id.time);
        mRatingTextView = findViewById(R.id.rating);
        ratingEditText = findViewById(R.id.edit_rating);
        ratingEditText.setFilters(new InputFilter[]{new InputFilterMinMax("1", "5")});

        TransactionArea = findViewById(R.id.transaction_area);
        TransactionBtn = findViewById(R.id.transaction_btn);
        Transaction = findViewById(R.id.transaction);
        driverInfoView = findViewById(R.id.driver);
        dialogView = findViewById(R.id.dialog_area);
        confirmedView = findViewById(R.id.confirmed_state);
        successfullyView = findViewById(R.id.successfully_state);
        ratingAreaView = findViewById(R.id.rating_area);

        TransactionBtn.setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.ok).setOnClickListener(this);
        findViewById(R.id.author_driver).setOnClickListener(this);
        findViewById(R.id.confirmed).setOnClickListener(this);
        findViewById(R.id.no_confirmed).setOnClickListener(this);
        findViewById(R.id.successfully).setOnClickListener(this);
        findViewById(R.id.no_successfully).setOnClickListener(this);
        findViewById(R.id.select_rating).setOnClickListener(this);
        findViewById(R.id.no_rating).setOnClickListener(this);


        //init Navigation menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView numberTextView = headerView.findViewById(R.id.phone_number);

        session = new MySession(getBaseContext());
        String email = session.getEmail();
        numberTextView.setText(email);
    }

    private void initMapData() {
        mapView = findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition(new Point(0, 0), 14, 0, 0));

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        cMapObjects = mapView.getMap().getMapObjects().addCollection();

    }

    private void initLocationListener() {
        myLocationListener = new LocationListener() {
            @Override
            public void onLocationUpdated(@NonNull Location location) {
                if (START_LOCATION == null) {
                    START_LOCATION = location.getPosition();
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    title = getAddress(geocoder);
                    moveCamera(location.getPosition(), COMFORTABLE_ZOOM_LEVEL);
                    Log.w("myLocationListener", "title - " + title);
                } else {
                    // myLocation = location.getPosition();
                    //Log.w("myLocationListener", "my location - " + myLocation.getLatitude() + "," + myLocation.getLongitude());
                }
            }

            @Override
            public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
                if (locationStatus == LocationStatus.NOT_AVAILABLE) {
                    Toast.makeText(YMapsActivity.this, R.string.error_get_location, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void initSearchPointListener() {

        mSearchPointListener = new Session.SearchListener() {
            @Override
            public void onSearchResponse(@NonNull com.yandex.mapkit.search.Response response) {
                Point resultLocation;
                for (GeoObjectCollection.Item searchResult : response.getCollection().getChildren()) {
                    resultLocation = searchResult.getObj().getGeometry().get(0).getPoint();
                    if (resultLocation != null) {
                        if (isStart) {
                            START_LOCATION = resultLocation;
                            submitRequest(START_LOCATION, END_LOCATION);
                            getInfo();
                        } else END_LOCATION = resultLocation;
                        geo = new Geo(START_LOCATION);
                        isStart = !isStart;
                        break;
                    }
                }
            }

            @Override
            public void onSearchError(@NonNull Error error) {
                Log.w("SearchPointListener", "onSearchError ERROR when try search point by address");
                // Toast.makeText(getContext(), R.string.error_cant_get_coordinates, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private synchronized void getInfo() {
        new DriverTask().execute();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDistance();
            }
        }, 1500);

        isOpen = true;
        TransactionArea.setVisibility(View.VISIBLE);
    }

    public void requestGeoPoint(String queryEnd, String queryStart) {
        requestSearch(queryEnd);
        requestSearch(queryStart);
    }

    private synchronized void requestSearch(String query) {
        Session searchSession = searchManager.submit(
                query,
                Geometry.fromPoint(START_LOCATION),
                SEARCH_OPTIONS,
                mSearchPointListener);
    }



    private void setDriver(Driver driver) {
        if (driver == null) {
            resetDriverData();
            return;
        }
        
        driverId = driver.getId();
        dNumbers.add(driver.getPhone());
        
        mModelTextView.setText("Car Model: " + driver.getModel());
        mColorTextView.setText("Car Color: " + driver.getColor());
        mRatingTextView.setText("Rating:" + driver.getRating());
        mPhoneTextView.setText("Driver Phone:" + driver.getPhone());

    }

    private void updateDistance() {
        mDistanceTextView.setText("Distance: " + mDistance / 1000 + " km");
        mPriceTextView.setText("Trip Price: " + Counter.calculatePrice(mDistance));
        mTimeTextView.setText("Time : " + Counter.getTime(TimeWithTraffic));
    }

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSIONS_CODE
            );

        } else {
            subscribeLocationUpdate();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        locationManager.unsubscribe(myLocationListener);
        MapKitFactory.getInstance().onStop();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.logout:
                session.setLogin(false);
                startActivity(new Intent(YMapsActivity.this, LoginActivity.class));
                return true;
            case R.id.home:
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.settings:
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(YMapsActivity.this, SettingsActivity.class));
                return true;
            case R.id.test:
                startActivity(new Intent(YMapsActivity.this, PanoramaActivity.class));
                mDrawerLayout.closeDrawers();
                return true;
        }
        return true;
    }

    void showDialog() {
        new SuggestFragment().show(this, title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.call_taxi:
                if (START_LOCATION != null) {
                    resetCallData();
                    showDialog();
                } else
                    Toast.makeText(this, R.string.coordinates_are_not_yet_determinate, Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab_current_location:
                if (START_LOCATION == null) {
                    Toast.makeText(this, R.string.coordinates_are_not_yet_determinate, Toast.LENGTH_SHORT).show();
                    return;
                }
                moveCamera(START_LOCATION, 16);
                break;
            case R.id.search_tab:
                resetCallData();
                showDialog();
                break;
            case R.id.cancel:
                changeVisibility(View.GONE, TransactionArea);
                resetCallData();
                cancel();
                break;
            case R.id.ok:
                changeVisibility(View.GONE, driverInfoView, dialogView);
                changeVisibility(View.VISIBLE, confirmedView);
                break;
            case R.id.author_driver:
                new DriverTask().execute();
                break;
            case R.id.confirmed:
                changeVisibility(View.GONE, confirmedView);
                changeVisibility(View.VISIBLE, successfullyView);
                confirmed();
                break;
            case R.id.no_confirmed:
                changeVisibility(View.GONE, Transaction);
                changeVisibility(View.VISIBLE, driverInfoView, dialogView);
                confirmed();
                break;
            case R.id.successfully:
                changeVisibility(View.GONE, successfullyView);
                changeVisibility(View.VISIBLE, ratingAreaView);
                successfully();
                break;
            case R.id.no_successfully:
                changeVisibility(View.GONE, Transaction);
                changeVisibility(View.VISIBLE, driverInfoView, dialogView);
                successfully();
                break;
            case R.id.select_rating:
                if (ratingEditText.getText().toString().length() > 0)
                    addRating(driverId, ratingEditText.getText().toString());
                changeVisibility(View.GONE, TransactionArea, ratingAreaView);
                changeVisibility(View.VISIBLE, driverInfoView, dialogView);
                break;
            case R.id.no_rating:
                changeVisibility(View.GONE, TransactionArea, ratingAreaView);
                changeVisibility(View.VISIBLE, driverInfoView, dialogView);
                break;
            case R.id.transaction_btn:
                if (isOpen) {
                    changeVisibility(View.GONE, Transaction);
                    TransactionBtn.setText(R.string.open);
                    isOpen = false;
                }else {
                    changeVisibility(View.VISIBLE, Transaction);
                    TransactionBtn.setText(R.string.close);
                    isOpen = true;
                }
                break;
        }
    }

    private void changeVisibility(int visibility, View... views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }

    private boolean checkPermission() {
        int Permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return Permission == PackageManager.PERMISSION_GRANTED;
    }

    private void subscribeLocationUpdate() {
        if (locationManager != null && myLocationListener != null) {
            locationManager.subscribeForLocationUpdates(
                    DESIRED_ACCURACY, MINIMAL_TIME, MINIMAL_DISTANCE, USE_IN_BACKGROUND, FilteringMode.ON, myLocationListener);
        }
    }

    private void moveCamera(Point point, float zoom) {
        Log.w("myLocationListener", "title - " + zoom);
        cMapObjects.addPlacemark(point,ImageProvider.fromResource(this, R.drawable.search_result),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
                );
        mapView.getMap().move(
                new CameraPosition(point, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 1),
                null);
    }

    private String getAddress(Geocoder geocoder) {
        String result;
        try {
            List<Address> addresses = geocoder.getFromLocation(START_LOCATION.getLatitude(), START_LOCATION.getLongitude(), 1);
            result = addresses.get(0).getCountryName() +
                    " , " + addresses.get(0).getAdminArea() +
                    " , " + addresses.get(0).getLocality() +
                    " , " + addresses.get(0).getThoroughfare() +
                    " , " + addresses.get(0).getFeatureName();

        } catch (IOException e) {
            result = "";
            e.printStackTrace();
        }
        return result;
    }

    private void submitRequest(Point pointStart, Point pointEnd) {
        DrivingOptions options = new DrivingOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();

        double latCenter = (pointStart.getLatitude() + pointEnd.getLatitude()) / 2;
        double lngCenter = (pointStart.getLongitude() + pointEnd.getLongitude()) / 2;

        Point SCREEN_CENTER = new Point(latCenter, lngCenter);

        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 12, 0, 0));

        requestPoints.add(new RequestPoint(pointStart, RequestPointType.WAYPOINT, null));
        requestPoints.add(new RequestPoint(pointEnd, RequestPointType.WAYPOINT, null));

        drivingRouter.requestRoutes(requestPoints, options, this);
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
        if (routes.size() == 0) return;
        resetRouteData();
        DrivingRoute mRoute = Counter.getMinRoute(routes);
        Polyline polyline = mRoute.getGeometry();
        mapObjects.addPolyline(polyline);

        for (DrivingSection section : mRoute.getSections()) {
            mDistance += section.getMetadata().getWeight().getDistance().getValue();
            TimeWithTraffic += section.getMetadata().getWeight().getTimeWithTraffic().getValue();
            Time += section.getMetadata().getWeight().getTime().getValue();
        }
        Log.i("DrivingRoute", "mDistance " + mDistance);
        Log.i("DrivingRoute", "TimeWithTraffic " + TimeWithTraffic);
        Log.i("DrivingRoute", "Time" + Time);
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = "Error";
        if (error instanceof RemoteError) {
            errorMessage = "remote_error_message";
        } else if (error instanceof NetworkError) {
            errorMessage = "network_error_message";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void resetRouteData() {
        Time = 0;
        mDistance = 0;
        TimeWithTraffic = 0;
        mapObjects.clear();
    }

    private void resetDriverData() {
        mModelTextView.setText("");
        mColorTextView.setText("Empty Drivers");
        mPhoneTextView.setText("");
        mRatingTextView.setText("");
    }

    private void resetCallData() {
        dNumbers = null;
        mModelTextView.setText("");
        mColorTextView.setText("");
        mRatingTextView.setText("");
        mPhoneTextView.setText("");
        mPriceTextView.setText("");
        mDistanceTextView.setText("");
        mTimeTextView.setText("");
    }

    private void addRating(String id, String rat) {

        Driver.Rating rating = new Driver.Rating(id, rat);

        App.getApi().addRating(Constants.CONTENT_TYPE_VALUE, rating).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.i("myLog", "onResponse: " + t.getMessage());
            }
        });
    }

    private void cancel() {
        App.getApi().cancel().enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.i("myLog", "onResponse: " + t.getMessage());
            }
        });
    }

    private void confirmed() {
        App.getApi().confirmed().enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.i("myLog", "onResponse: " + t.getMessage());
            }
        });
    }

    private void successfully() {
        App.getApi().successfully().enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                Log.i("myLog", "onResponse: " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Log.i("myLog", "onResponse: " + t.getMessage());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class DriverTask extends AsyncTask<Void, Void, List<Driver>> {
        @Override
        protected List<Driver> doInBackground(Void... params) {
            try {
                Response<List<Driver>> response = App.getApi().getDrivers().execute();
                if (response.body() == null) return null;
                else return response.body();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("myLog", "IOException: " + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Driver> result) {
            if (result == null){
                resetDriverData();
                return;
            }
            if (dNumbers == null) {
                dNumbers = new ArrayList<>();
            } else {
                for (int i = 0; i < result.size(); i++) {
                    if (dNumbers.contains(result.get(i).getPhone())) {
                        result.remove(i--);
                        Log.i("dNumbers", "onResponse: " + result.size());
                    }
                }
            }
            setDriver(Distance.getNearestDriver(geo, result));
            Log.i("myLog", "result: " + result);
        }
    }
}