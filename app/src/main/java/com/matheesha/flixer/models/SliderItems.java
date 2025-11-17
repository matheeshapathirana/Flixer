package com.matheesha.flixer.models;

public class SliderItems {

    private String title;


    private String poster_path;


    public SliderItems(String title, String poster_path) {
        this.title = title;
        this.poster_path = poster_path;
    }

    // Getters
    public String getTitle() {
        return title;
    }
    public String getPoster_path() {
        return poster_path;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}