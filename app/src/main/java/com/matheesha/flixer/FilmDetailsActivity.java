package com.matheesha.flixer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilmDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_film_details);

//      https://developer.android.com/reference/androidx/core/view/ViewCompat
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.film_details_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupGenreChips();
        setupCastRecycler();
        setupSimilarRecycler();
        populateStaticDetails();
    }

    private void setupGenreChips() {
        ChipGroup chipGroup = findViewById(R.id.chip_group_genres);
        if (chipGroup == null) return;
        List<String> genres = Arrays.asList("Action", "Adventure", "Sci-Fi");
        chipGroup.removeAllViews();
        for (String g : genres) {
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated);
            chip.setText(g);
            chip.setClickable(false);
            chip.setCheckable(false);
            chipGroup.addView(chip);
        }
    }

    private void setupCastRecycler() {
        RecyclerView rvCast = findViewById(R.id.rv_cast);
        if (rvCast == null) return;
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Cast> cast = new ArrayList<>();
        cast.add(new Cast("John Doe", "Hero", R.drawable.placeholder_person));
        cast.add(new Cast("Jane Smith", "Sidekick", R.drawable.placeholder_person));
        cast.add(new Cast("Alan S.", "Villain", R.drawable.placeholder_person));
        cast.add(new Cast("Maria R.", "Scientist", R.drawable.placeholder_person));

        rvCast.setAdapter(new CastAdapter(this, cast));
    }

    private void setupSimilarRecycler() {
        RecyclerView rvSimilar = findViewById(R.id.rv_similar_movies);
        if (rvSimilar == null) return;
        rvSimilar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Similar> movies = new ArrayList<>();
        movies.add(new Similar("Galaxy Wars", R.drawable.placeholder_poster));
        movies.add(new Similar("Future Quest", R.drawable.placeholder_poster));
        movies.add(new Similar("Star Ranger", R.drawable.placeholder_poster));
        movies.add(new Similar("Quantum Leap", R.drawable.placeholder_poster));

        rvSimilar.setAdapter(new SimilarAdapter(this, movies));
    }

    private void populateStaticDetails() {
        TextView title = findViewById(R.id.tv_movie_title);
        TextView year = findViewById(R.id.tv_release_year);
        TextView imdb = findViewById(R.id.tv_imdb_rating);
        TextView tmdb = findViewById(R.id.tv_tmdb_rating);
        TextView plot = findViewById(R.id.tv_plot);
        if (title != null) title.setText("Test Movie");
        if (year != null) year.setText("2025");
        if (imdb != null) imdb.setText("8.3");
        if (tmdb != null) tmdb.setText("7.9");
        if (plot != null) plot.setText("A thrilling space adventure that showcases how to preview a UI layout without building full backend logic.");
    }

}

