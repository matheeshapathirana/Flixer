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

public class WatchlistMoviesAdapter extends RecyclerView.Adapter<WatchlistMoviesAdapter.MyViewHolder> {

    Context context;
    ArrayList<WatchlistMovieModel> moviesWatchlist;

    public WatchlistMoviesAdapter(Context context, ArrayList<WatchlistMovieModel> moviesWatchlist) {
        this.context = context;
        this.moviesWatchlist = moviesWatchlist;
    }

    @NonNull
    @Override
    public WatchlistMoviesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.watchlist_movie_card, parent, false);

        return new WatchlistMoviesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistMoviesAdapter.MyViewHolder holder, int position) {
        holder.title.setText(moviesWatchlist.get(position).getTitle());
        holder.progressBar.setProgress(moviesWatchlist.get(position).getProgress());

        //set the image using Picasso
        Picasso.get().load(moviesWatchlist.get(position).getPoster()).into(holder.poster);

        //REFERENCE: ChatGPT
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.watch_statuses,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(spinnerAdapter);

        //Temporarily remove listener to prevent accidental triggers
        holder.statusSpinner.setOnItemSelectedListener(null);

        //Set current value
        int spinnerPosition = spinnerAdapter.getPosition(moviesWatchlist.get(position).getStatus());
        holder.statusSpinner.setSelection(spinnerPosition);

        //REFERENCE - ChatGPT : Updating database on spinner value change
        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedStatus = adapterView.getItemAtPosition(i).toString();

                //Only update Firestore if the value actually changed
                if (!selectedStatus.equals(moviesWatchlist.get(position).getStatus())) {
                    //Update local model
                    moviesWatchlist.get(position).setStatus(selectedStatus);

                    //Update FireStore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document("zxG3kkJH4WwOu4elGsCx")
                            .collection("watchlist_movies")
                            .document(moviesWatchlist.get(position).getDocumentId())
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

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REFERENCE: ChatGPT - Delete button functionality
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                WatchlistMovieModel movie = moviesWatchlist.get(adapterPosition);

                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove from Watchlist")
                        .setMessage("Are you sure you want to remove " + movie.getTitle() + " from your watchlist?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document("zxG3kkJH4WwOu4elGsCx")
                                    .collection("watchlist_movies")
                                    .document(movie.getDocumentId())
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
        return moviesWatchlist.size();
    }

    //REFERENCE: https://youtu.be/Mc0XT58A1Z4?si=WhYWOfTKLT4BmhND
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView poster, deleteBtn;
        TextView title;
        ProgressBar progressBar;
        Spinner statusSpinner;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_card_poster);
            title = itemView.findViewById(R.id.movie_card_title);
            progressBar = itemView.findViewById(R.id.movie_card_progressbar);
            statusSpinner = itemView.findViewById(R.id.movie_card_status_spinner);
            deleteBtn = itemView.findViewById(R.id.movie_card_delete_icon);
        }
    }
}
