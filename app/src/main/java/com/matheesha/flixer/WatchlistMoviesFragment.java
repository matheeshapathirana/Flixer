package com.matheesha.flixer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class WatchlistMoviesFragment extends Fragment {

    ArrayList<WatchlistMovieModel> moviesWatchlist = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvMoviesWatchlist = view.findViewById(R.id.movies_watchlist_recyclerView);

        setupMoviesWatchlist();

        WatchlistMoviesAdapter adapter = new WatchlistMoviesAdapter(this, moviesWatchlist);
        rvMoviesWatchlist.setAdapter(adapter);
        rvMoviesWatchlist.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupMoviesWatchlist() {
        //Make the database connection and create the movie classes
    }
}