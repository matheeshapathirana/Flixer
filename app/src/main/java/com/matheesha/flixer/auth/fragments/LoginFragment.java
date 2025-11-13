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

// Handles the user login flow.
public class LoginFragment extends Fragment {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> mGoogleSignInLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        initViews(view);
        setupListeners();
        configureGoogleSignIn();

        return view;
    }

    // Standard view initialization.
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

    // Set up all the click listeners for the fragment.
    private void setupListeners() {
        tvSignUp.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateToSignUp();
            }
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Also let the user log in by pressing the enter key on the keyboard.
        etPassword.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        mGoogleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // The ApiException status code gives a detailed failure reason.
                        // Common error codes include:
                        // 12501: User canceled the sign-in flow.
                        // 10: DEVELOPER_ERROR - often due to incorrect SHA-1 key or client ID.
                        showErrorDialog("Google sign in failed. Error Code: " + e.getStatusCode());
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mGoogleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        onLoginSuccess();
                    } else {
                        onLoginFailure(task.getException());
                    }
                });
    }


    // Kicks off the login process.
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            performLogin(email, password);
        }
    }

    // Basic input validation.
    private boolean validateInputs(String email, String password) {
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

        return isValid;
    }

    // The complex logic for Firebase authentication was generated with assistance from Gemini.
    private void performLogin(String email, String password) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        onLoginSuccess();
                    } else {
                        onLoginFailure(task.getException());
                    }
                });
    }

    // On a successful login, play a success animation and go to the main activity.
    private void onLoginSuccess() {
        playSuccessAnimation();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
        }, 1000);
    }

    // Handles different kinds of login failures.
    private void onLoginFailure(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid password.";
        } else {
            // Provide a more detailed error message for other failures, including Google Sign-In.
            errorMessage = "Authentication failed: " + exception.getLocalizedMessage();
        }

        showErrorDialog(errorMessage);
    }

    // Shows and hides the progress bar.
    private void showLoading(boolean show) {
        if (show) {
            btnLogin.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    // Plays a success animation after a successful login.
    private void playSuccessAnimation() {
        LottieAnimationView successAnimation = new LottieAnimationView(requireContext());
        successAnimation.setAnimation(R.raw.success_check);
        successAnimation.setSpeed(1.5f);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        successAnimation.setLayoutParams(params);

        FrameLayout rootView = (FrameLayout) requireActivity().findViewById(R.id.fragment_container);
        rootView.addView(successAnimation);

        successAnimation.playAnimation();

        // Make sure to remove the animation view after it's done.
        new Handler().postDelayed(() -> {
            if (rootView != null && successAnimation != null) {
                rootView.removeView(successAnimation);
            }
        }, 1500);
    }

    // Shows a dialog for the user to reset their password.
    private void showForgotPasswordDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Reset Password");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null);
        TextInputEditText etResetEmail = dialogView.findViewById(R.id.etResetEmail);

        builder.setView(dialogView);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = etResetEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendPasswordResetEmail(email);
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    // Sends a password reset email to the user.
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSnackbar("Password reset email sent!");
                    } else {
                        showSnackbar("Failed to send reset email.");
                    }
                });
    }

    // A generic error dialog.
    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Authentication Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // A generic snackbar.
    private void showSnackbar(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
    }
}