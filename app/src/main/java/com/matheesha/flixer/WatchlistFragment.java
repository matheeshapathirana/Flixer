package com.matheesha.flixer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class WatchlistFragment extends Fragment {

    private TabLayout watchlist_tabLayout;
    private ViewPager2 watchlist_viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TextView entryCount;
    private Spinner statusFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    //REFERENCE: ChatGPT
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        watchlist_tabLayout = view.findViewById(R.id.watchlist_tabLayout);
        watchlist_viewPager = view.findViewById(R.id.watchlist_viewPager);

        entryCount = view.findViewById(R.id.entryCount);
        statusFilter = view.findViewById(R.id.filterSpinner);

        viewPagerAdapter = new ViewPagerAdapter(this);
        watchlist_viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(watchlist_tabLayout, watchlist_viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Movies");

                        //REFERENCE: ChatGPT - get the movie fragment and set the listener for entry count
                        viewPagerAdapter.getMoviesFragment().setOnItemCountChangeListener(count -> {
                            if (watchlist_viewPager.getCurrentItem() == 0) {
                                entryCount.setText("Entries: " + count);
                            }
                        });
                    } else {
                        tab.setText("TV Series");

//                        viewPagerAdapter.getSeriesFragment().setOnItemCountChangeListener(count -> {
//                           if (watchlist_viewPager.getCurrentItem() == 1) {
//                               entryCount.setText("Entries: " + count);
//                        });
                    }
                }).attach();

        //REFERENCE: ChatGPT - update the entryCount when the user switches tabs
        watchlist_viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == 0) {
                    entryCount.setText("Entries: " + viewPagerAdapter.getMoviesFragment().getItemCount());
                } else {
                    entryCount.setText("Entries: 0");
                    //entryCount.setText("Entries: " + viewPagerAdapter.getSeriesFragment().getItemCount());
                }
            }
        });

        //REFERENCE: ChatGPT
        //Handling statusSpinner filtering
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.watchlist_filters,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(filterAdapter);

        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedStatus = adapterView.getItemAtPosition(i).toString();

                int currentTab = watchlist_viewPager.getCurrentItem();
                if (currentTab == 0) {
                    viewPagerAdapter.getMoviesFragment().filterByStatus(selectedStatus);
                } else {
                    //viewPagerAdapter.getSeriesFragment().filterByStatus(selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}