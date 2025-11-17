package com.matheesha.flixer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matheesha.flixer.adapters.CastAdapter;
import com.matheesha.flixer.adapters.SimilarAdapter;
import com.matheesha.flixer.models.Cast;
import com.matheesha.flixer.models.Similar;
import com.matheesha.flixer.utilities.DatabaseUtils;
import com.matheesha.flixer.utilities.NetworkUtils;
import com.matheesha.flixer.utilities.MediaCacheManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.squareup.picasso.Picasso;

public class FilmDetailsActivity extends AppCompatActivity {
    // Request tags
    private static String TAG_MOVIE_DETAILS = "movie_details";
    private static String TAG_MOVIE_CREDITS = "movie_credits";
    private static String TAG_SIMILAR_MOVIES = "similar_movies";
    private static String TAG_EXTERNAL_IDS = "external_ids";
    private static String TAG_OMDB = "omdb";

    // To hold IMDb id fetched from TMDB external ids
    public String imdbFromExternal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    RequestQueue queue;
    NetworkUtils networkUtils;
    TextView title, year, imdb, metascore, plot, duration, language;
    ImageView poster;
    MaterialButton moreInfoButton,addToWatchlistButton;
    private boolean inWatchlist = false;

    private List<Cast> castData = new ArrayList<>();
    private CastAdapter castAdapter;
    private List<Similar> similarData = new ArrayList<>();
    private SimilarAdapter similarAdapter;
    private String moreInfoHomepageUrl = "";

    int tmdbId = -1;
    String providedId = null;
    boolean isTv = false;
    boolean triedMediaTypeFallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("tmdb_id")) {
                tmdbId = intent.getIntExtra("tmdb_id", -1);
            }

            if (intent.hasExtra("is_tv")) {
                isTv = intent.getBooleanExtra("is_tv", false);
            } else {
                String explicitType = intent.getStringExtra("media_type");
                if (explicitType != null) {
                    if (explicitType.equalsIgnoreCase("tv")) {
                        isTv = true;
                    } else if (explicitType.equalsIgnoreCase("movie")) {
                        isTv = false;
                    }
                }
            }

            providedId = intent.getStringExtra("id");
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_film_details);

    title=findViewById(R.id.tv_movie_title);
    year=findViewById(R.id.tv_release_year);
    imdb=findViewById(R.id.tv_imdb_rating);
    metascore=findViewById(R.id.tv_metascore);
    plot=findViewById(R.id.tv_plot);
    duration=findViewById(R.id.tv_duration);
    language=findViewById(R.id.tv_language);
    poster=findViewById(R.id.iv_movie_poster);
    moreInfoButton = findViewById(R.id.more_info);
    addToWatchlistButton = findViewById(R.id.btn_add_watchlist);


//      https://developer.android.com/reference/androidx/core/view/ViewCompat
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.film_details_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    queue = Volley.newRequestQueue(this);
    networkUtils = new NetworkUtils(queue);
        if (moreInfoButton != null) {
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = moreInfoHomepageUrl;
                    if (url == null || url.trim().isEmpty()) {
                        //https://developer.android.com/guide/topics/ui/notifiers/toasts
                        Toast.makeText(FilmDetailsActivity.this, "No homepage available", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        setupCastRecycler();
        setupSimilarRecycler();

        addToWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imdbFromExternal == null || imdbFromExternal.trim().isEmpty()) {
                    Toast.makeText(FilmDetailsActivity.this, "Please wait…", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isMovie = !isTv;

                if (inWatchlist) {
                    DatabaseUtils.getInstance().deleteFromWatchlist(isMovie, imdbFromExternal, new DatabaseUtils.DeleteListener() {
                        @Override
                        public void onDeleteSuccess() {
                            inWatchlist = false;
                            setWatchlistButtonText(false);
                            Toast.makeText(FilmDetailsActivity.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDeleteFailure(Exception error) {
                            Toast.makeText(FilmDetailsActivity.this, "Error removing from watchlist", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                // Add to watchlist flow
                String fire_title = title.getText().toString();
                String DEFAULT_STATUS = "Plan to Watch";
                String fire_date_added = java.time.LocalDate.now().toString();
                String fire_time_added = java.time.LocalTime.now().withNano(0).toString();

                Map<String, Object> movie = new HashMap<>();
                movie.put("title", fire_title);
                movie.put("status", DEFAULT_STATUS);
                movie.put("date_added", fire_date_added);
                movie.put("time_added", fire_time_added);
                movie.put("tmdb_id", tmdbId);

                String collection = isMovie ? "watchlist_movies" : "watchlist_series";
                if (!isMovie) {
                    movie.put("current_episode", 1);
                    movie.put("current_season", 1);
                }

                DatabaseUtils.getInstance().addToWatchlist(collection, imdbFromExternal, movie,
                        new DatabaseUtils.addToWatchlistListener() {
                            @Override
                            public void onAddSuccess() {
                                inWatchlist = true;
                                setWatchlistButtonText(true);
                                Toast.makeText(FilmDetailsActivity.this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onAddFailure(Exception error) {
                                Toast.makeText(FilmDetailsActivity.this, "Error adding to Watchlist", Toast.LENGTH_SHORT).show();
                            }
                        });

                /*
                db.collection("users")
                        .document(DatabaseUtils.getCurrentUserID())
                        .collection(collection)
                        .document(imdbFromExternal)
                        .set(movie)
                        .addOnSuccessListener(documentReference -> {
                            inWatchlist = true;
                            setWatchlistButtonText(true);
                            Toast.makeText(FilmDetailsActivity.this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FilmDetailsActivity.this, "Error adding to Watchlist", Toast.LENGTH_SHORT).show();
                        });

                 */

            }
        });

        if (tmdbId > 0) {
            LoadMovieDetails(tmdbId);
        } else if (providedId != null && !providedId.trim().isEmpty()) {
            String trimmed = providedId.trim();
            if (trimmed.startsWith("tt")) {
                resolveFromImdbId(trimmed);
            } else {
                try {
                    tmdbId = Integer.parseInt(trimmed);
                } catch (NumberFormatException ignored) {}
                if (tmdbId > 0) {
                    LoadMovieDetails(tmdbId);
                } else {
                    Toast.makeText(this, "No valid TMDB/IMDB id provided", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No TMDB/IMDB id provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void LoadMovieDetails(int id) {
        cancelPendingRequests();
        triedMediaTypeFallback = false;
        // Try cache first
        JSONObject cached = isTv ? MediaCacheManager.getSeries(id) : MediaCacheManager.getMovie(id);
        if (cached != null) {
            handleDetailsResponse(id, cached, false);
            return;
        }

        // Fallback to network
        networkUtils.fetchDetails(id, isTv, TAG_MOVIE_DETAILS, response -> {
            if (response == null) {
                if (!attemptMediaTypeFallback(id)) {
                    errorGettingData("Failed to fetch details");
                }
                return;
            }
            try {
                if (!response.isNull("success") && !response.optBoolean("success", true) && response.optInt("status_code", 0) == 34) {
                    attemptMediaTypeFallback(id);
                    return;
                }
                // cache the response
                if (isTv) {
                    MediaCacheManager.putSeries(id, response);
                } else {
                    MediaCacheManager.putMovie(id, response);
                }
                handleDetailsResponse(id, response, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleDetailsResponse(int id, JSONObject response, boolean fromNetwork) {
        try {
            String posterPath = response.optString("poster_path", "");
            updatePosterImage(posterPath);
            moreInfoHomepageUrl = response.optString("homepage", "").trim();
            // Continue fetching other data (not cached yet)
            fetchExternalIds(id);
            fetchMovieCredits(id);
            fetchSimilarMovies(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean attemptMediaTypeFallback(int id) {
        return attemptMediaTypeFallback(id, null);
    }

    private boolean attemptMediaTypeFallback(int id, VolleyError error) {
        if (triedMediaTypeFallback) {
            return false;
        }

        boolean shouldFallback = false;

        if (error != null && error.networkResponse != null) {
            int httpCode = error.networkResponse.statusCode;
            if (httpCode == 404) {
                shouldFallback = true;
            } else {
                try {
                    String body = new String(error.networkResponse.data, "UTF-8");
                    if (body.contains("\"status_code\":34")) {
                        shouldFallback = true;
                    }
                } catch (Exception ignored) {}
            }
        } else if (error == null) {
            shouldFallback = true;
        }

        if (!shouldFallback) {
            return false;
        }

        triedMediaTypeFallback = true;
        isTv = !isTv;
        LoadMovieDetails(id);
        return true;
    }

    private void resolveFromImdbId(String imdbId) {
        cancelPendingRequests();
        networkUtils.findByImdb(imdbId, "find_request", (resolvedId, resolvedIsTv) -> {
            try {
                if (resolvedId == null || resolvedId <= 0) {
                    errorGettingData("Invalid TMDB id");
                    return;
                }
                tmdbId = resolvedId;
                if (resolvedIsTv != null) {
                    isTv = resolvedIsTv;
                }
                LoadMovieDetails(tmdbId);
            } catch (Exception e) {
                e.printStackTrace();
                errorGettingData("Parse error");
            }
        });
    }


    private void fetchMovieCredits(int movieId) {
        networkUtils.fetchCredits(movieId, isTv, TAG_MOVIE_CREDITS, response -> {
            if (response == null) return;
            try {
                List<Cast> updatedCast = new ArrayList<>();
                JSONArray castArray = response.optJSONArray("cast");
                if (castArray != null) {
                    for (int i = 0; i < castArray.length(); i++) {
                        JSONObject castObject = castArray.optJSONObject(i);
                        if (castObject == null){
                            continue;
                        }

                        String department = castObject.optString("known_for_department", "");

                        if (!"Acting".equalsIgnoreCase(department)){
                            continue;
                        }

                        String name = castObject.optString("name", "").trim();
                        String character = castObject.optString("character", "").trim();
                        if (name.isEmpty() && character.isEmpty()){
                            continue;
                        }

                        String profilePath = castObject.optString("profile_path", "");
                        String profileUrl = buildProfileImageUrl(profilePath);

                        updatedCast.add(new Cast(name, character, profileUrl));
                    }
                }

                castData.clear();
                castData.addAll(updatedCast);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter
                if (castAdapter != null) {
                    castAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private String buildProfileImageUrl(String profilePath) {
        return networkUtils != null ? networkUtils.buildProfileUrl(profilePath) : null;
    }

    private void fetchSimilarMovies(int movieId) {
        networkUtils.fetchSimilar(movieId, isTv, TAG_SIMILAR_MOVIES, response -> {
            if (response == null) return;
            try {
                List<Similar> updatedSimilar = new ArrayList<>();
                JSONArray results = response.optJSONArray("results");
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject resultObject = results.optJSONObject(i);
                        if (resultObject == null){
                            continue;
                        }

                        int similarId = resultObject.optInt("id", -1);
                        String title = resultObject.optString("title", "").trim();

                        if (title.isEmpty()) {
                            title = resultObject.optString("name", "").trim();
                        }

                        String posterPath = resultObject.optString("poster_path", "");
                        String posterUrl = buildPosterImageUrl(posterPath);

                        if (similarId <= 0){
                            continue;
                        }

                        if (title.isEmpty() && posterUrl == null){
                            continue;
                        }

                        updatedSimilar.add(new Similar(similarId, title, posterUrl));
                    }
                }

                similarData.clear();
                similarData.addAll(updatedSimilar);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (similarAdapter != null) {
                    similarAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private String buildPosterImageUrl(String posterPath) {
        return networkUtils != null ? networkUtils.buildPosterUrl(posterPath) : null;
    }

    private void fetchExternalIds(int movieId) {
        networkUtils.fetchExternalIds(movieId, isTv, TAG_EXTERNAL_IDS, response -> {
            if (response == null) return;
            try {
                imdbFromExternal = response.optString("imdb_id", "");
                if (!imdbFromExternal.isEmpty() && imdb != null) {
                    // Update watchlist button state now that we know the document id
                    checkIfInWatchlistAndUpdateButton();
                    fetchOmdbDetails(imdbFromExternal);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchOmdbDetails(String imdbId) {
        if (imdbId == null || imdbId.isEmpty() || imdbId.equals("null")){
            return;
        }

        networkUtils.fetchOmdbByImdb(imdbId, TAG_OMDB, response -> {
            if (response == null) {
                errorGettingData("OMDb fetch error");
                return;
            }
            try {
                String oTitle = safeField(response, "Title");
                String oYear = safeField(response, "Year");
                String oImdbRating = safeField(response, "imdbRating");
                String oMetascore = safeField(response, "Metascore");
                String oGenres = safeField(response, "Genre");
                String oPlot = safeField(response, "Plot");
                String oRuntime = safeField(response, "Runtime");
                String oLanguage = safeField(response, "Language");

                title.setText(oTitle);
                year.setText(oYear);
                imdb.setText(oImdbRating);
                metascore.setText(oMetascore);
                plot.setText(oPlot);
                duration.setText(oRuntime);
                language.setText(oLanguage);
                populateGenresFromOmdb(oGenres);


            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

//    https://www.youtube.com/watch?v=QdJxWQY7SIE
//    https://www.youtube.com/watch?v=0Qmtt81Ypyk
//    https://developer.android.com/reference/com/google/android/material/chip/ChipGroup
    private void populateGenresFromOmdb(String genresCsv) {
        if (genresCsv == null || genresCsv.isEmpty()){
            return;
        }

        ChipGroup chipGroup = findViewById(R.id.chip_group_genres);

        if (chipGroup == null){
            return;
        }

        chipGroup.removeAllViews();
        String[] parts = genresCsv.split(",");
        for (String raw : parts) {
            String g = raw.trim();

            if (g.isEmpty()){
                continue;
            }

            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated);
            chip.setText(g);
            chip.setClickable(false);
            chip.setCheckable(false);
            chipGroup.addView(chip);
        }
    }

    private String safeField(JSONObject obj, String key) {
        if (obj == null || !obj.has(key)){
            return "";
        }

        String val = obj.optString(key, "");
        if (val == null){
            return "";
        }

        if ("N/A".equalsIgnoreCase(val.trim())){
            return "";
        }

        return val.trim();
    }


    private void setupCastRecycler() {
        RecyclerView rvCast = findViewById(R.id.rv_cast);
        if (rvCast == null){
            return;
        }

        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        castAdapter = new CastAdapter(this, castData);
        rvCast.setAdapter(castAdapter);
    }

    private void setupSimilarRecycler() {
        RecyclerView rvSimilar = findViewById(R.id.rv_similar_movies);

        if (rvSimilar == null){
            return;
        }

        rvSimilar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        similarAdapter = new SimilarAdapter(this, similarData, new SimilarAdapter.OnSimilarClickListener() {
            @Override
            public void onSimilarClicked(Similar similar) {
                onSimilarMovieSelected(similar);
            }
        });
        rvSimilar.setAdapter(similarAdapter);
    }

    private void updatePosterImage(String posterPath) {
        if (poster == null){
            return;
        }

        if (posterPath == null || posterPath.trim().isEmpty() || "null".equalsIgnoreCase(posterPath.trim())) {
            poster.setImageResource(R.drawable.loading);
                return;
        }

        String fullUrl = networkUtils != null ? networkUtils.buildOriginalImageUrl(posterPath.trim()) : null;
        if (fullUrl == null || fullUrl.trim().isEmpty()) {
            poster.setImageResource(R.drawable.loading);
            return;
        }
        Picasso.get().load(fullUrl).placeholder(R.drawable.loading).into(poster);
    }

    private void onSimilarMovieSelected(Similar similar) {
        if (similar == null){
            return;
        }

        int newId = similar.getMovieId();
        if (newId <= 0){
            return;
        }

        tmdbId = newId; // media type stays the same as similar endpoint matches current context
        title.setText("N/A");
        year.setText("N/A");
        imdb.setText("N/A");
        metascore.setText("N/A");
        plot.setText("N/A");
        duration.setText("N/A");
        language.setText("N/A");
        moreInfoHomepageUrl = "";
        inWatchlist = false;
        setWatchlistButtonText(false);
        ChipGroup chipGroup = findViewById(R.id.chip_group_genres);
        if (chipGroup != null) {
            chipGroup.removeAllViews();
        }
        castData.clear();
        if (castAdapter != null) {
            castAdapter.notifyDataSetChanged();
        }
        poster.setImageResource(R.drawable.loading);

        LoadMovieDetails(newId);
    }

    //https://stackoverflow.com/questions/29051129/android-cancel-volley-request
    private void cancelPendingRequests() {
        if (queue == null) return;
        queue.cancelAll(TAG_MOVIE_DETAILS);
        queue.cancelAll(TAG_MOVIE_CREDITS);
        queue.cancelAll(TAG_SIMILAR_MOVIES);
        queue.cancelAll(TAG_EXTERNAL_IDS);
        queue.cancelAll(TAG_OMDB);
    }

    private void errorGettingData(String error) {
        title.setText(error);
        year.setText(error);
        imdb.setText(error);
        metascore.setText(error);
        plot.setText(error);
        moreInfoHomepageUrl = "";
    }

    private void checkIfInWatchlistAndUpdateButton() {
        if (imdbFromExternal == null || imdbFromExternal.trim().isEmpty()) {
            inWatchlist = false;
            setWatchlistButtonText(false);
            return;
        }

        String collection = (!isTv) ? "watchlist_movies" : "watchlist_series";


        DatabaseUtils.getInstance().checkIfInWatchlist(collection, imdbFromExternal,
                new DatabaseUtils.checkIfInWatchlistListener() {
                    @Override
                    public void inWatchlist(DocumentSnapshot doc) {
                        inWatchlist = doc.exists();
                        setWatchlistButtonText(inWatchlist);
                    }

                    @Override
                    public void notInWatchlist(Exception error) {
                        inWatchlist = false;
                        setWatchlistButtonText(false);
                    }
                });

        /*
        db.collection("users")
                .document(DatabaseUtils.getCurrentUserID())
                .collection(collection)
                .document(imdbFromExternal)
                .get()
                .addOnSuccessListener(doc -> {
                    inWatchlist = doc.exists();
                    setWatchlistButtonText(inWatchlist);
                })
                .addOnFailureListener(e -> {
                    inWatchlist = false;
                    setWatchlistButtonText(false);
                });

         */
    }

    private void setWatchlistButtonText(boolean isIn) {
        if (addToWatchlistButton == null) return;
        addToWatchlistButton.setText(isIn ? "Remove from watchlist" : "Add to Watchlist");
    }
}

