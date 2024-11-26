package com.app.yourrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.activities.ActivityCart;
import com.app.yourrestaurantapp.models.Cart;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.app.yourrestaurantapp.utilities.Tools;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.ViewHolder> {

    private Context context;
    private List<Cart> arrayCart;
    SharedPref sharedPref;
    DBHelper dbHelper;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView product_name;
        TextView product_quantity;
        TextView product_price;
        ImageView product_image;
        Button btn_decrease;
        Button btn_increase;
        TextView txt_quantity;

        public ViewHolder(View view) {
            super(view);
            product_name = view.findViewById(R.id.product_name);
            product_quantity = view.findViewById(R.id.product_quantity);
            product_price = view.findViewById(R.id.product_price);
            product_image = view.findViewById(R.id.product_image);
            btn_decrease = view.findViewById(R.id.btn_decrease);
            btn_increase = view.findViewById(R.id.btn_increase);
            txt_quantity = view.findViewById(R.id.txt_quantity);
        }

    }

    public AdapterCart(Context context, List<Cart> arrayCart) {
        this.context = context;
        this.arrayCart = arrayCart;
        this.sharedPref = new SharedPref(context);
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.product_name.setText(ActivityCart.product_name.get(position));

        double _single_item = ActivityCart.sub_total_price.get(position) / ActivityCart.product_quantity.get(position);
        holder.product_quantity.setText(Tools.decimalFormatter(_single_item) + " " + sharedPref.getCurrencyCode() + " x " + ActivityCart.product_quantity.get(position));
        holder.product_price.setText(Tools.decimalFormatter(ActivityCart.sub_total_price.get(position)) + " " + sharedPref.getCurrencyCode());

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/product/" + ActivityCart.product_image.get(position))
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .into(holder.product_image);

        holder.btn_decrease.setOnClickListener(view -> {
            int id = ActivityCart.product_id.get(position);
            int qty = ActivityCart.product_quantity.get(position) - 1;
            double price = (_single_item * qty);

            if (ActivityCart.product_quantity.get(position) > 1) {
                dbHelper.updateData(id, qty, price);
            } else {
                ((ActivityCart) context).removeSingleItem(id);
            }
            ((ActivityCart) context).clearData();
            ((ActivityCart) context).updateCart();
        });

        holder.btn_increase.setOnClickListener(view -> {
            int id = ActivityCart.product_id.get(position);
            int qty = ActivityCart.product_quantity.get(position) + 1;
            double price = (_single_item * qty);
            dbHelper.updateData(id, qty, price);

            ((ActivityCart) context).clearData();
            ((ActivityCart) context).updateCart();
        });

        holder.txt_quantity.setText("" + ActivityCart.product_quantity.get(position));

    }

    @Override
    public int getItemCount() {
        return ActivityCart.product_id.size();
    }

}
