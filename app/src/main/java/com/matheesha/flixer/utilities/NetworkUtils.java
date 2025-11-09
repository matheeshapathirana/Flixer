package com.matheesha.flixer.utilities;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.matheesha.flixer.WatchlistMoviesFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    //public static final???
    public final String FIND_BY_ID_URL = "https://api.themoviedb.org/3/find/";
    public final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    public final String TV_SERIES_INFO_URL = "https://api.themoviedb.org/3/tv/";
    public final String TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiYmNiMWFlZmYyOTQ2NWM0NWYwMWNkZDM0Y2JmNjJhZCIsIm5iZiI6MTc1OTE2MDE5Ni4yNTQwMDAyLCJzdWIiOiI2OGRhYTc4NDI3NDUyMjUyOTc1MzBjYTYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.UT1kxTV2oat5NuCDdLmyNxJbG2WBaO5-rw_1vXf-MUo";
    RequestQueue queue;

    public NetworkUtils(RequestQueue queue) {
        this.queue = queue;
    }

    public void fetchPosterByIMDB(String imdbID, WatchlistMoviesFragment.PosterFetchListener callback) {
        String singleMovieEndpoint = FIND_BY_ID_URL + imdbID + "?external_source=imdb_id";
        JsonObjectRequest movieRequest =
                new JsonObjectRequest(Request.Method.GET, singleMovieEndpoint, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    //REFERENCE: ChatGPT - bug fix
                                    if (response.has("movie_results")) {
                                        JSONObject movie = response.getJSONArray("movie_results").getJSONObject(0);
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

        queue.add(movieRequest);
    }

}
