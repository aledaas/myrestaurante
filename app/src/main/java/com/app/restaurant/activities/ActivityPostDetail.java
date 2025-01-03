package com.app.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.tucarta.restaurant.BuildConfig;
import com.app.tucarta.restaurant.R;
import com.app.restaurant.utilities.Tools;
import com.squareup.picasso.Picasso;

public class ActivityPostDetail extends AppCompatActivity {

    TextView post_title;
    ImageView post_image;
    WebView post_description;
    TextView toolbar_title;
    String title, image, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Tools.lightNavigation(this);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        image = intent.getStringExtra("image");
        description = intent.getStringExtra("description");

        initView();
        displayData();
        initToolbar();

    }

    private void initView() {
        post_title = findViewById(R.id.post_title);
        post_image = findViewById(R.id.post_image);
        post_description = findViewById(R.id.post_description);
        toolbar_title = findViewById(R.id.toolbar_title);
    }

    private void displayData() {
        post_title.setText(title);

        Picasso.with(this)
                .load(image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_loading)
                .into(post_image);

        post_image.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
            intent.putExtra("title", title);
            intent.putExtra("image", image);
            startActivity(intent);
        });

        Tools.showContentDescription(this, post_description, description);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.btn_share).setOnClickListener(view -> {
            String share_text = Html.fromHtml(getResources().getString(R.string.share_app)).toString();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, share_text + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
