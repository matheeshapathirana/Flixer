package com.matheesha.flixer;

public class WatchlistSeriesModel {
    private String poster;
    private String title;
    private int progress;
    private int currentSeason;
    private int currentEpisode;
    private String status;
    private int tmdb_id;
    private String documentId;

    public WatchlistSeriesModel(String poster, String title, int progress, int currentSeason, int currentEpisode, String status, int tmdb_id, String documentId) {
        this.poster = poster;
        this.title = title;
        this.progress = progress;
        this.currentSeason = currentSeason;
        this.currentEpisode = currentEpisode;
        this.status = status;
        this.tmdb_id = tmdb_id;
        this.documentId = documentId;
    }

    public String getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public int getProgress() {
        return progress;
    }

    public int getCurrentSeason() {
        return currentSeason;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public String getStatus() {
        return status;
    }

    public int getTmdb_id() {
        return tmdb_id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrentSeason(int currentSeason) {
        this.currentSeason = currentSeason;
    }

    public void setCurrentEpisode(int currentEpisode) {
        this.currentEpisode = currentEpisode;
    }
}
