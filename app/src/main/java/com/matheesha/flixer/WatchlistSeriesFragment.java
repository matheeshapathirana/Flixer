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
import com.matheesha.flixer.utilities.DatabaseUtils;
import com.matheesha.flixer.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WatchlistSeriesFragment extends Fragment implements DatabaseUtils.WatchlistListener {

    //Interface to update the entry count in WatchlistFragment
    public interface OnItemCountChangeListener {
        void onItemCountChanged(int count);
    }

    ArrayList<WatchlistSeriesModel> seriesWatchlist = new ArrayList<>();
    ArrayList<WatchlistSeriesModel> filteredSeries = new ArrayList<>();
    WatchlistSeriesAdapter adapter;
    private OnItemCountChangeListener listener;
    private ListenerRegistration seriesRegistration;
    RequestQueue queue;
    DatabaseUtils databaseUtils;

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
        databaseUtils = DatabaseUtils.getInstance();

        setupSeriesWatchlist();

        adapter = new WatchlistSeriesAdapter(requireContext(), filteredSeries, databaseUtils, new NetworkUtils(queue));
        rvSeriesWatchlist.setAdapter(adapter);
        rvSeriesWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSeriesWatchlist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        seriesRegistration = databaseUtils.setupWatchlistListener(false, this);
    }

    @Override
    public void onWatchlistUpdate(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
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

            NetworkUtils networkUtils = new NetworkUtils(queue);
            int progress = 0;

            //Calculate progress if cache is available
            JSONObject seriesJSON = SeriesCacheManager.getSeries(tmdb_id);
            if (seriesJSON != null) {
                progress = networkUtils.calculateSeriesProgress(seriesJSON, currentSeason, currentEpisode);
            }

            //Fetching and setting the movie poster
            WatchlistSeriesModel model = new WatchlistSeriesModel(null, title, progress, currentSeason, currentEpisode, status, tmdb_id, docId);
            seriesWatchlist.add(model);

            //REFERENCE: ChatGPT - fetch the actual poster and progress asynchronously
            networkUtils.fetchPosterByIMDB(doc.getId(), false, new NetworkUtils.PosterFetchListener() {
                @Override
                public void onPosterFetched(String posterURL) {
                    if (posterURL != null) {
                        model.setPoster(posterURL);
                    }

                    //If progress wasn't calculated because of a missing cache, calculate it after fetching
                    if (!SeriesCacheManager.contains(tmdb_id)) {
                        networkUtils.fetchSeriesInfoWithProgress(tmdb_id, currentSeason, currentEpisode, new NetworkUtils.SeriesProgressListener() {
                            @Override
                            public void onSeriesProgressCalculated(int progress, JSONObject series) {
                                model.setProgress(progress);
                                notifyItemChanged(docId); //DeepSeek: Update specific item
                            }
                        });
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

    public void notifyItemChanged(String documentId) {
        int position = -1;
        for (int i=0; i<filteredSeries.size(); i++) {
            if (filteredSeries.get(i).getDocumentId().equals(documentId)) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            adapter.notifyItemChanged(position);
        }
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

}