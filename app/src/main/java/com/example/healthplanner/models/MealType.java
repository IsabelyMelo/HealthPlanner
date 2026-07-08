package com.example.healthplanner.models;

public enum MealType {
    MAINMEAL("mainmeal", "Refeição Principal"),
    SNACK("snack", "Lanche"),
    SUPPLEMENT("supplement", "Suplemento");

    private final String value;
    private final String label;

    MealType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static MealType fromValue(String value) {
        if (value == null) {
            return SNACK;
        }

        for (MealType mealType : values()) {
            if (mealType.value.equalsIgnoreCase(value)) {
                return mealType;
            }
        }

        return SNACK;
    }
}