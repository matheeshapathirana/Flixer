package com.matheesha.flixer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WatchlistSeriesAdapter extends RecyclerView.Adapter<WatchlistSeriesAdapter.MyViewHolder> {
    Context context;
    ArrayList<WatchlistSeriesModel> seriesWatchlist;

    public WatchlistSeriesAdapter(Context context, ArrayList<WatchlistSeriesModel> seriesWatchlist) {
        this.context = context;
        this.seriesWatchlist = seriesWatchlist;
    }

    @NonNull
    @Override
    public WatchlistSeriesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.watchlist_tv_card, parent, false);

        return new WatchlistSeriesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistSeriesAdapter.MyViewHolder holder, int position) {
        holder.title.setText(seriesWatchlist.get(position).getTitle());
        holder.progressBar.setProgress(seriesWatchlist.get(position).getProgress());

        //setting the image with Picasso
        Picasso.get().load(seriesWatchlist.get(position).getPoster()).into(holder.poster);

        //TO DO: Create the season and episode spinners dynamically

        //-------------- Status Spinner ---------------------
        ArrayAdapter<CharSequence> statusSpinnerAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.watch_statuses,
                android.R.layout.simple_spinner_item
        );
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(statusSpinnerAdapter);

            //Temporarily remove listener to prevent accidental triggers
        holder.statusSpinner.setOnItemSelectedListener(null);

            //Set current value
        int statusSpinnerPosition = statusSpinnerAdapter.getPosition(seriesWatchlist.get(position).getStatus());
        holder.statusSpinner.setSelection(statusSpinnerPosition);

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedStatus = adapterView.getItemAtPosition(i).toString();

                //Only update Firestore if the value actually changed
                if (!selectedStatus.equals(seriesWatchlist.get(position).getStatus())) {
                    //Update local model
                    seriesWatchlist.get(position).setStatus(selectedStatus);

                    //Update FireStore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document("zxG3kkJH4WwOu4elGsCx")
                            .collection("watchlist_series")
                            .document(seriesWatchlist.get(position).getDocumentId())
                            .update("status", selectedStatus)
                            .addOnSuccessListener(success -> {
                                Toast.makeText(context.getApplicationContext(), "Movie status updated successfully!", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context.getApplicationContext(), "Failed to update movie status!", Toast.LENGTH_LONG).show();
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //-------------- Season Spinner ---------------------

        //-------------- Episode Spinner ---------------------

        //-------------- Delete Button ----------------------
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REFERENCE: ChatGPT - Delete button functionality
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                WatchlistSeriesModel series = seriesWatchlist.get(adapterPosition);

                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove from Watchlist")
                        .setMessage("Are you sure you want to remove " + series.getTitle() + " from your watchlist?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document("zxG3kkJH4WwOu4elGsCx")
                                    .collection("watchlist_series")
                                    .document(series.getDocumentId())
                                    .delete()
                                    .addOnSuccessListener(success -> {
                                        Toast.makeText(context.getApplicationContext(), "Removed from watchlist!", Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(error -> {
                                        Toast.makeText(context.getApplicationContext(), "Failed to remove from watchlist!", Toast.LENGTH_LONG).show();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return seriesWatchlist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView poster, deleteBtn;
        TextView title;
        ProgressBar progressBar;
        Spinner seasonSpinner, episodeSpinner, statusSpinner;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.tv_card_poster);
            deleteBtn = itemView.findViewById(R.id.tv_card_delete_icon);
            title = itemView.findViewById(R.id.tv_card_title);
            progressBar = itemView.findViewById(R.id.tv_card_progressbar);
            seasonSpinner = itemView.findViewById(R.id.tv_card_season_spinner);
            episodeSpinner = itemView.findViewById(R.id.tv_card_episode_spinner);
            statusSpinner = itemView.findViewById(R.id.tv_card_status_spinner);
        }
    }
}
