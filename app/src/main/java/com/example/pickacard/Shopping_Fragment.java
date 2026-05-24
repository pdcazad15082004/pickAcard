package com.example.pickacard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class Shopping_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private ShopAdapter adapter;
    private EditText searchBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shopping_, container, false);

        recyclerView = root.findViewById(R.id.shopRecyclerView);
        searchBar = root.findViewById(R.id.searchBar);

        List<ShopItem> shopList = Arrays.asList(
                new ShopItem("Amazon", R.drawable.amazon, "https://www.amazon.in"),
                new ShopItem("Flipkart", R.drawable.flipkart, "https://www.flipkart.com"),
                new ShopItem("Myntra", R.drawable.myntra, "https://www.myntra.com"),
                new ShopItem("Croma", R.drawable.croma, "https://www.croma.com"),
                new ShopItem("TataCliq", R.drawable.tatacliq, "https://www.tatacliq.com"),
                new ShopItem("Nykaa Man", R.drawable.nykaaman, "https://www.nykaaman.com"),
                new ShopItem("Nykaa", R.drawable.nykaa, "https://www.nykaa.com"),
                new ShopItem("Ajio", R.drawable.ajio, "https://www.ajio.com"),
                new ShopItem("Snapdeal", R.drawable.snapdeal, "https://www.snapdeal.com"),
                new ShopItem("H&M", R.drawable.hm, "https://www2.hm.com"),
                new ShopItem("OnePlus", R.drawable.oneplus, "https://www.oneplus.in"),
                new ShopItem("Realme", R.drawable.realme, "https://www.realme.com/in"),
                new ShopItem("Samsung", R.drawable.samsung, "https://www.samsung.com/in"),
                new ShopItem("Vivo", R.drawable.vivo, "https://www.vivo.com/in")
        );

        adapter = new ShopAdapter(shopList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return root;
    }

}
