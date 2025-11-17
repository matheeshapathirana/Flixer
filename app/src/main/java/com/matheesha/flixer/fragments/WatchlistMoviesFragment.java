package com.matheesha.flixer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.matheesha.flixer.R;
import com.matheesha.flixer.adapters.WatchlistMoviesAdapter;
import com.matheesha.flixer.models.WatchlistMovieModel;
import com.matheesha.flixer.utilities.DatabaseUtils;
import com.matheesha.flixer.utilities.MediaCacheManager;
import com.matheesha.flixer.utilities.NetworkUtils;

import org.json.JSONObject;

import java.util.ArrayList;

public class WatchlistMoviesFragment extends Fragment implements DatabaseUtils.WatchlistListener {

    //Interface to update the entry count in WatchlistFragment
    //REFERENCE: ChatGPT
    public interface OnItemCountChangeListener {
        void onItemCountChanged(int count);
    }

    ArrayList<WatchlistMovieModel> moviesWatchlist = new ArrayList<>();
    ArrayList<WatchlistMovieModel> filteredMovies = new ArrayList<>();
    WatchlistMoviesAdapter adapter;
    private OnItemCountChangeListener listener;
    private ListenerRegistration watchlistRegistration; //For removing the addSnapShotListner - REFERENCE: Gemini
    RequestQueue queue;
    DatabaseUtils databaseUtils;

    //REFERENCE: ChatGPT
    public void setOnItemCountChangeListener(OnItemCountChangeListener listener) {
        this.listener = listener;
    }

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
        queue = Volley.newRequestQueue(requireContext());
        databaseUtils = DatabaseUtils.getInstance(); //REFERENCE: Deepseek

        setupMoviesWatchlist();

        adapter = new WatchlistMoviesAdapter(requireContext(), filteredMovies, databaseUtils); //REFERENCE: ChatGPT
        rvMoviesWatchlist.setAdapter(adapter);
        rvMoviesWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));    //REFERENCE: ChatGPT
    }

    //REFERENCE: ChatGPT
    private void setupMoviesWatchlist() {
        watchlistRegistration = databaseUtils.setupWatchlistListener(true, this);
    }

    @Override
    public void onWatchlistUpdate(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
        moviesWatchlist.clear();

        if (error != null) {
            error.printStackTrace();
            return;
        }

        if (queryDocumentSnapshots == null) return;

        queryDocumentSnapshots.forEach(doc -> {
            String title = doc.getString("title");
            String status = doc.getString("status");
            String docId = doc.getId();

            Long tmdbNumber = doc.getLong("tmdb_id");
            int tmdb_id = tmdbNumber.intValue();

            int progress = 0;
            if ("Completed".equals(doc.getString("status"))) {
                progress = 100;
            }

            NetworkUtils networkUtils = new NetworkUtils(queue);
            String posterURL = null;

            //Check movie cache first
            JSONObject movieJSON = MediaCacheManager.getMovie(tmdb_id);
            if (movieJSON != null) {
                posterURL = networkUtils.extractMoviePosterURL(movieJSON);
            }

            //Fetching and setting the movie poster
            WatchlistMovieModel model = new WatchlistMovieModel(posterURL, title, progress, status, tmdb_id, docId);
            moviesWatchlist.add(model);

            //REFERENCE: ChatGPT - fetch the actual poster asynchronously
            if (movieJSON == null) {
                networkUtils.fetchMovieInfoByTMDB(tmdb_id, new NetworkUtils.MovieInfoListener() {
                    @Override
                    public void onMovieInfoFetched(JSONObject movieInfo) {
                        if (movieInfo != null) {
                            String posterURL = networkUtils.extractMoviePosterURL(movieInfo);
                            if (posterURL != null) {
                                model.setPoster(posterURL);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });

        filteredMovies.clear();
        filteredMovies.addAll(moviesWatchlist);
        adapter.notifyDataSetChanged();

        //Notify the parent fragment about the item count
        if (listener != null) {
            listener.onItemCountChanged(moviesWatchlist.size());
        }
    }

    //REFERENCE: ChatGPT
    //Filter by status logic
    public void filterByStatus(String status) {
        filteredMovies.clear();

        if (status.equals("All")) {
            filteredMovies.addAll(moviesWatchlist);
        } else {
            for (WatchlistMovieModel movie : moviesWatchlist) {
                if (movie.getStatus().equals(status)) {
                    filteredMovies.add(movie);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (listener != null) {
            listener.onItemCountChanged(filteredMovies.size());
        }
    }

    //REFERENCE: Gemini
    //Manually removing the listener when the fragment is not in use
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (watchlistRegistration != null) {
            watchlistRegistration.remove();
        }
    }

    //Method to get the entry count
    public int getItemCount() {
        return filteredMovies.size();
    }

}