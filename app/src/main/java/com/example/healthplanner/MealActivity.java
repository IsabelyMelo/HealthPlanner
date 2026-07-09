package com.example.healthplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.adapters.MealAdapter;
import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Meal;
import com.example.healthplanner.models.MealType;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.List;

public class MealActivity extends AppCompatActivity implements MealAdapter.OnMealActionListener {

    private DatabaseHelper dbHelper;
    private MealAdapter adapter;
    private RecyclerView rvMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMeals = findViewById(R.id.rvMeals);
        rvMeals.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton btnAddMeal = findViewById(R.id.btnAddMeal);
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());

        loadMeals();
    }

    private void loadMeals() {
        List<Meal> meals = dbHelper.getMealsForDate(LocalDateTime.now());
        if (adapter == null) {
            adapter = new MealAdapter(meals, this);
            rvMeals.setAdapter(adapter);
        } else {
            adapter.updateList(meals);
        }
    }

    private void showAddMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_meal, null);
        builder.setView(dialogView);

        EditText etMealName = dialogView.findViewById(R.id.etMealName);
        EditText etMealCalories = dialogView.findViewById(R.id.etMealCalories);
        Spinner spMealType = dialogView.findViewById(R.id.spMealType);
        Button btnSaveMeal = dialogView.findViewById(R.id.btnSaveMeal);

        // Setup Spinner
        String[] mealLabels = new String[MealType.values().length];
        for (int i = 0; i < MealType.values().length; i++) {
            mealLabels[i] = MealType.values()[i].getLabel();
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealLabels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMealType.setAdapter(spinnerAdapter);

        AlertDialog dialog = builder.create();

        btnSaveMeal.setOnClickListener(v -> {
            String name = etMealName.getText().toString();
            String caloriesStr = etMealCalories.getText().toString();

            if (name.isEmpty() || caloriesStr.isEmpty()) {
                Toast.makeText(MealActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            int calories = Integer.parseInt(caloriesStr);
            MealType selectedType = MealType.values()[spMealType.getSelectedItemPosition()];

            Meal newMeal = new Meal(name, calories, LocalDateTime.now(), selectedType);
            dbHelper.insertMeal(newMeal);

            loadMeals();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDeleteMeal(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Refeição")
                .setMessage("Deseja realmente excluir esta refeição?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    dbHelper.deleteMeal(meal.getId());
                    loadMeals();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}
