package com.matheesha.flixer;

public class WatchlistMovieModel {
    private int poster;
    private String title;
    private int progress;
    private String status;
    private String documentId;

    public WatchlistMovieModel(int poster, String title, int progress, String status, String documentId) {
        this.poster = poster;
        this.title = title;
        this.progress = progress;
        this.status = status;
        this.documentId = documentId;
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

    public String getDocumentId() {
        return documentId;
    }

    public void setStatus(String selectedStatus) {
        this.status = selectedStatus;
    }

}
