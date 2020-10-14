package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Customer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.realm.Realm;

public class WelcomeActivity extends AppCompatActivity {
    Button btnSignIn;
    RelativeLayout rootLayout;
    Realm realm;
    String latitude = "100.0", longitude = "200.0";
    //get access to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        init();
    }

    private void init() {

        btnSignIn = findViewById(R.id.btnSignIn);
        realm = Realm.getDefaultInstance();
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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


    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        dialog.setTitle("Sign In");


        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtName = login_layout.findViewById(R.id.edtName);
        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtContact = login_layout.findViewById(R.id.edtContact);

        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //validation
                if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Snackbar.make(rootLayout, "Please Enter Your Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtEmail.getText().toString().trim())) {
                    Snackbar.make(rootLayout, "Please Enter Your Email Address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtContact.getText().toString().trim())) {
                    Snackbar.make(rootLayout, "Please Enter Your Contact Number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtContact.getText().toString().trim().length() < 10) {
                    Snackbar.make(rootLayout, "Enter Valid Contact Number!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                writeToRealmDatabase(edtName.getText().toString().trim(), edtEmail.getText().toString().trim(),edtContact.getText().toString().trim(),
                        latitude,longitude);

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
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
                Toast.makeText(WelcomeActivity.this, "Data Inserted", Toast.LENGTH_SHORT).show();
                Log.v("database","Data Inserted");
                startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                finish();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                Toast.makeText(WelcomeActivity.this, "Error Inserting Data", Toast.LENGTH_SHORT).show();
                Log.e("database",error.getMessage());
            }
        });
    }
}