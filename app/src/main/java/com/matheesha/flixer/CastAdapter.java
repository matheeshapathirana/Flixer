package com.matheesha.flixer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

public class CastAdapter extends RecyclerView.Adapter<CastViewHolder> {
    Context context;
    List<Cast> castList=new ArrayList<>();

    public CastAdapter(Context context, List<Cast> castList) {
        this.context = context;
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_cast,parent,false);
        CastViewHolder mycastviewholder = new CastViewHolder(view);

        return mycastviewholder;
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);

        holder.actor_name.setText(cast.getActorName());
        holder.character_name.setText(cast.getCharacterName());

        String profileUrl = cast.getProfileImageUrl();
        if (profileUrl == null || profileUrl.trim().isEmpty()) {
            holder.profile_image.setImageResource(R.drawable.blank_pfp);
        } else {
            Picasso.get().load(profileUrl).placeholder(R.drawable.blank_pfp).into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return this.castList.size();
    }
}
