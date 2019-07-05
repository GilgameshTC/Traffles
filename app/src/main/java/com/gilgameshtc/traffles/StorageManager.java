package com.gilgameshtc.traffles;

import android.content.SharedPreferences;

public class StorageManager implements StorageManagerInterface {
    public static String BUDGET_DAILY_CAP_KEY = "DAILY_CAP";
    public static String BUDGET_MONTH_KEY = "MONTH_OF_CAP";
    public static String BUDGET_DAY_KEY = "DAY_OF_CAP";
    public static String BUDGET_BALANCE_KEY = "BUDGET_BALANCE";

    // StorageManager applies the singleton principal as
    // there should only be one reference to the database.
    public static StorageManager storageManager = new StorageManager();

    private StorageManager() {
        // To be filled when storage migrates to firebase.
    }

    @Override
    public boolean setBudget(int budgetValue) {
        return false;
    }

    @Override
    public int getBudgetValue() {
        return 0;
    }

    @Override
    public String getBudgetMonth() {
        return null;
    }
}
