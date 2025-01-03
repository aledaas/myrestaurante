package com.app.restaurant.activities;

import static com.app.restaurant.utilities.Constant.GET_SETTINGS;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.restaurant.Config;
import com.app.tucarta.restaurant.R;
import com.app.restaurant.utilities.SharedPref;
import com.app.restaurant.utilities.Tools;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivitySplash extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Tools.lightNavigation(this);

        progressBar = findViewById(R.id.progressBar);
        makeJsonObjectRequest();

    }

    private void makeJsonObjectRequest() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, GET_SETTINGS, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    SharedPref sharedPref = new SharedPref(ActivitySplash.this);
                    sharedPref.setConfig(
                            response.getString("currency_code"),
                            response.getInt("tax"),
                            response.getString("map_location"),
                            response.getString("privacy_policy")
                    );
                    startActivitySelector();
                } catch (JSONException e) {
                    e.printStackTrace();
                    startActivitySelector();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, error -> {
            startActivitySelector();
            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void startMainActivity() {
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }, Config.SPLASH_TIME);
    }
    private void startActivitySelector() {
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), ActivityRestaurantSelector.class);
            startActivity(intent);
            finish();
    }, Config.SPLASH_TIME);


    }
}