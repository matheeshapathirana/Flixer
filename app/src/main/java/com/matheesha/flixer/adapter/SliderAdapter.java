package com.matheesha.flixer.adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.matheesha.flixer.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private List<String> imageUrls;
    private Context context;

    public SliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.slide_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
    // Load the image so the whole poster fits inside the ImageView (no cropping).
    // Use resize with the known card dp size converted to px to avoid Picasso.fit() issues
    // Match the card width/height used in the layout (card width 240dp, ViewPager height 340dp)
    int widthDp = 240; // card width in dp (see slide_item.xml)
    int heightDp = 340; // ViewPager height in dp (see fragment_home.xml)
    float density = context.getResources().getDisplayMetrics().density;
    int widthPx = (int) (widthDp * density + 0.5f);
    int heightPx = (int) (heightDp * density + 0.5f);

    Picasso.get()
        .load(imageUrls.get(position))
        .resize(widthPx, heightPx)
        .centerCrop()
        .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slidePoster);
        }
    }
}
