package com.app.yourrestaurantapp.activities;

import static com.app.yourrestaurantapp.utilities.Tools.PERMISSIONS_REQUEST;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.utilities.Tools;
import com.app.yourrestaurantapp.utilities.TouchImageView;
import com.squareup.picasso.Picasso;

public class ActivityImageDetail extends AppCompatActivity {

    TouchImageView product_image;
    String str_title;
    String str_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        Tools.lightNavigation(this);

        if (Config.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        str_title = getIntent().getStringExtra("title");
        str_image = getIntent().getStringExtra("image");

        product_image = findViewById(R.id.image);

        Picasso.with(this)
                .load(str_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_loading)
                .into(product_image);

        initToolbar();

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.close_image) {
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 300);
            return true;
        } else if (itemId == R.id.save_image) {
            new Handler().postDelayed(this::requestStoragePermission, 300);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityImageDetail.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage();
            }
        } else {
            downloadImage();
        }
    }

    public void downloadImage() {
        Tools.downloadImage(this, str_title, str_image, "image/jpeg");
    }

}
