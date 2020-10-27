package com.example.delfoodiepassenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.delfoodiepassenger.databinding.ActivityPaymentBinding;
import com.example.delfoodiepassenger.model.*;
import com.example.delfoodiepassenger.util.*;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.stripe.android.databinding.ActivityCheckoutBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class PaymentActivity extends AppCompatActivity {
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final long SHIPPING_COST_CENTS = 90 * PaymentsUtil.CENTS_IN_A_UNIT.longValue();
    private PaymentsClient paymentsClient;
    private ActivityPaymentBinding layoutBinding;
    private View googlePayButton;
    private EditText cardExpiry, cardNumber, cvv;
    private TextView amount;
    private Button savePaymentDetails;
    Realm realm;
    Double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        paymentsClient = PaymentsUtil.createPaymentsClient(this);
        possiblyShowGooglePayButton();
        initializeUi();
        init();
    }
    private void init() {
        cardNumber = findViewById(R.id.cardNumber);
        cvv = findViewById(R.id.cvv);
        cardExpiry = findViewById(R.id.cardExpiry);
        savePaymentDetails = findViewById(R.id.savePaymentDetails);
        realm = Realm.getDefaultInstance();
        updateUI();
        Toolbar toolbar = findViewById(R.id.toolbarPayment);
        toolbar.bringToFront();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cardExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(PaymentActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }


        });
        savePaymentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrAddCreditCard(cardNumber.getText().toString().trim(), cardExpiry.getText().toString().trim(), cvv.getText().toString().trim());
                updateUI();
            }
        });
    }

    private void updateOrAddCreditCard(final String cardNumber, final String cardExpiry, final String cvv) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute (Realm realm) {
                Customer customer = realm.where(Customer.class).equalTo("email", getCustomerEmail()).findFirst();
                if(customer == null) {
                    Toast.makeText(PaymentActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
                customer.setCardNumber(cardNumber);
                customer.setCardExpiry(cardExpiry);
                customer.setCvv(cvv);
            }
        });
    }
    private void updateUI(){
        RealmResults<Customer> result = realm.where(Customer.class)
                .findAll();
        cardNumber.setText(result.first().getCardNumber());
        cardExpiry.setText(result.first().getCardExpiry());
        cvv.setText(result.first().getCvv());
    }
    private String getCustomerEmail(){
        RealmResults<Customer> result = realm.where(Customer.class)
                .findAll();
        return result.first().getEmailId();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;

                    case Activity.RESULT_CANCELED:
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                }

                googlePayButton.setClickable(true);
        }
    }
    private void initializeUi() {

        layoutBinding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());
        amount = findViewById(R.id.amount);
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("totalAmount",0);
        amount.setText(""+totalAmount);
        googlePayButton = layoutBinding.googlePayButton.getRoot();
        googlePayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPayment(view);
                    }
                });
    }
    private void possiblyShowGooglePayButton() {

        final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            setGooglePayAvailable(task.getResult());
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();
        if (paymentInfo == null) {
            return;
        }
        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            final String tokenizationType = tokenizationData.getString("type");
            final String token = tokenizationData.getString("token");

            if ("PAYMENT_GATEWAY".equals(tokenizationType) && "examplePaymentMethodToken".equals(token)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage(getString(R.string.gateway_replace_name_example))
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
            }

            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
            Toast.makeText(
                    this, getString(R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show();

            // Logging token string.
            Log.d("Google Pay token: ", token);
            RealmResults<Cart> result = realm.where(Cart.class)
                    .findAll();
            // All changes to data must happen in a transaction
            realm.beginTransaction();

            // Delete all matches
            result.deleteAllFromRealm();

            realm.commitTransaction();
            startActivity(new Intent(PaymentActivity.this,RestaurantsNearMe.class));
            finish();

        } catch (JSONException e) {
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }
    private void handleError(int statusCode) {
        Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    public void requestPayment(View view) {

        // Disables the button to prevent multiple clicks.
        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        double garmentPrice = Double.valueOf(amount.getText().toString());
        long garmentPriceCents = Math.round(garmentPrice * PaymentsUtil.CENTS_IN_A_UNIT.longValue());
        long priceCents = garmentPriceCents + SHIPPING_COST_CENTS;

        Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
        if (!paymentDataRequestJson.isPresent()) {
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }
    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        cardExpiry.setText(sdf.format(myCalendar.getTime()));
    }

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };


    @Override
    public void onBackPressed() {
        startActivity(new Intent(PaymentActivity.this,MainActivity.class));
        finish();
    }

}