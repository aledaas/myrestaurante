package com.app.restaurant.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.restaurant.activities.MainActivity;
import com.app.restaurant.models.Restaurant;
import com.app.tucarta.restaurant.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RestaurantGridAdapter extends RecyclerView.Adapter<RestaurantGridAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private Context context;
    private OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantGridAdapter(Context context, List<Restaurant> restaurantList, OnRestaurantClickListener listener) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        // Construcción de la URL de la imagen
        String imageUrl = "https://backend.tucarta.restaurant/upload/resto/" + restaurant.getRestoImage();

        // Cargar la imagen con Picasso
        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_loading)
                .resize(400, 250) // Ajustar tamaño para grilla
                .centerCrop()
                .into(holder.imgRestaurant);

        // Setear los datos del restaurante
        holder.txtRestaurantName.setText(restaurant.getRestoTitle());

        // Manejo de clic en el restaurante → ABRIR LA CARTA
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("restaurant_id", restaurant.getRestoId());
            intent.putExtra("restaurant_name", restaurant.getRestoTitle());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        TextView txtRestaurantName, txtRestaurantAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            txtRestaurantName = itemView.findViewById(R.id.txtRestaurantName);
            txtRestaurantAddress = itemView.findViewById(R.id.txtRestaurantAddress);
        }
    }
}