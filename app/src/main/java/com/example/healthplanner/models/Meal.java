package com.example.healthplanner.models;

import java.time.LocalDateTime;

public class Meal {
    private int id;
    private String name;
    private int calorie;
    private LocalDateTime mealtime;
    private MealType mealType;

    public Meal() {
    }

    public Meal(String name, int calorie, LocalDateTime mealtime, MealType mealType) {
        this.name = name;
        this.calorie = calorie;
        this.mealtime = mealtime;
        this.mealType = mealType;
    }

    public Meal(int id, String name, int calorie, LocalDateTime mealtime, MealType mealType) {
        this.id = id;
        this.name = name;
        this.calorie = calorie;
        this.mealtime = mealtime;
        this.mealType = mealType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public LocalDateTime getMealtime() {
        return mealtime;
    }

    public void setMealtime(LocalDateTime mealtime) {
        this.mealtime = mealtime;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
}
