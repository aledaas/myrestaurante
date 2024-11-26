package com.app.yourrestaurantapp.activities;

import static com.app.yourrestaurantapp.utilities.Constant.GET_CATEGORY_DETAIL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.JsonArrayRequest;
import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.adapters.AdapterProduct;
import com.app.yourrestaurantapp.models.Product;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.ItemOffsetDecoration;
import com.app.yourrestaurantapp.utilities.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ActivityProduct extends AppCompatActivity {

    RecyclerView recyclerView;
    private List<Product> productList;
    private AdapterProduct adapterProduct;
    private TextView toolbar_title;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ShimmerFrameLayout lytShimmer;
    private String category_id, category_name;
    DBHelper dbHelper;
    ImageView cart_badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Tools.lightNavigation(this);
        dbHelper = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        category_id = intent.getStringExtra("category_id");
        category_name = intent.getStringExtra("category_name");

        cart_badge = findViewById(R.id.cart_badge);
        toolbar_title = findViewById(R.id.toolbar_title);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        recyclerView = findViewById(R.id.recycler_view_product);
        productList = new ArrayList<>();
        adapterProduct = new AdapterProduct(this, productList);
        adapterProduct.setOnItemClickListener((view, product, position) -> Tools.onProductListClicked(this, product));

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterProduct);

        requestAction();
        swipeRefreshLayout.setOnRefreshListener(this::requestAction);

        setupToolbar();

    }

    private void requestAction() {
        productList.clear();
        swipeProgress(true);
        new Handler().postDelayed(this::fetchData, Config.DELAY_REFRESH);
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        onToolbarIconClicked();
        toolbar_title.setText(category_name);
    }

    private void onToolbarIconClicked() {
        findViewById(R.id.btn_search).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_cart).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCart.class);
            startActivity(intent);
        });
    }

    private void fetchData() {
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest request = new JsonArrayRequest(GET_CATEGORY_DETAIL + category_id, response -> {
            if (response == null) {
                showFailedView(true);
                swipeProgress(false);
                return;
            }

            List<Product> items = new Gson().fromJson(response.toString(), new TypeToken<List<Product>>() {
            }.getType());

            if (items.size() > 0) {
                productList.clear();
                productList.addAll(items);
                adapterProduct.notifyDataSetChanged();
                showNoItemView(false);
            } else {
                showNoItemView(true);
            }
            showFailedView(false);
            swipeProgress(false);
        }, error -> {
            showFailedView(true);
            swipeProgress(false);
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = findViewById(R.id.lyt_no_item);
        if (show) {
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            lytNoItem.setVisibility(View.GONE);
        }
    }

    private void showFailedView(boolean show) {
        View lytFailed = findViewById(R.id.lyt_failed);
        if (show) {
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            lytFailed.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartBadge();
    }

    private void refreshCartBadge() {
        if (dbHelper.getAllData().size() > 0) {
            cart_badge.setVisibility(View.VISIBLE);
        } else {
            cart_badge.setVisibility(View.INVISIBLE);
        }
    }

    private void swipeProgress(boolean show) {
        if (show) {
            swipeRefreshLayout.setRefreshing(true);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            recyclerView.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}
