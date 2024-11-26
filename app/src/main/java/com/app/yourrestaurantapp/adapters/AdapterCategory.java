package com.app.yourrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.models.Category;
import com.app.yourrestaurantapp.utilities.Tools;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder> {

    Context context;
    List<Category> categoryList;
    onItemClickListener listener;
    boolean isHome;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView category_name, product_count;
        public ImageView category_image;
        public CardView card_view;

        /**
         * @noinspection deprecation
         */
        public MyViewHolder(View view) {
            super(view);
            category_name = view.findViewById(R.id.category_name);
            product_count = view.findViewById(R.id.product_count);
            category_image = view.findViewById(R.id.category_image);
            card_view = view.findViewById(R.id.card_view);
            view.setOnClickListener(v -> listener.onItemSelected(categoryList.get(getAdapterPosition())));
        }
    }

    public AdapterCategory(Context context, List<Category> categoryList, onItemClickListener listener, boolean isHome) {
        this.context = context;
        this.listener = listener;
        this.categoryList = categoryList;
        this.isHome = isHome;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Category category = categoryList.get(position);
        holder.category_name.setText(category.getCategoryName());
        holder.product_count.setText(category.getProductCount() + " " + context.getResources().getString(R.string.txt_items));

        if (position <= 1) {
            Tools.setMargins(holder.card_view, context.getResources().getDimensionPixelOffset(R.dimen.item_offset), 0, 0, 0);
        }

        int lastPosition = (categoryList.size() - 1);
        if ((categoryList.size() % 2) == 0) {
            if (position == lastPosition || position == (lastPosition - 1)) {
                Tools.setMargins(holder.card_view, 0, 0, context.getResources().getDimensionPixelOffset(R.dimen.item_offset), 0);
            }
        } else {
            if (position == lastPosition) {
                Tools.setMargins(holder.card_view, 0, 0, context.getResources().getDimensionPixelOffset(R.dimen.item_offset), 0);
            }
        }

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/category/" + category.getCategoryImage())
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .into(holder.category_image);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface onItemClickListener {
        void onItemSelected(Category category);
    }
}
