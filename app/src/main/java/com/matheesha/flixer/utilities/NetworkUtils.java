package com.matheesha.flixer.utilities;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.matheesha.flixer.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    private static final String FIND_BY_ID_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    private static final String TMDB_PROFILE_URL = "https://image.tmdb.org/t/p/w185";
    private static final String TMDB_POSTER_W342_URL = "https://image.tmdb.org/t/p/w342";
    private static final String TV_SERIES_INFO_URL = "https://api.themoviedb.org/3/tv/";
    private static final String MOVIE_INFO_URL = "https://api.themoviedb.org/3/movie/";
    private static final String TRENDING_MOVIES_URL = "https://api.themoviedb.org/3/trending/movie/week?language=en-US";
    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/discover/movie?language=en-US&page=1&include_adult=false&sort_by=popularity.desc";
    private static final String POPULAR_SERIES_URL = "https://api.themoviedb.org/3/discover/tv?language=en-US&page=1&include_adult=false&sort_by=popularity.desc";
    private static final String OMDB_BASE_URL = "https://www.omdbapi.com/";
    //https://al-e-shevelev.medium.com/a-secure-way-to-store-api-keys-in-android-applications-238135709067
    private static final String TMDB_ACCESS_TOKEN = BuildConfig.TMDB_ACCESS_TOKEN;
    private static final String OMDB_API_KEY = BuildConfig.OMDB_API_KEY;
    RequestQueue queue;

    public NetworkUtils(RequestQueue queue) {
        this.queue = queue;
    }

    /*
    //REFERENCE: ChatGPT
    public interface PosterFetchListener {
        void onPosterFetched(String posterURL);
    }
    */

    public interface MovieInfoListener {
        void onMovieInfoFetched(JSONObject movieInfo);
    }

    public interface SeriesInfoListener {
        void onSeriesInfoFetched(JSONObject seriesInfo);
    }

    public interface SeriesProgressListener {
        void onSeriesProgressCalculated(int progress, JSONObject series);
    }

    // Generic JSON callback used for list endpoints (JSONArray root or array field)
    public interface JsonArrayListener {
        void onResponse(JSONArray jsonArray);
    }

    // Generic JSON callback used for multiple endpoints
    public interface JsonListener {
        void onResponse(JSONObject json);
    }

    // Resolve TMDB id and media type from IMDb id
    public interface IdResolveListener {
        void onResolved(Integer tmdbId, Boolean isTv);
    }

    /*
    public void fetchPosterByIMDB(String imdbID, boolean isMovie, PosterFetchListener callback) {
        String endpoint = FIND_BY_ID_URL + imdbID + "?external_source=imdb_id";
        String resultArrayName = isMovie ? "movie_results" : "tv_results";
        JsonObjectRequest posterRequest =
                new JsonObjectRequest(Request.Method.GET, endpoint, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    //REFERENCE: ChatGPT - bug fix
                                    if (response.has(resultArrayName)) {
                                        JSONObject movie = response.getJSONArray(resultArrayName).getJSONObject(0);
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

        queue.add(posterRequest);
    }
    */

    public void fetchMovieInfoByTMDB(int tmdbID, MovieInfoListener callback) {
        String movieURL = MOVIE_INFO_URL + tmdbID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, movieURL, null,
                response -> {
                    //Cache movie details
                    MediaCacheManager.putMovie(tmdbID, response);
                    callback.onMovieInfoFetched(response);
                },
                error -> {
                    error.printStackTrace();
                    callback.onMovieInfoFetched(null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }

    // Fetch trending movies (week)
    public void fetchTrendingMovies(Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                TRENDING_MOVIES_URL,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }

    // Fetch popular movies list
    public void fetchPopularMovies(Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                POPULAR_MOVIES_URL,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }

    // Fetch popular TV series list
    public void fetchPopularSeries(Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                POPULAR_SERIES_URL,
                null,
                listener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }

    // Unified details fetcher for Movie or TV with language parameter and optional tag
    public void fetchDetails(int tmdbID, boolean isTv, String tag, JsonListener callback) {
        String base = isTv ? TV_SERIES_INFO_URL : MOVIE_INFO_URL;
        String url = base + tmdbID + "?language=en-US";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onResponse(response),
                error -> {
                    error.printStackTrace();
                    callback.onResponse(null);
                }
        ) {
            //https://stackoverflow.com/questions/63870554/how-to-add-a-header-to-a-request-from-volley-library
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        //https://stackoverflow.com/questions/36127870/how-to-set-tag-to-the-request-and-get-it-from-response-volley-asynchronous-reque
        //Idea from ChatGPT
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    //Extracts poster URL from movie JSON
    public String extractMoviePosterURL (JSONObject movieDetails) {
        try {
            if (movieDetails != null && movieDetails.has("poster_path")) {
                String posterPath = movieDetails.getString("poster_path");
                return TMDB_IMAGE_URL + posterPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Builders for various image sizes
    public String buildOriginalImageUrl(String path) {
        if (path == null) return null;
        String trimmed = path.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        return TMDB_IMAGE_URL + trimmed;
    }

    public String buildPosterUrl(String posterPath) {
        if (posterPath == null) return null;
        String trimmed = posterPath.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        return TMDB_POSTER_W342_URL + trimmed;
    }

    public String buildProfileUrl(String profilePath) {
        if (profilePath == null) return null;
        String trimmed = profilePath.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        return TMDB_PROFILE_URL + trimmed;
    }

    //Fetching raw data for spinner setup
    public void fetchSeriesInfoByTMDB(int tmdbID, SeriesInfoListener callback) {
        String seriesURL = TV_SERIES_INFO_URL + tmdbID;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                seriesURL,
                null,
                response -> {
                    //cache the response when fetching it
                    MediaCacheManager.putSeries(tmdbID, response);
                    callback.onSeriesInfoFetched(response);
                },
                error -> {
                    error.printStackTrace();
                    callback.onSeriesInfoFetched(null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };

        queue.add(request);
    }

    //Fetch series info AND calculate progress - needed for fragment
    public void fetchSeriesInfoWithProgress(int tmdbID, int currentSeason, int currentEpisode, SeriesProgressListener callback) {
        fetchSeriesInfoByTMDB(tmdbID, new SeriesInfoListener() {
            @Override
            public void onSeriesInfoFetched(JSONObject seriesInfo) {
                int progress = calculateSeriesProgress(seriesInfo, currentSeason, currentEpisode);
                callback.onSeriesProgressCalculated(progress, seriesInfo);
            }
        });
    }

    // Fetch credits for movie/tv
    public void fetchCredits(int tmdbID, boolean isTv, String tag, JsonListener callback) {
        String base = isTv ? TV_SERIES_INFO_URL : MOVIE_INFO_URL;
        String url = base + tmdbID + "/credits?language=en-US";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onResponse(response),
                error -> {
                    error.printStackTrace();
                    callback.onResponse(null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    // Fetch similar items for movie/tv
    public void fetchSimilar(int tmdbID, boolean isTv, String tag, JsonListener callback) {
        String base = isTv ? TV_SERIES_INFO_URL : MOVIE_INFO_URL;
        String url = base + tmdbID + "/similar?language=en-US&page=1";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onResponse(response),
                error -> {
                    error.printStackTrace();
                    callback.onResponse(null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    // Fetch external ids for movie/tv
    public void fetchExternalIds(int tmdbID, boolean isTv, String tag, JsonListener callback) {
        String base = isTv ? TV_SERIES_INFO_URL : MOVIE_INFO_URL;
        String url = base + tmdbID + "/external_ids?language=en-US";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onResponse(response),
                error -> {
                    error.printStackTrace();
                    callback.onResponse(null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    // Resolve TMDB id by IMDb id and detect media type
    public void findByImdb(String imdbId, String tag, IdResolveListener callback) {
        String url = FIND_BY_ID_URL + imdbId + "?external_source=imdb_id";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray movieResults = response.optJSONArray("movie_results");
                        JSONArray tvResults = response.optJSONArray("tv_results");
                        if (movieResults != null && movieResults.length() > 0) {
                            JSONObject first = movieResults.optJSONObject(0);
                            if (first != null) {
                                callback.onResolved(first.optInt("id", -1), false);
                                return;
                            }
                        }
                        if (tvResults != null && tvResults.length() > 0) {
                            JSONObject first = tvResults.optJSONObject(0);
                            if (first != null) {
                                callback.onResolved(first.optInt("id", -1), true);
                                return;
                            }
                        }
                        callback.onResolved(null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResolved(null, null);
                    }
                },
                error -> {
                    error.printStackTrace();
                    callback.onResolved(null, null);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);
                return headers;
            }
        };
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    // Fetch OMDB details by IMDb id
    public void fetchOmdbByImdb(String imdbId, String tag, JsonListener callback) {
        if (imdbId == null || imdbId.isEmpty() || "null".equalsIgnoreCase(imdbId)) {
            callback.onResponse(null);
            return;
        }
        String url = OMDB_BASE_URL + "?apikey=" + OMDB_API_KEY + "&i=" + imdbId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onResponse(response),
                error -> {
                    error.printStackTrace();
                    callback.onResponse(null);
                }
        );
        if (tag != null) request.setTag(tag);
        queue.add(request);
    }

    public int calculateSeriesProgress(JSONObject series, int currentSeason, int currentEpisode) {
        if (series == null) {
            return 0;
        }

        try {
            JSONArray seasons = series.getJSONArray("seasons");
            int totalEpisodes = 0;
            int totalWatched = 0;

            for (int i=0; i<seasons.length(); i++) {
                JSONObject season = seasons.getJSONObject(i);
                if (season.getInt("season_number") == 0) continue; //skip specials

                if (season.getInt("season_number") < currentSeason) {
                    totalWatched += season.getInt("episode_count");
                }

                totalEpisodes += season.getInt("episode_count");
            }

            totalWatched += currentEpisode;

            return (int)(((double)totalWatched / totalEpisodes) * 100);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
