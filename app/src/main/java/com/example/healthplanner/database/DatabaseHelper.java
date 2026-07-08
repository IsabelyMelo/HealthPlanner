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
    private static final int DATABASE_VERSION = 2; // Incremented version

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
    private static final String COLUMN_CURRENT_GOAL = "current_goal";

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
                + COLUMN_END_DATE + " TEXT,"
                + COLUMN_CURRENT_GOAL + " INTEGER"
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
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_GOALS + " ADD COLUMN " + COLUMN_NAME_GOAL + " TEXT");
        }
    }

    // --- Goal Operations ---

    public void insertGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();

        // If this is the current goal, handle the replacement logic
        if (goal.isCurrentGoal()) {
            Goal current = getCurrentGoal();
            if (current != null) {
                // Check if the current goal was started today
                if (current.getBeginDate().toLocalDate().equals(goal.getBeginDate().toLocalDate())) {
                    // Delete it if it's from the same day
                    db.delete(TABLE_GOALS, COLUMN_ID + " = ?", new String[]{String.valueOf(current.getId())});
                } else {
                    // Otherwise, just end it
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_CURRENT_GOAL, 0);
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
        values.put(COLUMN_CURRENT_GOAL, goal.isCurrentGoal() ? 1 : 0);

        db.insert(TABLE_GOALS, null, values);
    }

    public Goal getCurrentGoal() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, null, COLUMN_CURRENT_GOAL + " = 1", null, null, null, null);

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
            // If created and ended on the same day, delete it
            if (goal.getBeginDate().toLocalDate().equals(endDate.toLocalDate())) {
                db.delete(TABLE_GOALS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            } else {
                // Otherwise, mark as ended
                ContentValues values = new ContentValues();
                values.put(COLUMN_END_DATE, endDate.format(formatter));
                values.put(COLUMN_CURRENT_GOAL, 0);
                db.update(TABLE_GOALS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            }
        }
    }

    public Goal getGoalForDate(LocalDateTime date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateString = date.format(formatter);

        // Find goal where date is between begin and end, OR end is null and it's the current goal
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
        goal.setCurrentGoal(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_GOAL)) == 1);
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
