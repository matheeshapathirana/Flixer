package com.matheesha.flixer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        loadUserProfile(currentUser);
        setupLogoutButton();
    }

    private void loadUserProfile(FirebaseUser user) {
        TextView userName = findViewById(R.id.user_name);
        TextView userEmail = findViewById(R.id.user_email);
        CircleImageView profileImage = findViewById(R.id.profile_image);

        userEmail.setText(user.getEmail());

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail().split("@")[0];
        }
        userName.setText(displayName);

        if (user.getPhotoUrl() != null) {
            Picasso.get().load(user.getPhotoUrl()).into(profileImage);
        }
    }

    private void setupLogoutButton() {
        Button logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
}
