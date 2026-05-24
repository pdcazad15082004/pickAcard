package com.example.pickacard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class History_Fragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryModel> historyList = new ArrayList<>();
    private List<HistoryModel> filteredList = new ArrayList<>();
    private String customId;
    private EditText searchBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_, container, false);

        recyclerView = view.findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar = view.findViewById(R.id.searchHistoryBar);
        setupSearchBar();

        loadHistory();

        return view;
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHistory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void loadHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        customId = prefs.getString("customId", null);
        if (customId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(customId).child("history");

        adapter = new HistoryAdapter(getContext(), filteredList, customId);
        recyclerView.setAdapter(adapter);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String key = snap.getKey();
                    String action = snap.child("action").getValue(String.class);
                    String time = snap.child("time").getValue(String.class);
                    String icon = snap.child("icon").getValue(String.class);

                    if (action != null && time != null && icon != null) {
                        historyList.add(new HistoryModel(action, time, icon, key));
                    }
                }

                // Sort newest first
                Collections.sort(historyList, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));

                filterHistory(searchBar.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle Firebase error if needed
            }
        });
    }

    private void filterHistory(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(historyList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (HistoryModel model : historyList) {
                if (model.getAction().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(model);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
