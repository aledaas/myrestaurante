package com.app.yourrestaurantapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.adapters.AdapterHistory;
import com.app.yourrestaurantapp.models.History;
import com.app.yourrestaurantapp.utilities.AsyncTaskExecutor;
import com.app.yourrestaurantapp.utilities.DBHelper;
import com.app.yourrestaurantapp.utilities.MyDividerItemDecoration;
import com.app.yourrestaurantapp.utilities.Tools;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ActivityHistory extends AppCompatActivity {

    RecyclerView recyclerView;
    View lyt_empty_history;
    RelativeLayout lyt_history;
    DBHelper dbhelper;
    AdapterHistory adapterHistory;
    final int CLEAR_ALL_ORDER = 0;
    final int CLEAR_ONE_ORDER = 1;
    int FLAG;
    int ID;
    ArrayList<ArrayList<Object>> data;
    public static ArrayList<Integer> id = new ArrayList<>();
    public static ArrayList<String> code = new ArrayList<>();
    public static ArrayList<String> order_list = new ArrayList<>();
    public static ArrayList<String> order_total = new ArrayList<>();
    public static ArrayList<String> date_time = new ArrayList<>();
    List<History> arrayItemHistory;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    View view, bottom_sheet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Tools.lightNavigation(this);
        view = findViewById(android.R.id.content);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_history);
        }

        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);

        recyclerView = findViewById(R.id.recycler_view_product);
        lyt_empty_history = findViewById(R.id.lyt_empty_history);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL, 0));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        lyt_history = findViewById(R.id.lyt_history);

        adapterHistory = new AdapterHistory(this, arrayItemHistory);
        dbhelper = new DBHelper(this);

        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        new getDataTask().execute();

    }

    public void setOnItemClickListener(int position) {
        showBottomSheetDialog(position);
    }

    public void setOnLongItemClickListener(int position) {
        showClearDialog(CLEAR_ONE_ORDER, id.get(position));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.clear) {
            if (id.size() > 0) {
                showClearDialog(CLEAR_ALL_ORDER, 1111);
            } else {
                Snackbar.make(view, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showClearDialog(int flag, int id) {
        FLAG = flag;
        ID = id;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.confirm);
        switch (FLAG) {
            case 0:
                builder.setMessage(getString(R.string.clear_all_order));
                break;
            case 1:
                builder.setMessage(getString(R.string.clear_one_order));
                break;
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> {
            switch (FLAG) {
                case 0:
                    dbhelper.deleteAllDataHistory();
                    clearData();
                    new getDataTask().execute();
                    break;
                case 1:
                    dbhelper.deleteDataHistory(ID);
                    clearData();
                    new getDataTask().execute();
                    break;
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void clearData() {
        id.clear();
        code.clear();
        order_list.clear();
        order_total.clear();
        date_time.clear();
    }

    public class getDataTask extends AsyncTaskExecutor<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void params) {
            getDataFromDatabase();
            return params;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (id.size() > 0) {
                lyt_history.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapterHistory);
            } else {
                lyt_empty_history.setVisibility(View.VISIBLE);
            }
        }

    }

    public void getDataFromDatabase() {

        clearData();
        data = dbhelper.getAllDataHistory();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);
            id.add(Integer.parseInt(row.get(0).toString()));
            code.add(row.get(1).toString());
            order_list.add(row.get(2).toString());
            order_total.add(row.get(3).toString());
            date_time.add(row.get(4).toString());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, MotionEvent e) {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    private void showBottomSheetDialog(final int position) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.item_bottom_sheet, null);
        ((TextView) view.findViewById(R.id.sheet_code)).setText(code.get(position));
        ((TextView) view.findViewById(R.id.sheet_date)).setText(Tools.getFormatedDate(date_time.get(position)));
        ((TextView) view.findViewById(R.id.sheet_order_list)).setText(order_list.get(position));
        ((TextView) view.findViewById(R.id.sheet_order_total)).setText(order_total.get(position));

        view.findViewById(R.id.img_copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Order Id", code.get(position));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(ActivityHistory.this, R.string.msg_copy, Toast.LENGTH_SHORT).show();
        });

        if (Config.ENABLE_RTL_MODE) {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogRtl);
        } else {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialog);
        }
        mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
    }

}