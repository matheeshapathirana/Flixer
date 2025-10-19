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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.squareup.picasso.Picasso;

public class FilmDetailsActivity extends AppCompatActivity {
    // Base endpoints
    private static final String TMBD_Movie_Base_URL = "https://api.themoviedb.org/3/movie/";
    private static final String TMBD_TV_Base_URL = "https://api.themoviedb.org/3/tv/";
    private static final String TMBD_FIND_URL = "https://api.themoviedb.org/3/find/";

    private static String TMBD_Movie_Search_URL = TMBD_Movie_Base_URL;
    private static String TMBD_TV_Search_URL = TMBD_TV_Base_URL;
    private static String TMDB_Image_Base_URL = "https://image.tmdb.org/t/p/original";
    private static String TMBD_Profile_Picture_Base_URL = "https://image.tmdb.org/t/p/w185";
    private static String TMDB_Poster_Base_URL = "https://image.tmdb.org/t/p/w342";
    private static String OMDB_Base_URL = "https://www.omdbapi.com/";
    private static String TAG_MOVIE_DETAILS = "movie_details";
    private static String TAG_MOVIE_CREDITS = "movie_credits";
    private static String TAG_SIMILAR_MOVIES = "similar_movies";
    private static String TAG_EXTERNAL_IDS = "external_ids";
    private static String TAG_OMDB = "omdb";
    private final static String TMDB_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
    private final static String OMDB_API_KEY = "f9c8b034";

    // To hold IMDb id fetched from TMDB external ids
    public String imdbFromExternal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    RequestQueue queue;
    TextView title, year, imdb, metascore, plot, duration, language;
    ImageView poster;
    MaterialButton moreInfoButton,addToWatchlistButton;

    private List<Cast> castData = new ArrayList<>();
    private CastAdapter castAdapter;
    private List<Similar> similarData = new ArrayList<>();
    private SimilarAdapter similarAdapter;
    private String moreInfoHomepageUrl = "";

    int tmdbId = 497698;
    //950387 - Minecraft Movie
    //1328049 - Sinhala Movie
    // 550 - Fight Club
    // 603 - The Matrix
    // 497698 - Black Widow
    String providedId = null;
    boolean isTv = false;
    boolean triedMediaTypeFallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            providedId = intent.getStringExtra("id");
            String explicitType = intent.getStringExtra("media_type");
            if (explicitType != null) {
                if (explicitType.equalsIgnoreCase("tv")) {
                    isTv = true;
                    TMBD_Movie_Search_URL = TMBD_TV_Base_URL;
                } else if (explicitType.equalsIgnoreCase("movie")) {
                    isTv = false;
                    TMBD_Movie_Search_URL = TMBD_Movie_Base_URL;
                }
            }
            applyActiveBaseUrl();
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
                String fire_title = title.getText().toString();
                String DEFAULT_STATUS = "Plan to Watch";
                String fire_date_added = java.time.LocalDate.now().toString();
                String fire_time_added = java.time.LocalTime.now().withNano(0).toString();

                Map<String, Object> movie = new HashMap<>();
                movie.put("title", fire_title);
                movie.put("status", DEFAULT_STATUS);
                movie.put("date_added", fire_date_added);
                movie.put("time_added", fire_time_added);

                db.collection("users")
                        .document("zxG3kkJH4WwOu4elGsCx") // Static user document ID for demonstration
                        .collection("watchlist_movies")
                        .document(imdbFromExternal)
                        .set(movie)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(FilmDetailsActivity.this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FilmDetailsActivity.this, "Error adding to Watchlist", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        if (providedId != null && !providedId.trim().isEmpty()) {
            String trimmed = providedId.trim();
            if (trimmed.startsWith("tt")) {
                resolveFromImdbId(trimmed);
            } else {
                try {
                    tmdbId = Integer.parseInt(trimmed);
                } catch (NumberFormatException ignored) {}
                LoadMovieDetails(tmdbId);
            }
        } else {
            LoadMovieDetails(tmdbId);
        }
    }

    private void LoadMovieDetails(int id) {
        cancelPendingRequests();
        triedMediaTypeFallback = false;
        String endpoint = TMBD_Movie_Search_URL + id + "?language=en-US";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, endpoint, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.isNull("success") && !response.optBoolean("success", true) && response.optInt("status_code", 0) == 34) {
                                attemptMediaTypeFallback(id);
                                return;
                            }
                            String posterPath = response.optString("poster_path","");
                            updatePosterImage(posterPath);
                            moreInfoHomepageUrl = response.optString("homepage","").trim();
                            fetchExternalIds(id);
                            fetchMovieCredits(id);
                            fetchSimilarMovies(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!attemptMediaTypeFallback(id, error)) {
                            errorGettingData(error.toString());
                        }
                    }

                }) {

                    //https://stackoverflow.com/questions/63870554/how-to-add-a-header-to-a-request-from-volley-library
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + TMDB_TOKEN);
                        headers.put("accept", "application/json");
                        return headers;
                    }
        };
        //https://stackoverflow.com/questions/36127870/how-to-set-tag-to-the-request-and-get-it-from-response-volley-asynchronous-reque
        //Idea from ChatGPT
        jsonObjectRequest.setTag(TAG_MOVIE_DETAILS);
        queue.add(jsonObjectRequest);
        fetchMovieCredits(id);
        fetchSimilarMovies(id);
        queue.add(jsonObjectRequest);
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
        applyActiveBaseUrl();
        LoadMovieDetails(id);
        return true;
    }

    private void resolveFromImdbId(String imdbId) {
        cancelPendingRequests();
        String url = TMBD_FIND_URL + imdbId + "?external_source=imdb_id";
        JsonObjectRequest findRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray movieResults = response.optJSONArray("movie_results");
                        JSONArray tvResults = response.optJSONArray("tv_results");

                        if (movieResults != null && movieResults.length() > 0) {
                            JSONObject first = movieResults.optJSONObject(0);
                            if (first != null) {
                                tmdbId = first.optInt("id", -1);
                                isTv = false;
                                TMBD_Movie_Search_URL = TMBD_Movie_Base_URL;
                            }
                        } else if (tvResults != null && tvResults.length() > 0) {
                            JSONObject first = tvResults.optJSONObject(0);
                            if (first != null) {
                                tmdbId = first.optInt("id", -1);
                                isTv = true;
                                TMBD_Movie_Search_URL = TMBD_TV_Base_URL;
                            }
                        } else {
                            errorGettingData("Unable to resolve IMDb id");
                            return;
                        }

                        if (tmdbId <= 0) {
                            errorGettingData("Invalid TMDB id");
                            return;
                        }
                        LoadMovieDetails(tmdbId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorGettingData("Parse error");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorGettingData(error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TMDB_TOKEN);
                headers.put("accept", "application/json");
                return headers;
            }
        };
        findRequest.setTag("find_request");
        queue.add(findRequest);
    }

    private void applyActiveBaseUrl() {
        TMBD_Movie_Search_URL = isTv ? TMBD_TV_Base_URL : TMBD_Movie_Base_URL;
    }

    private void fetchMovieCredits(int movieId) {
        String creditsUrl = TMBD_Movie_Search_URL + movieId + "/credits?language=en-US";
        JsonObjectRequest creditsRequest = new JsonObjectRequest(Request.Method.GET, creditsUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TMDB_TOKEN);
                headers.put("accept", "application/json");
                return headers;
            }
        };

        creditsRequest.setTag(TAG_MOVIE_CREDITS);
        queue.add(creditsRequest);
    }

    private String buildProfileImageUrl(String profilePath) {
        if (profilePath == null || profilePath.isEmpty()){
            return null;
        }

        String trimmed = profilePath.trim();

        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)){
            return null;
        }

        return TMBD_Profile_Picture_Base_URL + trimmed;
    }

    private void fetchSimilarMovies(int movieId) {
        String similarUrl = TMBD_Movie_Search_URL + movieId + "/similar?language=en-US&page=1";
        JsonObjectRequest similarRequest = new JsonObjectRequest(Request.Method.GET, similarUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TMDB_TOKEN);
                headers.put("accept", "application/json");
                return headers;
            }
        };

        similarRequest.setTag(TAG_SIMILAR_MOVIES);
        queue.add(similarRequest);
    }

    private String buildPosterImageUrl(String posterPath) {
        if (posterPath == null){
            return null;
        }

        String trimmed = posterPath.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)){
            return null;
        }

        return TMDB_Poster_Base_URL + trimmed;
    }

    private void fetchExternalIds(int movieId) {
        String externalIdsUrl = TMBD_Movie_Search_URL + movieId + "/external_ids?language=en-US";
        JsonObjectRequest extRequest = new JsonObjectRequest(Request.Method.GET, externalIdsUrl, null,  new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    imdbFromExternal = response.optString("imdb_id", "");
                    if (!imdbFromExternal.isEmpty() && imdb != null) {
                        fetchOmdbDetails(imdbFromExternal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorGettingData(error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TMDB_TOKEN);
                headers.put("accept", "application/json");
                return headers;
            }
        };
        extRequest.setTag(TAG_EXTERNAL_IDS);
        queue.add(extRequest);
    }

    private void fetchOmdbDetails(String imdbId) {
        if (imdbId == null || imdbId.isEmpty() || imdbId.equals("null")){
            return;
        }

        String omdbUrl = OMDB_Base_URL + "?apikey=" + OMDB_API_KEY + "&i=" + imdbId;
        JsonObjectRequest omdbRequest = new JsonObjectRequest(Request.Method.GET, omdbUrl, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
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
            }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                errorGettingData(error.toString());
            }
        });
        omdbRequest.setTag(TAG_OMDB);
        queue.add(omdbRequest);
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

        String fullUrl = TMDB_Image_Base_URL + posterPath.trim();
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
}

