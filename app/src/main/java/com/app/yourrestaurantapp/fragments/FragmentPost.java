package com.app.yourrestaurantapp.fragments;

import static com.app.yourrestaurantapp.utilities.Constant.GET_POST;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.JsonArrayRequest;
import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.activities.ActivityPostDetail;
import com.app.yourrestaurantapp.activities.MyApplication;
import com.app.yourrestaurantapp.adapters.AdapterPost;
import com.app.yourrestaurantapp.models.Post;
import com.app.yourrestaurantapp.utilities.ItemOffsetDecoration;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FragmentPost extends Fragment {

    RecyclerView recyclerView;
    private List<Post> postList;
    private AdapterPost adapterPost;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ShimmerFrameLayout lytShimmer;
    Activity activity;
    View rootView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post, container, false);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        postList = new ArrayList<>();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new ItemOffsetDecoration(activity, R.dimen.item_offset));

        adapterPost = new AdapterPost(activity, postList);
        recyclerView.setAdapter(adapterPost);

        adapterPost.setOnItemClickListener((v, post, position) -> {
            Intent intent = new Intent(activity, ActivityPostDetail.class);
            intent.putExtra("title", post.post_title);
            intent.putExtra("image", Config.ADMIN_PANEL_URL + "/upload/post/" + post.post_image);
            intent.putExtra("description", post.post_description);
            startActivity(intent);
        });

        fetchData();
        onRefresh();

        return rootView;
    }

    private void onRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            postList.clear();
            swipeProgress(true);
            new Handler().postDelayed(this::fetchData, Config.DELAY_REFRESH);
        });
    }

    private void fetchData() {
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest request = new JsonArrayRequest(GET_POST, response -> {
            if (response == null) {
                showFailedView(true);
                swipeProgress(false);
                return;
            }

            List<Post> items = new Gson().fromJson(response.toString(), new TypeToken<List<Post>>() {
            }.getType());

            if (items.size() > 0) {
                postList.clear();
                postList.addAll(items);
                adapterPost.notifyDataSetChanged();
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

}