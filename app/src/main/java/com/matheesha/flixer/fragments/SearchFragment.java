package com.matheesha.flixer.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.matheesha.flixer.FilmDetailsActivity;
import com.matheesha.flixer.R;
import com.matheesha.flixer.adapters.FilmAdapter;
import com.matheesha.flixer.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchResultsRv;
    private FilmAdapter searchAdapter;
    private NetworkUtils networkUtils;

    private final List<FilmAdapter.FilmItem> searchItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkUtils = new NetworkUtils(com.android.volley.toolbox.Volley.newRequestQueue(requireContext()));


        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());


        searchEditText = view.findViewById(R.id.search_input);


        searchResultsRv = view.findViewById(R.id.search_results_recyclerview);
        searchResultsRv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        // Use grid-specific item layout so posters have equal width and ratio.(Claude Sonnet)
        searchAdapter = new FilmAdapter(requireContext(), searchItems, R.layout.viewholder_film_grid, this::openDetails);
        searchResultsRv.setAdapter(searchAdapter);

        // Search on text change-(Gemini - AI)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    Log.d("SearchFragment", "Searching for: " + s);
                    searchMovies(s.toString());
                } else if (s.length() == 0) {
                    searchItems.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchMovies(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://api.themoviedb.org/3/search/multi?query=" + encodedQuery + "&include_adult=false&language=en-US&page=1";

            Log.d("SearchFragment", "Searching URL: " + url);

            networkUtils.enqueueJsonObjectRequest(url,
                    response -> {
                        try {
                            searchItems.clear();

                            JSONArray results = response.getJSONArray("results");
                            Log.d("SearchFragment", "Found " + results.length() + " results");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                if (item.optBoolean("adult", false)) continue;
                                String posterPath = item.optString("poster_path");
                                if (posterPath == null || posterPath.isEmpty() || posterPath.equals("null")) continue;

                                int id = item.optInt("id", -1);
                                if (id <= 0) continue;

                                String mediaType = item.optString("media_type", "");
                                boolean isTv = mediaType.equalsIgnoreCase("tv");
                                String title = item.has("title") ? item.optString("title", "") : item.optString("name", "Unknown");

                                searchItems.add(new FilmAdapter.FilmItem(
                                        id,
                                        isTv,
                                        title,
                                        networkUtils.buildPosterUrl(posterPath)
                                ));
                                Log.d("SearchFragment", "Added: " + title + " (" + (isTv ? "TV" : "Movie") + ")");
                            }
                            Log.d("SearchFragment", "Total added " + searchItems.size() + " items");
                            searchAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("SearchFragment", "Error parsing search results", e);
                        }
                    },
                    error -> {
                        Log.e("SearchFragment", "Search request failed: " + error.toString());
                    });
        } catch (UnsupportedEncodingException e) {
            Log.e("SearchFragment", "Error encoding query", e);
        }
    }

    private void openDetails(FilmAdapter.FilmItem item) {
        try {
            android.content.Intent intent = new android.content.Intent(requireContext(), FilmDetailsActivity.class);
            intent.putExtra("tmdb_id", item.tmdbId);
            intent.putExtra("is_tv", item.isTv);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("SearchFragment", "Failed to open details", e);
        }
    }
}