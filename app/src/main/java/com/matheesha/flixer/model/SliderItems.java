package com.matheesha.flixer.model;

public class SliderItems {
    // Field to store the movie title
    private String title;

    // Field to store the poster image path
    // Note: TMDb provides a partial path, not a full URL.
    private String poster_path;

    // Constructor to initialize the object with a title and poster path
    public SliderItems(String title, String poster_path) {
        this.title = title;
        this.poster_path = poster_path;
    }

    // Getter for the title
    public String getTitle() {
        return title;
    }

    // Setter for the title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for the poster path
    public String getPoster_path() {
        return poster_path;
    }

    // Setter for the poster path
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}
