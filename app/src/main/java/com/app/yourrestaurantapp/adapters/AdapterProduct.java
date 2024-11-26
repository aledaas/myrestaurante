package com.app.yourrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.models.Product;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.app.yourrestaurantapp.utilities.Tools;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.MyViewHolder> {

    Context context;
    private List<Product> productList;
    SharedPref sharedPref;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Product product, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView product_name;
        public TextView product_price;
        public ImageView product_image;
        public LinearLayout lyt_parent;

        public MyViewHolder(View view) {
            super(view);
            product_name = view.findViewById(R.id.product_name);
            product_price = view.findViewById(R.id.product_price);
            product_image = view.findViewById(R.id.category_image);
            lyt_parent = view.findViewById(R.id.lyt_parent);
        }
    }

    public AdapterProduct(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.sharedPref = new SharedPref(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Product product = productList.get(position);
        holder.product_name.setText(product.product_name);

        holder.product_price.setText(Tools.decimalFormatter(product.product_price) + " " + sharedPref.getCurrencyCode());

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/product/" + product.product_image)
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .into(holder.product_image);

        holder.lyt_parent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, product, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setListData(List<Product> posts) {
        this.productList = posts;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.productList.clear();
        notifyDataSetChanged();
    }

}
