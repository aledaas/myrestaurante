package com.app.yourrestaurantapp.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.yourrestaurantapp.BuildConfig;
import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.fragments.FragmentHelp;
import com.app.yourrestaurantapp.fragments.FragmentHome;
import com.app.yourrestaurantapp.fragments.FragmentPost;
import com.app.yourrestaurantapp.fragments.FragmentProfile;
import com.app.yourrestaurantapp.utilities.AppBarLayoutBehavior;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.OneSignalPush;
import com.app.yourrestaurantapp.utilities.RtlViewPager;
import com.app.yourrestaurantapp.utilities.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OneSignal;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BottomNavigationView navigation;
    public ViewPager viewPager;
    public RtlViewPager viewPagerRtl;
    private Toolbar toolbar;
    TextView toolbar_title;
    MenuItem prevMenuItem;
    int pager_number = 4;
    DBHelper dbhelper;
    private long exitTime = 0;
    View view;
    ImageView cart_badge;
    View lytExitDialog;
    LinearLayout lytPanelView;
    LinearLayout lytPanelDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);
        Tools.lightNavigation(this);

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String selectedRestaurantId = prefs.getString("SELECTED_RESTAURANT_ID", "default_value");
        String selectedRestaurantName = prefs.getString("SELECTED_RESTAURANT_NAME", "default_value");


        if (!selectedRestaurantId.equals("default_value")) {
            Log.d("MainActivity", "Restaurante seleccionado: " + selectedRestaurantName);
        } else {
            Log.d("MainActivity", "No se seleccionó ningún restaurante previamente.");
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        toolbar_title.setText(getString(R.string.app_name) +  selectedRestaurantName);

        cart_badge = findViewById(R.id.cart_badge);

        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewpager);
        viewPagerRtl = findViewById(R.id.viewpager_rtl);

        navigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_bottom_navigation));

        initViewPager();

        dbhelper = new DBHelper(this);
        try {
            dbhelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        findViewById(R.id.btn_cart).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCart.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_search).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
        });

        if (getIntent() != null) {
            notificationOpenHandler(getIntent());
        }

        new OneSignalPush.Builder(this).requestNotificationPermission();
        initExitDialog();

    }

    private void initViewPager() {
        if (Config.ENABLE_RTL_MODE) {
            viewPagerRtl.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            viewPagerRtl.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPagerRtl.setOffscreenPageLimit(pager_number);
            navigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPagerRtl.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.nav_updates) {
                    viewPagerRtl.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.nav_info) {
                    viewPagerRtl.setCurrentItem(2);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    viewPagerRtl.setCurrentItem(3);
                    return true;
                }
                return false;
            });

            viewPagerRtl.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    if (viewPagerRtl.getCurrentItem() == 1) {
                        toolbar_title.setText(getString(R.string.title_nav_post));
                    } else if (viewPagerRtl.getCurrentItem() == 2) {
                        toolbar_title.setText(getString(R.string.title_nav_help));
                    } else if (viewPagerRtl.getCurrentItem() == 3) {
                        toolbar_title.setText(getString(R.string.title_nav_profile));
                    } else {
                        toolbar_title.setText(getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPagerRtl.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(pager_number);
            navigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.nav_updates) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.nav_info) {
                    viewPager.setCurrentItem(2);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(3);
                    return true;
                }
                return false;
            });

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    if (viewPager.getCurrentItem() == 1) {
                        toolbar_title.setText(getString(R.string.title_nav_post));
                    } else if (viewPager.getCurrentItem() == 2) {
                        toolbar_title.setText(getString(R.string.title_nav_help));
                    } else if (viewPager.getCurrentItem() == 3) {
                        toolbar_title.setText(getString(R.string.title_nav_profile));
                    } else {
                        toolbar_title.setText(getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void notificationOpenHandler(Intent getIntent) {

        if (getIntent.hasExtra(OneSignalPush.EXTRA_ID)) {

            String id = getIntent.getStringExtra(OneSignalPush.EXTRA_ID);
            String title = getIntent.getStringExtra(OneSignalPush.EXTRA_TITLE);
            String message = getIntent.getStringExtra(OneSignalPush.EXTRA_MESSAGE);
            String bigImage = getIntent.getStringExtra(OneSignalPush.EXTRA_IMAGE);
            String launchUrl = getIntent.getStringExtra(OneSignalPush.EXTRA_LAUNCH_URL);

            String uniqueId = getIntent.getStringExtra(OneSignalPush.EXTRA_UNIQUE_ID);
            String postId = getIntent.getStringExtra(OneSignalPush.EXTRA_POST_ID);
            String link = getIntent.getStringExtra(OneSignalPush.EXTRA_LINK);

            if (link != null && !link.equals("")) {
                if (!link.equals("0")) {
                    Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                    intent.putExtra(OneSignalPush.EXTRA_TITLE, title);
                    intent.putExtra(OneSignalPush.EXTRA_LAUNCH_URL, launchUrl);
                    startActivity(intent);
                }
            }

            if (postId != null && !postId.equals("")) {
                if (!postId.equals("0")) {
                    Intent intent;
                    if (postId.equals("1010101010")) {
                        intent = new Intent(getApplicationContext(), ActivityHistory.class);
                    } else {
                        intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
                        intent.putExtra(OneSignalPush.EXTRA_UNIQUE_ID, uniqueId);
                        intent.putExtra(OneSignalPush.EXTRA_POST_ID, postId);
                    }
                    startActivity(intent);
                }
            }

        }

    }

//    public void notificationOpenHandler(Intent getIntent) {
//        long post_id = getIntent.getLongExtra("cat_id", 0);
//        String link = getIntent.getStringExtra("external_link");
//        if (post_id == 0) {
//            if (link != null && !link.equals("")) {
//                if (!link.contains("no_url")) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
//                }
//            }
//        } else if (post_id > 0) {
//            Intent intent;
//            if (post_id == 1010101010) {
//                intent = new Intent(getApplicationContext(), ActivityHistory.class);
//            } else {
//                intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
//                intent.putExtra("product_id", post_id);
//            }
//            startActivity(intent);
//        } else {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//        }
//    }

    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentPost();
                case 2:
                    return new FragmentHelp();
                case 3:
                    return new FragmentProfile();
            }
            return null;
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(getString(R.string.db_exist_alert));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.dialog_option_yes), (dialog, which) -> {
            dbhelper.deleteAllData();
            dbhelper.close();
        });

        builder.setNegativeButton(getString(R.string.dialog_option_no), (dialog, which) -> {
            dbhelper.close();
            dialog.cancel();
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void refreshCartBadge() {
        if (dbhelper.getAllData().size() > 0) {
            cart_badge.setVisibility(View.VISIBLE);
        } else {
            cart_badge.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartBadge();
    }

    @Override
    public void onBackPressed() {
        if (Config.ENABLE_RTL_MODE) {
            if (viewPagerRtl.getCurrentItem() != 0) {
                viewPagerRtl.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if (lytExitDialog.getVisibility() != View.VISIBLE) {
            showDialog(true);
        }
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    public void initExitDialog() {

        lytExitDialog = findViewById(R.id.lyt_dialog_exit);
        lytPanelView = findViewById(R.id.lyt_panel_view);
        lytPanelDialog = findViewById(R.id.lyt_panel_dialog);

        lytPanelView.setBackgroundColor(getResources().getColor(R.color.color_dialog_background_light));
        lytPanelDialog.setBackgroundResource(R.drawable.bg_rounded_default);

        lytPanelView.setOnClickListener(view -> {
            //empty state
        });

        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnExit = findViewById(R.id.btn_exit);

        FloatingActionButton btnRate = findViewById(R.id.btn_rate);
        FloatingActionButton btnShare = findViewById(R.id.btn_share);

        btnCancel.setOnClickListener(view -> showDialog(false));

        btnExit.setOnClickListener(view -> {
            showDialog(false);
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 300);
        });

        btnRate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            showDialog(false);
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
            showDialog(false);
        });
    }

    private void showDialog(boolean show) {
        if (show) {
            lytExitDialog.setVisibility(View.VISIBLE);
            slideUp(findViewById(R.id.dialog_card_view));
            ObjectAnimator.ofFloat(lytExitDialog, View.ALPHA, 0.1f, 1.0f).setDuration(300).start();
            Tools.fullScreen(this, true);
        } else {
            slideDown(findViewById(R.id.dialog_card_view));
            ObjectAnimator.ofFloat(lytExitDialog, View.ALPHA, 1.0f, 0.1f).setDuration(300).start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lytExitDialog.setVisibility(View.GONE);
                Tools.fullScreen(this, false);
                Tools.lightNavigation(this);
            }, 300);
        }
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, findViewById(R.id.main_content).getHeight(), 0);
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, findViewById(R.id.main_content).getHeight());
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

}
