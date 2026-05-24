package com.example.pickacard.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pickacard.CardAdapter;
import com.example.pickacard.CardModel;
import com.example.pickacard.CardAdapter;
import com.example.pickacard.CardModel;
import com.example.pickacard.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Your_card extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private List<CardModel> cardList;
    private CardAdapter adapter;
    private String customId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_card, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        customId = prefs.getString("customId", null);

        recyclerView = view.findViewById(R.id.shopRecyclerView1);
        searchBar = view.findViewById(R.id.searchBar1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);         // Newest card at top
        layoutManager.setStackFromEnd(true);          // Ensures reverse works smoothly
        recyclerView.setLayoutManager(layoutManager);

        cardList = new ArrayList<>();
        adapter = new CardAdapter(cardList, getContext(), customId, false);
        // ✅ pass customId
        recyclerView.setAdapter(adapter);

        loadCardDataFromFirebase();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadCardDataFromFirebase() {
        if (customId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users").child(customId).child("cards");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CardModel card = snap.getValue(CardModel.class);
                    if (card != null) {
                        card.setCardId(snap.getKey()); // ✅ set card ID
                        cardList.add(card);
                    }
                }
                adapter.filter(""); // Reset filter after loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
