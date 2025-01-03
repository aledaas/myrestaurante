package com.app.restaurant.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.tucarta.restaurant.R;
import com.app.restaurant.utilities.MaterialProgressDialog;
import com.app.restaurant.utilities.SharedPref;
import com.app.restaurant.utilities.Tools;
import com.google.android.material.snackbar.Snackbar;

public class ActivitySettings extends AppCompatActivity {

    EditText edt_name, edt_email, edt_phone, edt_address;
    String str_name, str_email, str_phone, str_address;
    Button btnUpdate;
    SharedPref sharedPref;
    MaterialProgressDialog.Builder progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Tools.lightNavigation(this);

        progressDialog = new MaterialProgressDialog.Builder(this);
        progressDialog.build();

        sharedPref = new SharedPref(this);

        initView();
        displayData();

        initToolbar();
    }

    private void initView() {
        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_phone = findViewById(R.id.edt_phone);
        edt_address = findViewById(R.id.edt_address);
        btnUpdate = findViewById(R.id.btn_update);
    }

    private void displayData() {
        edt_name.setText(sharedPref.getName());
        edt_email.setText(sharedPref.getEmail());
        edt_phone.setText(sharedPref.getPhone());
        edt_address.setText(sharedPref.getAddress());
        btnUpdate.setOnClickListener(view -> updateData());
    }

    private void updateData() {
        str_name = edt_name.getText().toString();
        str_email = edt_email.getText().toString();
        str_phone = edt_phone.getText().toString();
        str_address = edt_address.getText().toString();
        if (str_name.equalsIgnoreCase("") ||
                str_email.equalsIgnoreCase("") ||
                str_phone.equalsIgnoreCase("") ||
                str_address.equalsIgnoreCase("")) {
            Snackbar.make(findViewById(android.R.id.content), "Data cannot be empty", Snackbar.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage(getString(R.string.msg_updating_profile));
            progressDialog.show();
            sharedPref.saveProfile(str_name, str_email, str_phone, str_address);
            new Handler(Looper.getMainLooper()).postDelayed(()-> {
                progressDialog.dismiss();
                finish();
            }, 2000);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_profile);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
