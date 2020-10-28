package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.delfoodiepassenger.model.Cart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class RestaurantMenuListActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    String restaurantName;
    Realm realm;
    ArrayList<String> itemId = new ArrayList<>();
    ArrayList<String> itemName = new ArrayList<>();
    ArrayList<String> itemPrice = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    ItemListData[] itemsListData;
    Button viewCart;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu_list);
        if (getIntent().hasExtra("restaurantName")) {
            restaurantName = getIntent().getStringExtra("restaurantName");
        }
        getJson();
        init();
        ItemListAdapter itemListAdapter = new ItemListAdapter(itemsListData,this);
        recyclerView.setAdapter(itemListAdapter);
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
                        startActivity(new Intent(RestaurantMenuListActivity.this, ProfileActivity.class));
                        finish();
                        break;
                    case R.id.payment:
                        startActivity(new Intent(RestaurantMenuListActivity.this, PaymentActivity.class));
                        finish();
                        break;
                    case R.id.logout:
                        Toast.makeText(RestaurantMenuListActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                return true;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        viewCart = findViewById(R.id.viewCart);
        realm = Realm.getDefaultInstance();
        /*
        RealmResults<Cart> result = realm.where(Cart.class)
                .findAll();
        result.addChangeListener(new RealmChangeListener<RealmResults<Cart>>() {
            @Override
            public void onChange(RealmResults<Cart> carts) {
                if(carts.isEmpty()){
                    viewCart.setVisibility(View.GONE);
                }
                else if(!carts.get(0).getItemName().isEmpty()){
                    viewCart.setVisibility(View.VISIBLE);
                }
            }
        });
        */
        viewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsListData = new ItemListData[itemName.size()];
        for(int i = 0; i < itemName.size();i++){
            itemsListData[i] = new ItemListData(itemId.get(i), itemName.get(i),itemPrice.get(i),imageUrl.get(i));
        }
    }

    public void getJson(){
        String json;
        try{
            InputStream inputStream = getAssets().open("restaurants.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer,"UTF-8");
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.getString("restaurant_name").equals(restaurantName)){
                    itemId.add(jsonObject.getString("id"));
                    itemName.add(jsonObject.getString("item_name"));
                    itemPrice.add(jsonObject.getString("item_price"));
                    imageUrl.add(jsonObject.getString("image_URL"));
                }
            }

        }
        catch (IOException exception){
            exception.printStackTrace();
            Log.v("aaa","4");
        }
        catch (JSONException jsonException){
            jsonException.printStackTrace();
            Log.v("aaa","5");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}