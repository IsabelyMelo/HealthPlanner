package com.example.healthplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.R;
import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Goal;
import com.example.healthplanner.models.Meal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<String> dayList; // List of strings in YYYY-MM-DD format
    private DatabaseHelper dbHelper;
    private Set<Integer> expandedPositions = new HashSet<>();
    private static final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HistoryAdapter(List<String> dayList, DatabaseHelper dbHelper) {
        this.dayList = dayList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_day, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String dayStr = dayList.get(position);
        LocalDate date = LocalDate.parse(dayStr);
        LocalDateTime dateTime = date.atStartOfDay();

        holder.tvHistoryDate.setText(date.format(displayDateFormatter));
        
        int consumed = dbHelper.getTotalCaloriesForDate(dateTime);
        holder.tvHistoryTotalCalories.setText(consumed + " kcal");

        boolean isExpanded = expandedPositions.contains(position);
        holder.layoutExpandable.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpandIcon.setRotation(isExpanded ? 180 : 0);

        if (isExpanded) {
            // Use end of day to find the goal that was active during this day
            Goal goal = dbHelper.getGoalForDate(date.atTime(23, 59, 59));
            List<Meal> meals = dbHelper.getMealsForDate(dateTime);

            if (goal != null) {
                holder.tvHistoryGoalName.setText("Meta: " + goal.getName());
                int goalVal = goal.getDailyCalorieGoal();
                holder.tvHistoryProgressText.setText(consumed + " / " + goalVal + " kcal");
                int progress = (int) (((double) consumed / goalVal) * 100);
                holder.pbHistoryCalories.setProgress(Math.min(progress, 100));
            } else {
                holder.tvHistoryGoalName.setText("Nenhuma meta ativa");
                holder.tvHistoryProgressText.setText(consumed + " kcal");
                holder.pbHistoryCalories.setProgress(0);
            }

            StringBuilder mealsText = new StringBuilder();
            for (Meal meal : meals) {
                mealsText.append("• ").append(meal.getName())
                        .append(" (").append(meal.getMealType().getLabel()).append("): ")
                        .append(meal.getCalorie()).append(" kcal\n");
            }
            if (mealsText.length() > 0) {
                holder.tvHistoryMealsList.setText(mealsText.toString().trim());
            } else {
                holder.tvHistoryMealsList.setText("Nenhuma refeição registrada.");
            }
        }

        holder.layoutHeader.setOnClickListener(v -> {
            if (isExpanded) {
                expandedPositions.remove(position);
            } else {
                expandedPositions.add(position);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryDate, tvHistoryTotalCalories, tvHistoryGoalName, tvHistoryProgressText, tvHistoryMealsList;
        ProgressBar pbHistoryCalories;
        ImageView ivExpandIcon;
        LinearLayout layoutExpandable;
        View layoutHeader;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryTotalCalories = itemView.findViewById(R.id.tvHistoryTotalCalories);
            tvHistoryGoalName = itemView.findViewById(R.id.tvHistoryGoalName);
            tvHistoryProgressText = itemView.findViewById(R.id.tvHistoryProgressText);
            tvHistoryMealsList = itemView.findViewById(R.id.tvHistoryMealsList);
            pbHistoryCalories = itemView.findViewById(R.id.pbHistoryCalories);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
        }
    }
}
