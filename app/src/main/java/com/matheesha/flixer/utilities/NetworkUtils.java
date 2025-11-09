package com.matheesha.flixer.utilities;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.matheesha.flixer.SeriesCacheManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    public static final String FIND_BY_ID_URL = "https://api.themoviedb.org/3/find/";
    public static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    public static final String TV_SERIES_INFO_URL = "https://api.themoviedb.org/3/tv/";
    public static final String TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
    RequestQueue queue;

    public NetworkUtils(RequestQueue queue) {
        this.queue = queue;
    }

    //REFERENCE: ChatGPT
    public interface PosterFetchListener {
        void onPosterFetched(String posterURL);
    }

    public interface SeriesInfoListener {
        void onSeriesInfoFetched(JSONObject seriesInfo);
    }

    public interface SeriesProgressListener {
        void onSeriesProgressCalculated(int progress, JSONObject series);
    }

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

    //Fetching raw data for spinner setup
    public void fetchSeriesInfoByTMDB(int tmdbID, SeriesInfoListener callback) {
        String seriesURL = TV_SERIES_INFO_URL + tmdbID;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                seriesURL,
                null,
                response -> {
                    //cache the response when fetching it
                    SeriesCacheManager.putSeries(tmdbID, response);
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
