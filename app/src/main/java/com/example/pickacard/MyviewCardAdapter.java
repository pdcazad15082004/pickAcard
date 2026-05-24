package com.example.pickacard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.pickacard.Fragment.Request_Card;
import com.example.pickacard.Fragment.Your_card;

public class MyviewCardAdapter extends FragmentStateAdapter {

    public MyviewCardAdapter(@NonNull Fragment fragment) {
        super(fragment); // <-- Correct usage for Fragment
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Your_card();
            case 1:
                return new Request_Card();
            default:
                return new Your_card(); // fallback
        }
    }

    @Override
    public int getItemCount() {
        return 2; // You have 2 tabs
    }
}
