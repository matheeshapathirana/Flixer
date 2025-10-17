package com.matheesha.flixer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

//REFERENCE: https://youtu.be/LXl7D57fgOQ?si=_pnYx58qgK4yUBUD

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull WatchlistFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WatchlistMoviesFragment();
            case 1:
                return new WatchlistTvFragment();
            default:
                return new WatchlistMoviesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
