package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.delfoodiepassenger.model.Cart;
import com.example.delfoodiepassenger.model.Customer;

import io.realm.Realm;
import io.realm.RealmResults;

public class CartActivity extends AppCompatActivity {
    Realm realm;
    Cart[] cart;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        realm = Realm.getDefaultInstance();
        getCartItems();
        recyclerView = findViewById(R.id.recyclerViewForCart);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CartAdapter cartAdapter = new CartAdapter(cart, CartActivity.this);
        recyclerView.setAdapter(cartAdapter);
    }
    private void getCartItems() {
        RealmResults<Cart> result = realm.where(Cart.class)
                .findAll();
        Log.v("cartitems",""+result);
        cart = new Cart[result.size()];
        for(int i = 0 ; i < result.size() ; i++ ){
            cart[i] = result.get(i);
        }
    }
}