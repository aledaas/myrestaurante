package com.app.yourrestaurantapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.adapters.AdapterCart;
import com.app.yourrestaurantapp.models.Cart;
import com.app.yourrestaurantapp.utilities.AsyncTaskExecutor;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.MyDividerItemDecoration;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.app.yourrestaurantapp.utilities.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityCart extends AppCompatActivity {

    RecyclerView recyclerView;
    View lyt_empty_cart;
    RelativeLayout lyt_order;
    DBHelper dbhelper;
    AdapterCart adapterCart;
    double total_price;
    final int CLEAR_ALL_ORDER = 0;
    final int CLEAR_ONE_ORDER = 1;
    int FLAG;
    int ID;
    Button btn_checkout, btn_continue;
    ArrayList<ArrayList<Object>> data;
    public static ArrayList<Integer> product_id = new ArrayList<>();
    public static ArrayList<String> product_name = new ArrayList<>();
    public static ArrayList<Integer> product_quantity = new ArrayList<>();
    public static ArrayList<Double> product_price = new ArrayList<>();
    public static ArrayList<Double> sub_total_price = new ArrayList<>();
    public static ArrayList<String> product_image = new ArrayList<>();
    List<Cart> arrayCart;
    View view;
    SharedPref sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Tools.lightNavigation(this);
        view = findViewById(android.R.id.content);
        sharedPref = new SharedPref(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_cart);
        }

        recyclerView = findViewById(R.id.recycler_view_product);
        lyt_empty_cart = findViewById(R.id.lyt_empty_history);
        btn_checkout = findViewById(R.id.btn_checkout);
        btn_checkout.setOnClickListener(view -> {
            dbhelper.close();
            Intent intent1 = new Intent(ActivityCart.this, ActivityCheckout.class);
            startActivity(intent1);
        });
        btn_continue = findViewById(R.id.btn_continue);
        btn_continue.setOnClickListener(v -> finish());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL, 86));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        lyt_order = findViewById(R.id.lyt_history);

        adapterCart = new AdapterCart(this, arrayCart);
        dbhelper = new DBHelper(this);

        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        updateCart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.clear) {
            if (product_id.size() > 0) {
                showClearDialog(CLEAR_ALL_ORDER, 1111);
            } else {
                Snackbar.make(view, R.string.msg_empty_cart, Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showClearDialog(int flag, int id) {
        FLAG = flag;
        ID = id;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.confirm);
        switch (FLAG) {
            case 0:
                builder.setMessage(getString(R.string.clear_all_order));
                break;
            case 1:
                builder.setMessage(getString(R.string.clear_one_order));
                break;
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> {
            switch (FLAG) {
                case 0:
                    dbhelper.deleteAllData();
                    clearData();
                    updateCart();
                    break;
                case 1:
                    dbhelper.deleteData(ID);
                    clearData();
                    updateCart();
                    break;
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void removeSingleItem(int id) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(getString(R.string.remove_cart_item));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> {
            dbhelper.deleteData(id);
            clearData();
            updateCart();
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_option_cancel), (dialog, which) -> dialog.cancel());
        androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();

    }

    public void updateCart() {
        new getDataTask().execute();
    }

    public void clearData() {
        product_id.clear();
        product_name.clear();
        product_quantity.clear();
        product_price.clear();
        sub_total_price.clear();
        product_image.clear();
    }

    public class getDataTask extends AsyncTaskExecutor<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void params) {
            getDataFromDatabase();
            return params;
        }

        @Override
        protected void onPostExecute(Void result) {
            String _price = String.format(Locale.GERMAN, "%1$,.0f", total_price);
            String _tax = String.format(Locale.GERMAN, "%1$,.0f", (double) sharedPref.getTax());

            TextView txt_total_price = findViewById(R.id.txt_total_price);
            TextView txt_tax = findViewById(R.id.txt_tax);

            txt_total_price.setText(getResources().getString(R.string.txt_total) + " " + Tools.decimalFormatter(total_price) + " " + sharedPref.getCurrencyCode());
            txt_tax.setText(getResources().getString(R.string.txt_tax) + " " + Tools.decimalFormatter(sharedPref.getTax()) + "%");

            if (product_id.size() > 0) {
                lyt_order.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapterCart);
            } else {
                lyt_empty_cart.setVisibility(View.VISIBLE);
            }
        }

    }

    public void getDataFromDatabase() {

        total_price = 0;
        clearData();
        data = dbhelper.getAllData();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            product_id.add(Integer.parseInt(row.get(0).toString()));
            product_name.add(row.get(1).toString());
            product_quantity.add(Integer.parseInt(row.get(2).toString()));
            sub_total_price.add(Double.parseDouble(row.get(3).toString()));

            total_price += sub_total_price.get(i);

            product_image.add(row.get(4).toString());
        }

        total_price += (total_price * ((double) sharedPref.getTax() / 100));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, MotionEvent e) {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

}