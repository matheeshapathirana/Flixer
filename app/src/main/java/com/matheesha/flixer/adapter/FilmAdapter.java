package com.matheesha.flixer.adapter;

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

    private List<String> imageUrls;
    private List<String> titles;
    private Context context;
    private final int layoutResId;

    public FilmAdapter(Context context, List<String> imageUrls, List<String> titles) {
        this(context, imageUrls, titles, R.layout.viewholder_film);
    }

    public FilmAdapter(Context context, List<String> imageUrls, List<String> titles, int layoutResId) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.titles = titles;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        if (imageUrls != null && position < imageUrls.size()) {
            String imageUrl = imageUrls.get(position);
            com.squareup.picasso.Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        }

        if (titles != null && position < titles.size()) {
            holder.titleView.setText(titles.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
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
