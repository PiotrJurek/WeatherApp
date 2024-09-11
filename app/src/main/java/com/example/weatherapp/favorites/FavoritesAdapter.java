package com.example.weatherapp.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;

import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private ArrayList<String> favoriteCities;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String city);
        void onDeleteClick(String city);
    }

    public FavoritesAdapter(ArrayList<String> favoriteCities, OnItemClickListener listener) {
        this.favoriteCities = favoriteCities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder holder, int position) {
        String city = favoriteCities.get(position);
        holder.bind(city, listener);
    }

    @Override
    public int getItemCount() {
        return favoriteCities.size();
    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        private TextView cityName;
        private Button deleteButton;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(final String city, final OnItemClickListener listener) {
            cityName.setText(city);
            itemView.setOnClickListener(v -> listener.onItemClick(city));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(city));
        }
    }
}
