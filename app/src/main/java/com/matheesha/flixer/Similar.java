package com.matheesha.flixer;

public class Similar {
    private String title;
    private String posterUrl;
    private int movieId;

    public Similar(int movieId, String title, String posterUrl) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }
}
