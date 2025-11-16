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

    public interface OnSliderClickListener {
        void onSliderClick(com.matheesha.flixer.adapter.FilmAdapter.FilmItem item);
    }

    private final List<com.matheesha.flixer.adapter.FilmAdapter.FilmItem> items;
    private final Context context;
    private final OnSliderClickListener clickListener;

    public SliderAdapter(Context context, List<com.matheesha.flixer.adapter.FilmAdapter.FilmItem> items, OnSliderClickListener clickListener) {
        this.context = context;
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.slide_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        com.matheesha.flixer.adapter.FilmAdapter.FilmItem item = items.get(position);

        int widthDp = 240;
        int heightDp = 340;
        float density = context.getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density + 0.5f);
        int heightPx = (int) (heightDp * density + 0.5f);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(item.imageUrl)
                    .resize(widthPx, heightPx)
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSliderClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slidePoster);
        }
    }
}
