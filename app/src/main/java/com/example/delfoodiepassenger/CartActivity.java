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

public class CartActivity extends AppCompatActivity {
    Realm realm;
    Cart[] cart;
    RecyclerView recyclerView;
    CartAdapter cartAdapter;
    TextView netAmount, grossAmount;
    Double amount = 0.0;
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
            amount = Double.parseDouble(""+result.get(i).getItemPrice());
        }
        netAmount = findViewById(R.id.netAmount);
        grossAmount = findViewById(R.id.grossAmount);
        checkOut = findViewById(R.id.checkOut);
        netAmount.setText("Net Amount : $" + amount);
        Double gross = amount + (amount*0.13);
        grossAmount.setText("Gross Amount : $" + gross);
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CartActivity.this, DeliveryActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),RestaurantsNearMe.class));
        finish();
    }
}