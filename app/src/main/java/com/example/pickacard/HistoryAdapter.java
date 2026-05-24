package com.example.pickacard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryModel> historyList;
    private String customId;

    public HistoryAdapter(Context context, List<HistoryModel> historyList, String customId) {
        this.context = context;
        this.historyList = historyList;
        this.customId = customId;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryModel model = historyList.get(position);
        holder.action.setText(model.getAction());
        holder.time.setText(model.getTime());

        // ✅ Set icon based on static keyword from Firebase (like "netbanking", "card", etc.)
        String iconKeyword = model.getIcon();
        if (iconKeyword != null) switch (iconKeyword.toLowerCase()) {

            case "add_card":
                holder.icon.setImageResource(R.drawable.card_icon);
                break;
            case "delete_card":
                holder.icon.setImageResource(R.drawable.baseline_delete_24);
                break;

            default:
            holder.icon.setImageResource(R.drawable.netbanking_icon); // If icon is null
        }

        holder.delete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(customId)
                    .child("history")
                    .child(model.getKey())
                    .removeValue();
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView action, time;
        ImageView icon, delete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            action = itemView.findViewById(R.id.history_action);
            time = itemView.findViewById(R.id.history_time);
            icon = itemView.findViewById(R.id.history_icon);
            delete = itemView.findViewById(R.id.history_delete_btn);
        }
    }
}
