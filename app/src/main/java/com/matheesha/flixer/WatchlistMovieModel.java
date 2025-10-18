package com.matheesha.flixer;

public class WatchlistMovieModel {
    private int poster;
    private String title;
    private int progress;
    private String status;

    public WatchlistMovieModel(int poster, String title, int progress, String status) {
        this.poster = poster;
        this.title = title;
        this.progress = progress;
        this.status = status;
    }

    public int getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public int getProgress() {
        return progress;
    }

    public String getStatus() {
        return status;
    }
}
