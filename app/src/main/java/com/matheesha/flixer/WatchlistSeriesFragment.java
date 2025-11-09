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
import com.matheesha.flixer.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WatchlistSeriesFragment extends Fragment {

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
    public final String TV_SERIES_INFO_URL = "https://api.themoviedb.org/3/tv/";
    public final String TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
    RequestQueue queue;

    public void setOnItemCountChangeListener(WatchlistSeriesFragment.OnItemCountChangeListener listener) {
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
                            int tmdb_id = doc.getLong("tmdb_id").intValue();
                            String docId = doc.getId();
                            int progress = calculateProgress(tmdb_id, currentSeason, currentEpisode);

                            //Fetching and setting the movie poster
                            WatchlistSeriesModel model = new WatchlistSeriesModel(null, title, progress, currentSeason, currentEpisode, status, tmdb_id, docId);

                            //Calculate and set progress early if cache is available
                            JSONObject seriesJSON = SeriesCacheManager.getSeries(tmdb_id);
                            if (seriesJSON != null) {
                                int calculatedProgress = calculateProgress(tmdb_id, currentSeason, currentEpisode);
                                model.setProgress(calculatedProgress);
                            }

                            seriesWatchlist.add(model);

                            //REFERENCE: ChatGPT - fetch the actual poster and progress asynchronously
                            NetworkUtils networkUtils = new NetworkUtils(queue);
                            networkUtils.fetchPosterByIMDB(doc.getId(), false, new NetworkUtils.PosterFetchListener() {
                                @Override
                                public void onPosterFetched(String posterURL) {
                                    if (posterURL != null) {
                                        model.setPoster(posterURL);
                                    }

                                    //If progress wasn't calculated because of a missing cache, calculate it after fetching
                                    if (!SeriesCacheManager.contains(tmdb_id)) {
                                        String seriesURL = TV_SERIES_INFO_URL + tmdb_id;
                                        JsonObjectRequest request = new JsonObjectRequest(
                                                Request.Method.GET,
                                                seriesURL,
                                                null,
                                                response -> {
                                                    SeriesCacheManager.putSeries(tmdb_id, response);
                                                    int updatedProgress = calculateProgress(tmdb_id, currentSeason, currentEpisode);
                                                    model.setProgress(updatedProgress);

                                                    int position = -1;
                                                    for (int i=0; i<filteredSeries.size(); i++) {
                                                        if (filteredSeries.get(i).getDocumentId().equals(docId)) {
                                                            position = i;
                                                            break;
                                                        }
                                                    }

                                                    if (position != -1) {
                                                        adapter.notifyItemChanged(position);
                                                    }
                                                },
                                                error -> {
                                                    error.printStackTrace();
                                                }
                                        ) {
                                            @Override
                                            public Map<String, String> getHeaders() throws AuthFailureError {
                                                Map<String, String> headers = new HashMap<>();
                                                headers.put("accept", "application/json");
                                                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                                                return headers;
                                            }
                                        };

                                        queue.add(request);
                                    } else {
                                        int position = -1;
                                        for (int i=0; i<filteredSeries.size(); i++) {
                                            if (filteredSeries.get(i).getDocumentId().equals(docId)) {
                                                position = i;
                                                break;
                                            }
                                        }

                                        if (position != -1) {
                                            adapter.notifyItemChanged(position);
                                        }
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

    //Method to calculate series progress
    private int calculateProgress(int tmdbId, int currentSeason, int currentEpisode) {
        JSONObject series = SeriesCacheManager.getSeries(tmdbId);

        if (series == null) {
            return 0;
        }

        try {
            JSONArray seasons = series.getJSONArray("seasons");
            int totalEpisodes = 0;
            int totalWatched = 0;

            for (int i=1; i<seasons.length(); i++) {
                JSONObject season = seasons.getJSONObject(i);
                if (season.getInt("season_number") == 0) continue; //skip specials

                if (season.getInt("season_number") < currentSeason) {
                    totalWatched += season.getInt("episode_count");
                }

                totalEpisodes += season.getInt("episode_count");
            }

            totalWatched += currentEpisode;

            return (int)(((double) totalWatched / totalEpisodes) * 100);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}