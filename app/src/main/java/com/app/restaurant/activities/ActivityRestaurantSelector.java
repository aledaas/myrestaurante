package com.app.restaurant.activities;

import static com.app.restaurant.utilities.Constant.GET_RESTOS;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.restaurant.adapters.RestaurantGridAdapter;
import com.app.restaurant.models.Restaurant;
import com.app.tucarta.restaurant.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityRestaurantSelector extends AppCompatActivity {

    private RecyclerView recyclerViewRestaurants;
    private RestaurantGridAdapter adapter;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_selector);

        // Inicializar RecyclerView con GridLayoutManager
        recyclerViewRestaurants = findViewById(R.id.recyclerViewRestaurants);
        recyclerViewRestaurants.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RestaurantGridAdapter(this, restaurantList);
        recyclerViewRestaurants.setAdapter(adapter);

        // Inicializar cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener ubicación del usuario y buscar zona
        getUserLocation();
    }

    /**
     * Obtiene la ubicación del usuario y busca la zona más cercana.
     */
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, // prioridad
                10000 // intervalo en milisegundos
        )
                .setMinUpdateIntervalMillis(5000) // intervalo más rápido
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    double userLat = location.getLatitude();
                    double userLng = location.getLongitude();
                    Log.d("UserLocation", "Lat: " + userLat + ", Lng: " + userLng);
                    checkUserZone(userLat, userLng);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Llama a la API para obtener la zona más cercana.
     */
    private void findNearestZone(double lat, double lng) {
        Log.e("depurando", "ingresando a findNearestZone: con estas coordenadas: " + lat + " " + lng);
        String url = "https://backend.tucarta.restaurant/api/api.php?get_nearest_zone&lat=" + lat + "&lng=" + lng;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("zona_id")) {
                    int zonaId = jsonResponse.getInt("zona_id");
                    Log.d("NearestZone", "Zona ID: " + zonaId);

                    // Guardar la zona en SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("SELECTED_ZONE_ID", zonaId);
                    editor.apply();

                    // Cargar los restaurantes de esta zona
                    loadRestaurants(zonaId);
                } else {
                    Toast.makeText(this, "No se encontró una zona cercana", Toast.LENGTH_SHORT).show();
                    loadRestaurants(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                loadRestaurants(null);
            }
        }, error -> {
            Log.e("ZoneError", "Error al obtener la zona más cercana: " + error.getMessage());
            loadRestaurants(null);
        });

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Carga los restaurantes de la zona encontrada o todos si no se encuentra una zona.
     */
    private void loadRestaurants(Integer zonaId) {
        Log.e("depurando", "ingresando a loadRestaurants: con esta zona: " + zonaId);
        String url = GET_RESTOS;
        if (zonaId != null) {
            url = "https://backend.tucarta.restaurant/api/api.php?get_restos&zona_id=" + zonaId;
        }

        Log.d("LoadRestaurants", "Fetching restaurants from URL: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if (response == null) {
                Toast.makeText(this, "Error al cargar restaurantes", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject json = new JSONObject(response);
                JSONArray restaurantResponse = json.getJSONArray("restos");

                restaurantList.clear();

                for (int i = 0; i < restaurantResponse.length(); i++) {
                    JSONObject restaurantObject = restaurantResponse.getJSONObject(i);
                    String id = restaurantObject.getString("resto_id");
                    String name = restaurantObject.getString("resto_title");
                    String imageUrl = restaurantObject.getString("resto_image");
                    String description = restaurantObject.getString("resto_description");
                    String address = restaurantObject.getString("resto_address");
                    String celular = restaurantObject.getString("resto_celular");
                    String telfijo = restaurantObject.getString("resto_telfijo");
                    String whatsapp = restaurantObject.getString("resto_whatshap");
                    String date = restaurantObject.getString("resto_date");

                    restaurantList.add(new Restaurant(id, name, imageUrl, description, address, celular, telfijo, whatsapp, date));
                }

                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("LoadRestaurants", "Error fetching restaurants: " + error.getMessage());
            Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Maneja la respuesta del usuario al pedir permisos de ubicación.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        }
    }
    private void checkUserZone(double lat, double lng) {
        Log.e("depurando", "ingresando a checkUserZone: con estas coordenadas: " + lat + " " + lng);
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        int selectedZoneId = prefs.getInt("SELECTED_ZONE_ID", -1);
        String url = "https://backend.tucarta.restaurant/api/api.php?check_user_zone&lat=" + lat + "&lng=" + lng + "&zona_id=" + selectedZoneId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean isInZone = jsonResponse.getBoolean("is_in_zone");
                if (!isInZone) {
                    Toast.makeText(this, "Te has salido de la zona. Actualizando restaurantes...", Toast.LENGTH_LONG).show();
                    findNearestZone(lat, lng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("ZoneCheck", "Error verificando la zona: " + error.getMessage());
        });

        Volley.newRequestQueue(this).add(request);
    }
}