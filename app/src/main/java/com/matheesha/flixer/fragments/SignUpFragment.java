package com.matheesha.flixer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.matheesha.flixer.AuthActivity;
import com.matheesha.flixer.MainActivity;
import com.matheesha.flixer.R;

// Handles the user sign-up flow.
public class SignUpFragment extends Fragment {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp, btnGoogleSignIn;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();
        initViews(view);
        setupListeners();

        return view;
    }

    // Standard view initialization.
    private void initViews(View view) {
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvLogin = view.findViewById(R.id.tvLogin);
        progressBar = view.findViewById(R.id.progressBar);
    }

    // Set up all the click listeners for the fragment.
    private void setupListeners() {
        tvLogin.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateToLogin();
            }
        });

        btnSignUp.setOnClickListener(v -> attemptSignUp());

        // This gives the user instant feedback if their passwords don't match.
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Kicks off the sign-up process.
    private void attemptSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInputs(email, password, confirmPassword)) {
            performSignUp(email, password);
        }
    }

    // Basic input validation.
    private boolean validateInputs(String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords don't match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    // Checks if the passwords match and shows an error if they don't.
    private void validatePasswordMatch() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(confirmPassword) && !password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords don't match");
        } else {
            tilConfirmPassword.setError(null);
        }
    }

    // The complex logic for Firebase authentication was generated with assistance from Gemini.
    private void performSignUp(String email, String password) {
        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        onSignUpSuccess();
                    } else {
                        onSignUpFailure(task.getException());
                    }
                });
    }

    // On a successful sign-up, send a verification email and go to the main activity.
    private void onSignUpSuccess() {
        sendVerificationEmail();
        playWelcomeAnimation();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
        }, 2000);
    }

    // Sends a verification email to the user.
    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showSnackbar("Verification email sent!");
                        }
                    });
        }
    }

    // Plays a welcome animation after a successful sign-up.
    private void playWelcomeAnimation() {
        LottieAnimationView welcomeAnimation = new LottieAnimationView(requireContext());
        // The welcome_stars animation was causing a crash, so I'm using the success_check animation instead.
        welcomeAnimation.setAnimation(R.raw.success_check);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        welcomeAnimation.setLayoutParams(params);

        FrameLayout rootView = (FrameLayout) requireActivity().findViewById(R.id.fragment_container);
        rootView.addView(welcomeAnimation);

        welcomeAnimation.playAnimation();

        // Make sure to remove the animation view after it's done.
        new Handler().postDelayed(() -> {
            if (rootView != null && welcomeAnimation != null) {
                rootView.removeView(welcomeAnimation);
            }
        }, 2000);
    }

    // Handles different kinds of sign-up failures.
    private void onSignUpFailure(Exception exception) {
        String errorMessage = "Sign up failed. Please try again.";

        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "An account with this email already exists.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Password is too weak.";
        }

        showErrorDialog(errorMessage);
    }

    // Shows and hides the progress bar.
    private void showLoading(boolean show) {
        if (show) {
            btnSignUp.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnSignUp.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    // A generic error dialog.
    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Up Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // A generic snackbar.
    private void showSnackbar(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
    }
}
