package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private EditText postalCode;
    private Button searchPostalCodeButton;
    private ImageView logoImageView;
    long animationDuration = 1000;


    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoImageView =(ImageView) findViewById(R.id.imageView2);
        init();


    }

    @Override
    protected void onStart() {
        super.onStart();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                handleAnimationleft();
            }
        }, 1000);

    }

    public void handleAnimationleft () {
    ObjectAnimator animatorX = ObjectAnimator.ofFloat(logoImageView, "x", 200f);
    animatorX.setDuration(animationDuration);
    AnimatorSet animatorSetX = new AnimatorSet();
    animatorSetX.playTogether(animatorX);
    animatorSetX.start();
}
    public void handleAnimationright (){
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(logoImageView,"x",1220f);
        animatorY.setDuration(animationDuration);
        AnimatorSet animatorSetY = new AnimatorSet();
        animatorSetY.playTogether(animatorY);
        animatorSetY.start();

}
    private void init() {
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();
        searchPostalCodeButton = findViewById(R.id.searchPostalCode);
        drawerLayout = findViewById(R.id.drawerLayout);
        postalCode = findViewById(R.id.postalCode);
        postalCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final String PostalCodeInString = postalCode.getText().toString();
                if(!isPostalCodeValid(PostalCodeInString)) {
                    searchPostalCodeButton.setVisibility(View.GONE);
                    postalCode.setError("Invalid Postal Code");
                }
                else {
                    searchPostalCodeButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        finish();
                        break;
                    case R.id.payment:
                        startActivity(new Intent(MainActivity.this, PaymentActivity.class));
                        finish();
                        break;
                    case R.id.logout:
                        Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                return true;
            }
        });
        searchPostalCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAnimationright();
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, RestaurantsNearMe.class));
                    }
                }, 1000);

            }
        });
    }

    private boolean isPostalCodeValid(String postalCodeInString) {

        String regex = "^(?!.*[DFIOQU])[A-VXY][0-9][A-Z] ?[0-9][A-Z][0-9]$";


        Pattern pattern = Pattern.compile(regex);


            Matcher matcher = pattern.matcher(postalCodeInString);
            System.out.println(matcher.matches());

        return matcher.matches();
    }



    @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        }
}