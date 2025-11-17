package com.matheesha.flixer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.matheesha.flixer.R;
import com.matheesha.flixer.models.Similar;
import com.squareup.picasso.Picasso;

public class SimilarAdapter extends RecyclerView.Adapter<SimilarViewHolder> {
    Context context;
    List<Similar> similarList = new ArrayList<>();
    private final OnSimilarClickListener listener;

    public SimilarAdapter(Context context, List<Similar> similarList, OnSimilarClickListener listener) {
        this.context = context;
        this.similarList = similarList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SimilarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_movie_horizontal, parent, false);
        SimilarViewHolder mysimilarviewholder = new SimilarViewHolder(view);

        return mysimilarviewholder;
    }

    @Override
    public void onBindViewHolder(@NonNull SimilarViewHolder holder, int position) {
        Similar similar = similarList.get(position);

        holder.similar_film_title.setText(similar.getTitle());
        String posterUrl = similar.getPosterUrl();
        if (posterUrl == null || posterUrl.trim().isEmpty()) {
            holder.similar_film_poster.setImageResource(R.drawable.loading);
        } else {
            Picasso.get().load(posterUrl).placeholder(R.drawable.loading).into(holder.similar_film_poster);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSimilarClicked(similar);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.similarList.size();
    }

    public interface OnSimilarClickListener {
        void onSimilarClicked(Similar similar);
    }
}
