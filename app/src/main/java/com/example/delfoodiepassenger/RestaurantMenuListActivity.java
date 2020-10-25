package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RestaurantMenuListActivity extends AppCompatActivity {
    String restaurantName;
    ArrayList<String> itemId = new ArrayList<>();
    ArrayList<String> itemName = new ArrayList<>();
    ArrayList<String> itemPrice = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    ItemListData[] itemsListData;
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
        recyclerView = findViewById(R.id.recyclerView);
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
}