package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;

import io.realm.Realm;
import io.realm.RealmResults;

public class DeliveryActivity extends AppCompatActivity {
    Double amount;
    Double restaurantLat,restaurantLng, customerLat, customerLng;
    Realm realm;
    Double distanceFromRestaurantToCustomerLocation;
    TextView grossAmountInDelivery, totalDistance, deliveryCharge, amountToPay;
    Button payAmount, viewInMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
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

    private void updateUI() {
        grossAmountInDelivery.setText("Purchased : $"+amount);
        totalDistance.setText("Distance : "+distanceFromRestaurantToCustomerLocation+"Kms");
        deliveryCharge.setText("Delivery Charge : $"+distanceFromRestaurantToCustomerLocation);
        Double total = amount+Math.ceil(distanceFromRestaurantToCustomerLocation);
        amountToPay.setText("Amount To Pay : $"+total);
        payAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeliveryActivity.this,PaymentActivity.class));
            }
        });
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
}