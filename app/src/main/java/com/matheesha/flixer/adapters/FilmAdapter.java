package com.matheesha.flixer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matheesha.flixer.R;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    public static class FilmItem {
        public final int tmdbId;
        public final boolean isTv;
        public final String title;
        public final String imageUrl;

        public FilmItem(int tmdbId, boolean isTv, String title, String imageUrl) {
            this.tmdbId = tmdbId;
            this.isTv = isTv;
            this.title = title;
            this.imageUrl = imageUrl;
        }
    }

    public interface OnFilmClickListener {
        void onFilmClick(FilmItem item);
    }

    private final List<FilmItem> items;
    private final Context context;
    private final int layoutResId;
    private final OnFilmClickListener clickListener;

    public FilmAdapter(Context context, List<FilmItem> items, int layoutResId, OnFilmClickListener clickListener) {
        this.context = context;
        this.items = items;
        this.layoutResId = layoutResId;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        FilmItem item = items.get(position);

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            com.squareup.picasso.Picasso.get()
                    .load(item.imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(null);
        }

        holder.titleView.setText(item.title != null ? item.title : "");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFilmClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView2);
            titleView = itemView.findViewById(R.id.textView2);
        }
    }
}
