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


public class WatchlistTvFragment extends Fragment {

    //Interface to update the entry count in WatchlistFragment
    public interface OnItemCountChangeListener {
        void onItemCountChanged(int count);
    }

    ArrayList<WatchlistSeriesModel> seriesWatchlist = new ArrayList<>();
    ArrayList<WatchlistSeriesModel> filteredSeries = new ArrayList<>();
    WatchlistSeriesAdapter adapter;
    private OnItemCountChangeListener listener;
    private ListenerRegistration seriesRegistration;

    //API Endpoints to fetch posters
    public final String FIND_BY_ID_URL = "https://api.themoviedb.org/3/find/";
    public final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    public final String TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
    RequestQueue queue;

    public void setOnItemCountChangeListener(WatchlistTvFragment.OnItemCountChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist_tv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvSeriesWatchlist = view.findViewById(R.id.series_watchlist_recyclerView);
        queue = Volley.newRequestQueue(requireContext());

        setupSeriesWatchlist();

        adapter = new WatchlistSeriesAdapter(requireContext(), filteredSeries);
        rvSeriesWatchlist.setAdapter(adapter);
        rvSeriesWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSeriesWatchlist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        seriesRegistration = db.collection("users")
                .document("zxG3kkJH4WwOu4elGsCx")
                .collection("watchlist_series")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        seriesWatchlist.clear();

                        if (error != null) {
                            error.printStackTrace();
                            return;
                        }

                        if (queryDocumentSnapshots == null) return;

                        queryDocumentSnapshots.forEach(doc -> {
                            String title = doc.getString("title");
                            String status = doc.getString("status");
                            int currentSeason = doc.getLong("current_season").intValue();
                            int currentEpisode = doc.getLong("current_episode").intValue();
                            String docId = doc.getId();

                            //Hardcoded value - replace with the calculation function
                            int progress = 50;

                            //Fetching and setting the movie poster
                            WatchlistSeriesModel model = new WatchlistSeriesModel(null, title, progress, currentSeason, currentEpisode, status, docId);
                            seriesWatchlist.add(model);

                            //REFERENCE: ChatGPT - fetch the actual poster asynchronously
                            fetchSeriesPoster(doc.getId(), new WatchlistTvFragment.PosterFetchListener() {
                                @Override
                                public void onPosterFetched(String posterURL) {
                                    if (posterURL != null) {
                                        model.setPoster(posterURL);
                                        adapter.notifyDataSetChanged(); //Refresh when poster loads
                                    }
                                }
                            });
                        });

                        filteredSeries.clear();
                        filteredSeries.addAll(seriesWatchlist);
                        adapter.notifyDataSetChanged();

                        //Notify the parent fragment about the item count
                        if (listener != null) {
                            listener.onItemCountChanged(seriesWatchlist.size());
                        }
                    }
                });
    }

    //Filter by logic
    public void filterByStatus(String status) {
        filteredSeries.clear();

        if (status.equals("All")) {
            filteredSeries.addAll(seriesWatchlist);
        } else {
            for (WatchlistSeriesModel series : seriesWatchlist) {
                if (series.getStatus().equals(status)) {
                    filteredSeries.add(series);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (listener != null) {
            listener.onItemCountChanged(filteredSeries.size());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (seriesRegistration != null) {
            seriesRegistration.remove();
        }
    }

    //method to get entry count
    public int getItemCount() {
        return filteredSeries.size();
    }

    public interface PosterFetchListener {
        void onPosterFetched(String posterURL);
    }
    public void fetchSeriesPoster(String imdbID, PosterFetchListener callback) {
        String singleSeriesEndpoint = FIND_BY_ID_URL + imdbID + "?external_source=imdb_id";
        JsonObjectRequest seriesRequest =
                new JsonObjectRequest(Request.Method.GET, singleSeriesEndpoint, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.has("tv_results")) {
                                        JSONObject series = response.getJSONArray("tv_results").getJSONObject(0);
                                        String posterPath = series.getString("poster_path");
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
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                        return headers;
                    }
                };

        queue.add(seriesRequest);
    }
}