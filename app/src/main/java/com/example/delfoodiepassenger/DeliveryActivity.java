package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;
import com.example.delfoodiepassenger.model.PolylineData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DeliveryActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Double amount;
    Double restaurantLat,restaurantLng, customerLat, customerLng;
    Realm realm;
    Double distanceFromRestaurantToCustomerLocation;
    TextView grossAmountInDelivery, totalDistance, deliveryCharge, amountToPay;
    Button payAmount, viewInMap;
    private GoogleMap mGoogleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "AIzaSyBv-e9tRLwIvp-bRAx5tY7LfWs8BlRpUz4";
    private MapView mapView;
    private GeoApiContext geoApiContext = null;
    private ArrayList<PolylineData> polylinesData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        init();
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBv-e9tRLwIvp-bRAx5tY7LfWs8BlRpUz4")
                    .build();
        }
        grossAmountInDelivery = findViewById(R.id.grossAmountInDelivery);
        totalDistance = findViewById(R.id.totalDistance);
        deliveryCharge = findViewById(R.id.deliveryCharge);
        amountToPay = findViewById(R.id.amountToPay);
        payAmount = findViewById(R.id.payAmount);
        viewInMap = findViewById(R.id.viewInMap);
        Intent intent = getIntent();
        amount = intent.getDoubleExtra("amount",0);
        realm = Realm.getDefaultInstance();
        fetchCustomerLocationFromRealmDatabase();
        fetchRestaurantLocationFromRealmDatabase();
        //Log.v("lat1",""+restaurantLat);
        //Log.v("long1",""+restaurantLng);
        //Log.v("lat2",""+customerLat);
        //Log.v("long2",""+customerLng);
        distanceFromRestaurantToCustomerLocation = distance(restaurantLat,restaurantLng,customerLat,customerLng);
        updateUI();
        calculateDirections();
        viewInMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr="+restaurantLat+","+restaurantLng+"&daddr="+customerLat+","+customerLng+""));
                startActivity(intent);
            }
        });
    }

    private void init() {
        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.profile:
                        startActivity(new Intent(DeliveryActivity.this, ProfileActivity.class));
                        finish();
                        break;
                    case R.id.payment:
                        startActivity(new Intent(DeliveryActivity.this, PaymentActivity.class));
                        finish();
                        break;
                    case R.id.logout:
                        Toast.makeText(DeliveryActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                return true;
            }
        });
    }
    private void calculateDirections(){

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                restaurantLat,
                restaurantLng
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        customerLat,
                        customerLng
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                //ResultSet
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(polylinesData.size() > 0){
                    for(PolylineData mPolylineData : polylinesData){
                        mPolylineData.getPolyline().remove();
                    }
                    polylinesData.clear();
                    polylinesData = new ArrayList<>();
                }
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(DeliveryActivity.this, R.color.colorGrey));
                    polyline.setClickable(true);
                    polylinesData.add(new PolylineData(polyline,route.legs[0]));
                }
            }
        });
    }
    private void updateUI() {
        grossAmountInDelivery.setText("Purchased : $"+round(amount,2));
        totalDistance.setText("Distance : "+round(distanceFromRestaurantToCustomerLocation,2)+"Kms");
        deliveryCharge.setText("Delivery Charge : $"+Math.ceil(distanceFromRestaurantToCustomerLocation));
        final Double total = amount+Math.ceil(distanceFromRestaurantToCustomerLocation);
        amountToPay.setText("Amount To Pay : $"+round(total,2));
        payAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryActivity.this, PaymentActivity.class);
                intent.putExtra("totalAmount", round(total,2));
                startActivity(intent);
            }
        });
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    private void fetchCustomerLocationFromRealmDatabase() {
        RealmResults<Customer> result = realm.where(Customer.class)
                .findAll();
        customerLat = Double.parseDouble(result.first().getLatitude());
        customerLng = Double.parseDouble(result.first().getLongitude());
    }
    private void fetchRestaurantLocationFromRealmDatabase() {
        RealmResults<Cart> result = realm.where(Cart.class)
                .findAll();
        restaurantLat = Double.parseDouble(result.first().getLat());
        restaurantLng = Double.parseDouble(result.first().getLon());

        Log.v("testing2",""+restaurantLat);
        Log.v("testing2",""+restaurantLng);
    }
    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnPolylineClickListener(this::onPolylineClick);
        LatLng coordinates = new LatLng(restaurantLat, restaurantLng);
        googleMap.addMarker(new MarkerOptions().position(coordinates).title("Restuarant"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        mapView.onResume();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            return;
        }
        googleMap.setMyLocationEnabled(true);
        updateCameraPosition(googleMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void updateCameraPosition(GoogleMap googleMap) {
        Location startingLocation = new Location("starting point");
        startingLocation.setLatitude(customerLat); // location is current location
        startingLocation.setLongitude(customerLng);

        //Get the target location
        Location endingLocation = new Location("ending point");
        endingLocation.setLatitude(restaurantLat);
        endingLocation.setLongitude(restaurantLng);

        //Find the Bearing from current location to next location
        float targetBearing = startingLocation.bearingTo(endingLocation);
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(new LatLng(customerLat,customerLng))
                        .bearing(targetBearing)
                        .tilt(90)
                        .zoom(googleMap.getCameraPosition().zoom)
                        .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        for(PolylineData polylineData: polylinesData){
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(DeliveryActivity.this, R.color.colorBlue));
                polylineData.getPolyline().setZIndex(1);
                Log.v("polyline",""+polylineData.getLeg().distance);
                String distance = ""+polylineData.getLeg().distance;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(distance);
                stringBuilder.deleteCharAt(distance.length()-1);
                stringBuilder.deleteCharAt(distance.length()-2);
                stringBuilder.deleteCharAt(distance.length()-3);
                distance = stringBuilder.toString();
                distanceFromRestaurantToCustomerLocation = Double.valueOf(distance);
                updateUI();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(DeliveryActivity.this, R.color.colorGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}