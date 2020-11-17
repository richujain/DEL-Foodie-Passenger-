package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import io.realm.Realm;
import io.realm.RealmResults;

public class DeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Double amount;
    Double restaurantLat,restaurantLng, customerLat, customerLng;
    Realm realm;
    Double distanceFromRestaurantToCustomerLocation;
    TextView grossAmountInDelivery, totalDistance, deliveryCharge, amountToPay;
    Button payAmount, viewInMap;
    private static final String MAPVIEW_BUNDLE_KEY = "AIzaSyBv-e9tRLwIvp-bRAx5tY7LfWs8BlRpUz4";
    private MapView mapView;
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
}