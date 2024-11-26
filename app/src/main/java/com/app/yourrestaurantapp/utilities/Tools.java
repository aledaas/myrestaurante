package com.app.yourrestaurantapp.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.activities.ActivityProductDetail;
import com.app.yourrestaurantapp.models.Product;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@SuppressWarnings("deprecation")
public class Tools {

    public static final int PERMISSIONS_REQUEST = 102;
    Context context;

    // constructor
    public Tools(Context context) {
        this.context = context;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivity.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.d("Network", "NETWORKNAME: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_light_bottom_navigation));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_light_status_bar));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        if (Config.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static String getFormatedDateSimple(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yyyy");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String decimalFormatter(double price) {
        DecimalFormat decimalFormat;
        if (Config.ENABLE_DECIMAL_ROUNDING) {
            decimalFormat = new DecimalFormat("0");
        } else {
            decimalFormat = new DecimalFormat("0.00");
        }
        return decimalFormat.format(price);
    }

    public static String getFormatedDate(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void downloadImage(Activity activity, String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            //Toast.makeText(activity, "Image download started.", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void onProductListClicked(Activity activity, Product product) {
        Intent intent = new Intent(activity, ActivityProductDetail.class);
        intent.putExtra("product_id", product.product_id);
        intent.putExtra("title", product.product_name);
        intent.putExtra("image", product.product_image);
        intent.putExtra("product_price", product.product_price);
        intent.putExtra("product_description", product.product_description);
        intent.putExtra("serve_for", product.serve_for);
        intent.putExtra("product_status", product.product_status);
        intent.putExtra("currency_code", product.currency_code);
        intent.putExtra("category_name", product.category_name);
        activity.startActivity(intent);
    }

    public static void showContentDescription(Activity activity, WebView webView, String htmlData) {
        webView.setBackgroundColor(Color.parseColor("#ffffff"));
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        WebSettings webSettings = webView.getSettings();
        Resources res = activity.getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);
        webSettings.setJavaScriptEnabled(true);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = htmlData;

        String fontStyleDefault = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto; font-family: MyFont;font-size: medium;}</style>";

        String text = "<html><head>"
                + fontStyleDefault
                + "<style type=\"text/css\">body{color: #525252;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + fontStyleDefault
                + "<style type=\"text/css\">body{color: #525252;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        if (Config.ENABLE_RTL_MODE) {
            webView.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreen(AppCompatActivity activity, boolean show) {
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            int resultColor = ColorUtils.blendARGB(activity.getResources().getColor(R.color.color_light_status_bar), Color.BLACK, 0.35f);
            activity.getWindow().setStatusBarColor(resultColor);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void startExternalApplication(Context context, String url) {
        try {
            String[] results = url.split("package=");
            String packageName = results[1];
            boolean isAppInstalled = appInstalledOrNot(context, packageName);
            if (isAppInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Whoops! cannot handle this url.", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error", "NameNotFoundException");
        }
        return false;
    }

    public static void setNativeAdMargin(View view, int left, int top, int right, int bottom) {
        setMargins(view, left, top, right, bottom);
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

}
