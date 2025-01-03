package com.app.restaurant.activities;

import static com.app.restaurant.utilities.Constant.GET_RESTOS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.StringRequest;
import com.app.tucarta.restaurant.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


        loadRestaurants();

        btnSelect.setOnClickListener(view -> {
            int position = spinnerRestaurants.getSelectedItemPosition();
            if (position >= 0) {
                String selectedRestaurantId = restaurantIds.get(position);
                String selectedRestaurantName = restaurantNames.get(position);

                // Guardar el ID del restaurante en SharedPreferences
                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("SELECTED_RESTAURANT_ID", selectedRestaurantId);
                editor.putString("SELECTED_RESTAURANT_NAME", selectedRestaurantName);
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
        Log.d("LoadRestaurants", "Fetching restaurants from URL: " + GET_RESTOS);

        StringRequest request = new StringRequest(GET_RESTOS, response -> {
            if (response == null) {
                Toast.makeText(this, "Error al cargar restaurantes", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                // Parsear la respuesta JSON
                JSONObject json = new JSONObject(response);
               // JSONArray restaurantResponse = new JSONArray(response);
                JSONArray restaurantResponse = json.getJSONArray("restos");

                // Limpiar las listas actuales
                restaurantNames.clear();
                restaurantIds.clear();

                // Procesar cada restaurante en la respuesta
                for (int i = 0; i < restaurantResponse.length(); i++) {
                    JSONObject restaurantObject = restaurantResponse.getJSONObject(i);
                    String id = restaurantObject.getString("resto_id");
                    String name = restaurantObject.getString("resto_title");

                    restaurantIds.add(id);
                    restaurantNames.add(name);
                }

                // Configurar el Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, restaurantNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRestaurants.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("LoadRestaurants", "Error fetching restaurants: " + error.getMessage());
            Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
        });

        // Agregar la solicitud a la cola de Volley
        MyApplication.getInstance().addToRequestQueue(request);
    }
}