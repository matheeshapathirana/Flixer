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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.matheesha.flixer.utilities.NetworkUtils;

import java.util.ArrayList;

public class WatchlistMoviesFragment extends Fragment {

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

        setupMoviesWatchlist();

        adapter = new WatchlistMoviesAdapter(requireContext(), filteredMovies); //REFERENCE: ChatGPT
        rvMoviesWatchlist.setAdapter(adapter);
        rvMoviesWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));    //REFERENCE: ChatGPT
    }

    //REFERENCE: ChatGPT
    private void setupMoviesWatchlist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        watchlistRegistration = db.collection("users")
                .document("zxG3kkJH4WwOu4elGsCx")
                .collection("watchlist_movies")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
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

                            int progress = 0;
                            if ("Completed".equals(doc.getString("status"))) {
                                progress = 100;
                            }

                            //Fetching and setting the movie poster
                            WatchlistMovieModel model = new WatchlistMovieModel(null, title, progress, status, docId);
                            moviesWatchlist.add(model);

                            //REFERENCE: ChatGPT - fetch the actual poster asynchronously
                            NetworkUtils networkUtils = new NetworkUtils(queue);
                            networkUtils.fetchPosterByIMDB(doc.getId(), new PosterFetchListener() {
                                @Override
                                public void onPosterFetched(String posterURL) {
                                    if (posterURL != null) {
                                        model.setPoster(posterURL);
                                        adapter.notifyDataSetChanged(); //Refresh when poster loads
                                    }
                                }
                            });
                        });

                        filteredMovies.clear();
                        filteredMovies.addAll(moviesWatchlist);
                        adapter.notifyDataSetChanged();

                        //Notify the parent fragment about the item count
                        if (listener != null) {
                            listener.onItemCountChanged(moviesWatchlist.size());
                        }
                    }
                });
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

    //REFERENCE: ChatGPT
    public interface PosterFetchListener {
        void onPosterFetched(String posterURL);
    }
}