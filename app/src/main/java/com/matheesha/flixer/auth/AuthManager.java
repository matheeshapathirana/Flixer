package com.matheesha.flixer.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// A singleton class to manage the Firebase authentication state.
// This makes it easy to access the current user from anywhere in the app.
public class AuthManager {
    private static AuthManager instance;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public boolean isEmailVerified() {
        return currentUser != null && currentUser.isEmailVerified();
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public void signOut() {
        mAuth.signOut();
        currentUser = null;
    }

    public void addAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        mAuth.addAuthStateListener(listener);
    }

    public void removeAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        mAuth.removeAuthStateListener(listener);
    }
}