package com.matheesha.flixer;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.matheesha.flixer.adapter.FilmAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchResultsRv;
    private FilmAdapter searchAdapter;
    private RequestQueue requestQueue;
    
    private final List<String> searchImages = new ArrayList<>();
    private final List<String> searchTitles = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext());
        
        // Back button
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Search input
        searchEditText = view.findViewById(R.id.search_input);
        
        // Results RecyclerView
        searchResultsRv = view.findViewById(R.id.search_results_recyclerview);
        searchResultsRv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        // Use grid-specific item layout so posters have equal width and ratio.
        searchAdapter = new FilmAdapter(requireContext(), searchImages, searchTitles, R.layout.viewholder_film_grid);
        searchResultsRv.setAdapter(searchAdapter);

        // Search on text change
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    Log.d("SearchFragment", "Searching for: " + s);
                    searchMovies(s.toString());
                } else if (s.length() == 0) {
                    searchImages.clear();
                    searchTitles.clear();
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

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            searchImages.clear();
                            searchTitles.clear();

                            JSONArray results = response.getJSONArray("results");
                            Log.d("SearchFragment", "Found " + results.length() + " results");
                            
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                // Skip adult content if marked
                                if (item.optBoolean("adult", false)) continue;
                                String posterPath = item.optString("poster_path");
                                if (!posterPath.isEmpty() && !posterPath.equals("null")) {
                                    searchImages.add("https://image.tmdb.org/t/p/w342" + posterPath);
                                    
                                    String title = item.has("title") ? item.getString("title") : item.optString("name", "Unknown");
                                    searchTitles.add(title);
                                    Log.d("SearchFragment", "Added: " + title);
                                }
                            }
                            Log.d("SearchFragment", "Total added " + searchImages.size() + " items");
                            searchAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("SearchFragment", "Error parsing search results", e);
                        }
                    },
                    error -> {
                        Log.e("SearchFragment", "Search request failed: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("SearchFragment", "Status code: " + error.networkResponse.statusCode);
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e("SearchFragment", "Response: " + responseBody);
                            } catch (Exception e) {
                                Log.e("SearchFragment", "Error reading response", e);
                            }
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo");
                    headers.put("accept", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (UnsupportedEncodingException e) {
            Log.e("SearchFragment", "Error encoding query", e);
        }
    }
}
