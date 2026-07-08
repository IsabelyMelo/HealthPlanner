package com.example.healthplanner.models;

public class UserSettings {
    private int id;
    private int dailyCalorieGoal;

    public UserSettings() {
    }

    public UserSettings(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public UserSettings(int id, int dailyCalorieGoal) {
        this.id = id;
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }
}
