package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static java.lang.Math.round;

public class CartActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Realm realm;
    Cart[] cart;
    RecyclerView recyclerView;
    CartAdapter cartAdapter;
    TextView netAmount, grossAmount;
    Double amount = 0.0;
    int quantity = 1;
    Button checkOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        realm = Realm.getDefaultInstance();
        getCartItems();
        recyclerView = findViewById(R.id.recyclerViewForCart);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(cart, CartActivity.this);
        init();
        recyclerView.setAdapter(cartAdapter);


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
                        startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                        finish();
                        break;
                    case R.id.payment:
                        startActivity(new Intent(CartActivity.this, PaymentActivity.class));
                        finish();
                        break;
                    case R.id.logout:
                        Toast.makeText(CartActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                return true;
            }
        });
    }



    private void getCartItems() {
        RealmResults<Cart> result = realm.where(Cart.class)
                .findAll();
        result.addChangeListener(new RealmChangeListener<RealmResults<Cart>>() {
            @Override
            public void onChange(RealmResults<Cart> carts) {
                Log.v("testing","testin");
                startActivity(new Intent(getApplicationContext(),CartActivity.class));
                finish();
            }
        });
        Log.v("cartitems",""+result);
        cart = new Cart[result.size()];
        for(int i = 0 ; i < result.size() ; i++ ){
            cart[i] = result.get(i);
            quantity = Integer.parseInt(""+result.get(i).getItemQuantity());
            amount = amount + (Double.parseDouble(""+result.get(i).getItemPrice())*quantity);
        }
        netAmount = findViewById(R.id.netAmount);
        grossAmount = findViewById(R.id.grossAmount);
        checkOut = findViewById(R.id.checkOut);
        netAmount.setText("Net Amount : $" + round(amount,2));
        Double gross = amount + (amount*0.13);
        grossAmount.setText("Gross Amount : $" + round(gross,2));
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, DeliveryActivity.class);
                intent.putExtra("amount", amount);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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
    private void showCloseDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        dialog.setTitle("Going Back Will Clear Your Cart. Are you sure you still want to go back?");


        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_dialog_box,null);

        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("Clear and Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                RealmResults<Cart> results = realm.where(Cart.class)
                        .findAll();
                realm.beginTransaction();
                results.deleteAllFromRealm();
                realm.commitTransaction();
               Intent intent = new Intent(CartActivity.this, RestaurantsNearMe.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(intent);
               finish();
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
    @Override
    public void onBackPressed() {
        showCloseDialog();

    }
    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}