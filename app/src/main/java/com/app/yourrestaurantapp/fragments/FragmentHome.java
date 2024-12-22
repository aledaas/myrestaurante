package com.app.yourrestaurantapp.fragments;

import static com.app.yourrestaurantapp.utilities.Constant.GET_HOME;
import static com.app.yourrestaurantapp.utilities.Constant.arrayListProduct;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.toolbox.StringRequest;
import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.activities.ActivityCheckout;
import com.app.yourrestaurantapp.activities.ActivityPostDetail;
import com.app.yourrestaurantapp.activities.ActivityProduct;
import com.app.yourrestaurantapp.activities.ActivitySearch;
import com.app.yourrestaurantapp.activities.MyApplication;
import com.app.yourrestaurantapp.adapters.AdapterCategory;
import com.app.yourrestaurantapp.adapters.AdapterProduct;
import com.app.yourrestaurantapp.adapters.AdapterSlider;
import com.app.yourrestaurantapp.models.Category;
import com.app.yourrestaurantapp.models.Product;
import com.app.yourrestaurantapp.models.Slider;
import com.app.yourrestaurantapp.utilities.Constant;
import com.app.yourrestaurantapp.utilities.ItemOffsetDecoration;
import com.app.yourrestaurantapp.utilities.RtlViewPager;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.app.yourrestaurantapp.utilities.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    List<Product> productList;
    List<Category> categoryList;
    List<Slider> sliderList;
    private RecyclerView recyclerViewCategory;
    private RecyclerView recyclerViewProduct;
    AdapterCategory adapterCategory;
    AdapterProduct adapterProduct;
    AdapterSlider adapterSlider;
    SwipeRefreshLayout swipeRefreshLayout = null;
    private Runnable runnableCode = null;
    Handler handler = new Handler();
    private ViewPager viewPagerSlider;
    private RtlViewPager viewPagerSliderRtl;
    TabLayout tabLayout;
    View rootView;
    LinearLayout lytContent;
    ShimmerFrameLayout lytShimmer;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        lytContent = rootView.findViewById(R.id.lyt_content);
        recyclerViewCategory = rootView.findViewById(R.id.recycler_view_category);
        recyclerViewProduct = rootView.findViewById(R.id.recycler_view_product);
        viewPagerSlider = rootView.findViewById(R.id.view_pager_slider);
        viewPagerSliderRtl = rootView.findViewById(R.id.view_pager_slider_rtl);
        tabLayout = rootView.findViewById(R.id.tab_layout);

        recyclerViewProduct.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerViewProduct.addItemDecoration(new ItemOffsetDecoration(activity, R.dimen.item_offset));

        recyclerViewCategory.setLayoutManager(new GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false));
        recyclerViewCategory.addItemDecoration(new ItemOffsetDecoration(activity, R.dimen.item_offset));

        sliderList = new ArrayList<>();
        categoryList = new ArrayList<>();
        productList = new ArrayList<>();

        fetchData();
        onRefresh();

        return rootView;
    }

    private void displayCategories() {
        adapterCategory = new AdapterCategory(activity, Constant.arrayListCategory, category -> {
            Intent intent = new Intent(activity, ActivityProduct.class);
            intent.putExtra("category_id", category.getCategoryId());
            intent.putExtra("category_name", category.getCategoryName());
            startActivity(intent);
        }, true);
        recyclerViewCategory.setAdapter(adapterCategory);
    }

    private void displayProducts() {
        adapterProduct = new AdapterProduct(activity, Constant.arrayListProduct);
        adapterProduct.setOnItemClickListener((view, product, position) -> Tools.onProductListClicked(activity, product));
        recyclerViewProduct.setAdapter(adapterProduct);
    }

    private void displaySliders() {
        adapterSlider = new AdapterSlider(activity, Constant.arrayListSlider);
        if (Config.ENABLE_RTL_MODE) {
            viewPagerSliderRtl.setVisibility(View.VISIBLE);
            viewPagerSlider.setVisibility(View.GONE);
            viewPagerSliderRtl.setAdapter(adapterSlider);
            viewPagerSliderRtl.setOffscreenPageLimit(4);
            tabLayout.setupWithViewPager(viewPagerSliderRtl, true);
        } else {
            viewPagerSliderRtl.setVisibility(View.GONE);
            viewPagerSlider.setVisibility(View.VISIBLE);
            viewPagerSlider.setAdapter(adapterSlider);
            viewPagerSlider.setOffscreenPageLimit(4);
            tabLayout.setupWithViewPager(viewPagerSlider, true);
        }

        startAutoSlider(Constant.arrayListSlider.size(), viewPagerSlider, viewPagerSliderRtl);
        adapterSlider.setOnItemClickListener((view, slider) -> {
            Intent intent = new Intent(activity, ActivityPostDetail.class);
            intent.putExtra("title", slider.slider_title);
            intent.putExtra("image", Config.ADMIN_PANEL_URL + "/upload/slider/" + slider.slider_image);
            intent.putExtra("description", slider.slider_description);
            startActivity(intent);
        });
    }

    private void startAutoSlider(final int position, ViewPager viewPager, RtlViewPager viewPagerRtl) {
        if (runnableCode != null) {
            handler.removeCallbacks(runnableCode);
        }
        if (Config.ENABLE_RTL_MODE) {
            runnableCode = () -> {
                int currentItem = viewPagerSliderRtl.getCurrentItem() + 1;
                if (currentItem >= position) {
                    currentItem = 0;
                }
                viewPagerSliderRtl.setCurrentItem(currentItem);
                handler.postDelayed(runnableCode, Config.AUTO_SLIDER_DURATION);
            };
        } else {
            runnableCode = () -> {
                int currentItem = viewPagerSlider.getCurrentItem() + 1;
                if (currentItem >= position) {
                    currentItem = 0;
                }
                viewPagerSlider.setCurrentItem(currentItem);
                handler.postDelayed(runnableCode, Config.AUTO_SLIDER_DURATION);
            };
        }
        handler.postDelayed(runnableCode, Config.AUTO_SLIDER_DURATION);
    }

    private void onRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeProgress(true);
            sliderList.clear();
            categoryList.clear();
            productList.clear();
            new Handler().postDelayed(this::fetchData, Config.DELAY_REFRESH);
        });
    }

    private void fetchData() {
        // Recuperar el ID del restaurante almacenado en SharedPreferences
        SharedPreferences prefs = activity.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String restaurantId = prefs.getString("SELECTED_RESTAURANT_ID", null);

        if (restaurantId == null) {
            Toast.makeText(activity, "No se ha seleccionado un restaurante.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar el ID del restaurante como parámetro en la URL
        String urlWithParam = GET_HOME + "&restaurant_id=" + restaurantId;

        Log.d("Get_home", "Fetching data from URL: " + urlWithParam);

        StringRequest request = new StringRequest(urlWithParam, response -> {
            if (response == null) {
                showFailedView(true);
                swipeProgress(false);
                return;
            }
            try {
                JSONObject json = new JSONObject(response);
                JSONArray sliderResponse = json.getJSONArray("sliders");
                JSONArray categoryResponse = json.getJSONArray("categories");
                JSONArray productResponse = json.getJSONArray("products");
                JSONArray shippingResponse = json.getJSONArray("shippings");

                ActivityCheckout.arrayList.clear();
                setShipping(shippingResponse);

                List<Category> categories = new Gson().fromJson(categoryResponse.toString(), new TypeToken<List<Category>>() {
                }.getType());
                categoryList.clear();
                categoryList.addAll(categories);

                List<Product> products = new Gson().fromJson(productResponse.toString(), new TypeToken<List<Product>>() {
                }.getType());
                productList.clear();
                productList.addAll(products);

                List<Slider> sliders = new Gson().fromJson(sliderResponse.toString(), new TypeToken<List<Slider>>() {
                }.getType());
                sliderList.clear();
                sliderList.addAll(sliders);

                Constant.arrayListSlider = sliderList;
                Constant.arrayListCategory = categoryList;
                Constant.arrayListProduct = productList;

                displaySliders();
                displayCategories();
                displayProducts();

                ((TextView) rootView.findViewById(R.id.txt_home_category)).setText(getString(R.string.txt_home_category));
                ((TextView) rootView.findViewById(R.id.txt_home_product)).setText(getString(R.string.txt_home_menu));

                SharedPref sharedPref = new SharedPref(activity);
                sharedPref.saveProductList(products);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            swipeProgress(false);
            showFailedView(false);
        }, error -> {
            showFailedView(true);
            swipeProgress(false);
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void setShipping(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                ActivityCheckout.arrayList.add(json.getString("shipping_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void swipeProgress(boolean show) {
        if (show) {
            swipeRefreshLayout.setRefreshing(true);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytContent.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytContent.setVisibility(View.VISIBLE);
        }
    }

    private void showFailedView(boolean show) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        if (show) {
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            lytFailed.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.search) {
            Intent intent = new Intent(activity, ActivitySearch.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}