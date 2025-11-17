package com.matheesha.flixer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.matheesha.flixer.fragments.LoginFragment;
import com.matheesha.flixer.fragments.SignUpFragment;
import com.matheesha.flixer.fragments.WelcomeFragment;

// This activity is just a container for the auth fragments.
public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the status bar match the background.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.auth_dark_bg));

        // No action bar on the auth screens.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        // The back button should navigate through the fragments, not exit the app.
        // Got a bit of help from Gemini to get this logic right.
        setupBackPressedHandler();

        // On first launch, show the welcome screen.
        if (savedInstanceState == null) {
            showWelcomeFragment();
        }

        // If the user is already logged in, no need to show this activity.
        checkCurrentUser();
    }

    // This handles the back button press.
    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If there are fragments in the back stack, pop the stack to go back.
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Otherwise, just finish the activity.
                    finish();
                }
            }
        });
    }

    // Shows the welcome fragment.
    private void showWelcomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, new WelcomeFragment())
                .commit();
    }

    // Swaps the current fragment with the login fragment.
    public void navigateToLogin() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack("login")
                .commit();
    }

    // Swaps the current fragment with the sign-up fragment.
    public void navigateToSignUp() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, new SignUpFragment())
                .addToBackStack("signup")
                .commit();
    }

    // Clears the back stack to go back to the welcome screen.
    public void navigateToWelcome() {
        getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
    }

    // If the user is already signed in, just go to the main activity.
    private void checkCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
