package com.app.restaurant.fragments;

import static com.app.restaurant.utilities.Constant.GET_HELP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.JsonArrayRequest;
import com.app.restaurant.Config;
import com.app.tucarta.restaurant.R;
import com.app.restaurant.activities.ActivityHelp;
import com.app.restaurant.activities.MyApplication;
import com.app.restaurant.adapters.AdapterHelp;
import com.app.restaurant.models.Help;
import com.app.restaurant.utilities.MyDividerItemDecoration;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FragmentHelp extends Fragment implements AdapterHelp.onItemClickListener {

    RecyclerView recyclerView;
    private List<Help> helpList;
    private AdapterHelp mAdapter;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ShimmerFrameLayout lytShimmer;
    View rootView;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_help, container, false);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        helpList = new ArrayList<>();
        mAdapter = new AdapterHelp(activity, helpList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(activity, DividerItemDecoration.VERTICAL, 0));
        recyclerView.setAdapter(mAdapter);

        fetchData();
        onRefresh();

        return rootView;
    }

    private void onRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            helpList.clear();
            swipeProgress(true);
            new Handler().postDelayed(this::fetchData, Config.DELAY_REFRESH);
        });
    }

    private void fetchData() {
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest request = new JsonArrayRequest(GET_HELP, response -> {
            if (response == null) {
                showFailedView(true);
                swipeProgress(false);
                return;
            }

            List<Help> items = new Gson().fromJson(response.toString(), new TypeToken<List<Help>>() {
            }.getType());

            if (items.size() > 0) {
                helpList.clear();
                helpList.addAll(items);
                mAdapter.notifyDataSetChanged();
                showNoItemView(false);
            } else {
                showNoItemView(true);
            }
            showFailedView(false);
            swipeProgress(false);

        }, error -> {
            showFailedView(true);
            swipeProgress(false);
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        if (show) {
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            lytNoItem.setVisibility(View.GONE);
        }
    }

    private void showFailedView(boolean show) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        if (show) {
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            lytFailed.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(boolean show) {
        if (show) {
            swipeRefreshLayout.setRefreshing(true);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            recyclerView.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(Help help) {
        Intent intent = new Intent(activity, ActivityHelp.class);
        intent.putExtra("title", help.getTitle());
        intent.putExtra("content", help.getContent());
        startActivity(intent);
    }

}