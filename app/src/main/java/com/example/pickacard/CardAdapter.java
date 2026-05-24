package com.example.pickacard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<CardModel> cardList, filteredList;
    private Context context;
    private String customId;
    private boolean isRequestMode;


    public CardAdapter(List<CardModel> cardList, Context context, String customId, boolean isRequestMode) {
        this.cardList = cardList;
        this.filteredList = new ArrayList<>(cardList);
        this.context = context;
        this.customId = customId;
        this.isRequestMode = isRequestMode;
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cards, parent, false);
        return new CardViewHolder(view);



    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        CardModel card = filteredList.get(position);

        if (isRequestMode) {

            // ===== REQUEST CARD MODE =====

            holder.cardNumber.setText(maskCardNumber(card.number));
            holder.cardValidity.setText(formatRequestValidity(card.validity));
            holder.cardHolder.setText(maskHolder(card.holder));

            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null); // IMPORTANT

            holder.cardIndex.setVisibility(View.VISIBLE);
            holder.cardIndex.setText(String.valueOf(position + 1));

            if (card.limit != null && !card.limit.isEmpty()) {
                holder.cardLimit.setVisibility(View.VISIBLE);
                holder.cardLimit.setText("₹ " + card.limit);
            } else {
                holder.cardLimit.setVisibility(View.GONE);
            }

        } else {

            // ===== YOUR CARD MODE =====

            holder.cardNumber.setText(card.number);
            holder.cardValidity.setText(card.validity);
            holder.cardHolder.setText(card.holder);

            holder.cardIndex.setVisibility(View.GONE);
            holder.cardLimit.setVisibility(View.GONE);

            holder.deleteButton.setVisibility(View.VISIBLE);

            // ✅ DELETE LISTENER EXISTS ONLY HERE
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Card")
                        .setMessage("Are you sure you want to delete this card?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            String cardId = card.getCardId();
                            if (cardId == null) return;

                            DatabaseReference ref = FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(customId)
                                    .child("cards")
                                    .child(cardId);

                            ref.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        logCardDeletedToHistory(card);
                                        filteredList.remove(card);
                                        cardList.remove(card);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Card successfully deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Failed to delete card", Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        // ===== Card type =====
        holder.cardType.setText(card.type);

        // ===== Bank logo =====
        if (card.logo != null) {
            switch (card.logo) {
                case "sbilogo": holder.bankLogo.setImageResource(R.drawable.sbi); break;
                case "kotaklogo": holder.bankLogo.setImageResource(R.drawable.kotak); break;
                case "icicilogo": holder.bankLogo.setImageResource(R.drawable.icici); break;
                case "axislogo": holder.bankLogo.setImageResource(R.drawable.axis); break;
                case "indusindlogo": holder.bankLogo.setImageResource(R.drawable.indusind); break;
                case "rbllogo": holder.bankLogo.setImageResource(R.drawable.rbl); break;
                case "hdfclogo": holder.bankLogo.setImageResource(R.drawable.hdfc); break;
                case "canaralogo": holder.bankLogo.setImageResource(R.drawable.canara); break;
                default: holder.bankLogo.setImageResource(R.drawable.wwwwmp); break;
            }
        } else {
            holder.bankLogo.setImageResource(R.drawable.wwwwmp);
        }

        // ===== Network logo =====
        if (card.network != null) {
            switch (card.network.toLowerCase()) {
                case "visa": holder.networkLogo.setImageResource(R.drawable.visa); break;
                case "mastercard": holder.networkLogo.setImageResource(R.drawable.mastercard); break;
                case "rupay": holder.networkLogo.setImageResource(R.drawable.rupay); break;
                case "discover": holder.networkLogo.setImageResource(R.drawable.discover); break;
                default: holder.networkLogo.setImageResource(R.drawable.default123); break;
            }
        } else {
            holder.networkLogo.setImageResource(R.drawable.default123);
        }
    }



    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(cardList);
        } else {
            for (CardModel card : cardList) {
                if (card.holder.toLowerCase().contains(query.toLowerCase()) ||
                        card.number.contains(query) ||
                        card.bank.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(card);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumber, cardValidity, cardHolder, cardType, cardLimit,cardIndex;
        ImageView bankLogo, networkLogo;
        ImageButton deleteButton;



        CardViewHolder(@NonNull View itemView) {
            super(itemView);


            cardNumber = itemView.findViewById(R.id.card_number);
            cardValidity = itemView.findViewById(R.id.expiry_date);
            cardHolder = itemView.findViewById(R.id.card_owner);
            cardType = itemView.findViewById(R.id.cardtype);
            bankLogo = itemView.findViewById(R.id.bank_logo);
            networkLogo = itemView.findViewById(R.id.bank_type_logo);
            deleteButton = itemView.findViewById(R.id.card_delete);
            cardIndex = itemView.findViewById(R.id.card_index);
            cardLimit = itemView.findViewById(R.id.card_limit);



        }
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


    // ✅ This function logs "Card Removed" to Firebase History
    private void logCardDeletedToHistory(CardModel cardModel) {
        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String customId = prefs.getString("customId", null);
        if (customId == null) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(customId)
                .child("history");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String historyId = "history" + (count + 1);

                String actionText = "Card removed - " + cardModel.type + " (" + capitalizeFirstLetter(cardModel.bank) + ")";
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                String iconTag = "delete_card";

                HashMap<String, Object> historyData = new HashMap<>();
                historyData.put("action", actionText);
                historyData.put("time", time);
                historyData.put("icon", iconTag);

                historyRef.child(historyId).setValue(historyData)
                        .addOnSuccessListener(aVoid -> Log.d("CardAdapter", "Card removal logged"))
                        .addOnFailureListener(e -> Log.e("CardAdapter", "Failed to log removal", e));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CardAdapter", "Database error logging removal", error.toException());
            }
        });
    }
    private String maskCardNumber(String number) {
        if (number == null || number.length() < 4)
            return "XXXX XXXX XXXX XXXX";
        return "XXXX XXXX XXXX " + number.substring(number.length() - 4);
    }

    private String maskValidity() {
        return "MM/YY";
    }

    private String maskHolder(String holder) {
        if (holder == null || holder.length() < 4)
            return "****";
        return holder.substring(0, 4).toUpperCase() + "****";
    }

    private String formatRequestValidity(String validity) {

        if (validity == null || !validity.contains("/"))
            return "XX/YY";

        String[] parts = validity.split("/");
        if (parts.length != 2)
            return "XX/YY";

        String year = parts[1];
        return "XX/" + year.substring(year.length() - 2);
    }





}
