package com.example.healthplanner.models;

public class Meal {
    private int id;
    private String name;
    private String quantity;
    private int calories;
    private MealType mealType;
    private String date;

    public Meal() {
    }

    public Meal(String name, String quantity, int calories, MealType mealType, String date) {
        this.name = name;
        this.quantity = quantity;
        this.calories = calories;
        this.mealType = mealType;
        this.date = date;
    }

    public Meal(int id, String name, String quantity, int calories, MealType mealType, String date) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.calories = calories;
        this.mealType = mealType;
        this.date = date;
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
