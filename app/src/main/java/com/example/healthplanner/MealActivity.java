package com.example.healthplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.adapters.MealAdapter;
import com.example.healthplanner.ai.CalorieEstimatorClient;
import com.example.healthplanner.database.DatabaseHelper;
import com.example.healthplanner.models.Meal;
import com.example.healthplanner.models.MealType;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealActivity extends AppCompatActivity implements MealAdapter.OnMealActionListener {

    private DatabaseHelper dbHelper;
    private MealAdapter adapter;
    private RecyclerView rvMeals;
    private CalorieEstimatorClient calorieEstimatorClient;
    private final ExecutorService calorieEstimatorExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal);

        dbHelper = new DatabaseHelper(this);
        calorieEstimatorClient = new CalorieEstimatorClient();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        calorieEstimatorExecutor.shutdownNow();
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
        CheckBox cbEstimateCalories = dialogView.findViewById(R.id.cbEstimateCalories);
        LinearLayout layoutCalorieEstimate = dialogView.findViewById(R.id.layoutCalorieEstimate);
        EditText etMealDescription = dialogView.findViewById(R.id.etMealDescription);
        Button btnEstimateCalories = dialogView.findViewById(R.id.btnEstimateCalories);
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

        cbEstimateCalories.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutCalorieEstimate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnEstimateCalories.setOnClickListener(v -> {
            String description = etMealDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(MealActivity.this, "Descreva a refeição para estimar as calorias", Toast.LENGTH_SHORT).show();
                return;
            }

            if (BuildConfig.AI_CALORIE_API_KEY.trim().isEmpty()) {
                Toast.makeText(MealActivity.this, "Configure AI_CALORIE_API_KEY no arquivo .env", Toast.LENGTH_LONG).show();
                return;
            }

            btnEstimateCalories.setEnabled(false);
            btnEstimateCalories.setText("Estimando...");

            calorieEstimatorExecutor.execute(() -> {
                try {
                    CalorieEstimatorClient.EstimateResult result =
                            calorieEstimatorClient.estimateCalories(description);

                    runOnUiThread(() -> {
                        if (!dialog.isShowing()) {
                            return;
                        }

                        btnEstimateCalories.setEnabled(true);
                        btnEstimateCalories.setText("Estimar");

                        if (result.hasCalories()) {
                            etMealCalories.setText(String.valueOf(result.getCalories()));
                            Toast.makeText(MealActivity.this, "Calorias estimadas preenchidas", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MealActivity.this, "Informe alimentos e porções com mais detalhes", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        if (!dialog.isShowing()) {
                            return;
                        }

                        btnEstimateCalories.setEnabled(true);
                        btnEstimateCalories.setText("Estimar");
                        Toast.makeText(MealActivity.this, getCalorieEstimateErrorMessage(e), Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

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

    private String getCalorieEstimateErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "Não foi possível estimar as calorias agora";
        }

        return message;
    }
}
