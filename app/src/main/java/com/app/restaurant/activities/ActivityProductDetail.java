package com.app.restaurant.activities;

import static com.app.restaurant.utilities.Tools.PERMISSIONS_REQUEST;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.app.tucarta.restaurant.BuildConfig;
import com.app.restaurant.Config;
import com.app.tucarta.restaurant.R;
import com.app.restaurant.utilities.DBHelper;
import com.app.restaurant.utilities.SharedPref;
import com.app.restaurant.utilities.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("deprecation")
public class ActivityProductDetail extends AppCompatActivity {

    long product_id;
    TextView txt_product_name, txt_product_price, txt_product_quantity;
    private String product_name, product_image, category_name, product_status, product_description;
    private double product_price;
    private int serve_for;
    WebView webView;
    ImageView img_product_image;
    Button btn_cart;
    ImageView cart_badge;
    public static DBHelper dbHelper;
    final Context context = this;
    private AppBarLayout appBarLayout;
    SharedPref sharedPref;
    CoordinatorLayout parent_view;
    TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Tools.lightNavigation(this);

        dbHelper = new DBHelper(this);
        sharedPref = new SharedPref(this);
        getData();
        initComponent();
        displayData();
        setupToolbar();
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

    public void getData() {
        Intent intent = getIntent();
        product_id = intent.getLongExtra("product_id", 0);
        product_name = intent.getStringExtra("title");
        product_image = intent.getStringExtra("image");
        product_price = intent.getDoubleExtra("product_price", 0);
        product_description = intent.getStringExtra("product_description");
        serve_for = intent.getIntExtra("serve_for", 0);
        product_status = intent.getStringExtra("product_status");
        category_name = intent.getStringExtra("category_name");
    }

    public void initComponent() {
        txt_product_name = findViewById(R.id.product_name);
        img_product_image = findViewById(R.id.product_image);
        txt_product_price = findViewById(R.id.product_price);
        webView = findViewById(R.id.product_description);
        txt_product_quantity = findViewById(R.id.product_quantity);
        btn_cart = findViewById(R.id.btn_add_cart);
        cart_badge = findViewById(R.id.cart_badge);
        parent_view = findViewById(R.id.parent_view);
        toolbar_title = findViewById(R.id.toolbar_title);
    }

    public void displayData() {
        txt_product_name.setText(product_name);

        Picasso.with(this)
                .load(Config.ADMIN_PANEL_URL + "/upload/product/" + product_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_loading)
                .into(img_product_image);

        img_product_image.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
            intent.putExtra("title", product_name);
            intent.putExtra("image", Config.ADMIN_PANEL_URL + "/upload/product/" + product_image);
            startActivity(intent);
        });

        txt_product_price.setText(Tools.decimalFormatter(product_price) + " " + sharedPref.getCurrencyCode());

        txt_product_quantity.setText(serve_for + " " + getString(R.string.txt_items));

        if (product_status.equals("1")) {

            refreshCartButton();

            btn_cart.setOnClickListener(v -> {
                if (dbHelper.isDataExist(product_id)) {
                    dbHelper.deleteData(product_id);
                    Snackbar.make(parent_view, getString(R.string.msg_remove_cart), Snackbar.LENGTH_SHORT).show();
                    btn_cart.setText(R.string.btn_add_to_cart);
                    new Handler().postDelayed(() -> {
                        btn_cart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_status_bar));
                    }, 100);
                } else {
                    dbHelper.addData(product_id, product_name, 1, (product_price * 1), product_image, String.valueOf(System.currentTimeMillis()));
                    Snackbar.make(parent_view, getString(R.string.msg_add_cart), Snackbar.LENGTH_SHORT).show();
                    btn_cart.setText(R.string.btn_remove_from_cart);
                    new Handler().postDelayed(() -> {
                        btn_cart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_yellow));
                    }, 100);
                }
                refreshCartBadge();
            });
        } else {
            btn_cart.setEnabled(false);
            btn_cart.setText(R.string.btn_out_of_stock);
            btn_cart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_sold));
        }

        Tools.showContentDescription(this, webView, product_description);

    }

    private void refreshCartButton() {
        if (dbHelper.isDataExist(product_id)) {
            btn_cart.setText(R.string.btn_remove_from_cart);
            btn_cart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_yellow));
        } else {
            btn_cart.setText(R.string.btn_add_to_cart);
            btn_cart.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_status_bar));
        }
    }

    private void refreshCartBadge() {
        if (dbHelper.getAllData().size() > 0) {
            cart_badge.setVisibility(View.VISIBLE);
        } else {
            cart_badge.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void onToolbarIconClicked() {
        findViewById(R.id.btn_share).setOnClickListener(view -> {
            requestStoragePermission();
        });

        findViewById(R.id.btn_cart).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCart.class);
            startActivity(intent);
        });
    }

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityProductDetail.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                (new ShareTask(ActivityProductDetail.this)).execute(Config.ADMIN_PANEL_URL + "/upload/product/" + product_image);
            }
        } else {
            (new ShareTask(ActivityProductDetail.this)).execute(Config.ADMIN_PANEL_URL + "/upload/product/" + product_image);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartButton();
        refreshCartBadge();
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    public class ShareTask extends AsyncTask<String, String, String> {
        private Context context;
        private ProgressDialog pDialog;
        URL myFileUrl;
        Bitmap bmImg = null;
        File file;

        public ShareTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getString(R.string.loading_msg));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                myFileUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.app_name) + "/");
                dir.mkdirs();
                String fileName = idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(Bitmap.CompressFormat.PNG, 99, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String args) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_product_section_one) + " " + product_name + " " + getString(R.string.share_product_section_two) + " " + Tools.decimalFormatter(product_price) + " " + sharedPref.getCurrencyCode() + getString(R.string.share_product_section_three) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            startActivity(Intent.createChooser(intent, "Share Image"));
            pDialog.dismiss();
        }
    }

}
