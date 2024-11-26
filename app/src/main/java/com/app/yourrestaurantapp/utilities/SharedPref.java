package com.app.yourrestaurantapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.yourrestaurantapp.BuildConfig;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID + " " + "settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveProductList(List<Product> apps) {
        Gson gson = new Gson();
        String json = gson.toJson(apps);
        editor.putString("products", json);
        editor.apply();
    }

    public List<Product> getProductList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("products", null);
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void setConfig(String currencyCode, int tax, String map_location, String privacy_policy) {
        editor.putString("currency_code", currencyCode);
        editor.putInt("tax", tax);
        editor.putString("map_location", map_location);
        editor.putString("privacy_policy", privacy_policy);
        editor.apply();
    }

    public String getCurrencyCode() {
        return sharedPreferences.getString("currency_code", "IDR");
    }

    public int getTax() {
        return sharedPreferences.getInt("tax", 0);
    }

    public String getMapLocation() {
        return sharedPreferences.getString("map_location", "");
    }

    public String getPrivacyPolicy() {
        return sharedPreferences.getString("privacy_policy", "");
    }

    public void saveProfile(String name, String email, String phone, String address) {
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("address", address);
        editor.apply();
    }

    public String getName() {
        return sharedPreferences.getString("name", context.getResources().getString(R.string.default_your_name));
    }

    public String getEmail() {
        return sharedPreferences.getString("email", context.getResources().getString(R.string.default_your_email));
    }

    public String getPhone() {
        return sharedPreferences.getString("phone", context.getResources().getString(R.string.default_your_phone));
    }

    public String getAddress() {
        return sharedPreferences.getString("address", context.getResources().getString(R.string.default_your_address));
    }

//    private String str(int string_id) {
//        return context.getString(string_id);
//    }
//
//    public void setYourName(String name) {
//        sharedPreferences.edit().putString(str(R.string.pref_title_name), name).apply();
//    }
//
//    public String getYourName() {
//        return sharedPreferences.getString(str(R.string.pref_title_name), str(R.string.default_your_name));
//    }
//
//    public void setYourEmail(String name) {
//        sharedPreferences.edit().putString(str(R.string.pref_title_email), name).apply();
//    }
//
//    public String getYourEmail() {
//        return sharedPreferences.getString(str(R.string.pref_title_email), str(R.string.default_your_email));
//    }
//
//    public void setYourPhone(String name) {
//        sharedPreferences.edit().putString(str(R.string.pref_title_phone), name).apply();
//    }
//
//    public String getYourPhone() {
//        return sharedPreferences.getString(str(R.string.pref_title_phone), str(R.string.default_your_phone));
//    }
//
//    public void setYourAddress(String name) {
//        sharedPreferences.edit().putString(str(R.string.pref_title_address), name).apply();
//    }
//
//    public String getYourAddress() {
//        return sharedPreferences.getString(str(R.string.pref_title_address), str(R.string.default_your_address));
//    }

}
