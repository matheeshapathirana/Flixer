package com.matheesha.flixer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        Button openDetails = findViewById(R.id.btn_open_details);
        if (openDetails != null) {
            openDetails.setOnClickListener(v -> {
                int tmdbId = 550;
                //950387 - Minecraft Movie
                //1328049 - Sinhala Movie
                // 550 - Fight Club
                // 603 - The Matrix
                // 497698 - Black Widow
                //100088 - tlou
                boolean isTv = false;
                Intent intent = new Intent(this, FilmDetailsActivity.class);
                intent.putExtra("tmdb_id", tmdbId);
                intent.putExtra("is_tv", isTv);
                startActivity(intent);
            });
        }
    }
}