package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static java.lang.Math.round;

public class CartActivity extends AppCompatActivity {
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
        recyclerView.setAdapter(cartAdapter);


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
        netAmount.setText("Net Amount : $" + amount);
        Double gross = amount + (amount*0.13);
        grossAmount.setText("Gross Amount : $" + round(gross,2));
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CartActivity.this, DeliveryActivity.class);
                intent.putExtra("amount", amount);
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),RestaurantsNearMe.class));
        finish();
    }
}