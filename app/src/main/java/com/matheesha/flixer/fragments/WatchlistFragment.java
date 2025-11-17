package com.matheesha.flixer.fragments;

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
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.matheesha.flixer.R;
import com.matheesha.flixer.adapters.ViewPagerAdapter;


public class WatchlistFragment extends Fragment {

    private TabLayout watchlist_tabLayout;
    private ViewPager2 watchlist_viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TextView entryCount;
    private TextInputLayout statusLayout;
    private MaterialAutoCompleteTextView statusDropdown;

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

        statusLayout = view.findViewById(R.id.watchlist_fragment_status_layout);
        statusDropdown = view.findViewById(R.id.watchlist_fragment_status_dropdown);

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

                        viewPagerAdapter.getSeriesFragment().setOnItemCountChangeListener(count -> {
                           if (watchlist_viewPager.getCurrentItem() == 1) {
                               entryCount.setText("Entries: " + count);
                           }
                        });
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
                    entryCount.setText("Entries: " + viewPagerAdapter.getSeriesFragment().getItemCount());
                }
            }
        });

        //REFERENCE: ChatGPT
        //Handling statusSpinner filtering - Updated for MaterialAutoCompleteTextView
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.watchlist_filters,
                android.R.layout.simple_spinner_item
        );
        statusDropdown.setAdapter(filterAdapter);

        //Set initial text to first item in the array
        if (filterAdapter.getCount() > 0) {
            statusDropdown.setText(filterAdapter.getItem(0).toString(), false);
        }

        statusDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedStatus = adapterView.getItemAtPosition(position).toString();

                int currentTab = watchlist_viewPager.getCurrentItem();
                if (currentTab == 0) {
                    viewPagerAdapter.getMoviesFragment().filterByStatus(selectedStatus);
                } else {
                    viewPagerAdapter.getSeriesFragment().filterByStatus(selectedStatus);
                }
            }
        });
    }
}