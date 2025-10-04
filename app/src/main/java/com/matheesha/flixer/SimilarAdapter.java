package com.matheesha.flixer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SimilarAdapter extends RecyclerView.Adapter<SimilarViewHolder> {
    Context context;
    List<Similar> similarList = new ArrayList<>();

    public SimilarAdapter(Context context, List<Similar> similarList) {
        this.context = context;
        this.similarList = similarList;
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
        holder.similar_film_poster.setImageResource(similar.getPoster());
    }

    @Override
    public int getItemCount() {
        return this.similarList.size();
    }
}
