package com.matheesha.flixer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        //TO DO: Set the status spinner

        //TO DO: Set the poster
    }

    @Override
    public int getItemCount() {
        return moviesWatchlist.size();
    }

    //REFERENCE: https://youtu.be/Mc0XT58A1Z4?si=WhYWOfTKLT4BmhND
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        TextView title;
        ProgressBar progressBar;
        Spinner statusSpinner;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_card_poster);
            title = itemView.findViewById(R.id.movie_card_title);
            progressBar = itemView.findViewById(R.id.movie_card_progressbar);
            statusSpinner = itemView.findViewById(R.id.movie_card_status_spinner);
        }
    }
}
