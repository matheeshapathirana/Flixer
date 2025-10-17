package com.matheesha.flixer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class WatchlistFragment extends Fragment {

    private TabLayout watchlist_tabLayout;
    private ViewPager2 watchlist_viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        watchlist_tabLayout = view.findViewById(R.id.watchlist_tabLayout);
        watchlist_viewPager = view.findViewById(R.id.watchlist_viewPager);

        viewPagerAdapter = new ViewPagerAdapter(this);
        watchlist_viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(watchlist_tabLayout, watchlist_viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Movies");
                    } else {
                        tab.setText("TV Series");
                    }
                }).attach();
    }
}