package com.matheesha.flixer.utils;

import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

// A helper class for creating some common animations.
public class AnimationHelper {

    // Scales a view in or out.
    public static void scaleView(View view, float scale, long duration) {
        ViewCompat.animate(view)
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    // Fades a view in.
    public static void fadeInView(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    // Shakes a view to get the user's attention.
    public static void shakeView(View view) {
        ViewCompat.animate(view)
                .translationX(-20f)
                .setDuration(50)
                .withEndAction(() -> ViewCompat.animate(view)
                        .translationX(20f)
                        .setDuration(50)
                        .withEndAction(() -> ViewCompat.animate(view)
                                .translationX(0f)
                                .setDuration(50)
                                .start())
                        .start())
                .start();
    }

    // A simple button press animation.
    public static ViewPropertyAnimatorCompat createButtonPressAnimation(View view) {
        return ViewCompat.animate(view)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> ViewCompat.animate(view)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start());
    }
}