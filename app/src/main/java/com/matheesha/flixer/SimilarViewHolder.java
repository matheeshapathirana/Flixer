package com.matheesha.flixer;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SimilarViewHolder extends RecyclerView.ViewHolder {
    TextView similar_film_title;
    ImageView similar_film_poster;

    public SimilarViewHolder(@NonNull View itemView) {
        super(itemView);

        similar_film_title = itemView.findViewById(R.id.tv_similar_title);
                        similar_film_poster = itemView.findViewById(R.id.iv_similar_poster);
    }
}
