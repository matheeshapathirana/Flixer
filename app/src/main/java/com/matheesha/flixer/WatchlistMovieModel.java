package com.matheesha.flixer;

public class WatchlistMovieModel {
    private String poster;
    private String title;
    private int progress;
    private String status;
    private int tmdb_id;
    private String documentId;

    public WatchlistMovieModel(String poster, String title, int progress, String status, int tmdb_id, String documentId) {
        this.poster = poster;
        this.title = title;
        this.progress = progress;
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
    public void setStatus(String selectedStatus) {
        this.status = selectedStatus;
    }

}
