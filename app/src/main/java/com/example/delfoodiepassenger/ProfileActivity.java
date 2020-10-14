package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Customer;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProfileActivity extends AppCompatActivity {
    EditText name, contact,address;
    TextView email;
    Button saveProfileDetails;
    Realm realm;
    String latitude = "100.0", longitude = "200.0";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        init();
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        contact = findViewById(R.id.contact);
        address = findViewById(R.id.address);
        saveProfileDetails = findViewById(R.id.saveProfileDetails);
        realm = Realm.getDefaultInstance();
        updateUI();
        saveProfileDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRealmDatabase(name.getText().toString().trim(), email.getText().toString().trim(),contact.getText().toString().trim(),
                        latitude,longitude);
                updateUI();
            }
        });
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }
        updateLocation();
    }
    private void updateLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = String.valueOf(location.getLongitude());
        latitude = String.valueOf(location.getLatitude());
        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                longitude = String.valueOf(location.getLongitude());
                latitude = String.valueOf(location.getLatitude());
            }
        };
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        Log.v("location", String.valueOf(location.getLongitude()+location.getLatitude()));
        Toast.makeText(this, "updatelocation"+location.getLongitude()+location.getLatitude(), Toast.LENGTH_SHORT).show();
    }
    private void updateRealmDatabase(final String name, final String email, final String contact, final String latitute, final String longitude) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute (Realm realm) {
                Customer customer = realm.where(Customer.class).equalTo("email", email).findFirst();
                if(customer == null) {
                    Toast.makeText(ProfileActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
                customer.setName(name);
                customer.setContact(contact);
                customer.setLatitude(latitute);
                customer.setLongitude(longitude);

            }
        });
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        RealmResults<Customer> result = realm.where(Customer.class)
                .findAll();
        name.setText(result.first().getName());
        email.setText(result.first().getEmailId());
        contact.setText(result.first().getContact());
        address.setText("Lat : " + result.first().getLatitude() + " Long : " + result.first().getLongitude());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ProfileActivity.this,MainActivity.class));
        finish();
    }
}