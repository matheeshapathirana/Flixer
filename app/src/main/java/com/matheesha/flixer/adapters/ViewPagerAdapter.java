package com.matheesha.flixer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.matheesha.flixer.fragments.WatchlistFragment;
import com.matheesha.flixer.fragments.WatchlistMoviesFragment;
import com.matheesha.flixer.fragments.WatchlistSeriesFragment;

//REFERENCE: https://youtu.be/LXl7D57fgOQ?si=_pnYx58qgK4yUBUD

public class ViewPagerAdapter extends FragmentStateAdapter {

    //REFERENCE: ChatGPT
    //To avoid creating new instances every time, we can store references
    private Fragment moviesFragment;
    private Fragment seriesFragment;

    public ViewPagerAdapter(@NonNull WatchlistFragment fragmentActivity) {
        super(fragmentActivity);

        //REFERENCE: ChatGPT
        //Initialize the fragments once
        moviesFragment = new WatchlistMoviesFragment();
        seriesFragment = new WatchlistSeriesFragment();
    }

    //REFERENCE: https://youtu.be/LXl7D57fgOQ?si=_pnYx58qgK4yUBUD
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return moviesFragment;
            case 1:
                return seriesFragment;
            default:
                return moviesFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    //REFERENCE: ChatGPT
    public WatchlistMoviesFragment getMoviesFragment() {
        return (WatchlistMoviesFragment) moviesFragment;
    }

    public WatchlistSeriesFragment getSeriesFragment() {
        return (WatchlistSeriesFragment) seriesFragment;
    }
}
