package com.matheesha.flixer.adapter;

import com.matheesha.flixer.model.SliderItems;
import java.util.ArrayList;
import java.util.List;

public class SliderAdapter {
    // The list will now hold SliderItems objects
    private List<SliderItems> sliderItems = new ArrayList<>();

    // You can add a constructor or a method to update the list
    public SliderAdapter(List<SliderItems> sliderItems) {
        this.sliderItems = sliderItems;
    }

    // A method to get the item count
    public int getItemCount() {
        return sliderItems.size();
    }
}
