package com.app.yourrestaurantapp.activities;

import static com.app.yourrestaurantapp.utilities.Constant.POST_ORDER;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.MaterialProgressDialog;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.app.yourrestaurantapp.utilities.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OneSignal;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("deprecation")
public class ActivityCheckout extends AppCompatActivity {

    public static final String TAG = "ActivityCheckout";
    RequestQueue requestQueue;
    Button btn_submit_order;
    EditText edt_name, edt_email, edt_phone, edt_address, edt_shipping, edt_order_list, edt_order_total, edt_comment;
    static EditText edt_date_picker, edt_time_picker;
    private static int mYear;
    private static int mMonth;
    private static int mDay;
    private static int mHour;
    private static int mMinute;
    public static final String TIME_DIALOG_ID = "timePicker";
    public static final String DATE_DIALOG_ID = "datePicker";
    String str_name, str_email, str_phone, str_address, str_date, str_time, str_shipping, str_order_list, str_order_total, str_comment;
    String data_order_list = "";
    MaterialProgressDialog.Builder progressDialog;
    DBHelper dbhelper;
    ArrayList<ArrayList<Object>> data;
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    View view;
    private String rand = getRandomString(9);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = dateFormat.format(Calendar.getInstance().getTime());
    SharedPref sharedPref;
    private Spinner spinner;
    public static ArrayList<String> arrayList = new ArrayList<>();
    String playerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Tools.lightNavigation(this);
        view = findViewById(android.R.id.content);

        sharedPref = new SharedPref(this);

        setupToolbar();
        getSpinnerData();

        dbhelper = new DBHelper(this);
        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new MaterialProgressDialog.Builder(this);
        progressDialog.build();

        btn_submit_order = findViewById(R.id.btn_submit_order);

        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_phone = findViewById(R.id.edt_phone);
        edt_address = findViewById(R.id.edt_address);
        edt_shipping = findViewById(R.id.edt_shipping);
        edt_order_list = findViewById(R.id.edt_order_list);
        edt_order_total = findViewById(R.id.edt_order_total);
        edt_comment = findViewById(R.id.edt_comment);

        edt_date_picker = findViewById(R.id.edt_date_picker);
        findViewById(R.id.btn_date_picker).setOnClickListener(view -> {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), DATE_DIALOG_ID);
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        edt_date_picker.setText(date);
        edt_date_picker.setEnabled(false);

        edt_time_picker = findViewById(R.id.edt_time_picker);
        findViewById(R.id.btn_time_picker).setOnClickListener(view -> {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getSupportFragmentManager(), TIME_DIALOG_ID);
        });

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:00");
        String time = timeFormat.format(Calendar.getInstance().getTime());
        edt_time_picker.setText(time);
        edt_time_picker.setEnabled(false);

        edt_order_list.setEnabled(false);

        getDataFromDatabase();
        submitOrder();

        edt_name.setText(sharedPref.getName());
        edt_email.setText(sharedPref.getEmail());
        edt_phone.setText(sharedPref.getPhone());
        edt_address.setText(sharedPref.getAddress());

    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mYear = year;
            mMonth = month;
            mDay = day;

            edt_date_picker.setText(new StringBuilder()
                    .append(mYear).append("-")
                    .append(mMonth + 1).append("-")
                    .append(mDay).append(" "));

        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;

            edt_time_picker.setText(new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute)).append(":")
                    .append("00"));
        }
    }

    private static String pad(int c) {
        if (c >= 10) {
            return String.valueOf(c);
        } else {
            return "0" + c;
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_checkout);
        }
    }

    private void getSpinnerData() {

        spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(ActivityCheckout.this, R.layout.spinner_item, arrayList);
        myAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt_shipping.setText(arrayList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void submitOrder() {
        btn_submit_order.setOnClickListener(v -> getValueFromEditText());
    }

    public void getValueFromEditText() {

        str_name = edt_name.getText().toString();
        str_email = edt_email.getText().toString();
        str_phone = edt_phone.getText().toString();
        str_address = edt_address.getText().toString();
        str_date = edt_date_picker.getText().toString();
        str_time = edt_time_picker.getText().toString();
        str_shipping = edt_shipping.getText().toString();
        str_order_list = edt_order_list.getText().toString();
        str_order_total = edt_order_total.getText().toString();
        str_comment = edt_comment.getText().toString();

        if (str_name.equalsIgnoreCase("") ||
                str_email.equalsIgnoreCase("") ||
                str_phone.equalsIgnoreCase("") ||
                str_address.equalsIgnoreCase("") ||
                str_date.equalsIgnoreCase("") ||
                str_time.equalsIgnoreCase("") ||
                str_shipping.equalsIgnoreCase("") ||
                str_order_list.equalsIgnoreCase("")) {
            Snackbar.make(view, R.string.checkout_fill_form, Snackbar.LENGTH_SHORT).show();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(R.string.checkout_dialog_title);
            builder.setMessage(R.string.checkout_dialog_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> requestAction());
            builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), null);
            builder.setCancelable(false);
            builder.show();
        }
    }

    public void requestAction() {

        progressDialog.setMessage(getString(R.string.checkout_submit_msg));
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, POST_ORDER, ServerResponse -> {
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                dialogSuccessOrder();
                sharedPref.saveProfile(str_name, str_email, str_phone, str_address);
            }, 2000);

        }, volleyError -> {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.checkout_error), Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Map<String, String> getParams() {

                playerId = OneSignal.getUser().getPushSubscription().getId();

                Map<String, String> params = new HashMap<>();
                params.put("code", rand);
                params.put("name", str_name);
                params.put("email", str_email);
                params.put("phone", str_phone);
                params.put("address", str_address);
                params.put("date_time", str_date + " " + str_time);
                params.put("shipping", str_shipping);
                params.put("order_list", str_order_list);
                params.put("order_total", str_order_total);
                params.put("comment", str_comment);
                if (!playerId.equals("")) {
                    params.put("player_id", playerId);
                } else {
                    params.put("player_id", "0");
                }
                params.put("date", date);
                params.put("server_url", Config.ADMIN_PANEL_URL);

                return params;
            }

        };

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityCheckout.this);
        requestQueue.add(stringRequest);
    }

    public void getDataFromDatabase() {

        data = dbhelper.getAllData();

        double Order_price = 0;
        double Total_price = 0;
        double tax = 0;

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            String Menu_name = row.get(1).toString();
            String Quantity = row.get(2).toString();

            double Sub_total_price = Double.parseDouble(row.get(3).toString());

            String _Sub_total_price = String.format(Locale.GERMAN, "%1$,.0f", Sub_total_price);

            Order_price += Sub_total_price;

            data_order_list += (Quantity + " " + Menu_name + " " + Tools.decimalFormatter(Sub_total_price) + " " + sharedPref.getCurrencyCode() + ",\n");
        }

        if (data_order_list.equalsIgnoreCase("")) {
            data_order_list += getString(R.string.no_order_menu);
        }

        tax = Order_price * ((double) sharedPref.getTax() / 100);
        Total_price = Order_price + tax;

        String price_tax = String.format(Locale.GERMAN, "%1$,.0f", (double) sharedPref.getTax());
        String _Order_price = String.format(Locale.GERMAN, "%1$,.0f", Order_price);
        String _tax = String.format(Locale.GERMAN, "%1$,.0f", tax);
        String _Total_price = String.format(Locale.GERMAN, "%1$,.0f", Total_price);

        data_order_list += "\n" + getResources().getString(R.string.txt_order) + " " + Tools.decimalFormatter(Order_price) + " " + sharedPref.getCurrencyCode() +
                "\n" + getResources().getString(R.string.txt_tax) + " " + Tools.decimalFormatter(sharedPref.getTax()) + "%: " + Tools.decimalFormatter(tax) + " " + sharedPref.getCurrencyCode() +
                "\n" + getResources().getString(R.string.txt_total) + " " + Tools.decimalFormatter(Total_price) + " " + sharedPref.getCurrencyCode();

        edt_order_total.setText(Tools.decimalFormatter(Total_price) + " " + sharedPref.getCurrencyCode());

        edt_order_list.setText(data_order_list);

    }

    public void dialogSuccessOrder() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.checkout_success_title);
        builder.setMessage(R.string.checkout_success_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.checkout_option_ok, (dialog, which) -> {
            dbhelper.addDataHistory(rand, str_order_list, str_order_total, date);
            dbhelper.deleteAllData();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder stringBuilder = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return stringBuilder.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
