package com.matheesha.flixer.auth.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.matheesha.flixer.AuthActivity;
import com.matheesha.flixer.MainActivity;
import com.matheesha.flixer.R;

/**
 * This fragment handles the entire user login experience, including email/password, 
 * Google Sign-In, and password reset.
 */
public class LoginFragment extends Fragment {

    // UI Views
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    // Firebase & Google Sign-In
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> mGoogleSignInLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase and all our views.
        mAuth = FirebaseAuth.getInstance();
        initViews(view);
        setupClickListeners();
        configureGoogleSignIn();

        return view;
    }

    /**
     * A simple method to find all the UI elements in our layout file.
     */
    private void initViews(View view) {
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvSignUp = view.findViewById(R.id.tvSignUp);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        progressBar = view.findViewById(R.id.progressBar);
    }

    /**
     * Sets up all the click listeners for the buttons and text views on this screen.
     */
    private void setupClickListeners() {
        // When the user clicks "Register here", we'll take them to the sign-up screen.
        tvSignUp.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateToSignUp();
            }
        });

        // Set up the main login button.
        btnLogin.setOnClickListener(v -> attemptLoginWithEmail());

        // Set up the Google Sign-In button.
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // When the user clicks "Forgot your password?", we show the reset password dialog.
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // For a better user experience, we also allow the user to log in by pressing the "Enter" key
        // on their keyboard after typing their password.
        etPassword.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                attemptLoginWithEmail();
                return true;
            }
            return false;
        });
    }

    /**
     * This method starts the login process when the user clicks the main "Log In" button.
     */
    private void attemptLoginWithEmail() {
        // First, we get the email and password from the input fields.
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Then, we check if the inputs are valid (e.g., not empty).
        if (validateInputs(email, password)) {
            // If they are valid, we can try to log in with Firebase.
            performFirebaseLogin(email, password);
        }
    }

    /**
     * This method performs some basic checks to make sure the user has entered an email and password.
     */
    private boolean validateInputs(String email, String password) {
        // Reset any previous errors.
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        // Check if the email is empty or not a valid email address.
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        // Check if the password is empty.
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    /**
     * This method handles the actual login process with Firebase.
     */
    private void performFirebaseLogin(String email, String password) {
        // Show the loading spinner while we work.
        showLoading(true);

        // This is the main Firebase login method.
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    // Hide the loading spinner when we're done.
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // If the login was successful, we can go to the main profile screen.
                        onLoginSuccess();
                    } else {
                        // If there was a problem, we'll figure out what it was and show an error.
                        handleLoginFailure(task.getException());
                    }
                });
    }

    /**
     * This method is called when a login attempt fails. It checks the type of error and displays
     * a clear, helpful message to the user.
     */
    private void handleLoginFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // This error means there is no user account with this email address.
            tilEmail.setError("No account found with this email");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // This error means the password was incorrect.
            tilPassword.setError("Incorrect password");
        } else {
            // For any other errors, we'll just show a generic message.
            showSnackbar("Login failed. Please try again.");
        }
    }

    /**
     * This method is called after a successful login. It plays a quick success animation
     * and then navigates to the main profile screen.
     */
    private void onLoginSuccess() {
        playSuccessAnimation();
        // We wait for the animation to play before going to the next screen.
        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish(); // We finish this activity so the user can't come back to it.
            }
        }, 1200); // 1.2 seconds delay
    }


    // --- Google Sign-In Methods --- //

    /**
     * Configures the Google Sign-In client and registers the activity result launcher.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Your web client ID from Firebase
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        mGoogleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign-In was successful, we can now authenticate with Firebase.
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Google Sign-In failed.
                        showSnackbar("Google Sign-In failed. Please try again.");
                    }
                });
    }

    /**
     * Starts the Google Sign-In flow.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mGoogleSignInLauncher.launch(signInIntent);
    }

    /**
     * Authenticates the user with Firebase using the Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        onLoginSuccess();
                    } else {
                        showSnackbar("Firebase authentication with Google failed.");
                    }
                });
    }

    // --- UI Helper Methods --- //

    /**
     * This method shows or hides the loading spinner.
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * This method shows a simple message at the bottom of the screen.
     */
    private void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * This method plays a quick, satisfying success animation when the login is successful.
     */
    private void playSuccessAnimation() {
        if (getView() == null) return;

        FrameLayout rootView = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        LottieAnimationView animationView = new LottieAnimationView(getContext());
        animationView.setAnimation(R.raw.success_check);
        animationView.setSpeed(1.5f);

        rootView.addView(animationView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        animationView.playAnimation();

        // Remove the animation view after it has finished playing.
        new Handler().postDelayed(() -> rootView.removeView(animationView), 1500);
    }


    // --- Password Reset --- //

    /**
     * Shows a dialog that allows the user to enter their email to receive a password reset link.
     */
    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_password, null);
        final TextInputEditText etResetEmail = dialogView.findViewById(R.id.etResetEmail);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Send Link", (dialog, which) -> {
                    String email = etResetEmail.getText().toString().trim();
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        sendPasswordResetEmail(email);
                    } else {
                        showSnackbar("Please enter a valid email address.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a password reset email to the given email address using Firebase.
     */
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSnackbar("Password reset email sent to " + email);
                    } else {
                        showSnackbar("Failed to send reset email. Please try again.");
                    }
                });
    }
}
