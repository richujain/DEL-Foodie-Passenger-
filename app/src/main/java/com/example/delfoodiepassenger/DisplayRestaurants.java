package com.example.delfoodiepassenger;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

    private ListView mListView;
    private static final String API_KEY = "AIzaSyDqLGYkb-JRDLoh9lRnRDCrvQMUCUjqRQI";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";
    private static final String OUT_JSON = "/json?";
    private static final String LOG_TAG = "ListRest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_restaurants);
        Intent intent = getIntent();
        String longitude = intent.getStringExtra("long");
        String latitude = intent.getStringExtra("lat");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Double lng = Double.parseDouble(longitude);
        Double lat = Double.parseDouble(latitude);
        int radius = 3000;


        ArrayList<Place> list = search(lat, lng, radius);
        String[] maintitle = new String[list.size()];
        String[] subtitle = new String[list.size()];
        String[] imageUrl = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            maintitle[i] = list.get(i).name;
            subtitle[i] = list.get(i).rating;
            switch (maintitle[i]){
                case "Boston Pizza" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Boston_Pizza.jpg";
                                     break;
                case "Tim Hortons" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Tim_Hortons.png";
                    break;
                case "McDonald's" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/McDonalds.jpeg";
                    break;
                case "Harvey's" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Harveys.png";
                    break;
                case "Popeyes Louisana Kitchen" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Popeyes-Louisana-Kitchen.jpg";
                    break;
                case "Subway" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Subway.png";
                    break;
                case "KFC" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/KFC.jpg";
                    break;
                case "Pizza Pizza" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Pizza-Pizza.png";
                    break;
                case "A&W Restaurants" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/AW_Restaurants.jpg";
                    break;
                case "Burger King" : imageUrl[i] = "https://qualitymiddleeast.com/wp-content/uploads/2020/10/Burger_King.png";
                    break;
                default: imageUrl[i] = list.get(i).icon;
            }

        }

        if (list != null)
        {
            /*mListView = (ListView) findViewById(R.id.listView);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list);
            mListView.setAdapter(adapter);*/
            MyListAdapter adapter=new MyListAdapter(this, maintitle, subtitle,imageUrl);
            mListView=(ListView)findViewById(R.id.listView);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    if(position == 0) {
                        //code specific to first list item
                        Toast.makeText(getApplicationContext(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
                    }

                    else if(position == 1) {
                        //code specific to 2nd list item
                        Toast.makeText(getApplicationContext(),"Place Your Second Option Code", Toast.LENGTH_SHORT).show();
                    }

                    else if(position == 2) {

                        Toast.makeText(getApplicationContext(),"Place Your Third Option Code",Toast.LENGTH_SHORT).show();
                    }
                    else if(position == 3) {

                        Toast.makeText(getApplicationContext(),"Place Your Forth Option Code",Toast.LENGTH_SHORT).show();
                    }
                    else if(position == 4) {

                        Toast.makeText(getApplicationContext(),"Place Your Fifth Option Code",Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
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
                Log.e("name",""+predsJsonArray.getJSONObject(i));

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