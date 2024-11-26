package com.app.yourrestaurantapp.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.app.yourrestaurantapp.Config;
import com.app.yourrestaurantapp.R;
import com.app.yourrestaurantapp.models.Slider;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterSlider extends PagerAdapter {

    private Context context;
    private List<Slider> items;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Slider slider);
    }

    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterSlider(Context context, List<Slider> list) {
        this.context = context;
        this.items = list;
    }

    @NonNull
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        final Slider slider = items.get(position);
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_slider, viewGroup, false);

        ImageView sliderImage = inflate.findViewById(R.id.slider_image);
        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/slider/" + slider.slider_image)
                .placeholder(R.drawable.ic_loading)
                .into(sliderImage);

        ((TextView) inflate.findViewById(R.id.slider_title)).setText(Html.fromHtml(slider.slider_title));

        inflate.findViewById(R.id.lyt_parent).setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, slider);
            }
        });

        viewGroup.addView(inflate);
        return inflate;
    }

    public int getCount() {
        return this.items.size();
    }

    public void destroyItem(ViewGroup viewGroup, int i, @NonNull Object obj) {
        viewGroup.removeView((View) obj);
    }

}