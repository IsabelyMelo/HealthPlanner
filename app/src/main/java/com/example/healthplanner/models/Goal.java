package com.example.healthplanner.models;

import java.time.LocalDateTime;

public class Goal {
    private int id;
    private String name;
    private int dailyCalorieGoal;
    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private boolean currentGoal;

    public Goal() {
    }

    public Goal(String name, int dailyCalorieGoal, LocalDateTime beginDate, LocalDateTime endDate, boolean currentGoal) {
        this.name = name;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.currentGoal = currentGoal;
    }

    public Goal(int id, String name, int dailyCalorieGoal, LocalDateTime beginDate, LocalDateTime endDate, boolean currentGoal) {
        this.id = id;
        this.name = name;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.currentGoal = currentGoal;
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

    public int getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }


    public void setDailyCalorieGoal(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public LocalDateTime getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDateTime beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(boolean currentGoal) {
        this.currentGoal = currentGoal;
    }
}
