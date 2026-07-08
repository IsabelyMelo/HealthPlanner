package com.example.healthplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.adapters.GoalAdapter;
import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Goal;

import java.time.LocalDateTime;
import java.util.List;

public class GoalActivity extends AppCompatActivity implements GoalAdapter.OnGoalActionListener {

    private DatabaseHelper dbHelper;
    private GoalAdapter adapter;
    private RecyclerView rvGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        Button btnAddGoal = findViewById(R.id.btnAddGoal);
        btnAddGoal.setOnClickListener(v -> showAddGoalDialog());

        rvGoals = findViewById(R.id.rvGoals);
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        
        loadGoals();
    }

    private void loadGoals() {
        List<Goal> goals = dbHelper.getAllGoals();
        if (adapter == null) {
            adapter = new GoalAdapter(goals, this);
            rvGoals.setAdapter(adapter);
        } else {
            adapter.updateList(goals);
        }
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_goal, null);
        builder.setView(dialogView);

        EditText etGoalName = dialogView.findViewById(R.id.etGoalName);
        EditText etCalorieGoal = dialogView.findViewById(R.id.etCalorieGoal);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etGoalName.getText().toString();
            String caloriesStr = etCalorieGoal.getText().toString();

            if (name.isEmpty() || caloriesStr.isEmpty()) {
                Toast.makeText(GoalActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            int calories = Integer.parseInt(caloriesStr);
            Goal newGoal = new Goal(name, calories, LocalDateTime.now(), null, true);
            
            dbHelper.insertGoal(newGoal);
            loadGoals();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onEndGoal(Goal goal) {
        dbHelper.endGoal(goal.getId(), LocalDateTime.now());
        loadGoals();
    }
}
