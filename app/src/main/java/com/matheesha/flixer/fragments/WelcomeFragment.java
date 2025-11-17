package com.matheesha.flixer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.matheesha.flixer.AuthActivity;
import com.matheesha.flixer.R;

// This is the first screen the user sees. It has buttons for logging in and signing up.
public class WelcomeFragment extends Fragment {

    private LottieAnimationView animationView;
    private Button btnLogin, btnSignUp;
    private TextView tagline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        initViews(view);
        setupAnimations();
        setupClickListeners();

        return view;
    }

    // Standard view initialization.
    private void initViews(View view) {
        animationView = view.findViewById(R.id.animationView);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        tagline = view.findViewById(R.id.tagline);

        // The logic for this gradient text was generated with assistance from Gemini.
        setModernGradientText(tagline, "Your cinematic universe awaits");
    }

    // This method applies a gradient color to the tagline text.
    private void setModernGradientText(TextView textView, String text) {
        SpannableString spannable = new SpannableString(text);

        int pinkColor = ContextCompat.getColor(requireContext(), R.color.auth_primary);
        int blueColor = ContextCompat.getColor(requireContext(), R.color.auth_accent);

        String[] words = text.split(" ");
        if (words.length >= 3) {
            int currentPosition = 0;

            spannable.setSpan(
                    new ForegroundColorSpan(pinkColor),
                    currentPosition,
                    currentPosition + words[0].length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            currentPosition += words[0].length() + 1;

            spannable.setSpan(
                    new ForegroundColorSpan(0xCCFFFFFF),
                    currentPosition,
                    currentPosition + words[1].length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            currentPosition += words[1].length() + 1;

            spannable.setSpan(
                    new ForegroundColorSpan(blueColor),
                    currentPosition,
                    text.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        textView.setText(spannable);
    }

    // Sets up the Lottie animation.
    private void setupAnimations() {
        // The movie_camera animation was causing a crash, so I'm using the cinema_background animation instead.
        animationView.setAnimation(R.raw.cinema_background);
        animationView.setSpeed(0.8f);
        animationView.playAnimation();
    }

    // Set up all the click listeners for the fragment.
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            animateModernClick(btnLogin);
            // A small delay to allow the animation to play before navigating.
            new Handler().postDelayed(() -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).navigateToLogin();
                }
            }, 120);
        });

        btnSignUp.setOnClickListener(v -> {
            animateModernClick(btnSignUp);
            new Handler().postDelayed(() -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).navigateToSignUp();
                }
            }, 120);
        });
    }

    // A simple animation to give the buttons a "bouncy" feel when clicked.
    private void animateModernClick(Button button) {
        button.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(80)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }
}
