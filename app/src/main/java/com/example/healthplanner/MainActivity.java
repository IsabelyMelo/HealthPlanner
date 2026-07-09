package com.example.healthplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Goal;

import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvActiveGoalName, tvConsumed, tvGoalValue, tvRemaining;
    private ProgressBar pbCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tvActiveGoalName = findViewById(R.id.tvActiveGoalName);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvGoalValue = findViewById(R.id.tvGoalValue);
        tvRemaining = findViewById(R.id.tvRemaining);
        pbCalories = findViewById(R.id.pbCalories);

        LinearLayout btnMeal = findViewById(R.id.btnMeal);
        LinearLayout btnGoal = findViewById(R.id.btnGoal);
        LinearLayout btnHistory = findViewById(R.id.btnHistory);

        btnMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MealActivity.class);
                startActivity(intent);
            }
        });

        btnGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GoalActivity.class);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        Goal currentGoal = dbHelper.getCurrentGoal();
        int consumed = dbHelper.getTotalCaloriesForDate(LocalDateTime.now());

        tvConsumed.setText(consumed + " kcal");

        if (currentGoal != null) {
            tvActiveGoalName.setText(currentGoal.getName());
            int goalValue = currentGoal.getDailyCalorieGoal();
            tvGoalValue.setText(goalValue + " kcal");

            int progress = (int) (((double) consumed / goalValue) * 100);
            pbCalories.setProgress(Math.min(progress, 100));

            int remaining = goalValue - consumed;
            if (remaining > 0) {
                tvRemaining.setText("Faltam " + remaining + " kcal");
                tvRemaining.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            } else {
                tvRemaining.setText("Meta atingida!");
                tvRemaining.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
            }
        } else {
            tvActiveGoalName.setText("Nenhuma meta ativa");
            tvGoalValue.setText("---");
            pbCalories.setProgress(0);
            tvRemaining.setText("Crie uma meta para começar");
            tvRemaining.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        }
    }
}

