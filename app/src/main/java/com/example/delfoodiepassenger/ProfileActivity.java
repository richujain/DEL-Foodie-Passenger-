package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
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
    EditText name, contact;
    TextView email;
    Button saveProfileDetails;
    Realm realm;
    String latitute, longitude;
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
        saveProfileDetails = findViewById(R.id.saveProfileDetails);
        realm = Realm.getDefaultInstance();
        //updateUI();
        saveProfileDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRealmDatabase(name.getText().toString().trim(), email.getText().toString().trim(),contact.getText().toString().trim(),
                        latitute,longitude);
            }
        });
    }

    private void writeToRealmDatabase(final String name, final String email, final String contact, final String latitute, final String longitude) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Customer customer = bgRealm.createObject(Customer.class);
                customer.setName(name);
                customer.setEmail(email);
                customer.setContact(contact);
                customer.setLatitude(latitute);
                customer.setLongitude(longitude);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileActivity.this, "Updated Data", Toast.LENGTH_SHORT).show();
                Log.v("database","Data Inserted");
                updateUI();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                Toast.makeText(ProfileActivity.this, "Error Updating Data", Toast.LENGTH_SHORT).show();
                Log.e("database",error.getMessage());
            }
        });
    }

    private void updateUI() {
        RealmResults<Customer> result = realm.where(Customer.class)
                .equalTo("email", email.getText().toString().trim())
                .findAll();
        Log.v("trial",result.first().getName());
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