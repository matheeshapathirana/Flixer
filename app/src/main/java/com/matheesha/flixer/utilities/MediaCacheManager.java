package com.matheesha.flixer.utilities;

import org.json.JSONObject;

import java.util.HashMap;

//IDEA from ChatGPT: Create a separate class for caching series data so that any class can access them
public class MediaCacheManager {
    private static final HashMap<Integer, JSONObject> seriesCache = new HashMap<>();
    private static final HashMap<Integer, JSONObject> movieCache = new HashMap<>();

    //Series Cache methods
    public static JSONObject getSeries(int tmdbId) {
        return seriesCache.get(tmdbId);
    }

    public static void putSeries(int tmdbId, JSONObject series) {
        seriesCache.put(tmdbId, series);
    }

    public static boolean contains(int tmdbId) {
        return seriesCache.containsKey(tmdbId);
    }

    //Movie cache methods
    public static JSONObject getMovie(int tmdbId) {
        return movieCache.get(tmdbId);
    }

    public static void putMovie(int tmdbId, JSONObject movie) {
        movieCache.put(tmdbId, movie);
    }

    public static boolean containsMovie(int tmdbId) {
        return movieCache.containsKey(tmdbId);
    }

    //Clear methods
    public static void clearSeriesCache() {
        seriesCache.clear();
    }

    public static void clearMovieCache() {
        movieCache.clear();
    }

    public static void clearAllCaches() {
        seriesCache.clear();
        movieCache.clear();
    }
}
