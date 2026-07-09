package com.example.healthplanner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthplanner.adapters.HistoryAdapter;
import com.example.healthplanner.database.DatabaseHelper;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        List<String> days = dbHelper.getDistinctDaysWithMeals();
        adapter = new HistoryAdapter(days, dbHelper);
        rvHistory.setAdapter(adapter);
    }
}
