package com.matheesha.flixer.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matheesha.flixer.R;

public class CastViewHolder extends RecyclerView.ViewHolder {
    TextView actor_name, character_name;
    ImageView profile_image;

    public CastViewHolder(@NonNull View itemView) {
        super(itemView);

        actor_name = itemView.findViewById(R.id.tv_cast_name);
        character_name = itemView.findViewById(R.id.tv_character_name);
        profile_image = itemView.findViewById(R.id.iv_cast_photo);
    }
}
