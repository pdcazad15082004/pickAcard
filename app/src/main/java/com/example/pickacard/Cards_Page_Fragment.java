package com.example.pickacard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class Cards_Page_Fragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyviewCardAdapter myviewCardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cards__page_, container, false);

        tabLayout = view.findViewById(R.id.cardtab);
        viewPager2 = view.findViewById(R.id.viewpage);
        myviewCardAdapter = new MyviewCardAdapter(this);
        viewPager2.setAdapter(myviewCardAdapter);

        // Tab selected listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional
            }
        });

        // Sync tabs with ViewPager2
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        return view;
    }
}
