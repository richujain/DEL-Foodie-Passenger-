package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class DisplayRestaurants extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private ListView mListView;
    private static final String API_KEY = "AIzaSyDqLGYkb-JRDLoh9lRnRDCrvQMUCUjqRQI";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";
    private static final String OUT_JSON = "/json?";
    private static final String LOG_TAG = "ListRest";
    CardView cardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_restaurants);
        Intent intent = getIntent();
        String longitude = intent.getStringExtra("long");
        String latitude = intent.getStringExtra("lat");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        init();
        Double lng = Double.parseDouble(longitude);
        Double lat = Double.parseDouble(latitude);
        int radius = 3000;

        ArrayList<Place> list = search(lat, lng, radius);
        final String[] maintitle = new String[list.size()];
        String[] subtitle = new String[list.size()];
        String[] imageUrl = new String[list.size()];
        String[] coverUrl = new String[list.size()];
        final String[] restaurantLat = new String[list.size()];
        final String[] restaurantLng = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            maintitle[i] = list.get(i).name;
            subtitle[i] = list.get(i).rating;
            restaurantLat[i] = list.get(i).lat;
            restaurantLng[i] = list.get(i).lng;
            switch (maintitle[i]){
                case "Boston Pizza" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Boston_Pizza.jpg";
                    coverUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/boston-1.jpeg";
                    break;
                case "Tim Hortons" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Tim_Hortons.png";
                    coverUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/tim.jpg";
                    break;
                case "McDonald's" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/McDonalds.jpeg";
                    coverUrl[i] ="https://qualitymiddleeast.com/wp-content/uploads/2020/10/mcd.jpg";
                    break;
                case "Harvey's" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Harveys.png";
                    coverUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/harveys.jpg";
                    break;
                case "Popeyes Louisana Kitchen" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Popeyes-Louisana-Kitchen.jpg";
                    coverUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/popeyes.jpg";
                    break;
                case "Subway" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Subway.png";
                    coverUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/subway-1.png";
                    break;
                case "KFC" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/KFC.jpg";
                    coverUrl[i] ="https://qualitymiddleeast.com/wp-content/uploads/2020/10/kfc-1.jpg";
                    break;
                case "Pizza Pizza" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Pizza-Pizza.png";
                    coverUrl[i] ="https://qualitymiddleeast.com/wp-content/uploads/2020/10/pizza-pizza.jpg";
                    break;
                case "A&W Restaurants" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/AW_Restaurants.jpg";
                    coverUrl[i] ="https://qualitymiddleeast.com/wp-content/uploads/2020/10/AandW.jpg";
                    break;
                case "Burger King" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Burger_King.png";
                    coverUrl[i] ="https://qualitymiddleeast.com/wp-content/uploads/2020/10/burgerking.jpg";
                    break;
                default: imageUrl[i] = list.get(i).icon;
            }
        }

        if (list != null)
        {
            /*mListView = (ListView) findViewById(R.id.listView);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list);
            mListView.setAdapter(adapter);*/
            MyListAdapter adapter=new MyListAdapter(this, maintitle, subtitle,imageUrl,coverUrl);
            mListView=(ListView)findViewById(R.id.listView);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(DisplayRestaurants.this, RestaurantMenuListActivity.class);
                    intent.putExtra("restaurantName", maintitle[position]);
                    intent.putExtra("restaurantLat", restaurantLat[position]);
                    intent.putExtra("restaurantLng", restaurantLng[position]);
                    startActivity(intent);

                }
            });

        }
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
                        startActivity(new Intent(DisplayRestaurants.this, ProfileActivity.class));
                        finish();
                        break;
                    case R.id.payment:
                        startActivity(new Intent(DisplayRestaurants.this, PaymentActivity.class));
                        finish();
                        break;
                    case R.id.logout:
                        Toast.makeText(DisplayRestaurants.this, "Logout", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                return true;
            }
        });
    }

    public static ArrayList<Place> search(double lat, double lng, int radius) {
        ArrayList<Place> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_SEARCH);
            sb.append(OUT_JSON);
            sb.append("location=" + String.valueOf(lat) + "," + String.valueOf(lng));
            sb.append("&radius=" + String.valueOf(radius));
            sb.append("&type=restaurant");
            sb.append("&key=" + API_KEY);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");
            // Extract the descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++) {
                Place place = new Place();
                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
                place.name = predsJsonArray.getJSONObject(i).getString("name");
                place.icon = predsJsonArray.getJSONObject(i).getString("icon");
                place.lat = predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                place.lng = predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                place.rating = predsJsonArray.getJSONObject(i).getString("rating");
                String[] foodChains = {
                        "Boston Pizza",
                        "Tim Hortons",
                        "McDonald's",
                        "Harvey's",
                        "Popeyes Louisana Kitchen",
                        "Subway",
                        "KFC",
                        "Pizza Pizza",
                        "A&W Restaurants",
                        "Burger King",
                };
                if (Arrays.asList(foodChains).contains(place.name)) {
                    resultList.add(place);
                }
                //Log.e("name",""+predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat"));
                //Log.e("name",""+predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng"));
                //Log.e("name",""+predsJsonArray.getJSONObject(i));

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }

        return resultList;
    }


    //Value Object for the ArrayList
    public static class Place {
        private String reference;
        private String name;
        private String icon;
        private String lat;
        private String lng;
        private String rating;

        public Place(){
            super();
        }


    }
}