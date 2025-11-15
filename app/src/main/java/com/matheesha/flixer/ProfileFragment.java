package com.matheesha.flixer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // If somehow we ended up here unauthenticated, go to AuthActivity
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AuthActivity.class));
                getActivity().finish();
            }
            return;
        }

        CircleImageView profileImage = view.findViewById(R.id.profile_image);
        TextView userName = view.findViewById(R.id.user_name);
        TextView userEmail = view.findViewById(R.id.user_email);
        MaterialButton logoutBtn = view.findViewById(R.id.btn_logout);

        userEmail.setText(user.getEmail());
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";
        }
        userName.setText(displayName);

        if (user.getPhotoUrl() != null) {
            Picasso.get().load(user.getPhotoUrl()).into(profileImage);
        }

        logoutBtn.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btnLogout);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            mAuth.signOut();
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AuthActivity.class));
                getActivity().finish();
            }
        });

        dialog.show();
    }
}