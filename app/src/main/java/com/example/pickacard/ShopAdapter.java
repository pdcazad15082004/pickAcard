package com.example.pickacard;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<ShopItem> fullList;
    private List<ShopItem> filteredList;

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView title;

        public ShopViewHolder(View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.shopLogo);
            title = itemView.findViewById(R.id.shopTitle);
        }
    }

    public ShopAdapter(List<ShopItem> shopList) {
        this.fullList = new ArrayList<>(shopList);
        this.filteredList = new ArrayList<>(shopList);
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        ShopItem item = filteredList.get(position);
        holder.logo.setImageResource(item.getLogoResId());
        holder.title.setText(item.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        for (ShopItem item : fullList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        notifyDataSetChanged();
    }
}
