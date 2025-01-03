package com.app.restaurant.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.tucarta.restaurant.R;
import com.app.restaurant.activities.ActivityHistory;
import com.app.restaurant.models.History;
import com.app.restaurant.utilities.Tools;

import java.util.List;

public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.ViewHolder> {

    Context context;
    List<History> arrayItemCart;

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt_purchase_code;
        TextView txt_order_date;
        TextView txt_order_total;
        LinearLayout lyt_parent;

        public ViewHolder(View view) {
            super(view);
            txt_purchase_code = view.findViewById(R.id.txt_purchase_code);
            txt_order_date = view.findViewById(R.id.txt_order_date);
            txt_order_total = view.findViewById(R.id.txt_order_total);
            lyt_parent = view.findViewById(R.id.lyt_parent);
        }

    }

    public AdapterHistory(Context context, List<History> arrayItemCart) {
        this.context = context;
        this.arrayItemCart = arrayItemCart;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.txt_purchase_code.setText(ActivityHistory.code.get(position));
        holder.txt_order_date.setText(Tools.getFormatedDateSimple(ActivityHistory.date_time.get(position)));
        holder.txt_order_total.setText(ActivityHistory.order_total.get(position));
        holder.lyt_parent.setOnClickListener(view -> {
            ((ActivityHistory) context).setOnItemClickListener(position);
        });
        holder.lyt_parent.setOnLongClickListener(view -> {
            ((ActivityHistory) context).setOnLongItemClickListener(position);
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return ActivityHistory.id.size();
    }

}
