package com.matheesha.flixer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This is the code that loads your HomeFragment
        if (savedInstanceState == null) {
            // 1. Create an instance of the HomeFragment
            Fragment homeFragment = new HomeFragment();

            // 2. Get the FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();

            // 3. Start a transaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 4. Replace the content of the container with the new fragment
            fragmentTransaction.replace(R.id.fragment_container, homeFragment);

            // 5. Commit the transaction
            fragmentTransaction.commit();
        }
    }
}
