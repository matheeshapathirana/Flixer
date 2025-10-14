package com.matheesha.flixer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.matheesha.flixer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment()); //create a home fragment on app launch


        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Change fragment when the user taps on the icon in the bottom nav
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.nav_home_icon) {
                replaceFragment(new HomeFragment());
            } else if (itemID == R.id.nav_watchlist_icon) {
                replaceFragment(new WatchlistFragment());
            } else if (itemID == R.id.nav_profile_icon) {
                replaceFragment(new ProfileFragment());
            }

            return true;

        });
    }

    //Method to replace FrameLayout with the particular fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}