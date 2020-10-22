package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;

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
    ArrayList<String> itemName = new ArrayList<>();
    ArrayList<String> itemPrice = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu_list);
        if (getIntent().hasExtra("restaurantName")) {
            restaurantName = getIntent().getStringExtra("restaurantName");
        }
        getJson();
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
                    itemName.add(jsonObject.getString("item_name"));
                    itemPrice.add(jsonObject.getString("item_price"));
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