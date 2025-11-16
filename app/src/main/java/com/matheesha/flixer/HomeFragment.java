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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.matheesha.flixer.adapter.SliderAdapter;
import com.matheesha.flixer.adapter.FilmAdapter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.matheesha.flixer.utilities.NetworkUtils;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private SliderAdapter sliderAdapter;
    private final List<FilmAdapter.FilmItem> trendingItems = new ArrayList<>();
    private RequestQueue requestQueue;
    private NetworkUtils networkUtils;

    private RecyclerView popularMoviesRv;
    private RecyclerView popularSeriesRv;
    private FilmAdapter moviesAdapter;
    private FilmAdapter seriesAdapter;

    private final List<FilmAdapter.FilmItem> popularMovies = new ArrayList<>();
    private final List<FilmAdapter.FilmItem> popularSeries = new ArrayList<>();

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
            networkUtils = new NetworkUtils(requestQueue);

            // 2. Find the ViewPager2 by its ID from the inflated view
            viewPager = view.findViewById(R.id.slider);

            // 3. Initialize the adapter with an empty list.
            //    The adapter will be updated later when the network request finishes.
            sliderAdapter = new SliderAdapter(requireContext(), trendingItems, this::openDetails);
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
                moviesAdapter = new FilmAdapter(requireContext(), popularMovies, R.layout.viewholder_film, item -> openDetails(item));
                popularMoviesRv.setAdapter(moviesAdapter);
                fetchPopularMovies();
            }

            // 6. Setup Popular Series RecyclerView
            popularSeriesRv = view.findViewById(R.id.popular_series_recyclerview);
            if (popularSeriesRv != null) {
                popularSeriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                seriesAdapter = new FilmAdapter(requireContext(), popularSeries, R.layout.viewholder_film, item -> openDetails(item));
                popularSeriesRv.setAdapter(seriesAdapter);
                fetchPopularSeries();
            }

            // 7. Setup Search Button
            view.findViewById(R.id.searchbutton).setOnClickListener(v -> {
                // Navigate to search fragment within MainActivity's container
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            });
        } catch (Exception e) {
            Log.e("HomeFragment", "Exception in onViewCreated", e);
        }
    }

    private void fetchTrendingMovies() {
        networkUtils.fetchTrendingMovies(response -> {
            try {
                trendingItems.clear();
                JSONArray results = response.getJSONArray("results");

                int added = 0;
                for (int i = 0; i < results.length() && added < 5; i++) {
                    JSONObject movie = results.getJSONObject(i);
                    if (movie.optBoolean("adult", false)) continue;

                    String posterPath = movie.optString("poster_path", null);
                    if (posterPath == null || posterPath.equals("null")) continue;
                    String title = movie.optString("title", movie.optString("original_title", ""));
                    int id = movie.optInt("id", -1);
                    if (id <= 0) continue;

                    String fullPosterUrl = "https://image.tmdb.org/t/p/w780" + posterPath;
                    trendingItems.add(new FilmAdapter.FilmItem(
                            id,
                            false,
                            title,
                            fullPosterUrl
                    ));
                    added++;
                }

                sliderAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
        },
        error -> error.printStackTrace());
    }

    private void fetchPopularMovies() {
        networkUtils.fetchPopularMovies(response -> {
            try {
                popularMovies.clear();
                JSONArray results = response.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);

                    if (item.optBoolean("adult", false)) continue;
                    String posterPath = item.optString("poster_path", null);
                    if (posterPath == null || posterPath.equals("null")) continue;
                    String title = item.optString("title", item.optString("original_title", ""));
                    int id = item.optInt("id", -1);
                    if (id <= 0) continue;
                    popularMovies.add(new FilmAdapter.FilmItem(
                            id,
                            false,
                            title,
                            "https://image.tmdb.org/t/p/w342" + posterPath
                    ));
                }
                moviesAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("HomeFragment", "parse movies error", e);
            }
        },
        error -> Log.e("HomeFragment", "movies request error", error));
    }

    private void fetchPopularSeries() {
        networkUtils.fetchPopularSeries(response -> {
            try {
                popularSeries.clear();
                JSONArray results = response.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    if (item.optBoolean("adult", false)) continue;
                    String posterPath = item.optString("poster_path", null);
                    if (posterPath == null || posterPath.equals("null")) continue;
                    String name = item.optString("name", item.optString("original_name", ""));
                    int id = item.optInt("id", -1);
                    if (id <= 0) continue;
                    popularSeries.add(new FilmAdapter.FilmItem(
                            id,
                            true,
                            name,
                            "https://image.tmdb.org/t/p/w342" + posterPath
                    ));
                }
                seriesAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("HomeFragment", "parse series error", e);
            }
        },
        error -> Log.e("HomeFragment", "series request error", error));
    }

    private void openDetails(FilmAdapter.FilmItem item) {
        try {
            android.content.Intent intent = new android.content.Intent(requireContext(), FilmDetailsActivity.class);
            intent.putExtra("tmdb_id", item.tmdbId);
            intent.putExtra("is_tv", item.isTv);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("HomeFragment", "Failed to open details", e);
        }
    }

}
