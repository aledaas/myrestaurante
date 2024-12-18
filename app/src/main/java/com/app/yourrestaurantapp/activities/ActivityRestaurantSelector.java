package com.app.yourrestaurantapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.yourrestaurantapp.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityRestaurantSelector extends AppCompatActivity {

    private Spinner spinnerRestaurants;
    private Button btnSelect;
    private List<String> restaurantNames = new ArrayList<>();
    private List<String> restaurantIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_selector);

        spinnerRestaurants = findViewById(R.id.spinner_restaurants);
        btnSelect = findViewById(R.id.btn_select_restaurant);

        // Simulación de llamada al servicio - Reemplaza por Retrofit o tu cliente HTTP
        loadRestaurants();

        btnSelect.setOnClickListener(view -> {
            int position = spinnerRestaurants.getSelectedItemPosition();
            if (position >= 0) {
                String selectedRestaurantId = restaurantIds.get(position);

                // Guardar el ID del restaurante en SharedPreferences
                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("SELECTED_RESTAURANT_ID", selectedRestaurantId);
                editor.apply();

                // Navegar a MainActivity
                Intent intent = new Intent(ActivityRestaurantSelector.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, seleccione un restaurante", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRestaurants() {
        // Simulación de datos - Reemplazar con llamada al servicio
        restaurantNames.add("Restaurante A");
        restaurantNames.add("Restaurante B");
        restaurantNames.add("Restaurante C");

        restaurantIds.add("1");
        restaurantIds.add("2");
        restaurantIds.add("3");

        // Configurar el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restaurantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestaurants.setAdapter(adapter);
    }
}