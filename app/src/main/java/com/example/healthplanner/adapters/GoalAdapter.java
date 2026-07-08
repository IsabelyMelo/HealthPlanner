package com.example.healthplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.R;
import com.example.healthplanner.models.Goal;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goalList;
    private OnGoalActionListener listener;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public interface OnGoalActionListener {
        void onEndGoal(Goal goal);
    }

    public GoalAdapter(List<Goal> goalList, OnGoalActionListener listener) {
        this.goalList = goalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.tvGoalName.setText(goal.getName());
        holder.tvCalorieGoal.setText("Meta: " + goal.getDailyCalorieGoal() + " kcal");

        String start = goal.getBeginDate().format(dateFormatter);
        String end = goal.getEndDate() != null ? goal.getEndDate().format(dateFormatter) : "Em vigência";
        holder.tvDateRange.setText(start + " - " + end);

        if (goal.isCurrentGoal()) {
            holder.btnEndGoal.setVisibility(View.VISIBLE);
            holder.btnEndGoal.setOnClickListener(v -> listener.onEndGoal(goal));
        } else {
            holder.btnEndGoal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public void updateList(List<Goal> newList) {
        this.goalList = newList;
        notifyDataSetChanged();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvCalorieGoal, tvDateRange;
        Button btnEndGoal;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvCalorieGoal = itemView.findViewById(R.id.tvCalorieGoal);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            btnEndGoal = itemView.findViewById(R.id.btnEndGoal);
        }
    }
}
