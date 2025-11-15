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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

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

    //API Endpoints to fetch posters - private static final??
    public final String FIND_BY_ID_URL = "https://api.themoviedb.org/3/find/";
    public final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    public final String TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
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
                            fetchMoviePoster(doc.getId(), new PosterFetchListener() {
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

    public void fetchMoviePoster(String imdbID, PosterFetchListener callback) {
        String singleMovieEndpoint = FIND_BY_ID_URL + imdbID + "?external_source=imdb_id";
        JsonObjectRequest movieRequest =
                new JsonObjectRequest(Request.Method.GET, singleMovieEndpoint, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    //REFERENCE: ChatGPT - bug fix
                                    if (response.has("movie_results")) {
                                        JSONObject movie = response.getJSONArray("movie_results").getJSONObject(0);
                                        String posterPath = movie.getString("poster_path");
                                        String fullPosterURL = TMDB_IMAGE_URL + posterPath;
                                        callback.onPosterFetched(fullPosterURL);
                                    } else {
                                        callback.onPosterFetched(null);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    callback.onPosterFetched(null);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String error = volleyError.toString();
                        System.out.println(error);
                    }
                }) {
                    //REFERENCE: https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                        return headers;
                    }
                };

        queue.add(movieRequest);
    }

    //REFERENCE: ChatGPT
    public interface PosterFetchListener {
        void onPosterFetched(String posterURL);
    }
}