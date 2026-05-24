package com.example.pickacard.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pickacard.CardAdapter;
import com.example.pickacard.CardModel;
import com.example.pickacard.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Request_Card extends Fragment {

    RecyclerView recyclerView;
    CardAdapter adapter;
    ArrayList<CardModel> cardList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    DatabaseReference ref;
    String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request__card, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);

        recyclerView = view.findViewById(R.id.shopRecyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("UserData", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("customId", null);

        ref = FirebaseDatabase.getInstance().getReference("Users");

        adapter = new CardAdapter(cardList, getContext(), null, true);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRequestCards();
        });

        loadRequestCards(); // initial load

        return view;
    }

    private void loadRequestCards() {
        swipeRefreshLayout.setRefreshing(true);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cardList.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {

                    if (userSnap.getKey().equals(currentUserId)) continue;

                    for (DataSnapshot cardSnap : userSnap.child("cards").getChildren()) {

                        CardModel card = cardSnap.getValue(CardModel.class);
                        if (card != null) cardList.add(card);
                    }
                }

                adapter.filter(""); // IMPORTANT
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
