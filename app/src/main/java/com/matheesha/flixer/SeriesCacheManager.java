package com.matheesha.flixer;

import org.json.JSONObject;

import java.util.HashMap;

//IDEA from ChatGPT: Create a separate class for caching series data so that any class can access them
public class SeriesCacheManager {
    private static final HashMap<Integer, JSONObject> seriesCache = new HashMap<>();

    public static final JSONObject getSeries(int tmdbId) {
        return seriesCache.get(tmdbId);
    }

    public static void putSeries(int tmdbId, JSONObject series) {
        seriesCache.put(tmdbId, series);
    }

    public static boolean contains(int tmdbId) {
        return seriesCache.containsKey(tmdbId);
    }

    public static void clear() {
        seriesCache.clear();
    }
}
