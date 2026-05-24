package com.example.pickacard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class BankSpinnerAdapter extends ArrayAdapter<BankItem> {

    public BankSpinnerAdapter(@NonNull Context context, List<BankItem> banks) {
        super(context, 0, banks);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_items, parent, false);

        BankItem item = getItem(position);

        ImageView logo = convertView.findViewById(R.id.bankLogo);
        TextView name = convertView.findViewById(R.id.bankName);

        if (item != null) {
            logo.setImageResource(item.getBankLogo());   // ✅ use getter
            name.setText(item.getBankName());            // ✅ use getter
        }

        return convertView;
    }
}
