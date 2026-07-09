package com.example.healthplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.R;
import com.example.healthplanner.models.Meal;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;
    private OnMealActionListener listener;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public interface OnMealActionListener {
        void onDeleteMeal(Meal meal);
    }

    public MealAdapter(List<Meal> mealList, OnMealActionListener listener) {
        this.mealList = mealList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.tvMealName.setText(meal.getName());
        String details = meal.getMealType().getLabel() + " - " + meal.getCalorie() + " kcal";
        holder.tvMealDetails.setText(details);

        String dateStr = "Consumido em: " + meal.getMealtime().format(dateFormatter);
        holder.tvMealDate.setText(dateStr);

        holder.btnDeleteMeal.setOnClickListener(v -> listener.onDeleteMeal(meal));
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void updateList(List<Meal> newList) {
        this.mealList = newList;
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName, tvMealDetails, tvMealDate;
        ImageButton btnDeleteMeal;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDetails = itemView.findViewById(R.id.tvMealDetails);
            tvMealDate = itemView.findViewById(R.id.tvMealDate);
            btnDeleteMeal = itemView.findViewById(R.id.btnDeleteMeal);
        }
    }
}
