package com.matheesha.flixer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.matheesha.flixer.adapter.SliderAdapter;
import com.matheesha.flixer.adapter.FilmAdapter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;

import androidx.recyclerview.widget.LinearLayoutManager;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private SliderAdapter sliderAdapter;
    private List<String> sliderImages = new ArrayList<>();
    private RequestQueue requestQueue;

    private RecyclerView popularMoviesRv;
    private RecyclerView popularSeriesRv;
    private FilmAdapter moviesAdapter;
    private FilmAdapter seriesAdapter;

    private final List<String> movieImages = new ArrayList<>();
    private final List<String> movieTitles = new ArrayList<>();
    private final List<String> seriesImages = new ArrayList<>();
    private final List<String> seriesTitles = new ArrayList<>();

    // onCreateView is only for creating and returning the view.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    // onViewCreated is the correct place to find views and set up listeners/adapters.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Wrap lifecycle setup in a try/catch to log any exceptions instead of letting the app crash
        try {
            // 1. Initialize Volley RequestQueue
            requestQueue = Volley.newRequestQueue(requireContext());

            // 2. Find the ViewPager2 by its ID from the inflated view
            viewPager = view.findViewById(R.id.slider);

            // 3. Initialize the adapter with an empty list.
            //    The adapter will be updated later when the network request finishes.
            sliderAdapter = new SliderAdapter(requireContext(), sliderImages);
            viewPager.setAdapter(sliderAdapter);

            // Make the ViewPager show a preview of adjacent pages and apply a scaling transform
            viewPager.setOffscreenPageLimit(3);

            // Access the internal RecyclerView safely (it may not be created immediately)
            viewPager.post(() -> {
                View child = viewPager.getChildAt(0);
                if (child instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) child;
                    recyclerView.setClipToPadding(false);
                    recyclerView.setClipChildren(false);

                    // Provide horizontal padding so adjacent pages peek in from the sides.
                    // Use a smaller padding so center page appears larger on screen.
                    int sidePadding = (int) (getResources().getDisplayMetrics().density * 16); // ~16dp
                    recyclerView.setPadding(sidePadding, 0, sidePadding, 0);
                }
            });

            // Add margin + scale transformer so center page is emphasized and neighbors are visible
            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer(new MarginPageTransformer((int) (getResources().getDisplayMetrics().density * 8)));
            compositePageTransformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.9f + r * 0.1f);
            });
            viewPager.setPageTransformer(compositePageTransformer);

            // 4. Fetch the movie data from the API
            fetchTrendingMovies();

            // 5. Setup Popular Movies RecyclerView
            popularMoviesRv = view.findViewById(R.id.popular_movies_recyclerview);
            if (popularMoviesRv != null) {
                popularMoviesRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                moviesAdapter = new FilmAdapter(requireContext(), movieImages, movieTitles, R.layout.viewholder_film);
                popularMoviesRv.setAdapter(moviesAdapter);
                fetchPopularMovies();
            }

            // 6. Setup Popular Series RecyclerView
            popularSeriesRv = view.findViewById(R.id.popular_series_recyclerview);
            if (popularSeriesRv != null) {
                popularSeriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                seriesAdapter = new FilmAdapter(requireContext(), seriesImages, seriesTitles, R.layout.viewholder_film);
                popularSeriesRv.setAdapter(seriesAdapter);
                fetchPopularSeries();
            }

            // 7. Setup Search Button
            view.findViewById(R.id.searchbutton).setOnClickListener(v -> {
                // Navigate to search fragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            });
        } catch (Exception e) {
            Log.e("HomeFragment", "Exception in onViewCreated", e);
        }
    }

    private void fetchTrendingMovies() {

        String url = "https://api.themoviedb.org/3/trending/movie/week?language=en-US";



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        sliderImages.clear();

                        JSONArray results = response.getJSONArray("results");

                        int added = 0;
                        for (int i = 0; i < results.length() && added < 5; i++) {
                            JSONObject movie = results.getJSONObject(i);
                            // Skip adult content
                            if (movie.optBoolean("adult", false)) continue;

                            String posterPath = movie.optString("poster_path", null);
                            if (posterPath == null || posterPath.equals("null")) continue;

                            String fullPosterUrl = "https://image.tmdb.org/t/p/w780" + posterPath;
                            sliderImages.add(fullPosterUrl);
                            added++;
                        }

                        sliderAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo");
                headers.put("accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void fetchPopularMovies() {
        String url = "https://api.themoviedb.org/3/discover/movie?language=en-US&page=1&include_adult=false&sort_by=popularity.desc";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        movieImages.clear();
                        movieTitles.clear();
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);

                            if (item.optBoolean("adult", false)) continue;
                            String posterPath = item.optString("poster_path", null);
                            if (posterPath == null || posterPath.equals("null")) continue;
                            String title = item.optString("title", item.optString("original_title", ""));
                            movieImages.add("https://image.tmdb.org/t/p/w342" + posterPath);
                            movieTitles.add(title);
                        }
                        moviesAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("HomeFragment", "parse movies error", e);
                    }
                },
                error -> Log.e("HomeFragment", "movies request error", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    private void fetchPopularSeries() {
        String url = "https://api.themoviedb.org/3/discover/tv?language=en-US&page=1&include_adult=false&sort_by=popularity.desc";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        seriesImages.clear();
                        seriesTitles.clear();
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            // Skip adult content if field exists
                            if (item.optBoolean("adult", false)) continue;
                            String posterPath = item.optString("poster_path", null);
                            if (posterPath == null || posterPath.equals("null")) continue;
                            String name = item.optString("name", item.optString("original_name", ""));
                            seriesImages.add("https://image.tmdb.org/t/p/w342" + posterPath);
                            seriesTitles.add(name);
                        }
                        seriesAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("HomeFragment", "parse series error", e);
                    }
                },
                error -> Log.e("HomeFragment", "series request error", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(request);
    }

}
