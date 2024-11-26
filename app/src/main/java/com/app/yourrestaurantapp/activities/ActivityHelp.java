package com.app.yourrestaurantapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.utilities.Tools;

public class ActivityHelp extends AppCompatActivity {

    String str_title, str_content;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Tools.lightNavigation(this);

        Intent intent = getIntent();
        str_title = intent.getStringExtra("title");
        str_content = intent.getStringExtra("content");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        ((TextView) findViewById(R.id.toolbar_title)).setText(str_title);

        findViewById(R.id.btn_share).setVisibility(View.GONE);

        webView = findViewById(R.id.web_view);

        Tools.showContentDescription(this, webView, str_content);

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
