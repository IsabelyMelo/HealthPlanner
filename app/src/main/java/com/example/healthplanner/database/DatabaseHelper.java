package com.example.healthplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.healthplanner.models.Goal;
import com.example.healthplanner.models.Meal;
import com.example.healthplanner.models.MealType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_planner.db";
    private static final int DATABASE_VERSION = 3; // Incremented version to 3

    // Table names
    private static final String TABLE_GOALS = "goals";
    private static final String TABLE_MEALS = "meals";

    // Common column names
    private static final String COLUMN_ID = "id";

    // Goals table columns
    private static final String COLUMN_NAME_GOAL = "name";
    private static final String COLUMN_DAILY_CALORIE_GOAL = "daily_calorie_goal";
    private static final String COLUMN_BEGIN_DATE = "begin_date";
    private static final String COLUMN_END_DATE = "end_date";

    // Meals table columns
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CALORIE = "calorie";
    private static final String COLUMN_MEALTIME = "mealtime";
    private static final String COLUMN_MEAL_TYPE = "meal_type";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_GOAL + " TEXT,"
                + COLUMN_DAILY_CALORIE_GOAL + " INTEGER,"
                + COLUMN_BEGIN_DATE + " TEXT,"
                + COLUMN_END_DATE + " TEXT"
                + ")";

        String CREATE_MEALS_TABLE = "CREATE TABLE " + TABLE_MEALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_CALORIE + " INTEGER,"
                + COLUMN_MEALTIME + " TEXT,"
                + COLUMN_MEAL_TYPE + " TEXT"
                + ")";

        db.execSQL(CREATE_GOALS_TABLE);
        db.execSQL(CREATE_MEALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Drop and recreate goals table to remove current_goal column and reset
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
            
            // Re-create ONLY the goals table
            String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME_GOAL + " TEXT,"
                    + COLUMN_DAILY_CALORIE_GOAL + " INTEGER,"
                    + COLUMN_BEGIN_DATE + " TEXT,"
                    + COLUMN_END_DATE + " TEXT"
                    + ")";
            db.execSQL(CREATE_GOALS_TABLE);
        }
    }

    // --- Goal Operations ---

    public void insertGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();

        // If this is an active goal (no end date), terminate the previous active goal
        if (goal.getEndDate() == null) {
            Goal current = getCurrentGoal();
            if (current != null) {
                // If the current goal was started today, delete it (same day replacement)
                if (current.getBeginDate().toLocalDate().equals(goal.getBeginDate().toLocalDate())) {
                    db.delete(TABLE_GOALS, COLUMN_ID + " = ?", new String[]{String.valueOf(current.getId())});
                } else {
                    // Otherwise, set end date to now (or begin date of the new goal)
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_END_DATE, goal.getBeginDate().format(formatter));
                    db.update(TABLE_GOALS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(current.getId())});
                }
            }
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_GOAL, goal.getName());
        values.put(COLUMN_DAILY_CALORIE_GOAL, goal.getDailyCalorieGoal());
        values.put(COLUMN_BEGIN_DATE, goal.getBeginDate().format(formatter));
        values.put(COLUMN_END_DATE, goal.getEndDate() != null ? goal.getEndDate().format(formatter) : null);

        db.insert(TABLE_GOALS, null, values);
    }

    public Goal getCurrentGoal() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Active goal is the one with no end date
        Cursor cursor = db.query(TABLE_GOALS, null, COLUMN_END_DATE + " IS NULL", null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Goal goal = cursorToGoal(cursor);
            cursor.close();
            return goal;
        }
        return null;
    }

    public Goal getGoalById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Goal goal = cursorToGoal(cursor);
            cursor.close();
            return goal;
        }
        return null;
    }

    public List<Goal> getAllGoals() {
        List<Goal> goals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, null, null, null, null, null, COLUMN_BEGIN_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                goals.add(cursorToGoal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return goals;
    }

    public void endGoal(int id, LocalDateTime endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        Goal goal = getGoalById(id);
        
        if (goal != null) {
            // If created and ended on the same day, delete it to avoid clutter
            if (goal.getBeginDate().toLocalDate().equals(endDate.toLocalDate())) {
                db.delete(TABLE_GOALS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            } else {
                // Otherwise, mark as ended by setting the end date
                ContentValues values = new ContentValues();
                values.put(COLUMN_END_DATE, endDate.format(formatter));
                db.update(TABLE_GOALS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            }
        }
    }

    public Goal getGoalForDate(LocalDateTime date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateString = date.format(formatter);

        // Find goal where date is between begin and end, OR end is null
        String selection = "(" + COLUMN_BEGIN_DATE + " <= ? AND (" + COLUMN_END_DATE + " >= ? OR " + COLUMN_END_DATE + " IS NULL))";
        String[] selectionArgs = {dateString, dateString};

        Cursor cursor = db.query(TABLE_GOALS, null, selection, selectionArgs, null, null, COLUMN_BEGIN_DATE + " DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            Goal goal = cursorToGoal(cursor);
            cursor.close();
            return goal;
        }
        return null;
    }

    private Goal cursorToGoal(Cursor cursor) {
        Goal goal = new Goal();
        goal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        goal.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_GOAL)));
        goal.setDailyCalorieGoal(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_CALORIE_GOAL)));
        goal.setBeginDate(LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BEGIN_DATE)), formatter));
        String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE));
        if (endDateStr != null) {
            goal.setEndDate(LocalDateTime.parse(endDateStr, formatter));
        }
        // Current goal is true if end date is null
        goal.setCurrentGoal(goal.getEndDate() == null);
        return goal;
    }

    // --- Meal Operations ---

    public void insertMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_CALORIE, meal.getCalorie());
        values.put(COLUMN_MEALTIME, meal.getMealtime().format(formatter));
        values.put(COLUMN_MEAL_TYPE, meal.getMealType().getValue());

        db.insert(TABLE_MEALS, null, values);
    }

    public List<Meal> getMealsForDate(LocalDateTime date) {
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format to search for the specific day (start and end of day)
        String startOfDay = date.toLocalDate().atStartOfDay().format(formatter);
        String endOfDay = date.toLocalDate().atTime(23, 59, 59).format(formatter);

        String selection = COLUMN_MEALTIME + " BETWEEN ? AND ?";
        String[] selectionArgs = {startOfDay, endOfDay};

        Cursor cursor = db.query(TABLE_MEALS, null, selection, selectionArgs, null, null, COLUMN_MEALTIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                meals.add(cursorToMeal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return meals;
    }

    public int getTotalCaloriesForDate(LocalDateTime date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startOfDay = date.toLocalDate().atStartOfDay().format(formatter);
        String endOfDay = date.toLocalDate().atTime(23, 59, 59).format(formatter);

        String query = "SELECT SUM(" + COLUMN_CALORIE + ") FROM " + TABLE_MEALS 
                     + " WHERE " + COLUMN_MEALTIME + " BETWEEN ? AND ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{startOfDay, endOfDay});
        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        }
        return total;
    }

    public List<String> getDistinctDaysWithMeals() {
        List<String> days = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Extract YYYY-MM-DD from the ISO datetime string
        String query = "SELECT DISTINCT substr(" + COLUMN_MEALTIME + ", 1, 10) as day FROM " + TABLE_MEALS 
                     + " ORDER BY day DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                days.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return days;
    }

    public void deleteMeal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEALS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Meal getLastMeal() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEALS, null, null, null, null, null, COLUMN_MEALTIME + " DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            Meal meal = cursorToMeal(cursor);
            cursor.close();
            return meal;
        }
        return null;
    }

    public int clearAllMeals() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MEALS, null, null);
    }

    private Meal cursorToMeal(Cursor cursor) {
        Meal meal = new Meal();
        meal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        meal.setCalorie(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIE)));
        meal.setMealtime(LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEALTIME)), formatter));
        meal.setMealType(MealType.fromValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TYPE))));
        return meal;
    }
}
