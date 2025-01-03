package com.app.restaurant.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.tucarta.restaurant.R;
import com.app.restaurant.adapters.AdapterProduct;
import com.app.restaurant.adapters.AdapterSearch;
import com.app.restaurant.models.Product;
import com.app.restaurant.utilities.ItemOffsetDecoration;
import com.app.restaurant.utilities.SharedPref;
import com.app.restaurant.utilities.Tools;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ActivitySearch extends AppCompatActivity {

    private EditText edtSearch;
    private ImageButton btnClear;
    private AdapterSearch adapterSearch;
    private LinearLayout lytSuggestion;
    RecyclerView recyclerView;
    RecyclerView recyclerViewSuggestion;
    List<Product> filteredList;
    AdapterProduct adapterProduct;
    SharedPref sharedPref;
    CoordinatorLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Tools.lightNavigation(this);
        sharedPref = new SharedPref(this);
        initView();
        setupToolbar();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        parentView = findViewById(R.id.parent_view);

        edtSearch = findViewById(R.id.et_search);
        btnClear = findViewById(R.id.bt_clear);
        btnClear.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.item_offset));
        adapterProduct = new AdapterProduct(this, sharedPref.getProductList());
        adapterProduct.setOnItemClickListener((view, product, position) -> Tools.onProductListClicked(this, product));

        recyclerView.setAdapter(adapterProduct);

        edtSearch.addTextChangedListener(textWatcher);

        lytSuggestion = findViewById(R.id.lyt_suggestion);
        recyclerViewSuggestion = findViewById(R.id.recycler_view_suggestion);
        recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this));

        adapterSearch = new AdapterSearch(this);
        recyclerViewSuggestion.setAdapter(adapterSearch);
        showSuggestionSearch();
        adapterSearch.setOnItemClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
            lytSuggestion.setVisibility(View.GONE);
            adapterProduct.resetListData();
            hideKeyboard();
            searchAction();
        });

        adapterSearch.setOnItemActionClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
        });

        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction();
                return true;
            }
            return false;
        });

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            showSuggestionSearch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

        swipeProgress(false);

    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_white), PorterDuff.Mode.SRC_IN);
        lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                btnClear.setVisibility(View.GONE);
            } else {
                btnClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void searchAction() {
        showNotFoundView(false);
        lytSuggestion.setVisibility(View.GONE);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            adapterProduct.resetListData();
            swipeProgress(true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                requestSearch(sharedPref.getProductList(), query);
                adapterSearch.addSearchHistory(query);
            }, 1000);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_no_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void requestSearch(List<Product> posts, final String query) {
        filteredList = new ArrayList<>();
        if (posts != null && posts.size() > 0) {
            for (Product post : posts) {
                if (post.product_name.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                }
            }
            if (filteredList.size() > 0) {
                adapterProduct.setListData(filteredList);
                showNotFoundView(false);
            } else {
                showNotFoundView(true);
            }
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        adapterSearch.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
    }

    private void swipeProgress(boolean show) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showNotFoundView(boolean show) {
        View lytNotFound = findViewById(R.id.lyt_no_item);
        if (show) {
            lytNotFound.setVisibility(View.VISIBLE);
        } else {
            lytNotFound.setVisibility(View.GONE);
        }
    }

    public void hideKeyboard() {
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (edtSearch.length() > 0) {
            edtSearch.setText("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
