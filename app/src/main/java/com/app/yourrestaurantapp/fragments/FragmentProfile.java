package com.app.yourrestaurantapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.app.yourrestaurantapp.BuildConfig;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.activities.ActivityHelp;
import com.app.yourrestaurantapp.activities.ActivityHistory;
import com.app.yourrestaurantapp.activities.ActivitySettings;
import com.app.yourrestaurantapp.adapters.AdapterSearch;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class FragmentProfile extends Fragment {

    private SharedPref sharedPref;
    TextView txt_user_name;
    TextView txt_user_email;
    TextView txt_user_phone;
    TextView txt_user_address;
    TextView btn_edit_user;
    TextView txt_cache_size;
    LinearLayout btn_order_history;
    LinearLayout btn_map_location;
    LinearLayout btn_rate;
    LinearLayout btn_share;
    LinearLayout btn_privacy;
    LinearLayout btn_about;
    LinearLayout parent_view;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPref = new SharedPref(activity);

        parent_view = rootView.findViewById(R.id.parent_view);
        txt_cache_size = rootView.findViewById(R.id.txt_cache_size);

        txt_user_name = rootView.findViewById(R.id.txt_user_name);
        txt_user_email = rootView.findViewById(R.id.txt_user_email);
        txt_user_phone = rootView.findViewById(R.id.txt_user_phone);
        txt_user_address = rootView.findViewById(R.id.txt_user_address);

        btn_edit_user = rootView.findViewById(R.id.btn_edit_user);
        btn_edit_user.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ActivitySettings.class);
            startActivity(intent);
        });

        btn_order_history = rootView.findViewById(R.id.btn_order_history);
        btn_order_history.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ActivityHistory.class);
            startActivity(intent);
        });

        btn_map_location = rootView.findViewById(R.id.btn_map_location);
        btn_map_location.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMapLocation()));
            startActivity(intent);
        });

        btn_rate = rootView.findViewById(R.id.btn_rate);
        btn_rate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        });

        btn_share = rootView.findViewById(R.id.btn_share);
        btn_share.setOnClickListener(v -> {
            String share_text = Html.fromHtml(getResources().getString(R.string.share_app)).toString();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, share_text + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        });

        btn_privacy = rootView.findViewById(R.id.btn_privacy);
        btn_privacy.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ActivityHelp.class);
            intent.putExtra("title", getString(R.string.txt_profile_privacy));
            intent.putExtra("content", sharedPref.getPrivacyPolicy());
            startActivity(intent);
        });

        rootView.findViewById(R.id.btn_search_history).setOnClickListener(v -> {
            AdapterSearch adapterSearch = new AdapterSearch(activity);
            if (adapterSearch.getItemCount() > 0) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
                builder.setTitle(getString(R.string.title_dialog_clear_search_history));
                builder.setMessage(getString(R.string.msg_dialog_clear_search_history));
                builder.setPositiveButton(R.string.dialog_option_yes, (di, i) -> {
                    adapterSearch.clearSearchHistory();
                    Snackbar.make(parent_view, getString(R.string.clearing_success), Snackbar.LENGTH_SHORT).show();
                });
                builder.setNegativeButton(R.string.dialog_option_cancel, null);
                builder.show();
            } else {
                Snackbar.make(parent_view, getString(R.string.clearing_empty), Snackbar.LENGTH_SHORT).show();
            }
        });

        initializeCache();
        rootView.findViewById(R.id.btn_clear_cache).setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            builder.setTitle(getString(R.string.title_dialog_clear_cache));
            builder.setMessage(getString(R.string.msg_dialog_clear_cache));
            builder.setPositiveButton(R.string.dialog_option_yes, (di, i) -> {
                FileUtils.deleteQuietly(activity.getCacheDir());
                FileUtils.deleteQuietly(activity.getExternalCacheDir());
                txt_cache_size.setText("(0 Bytes)");
                Snackbar.make(parent_view, getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
            });
            builder.setNegativeButton(R.string.dialog_option_cancel, null);
            builder.show();
        });

        btn_about = rootView.findViewById(R.id.btn_about);
        btn_about.setOnClickListener(v -> {
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View view = layoutInflater.inflate(R.layout.dialog_about, null);
            TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
            txtAppVersion.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_NAME);
            final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
            alert.setView(view);
            alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
            alert.show();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        txt_user_name.setText(sharedPref.getName());
        txt_user_email.setText(sharedPref.getEmail());
        txt_user_phone.setText(sharedPref.getPhone());
        txt_user_address.setText(sharedPref.getAddress());
        super.onResume();
    }

    private void initializeCache() {
        txt_cache_size.setText("(" + readableFileSize((0 + getDirSize(activity.getCacheDir())) + getDirSize(activity.getExternalCacheDir())) + ")");
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

}
