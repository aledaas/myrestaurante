package com.app.yourrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.models.Post;
import com.app.yourrestaurantapp.utilities.SharedPref;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyViewHolder> {

    Context context;
    private List<Post> posts;
    SharedPref sharedPref;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Post post, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView post_title;
        public ImageView post_image;
        public LinearLayout lyt_parent;

        public MyViewHolder(View view) {
            super(view);
            post_title = view.findViewById(R.id.post_title);
            post_image = view.findViewById(R.id.post_image);
            lyt_parent = view.findViewById(R.id.lyt_parent);
        }
    }

    public AdapterPost(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.sharedPref = new SharedPref(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Post post = posts.get(position);

        holder.post_title.setText(post.post_title);

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/post/" + post.post_image)
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .into(holder.post_image);

        holder.lyt_parent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, post, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setListData(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.posts.clear();
        notifyDataSetChanged();
    }

}
