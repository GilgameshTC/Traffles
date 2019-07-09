package com.gilgameshtc.traffles;

import android.content.SharedPreferences;

public class StorageManager implements StorageManagerInterface {
    public static String BUDGET_DAILY_CAP_KEY = "DAILY_CAP";
    public static String BUDGET_MONTH_KEY = "MONTH_OF_CAP";
    public static String BUDGET_DAY_KEY = "DAY_OF_CAP";
    public static String BUDGET_BALANCE_KEY = "BUDGET_BALANCE";

    private SharedPreferences database;
    private boolean isValidForTransaction;
    /*
    // StorageManager applies the singleton principal as
    // there should only be one reference to the database.
    public static StorageManager storageManager = new StorageManager();
    */

    public StorageManager(SharedPreferences savedSettings) {
        database = savedSettings;
        isValidForTransaction = false;
    }

    @Override
    public boolean isValidForTransaction() {
        return isValidForTransaction;
    }

    @Override
    public void makeValidForTransaction() {
        isValidForTransaction = true;
    }

    @Override
    public void makeInvalidForTransaction() {
        isValidForTransaction = false;
    }

    @Override
    public void setNewBudget(float newBudget, String month, int day) {
        // Save new budget
        setDailyCap(newBudget);
        setBalance(newBudget);
        // Save current date
        setBudgetMonth(month);
        setDayOfCap(day);
        makeValidForTransaction();
    }

    @Override
    public boolean setDailyCap(float dailyCap) {
        SharedPreferences.Editor preferencesEditor = database.edit();
        preferencesEditor.putFloat(BUDGET_DAILY_CAP_KEY, dailyCap);
        return preferencesEditor.commit();
    }

    @Override
    public float getDailyCap() {
        return database.getFloat(BUDGET_DAILY_CAP_KEY, -1);
    }

    @Override
    public boolean setBudgetMonth(String month) {
        SharedPreferences.Editor preferencesEditor = database.edit();
        preferencesEditor.putString(BUDGET_MONTH_KEY, month);
        return preferencesEditor.commit();
    }

    @Override
    public String getBudgetMonth() {
        return database.getString(BUDGET_MONTH_KEY, null);
    }

    @Override
    public boolean setDayOfCap(int day) {
        SharedPreferences.Editor preferencesEditor = database.edit();
        preferencesEditor.putInt(BUDGET_DAY_KEY, day);
        return preferencesEditor.commit();
    }

    // Returns: 0 if data isn't available
    @Override
    public int getDayOfCap() {
        return database.getInt(BUDGET_DAY_KEY, 0);
    }

    @Override
    public float incurTransaction(float transactionValue) {
        float balance = database.getFloat(BUDGET_BALANCE_KEY, 0);
        return balance - transactionValue;
    }

    @Override
    public boolean setBalance(float balance) {
        SharedPreferences.Editor preferencesEditor = database.edit();
        preferencesEditor.putFloat(BUDGET_BALANCE_KEY, balance);
        return preferencesEditor.commit();
    }

    @Override
    public float getBalance() {
        return Math.round(database.getFloat(BUDGET_BALANCE_KEY, -1) * 100.0) / (float)100.0;
    }

    @Override
    public boolean updateBalance(float newBalance, int day) {
        return setBalance(newBalance) && setDayOfCap(day);
    }

    @Override
    public boolean resetBudget() {
        SharedPreferences.Editor preferencesEditor = database.edit();
        preferencesEditor.remove(BUDGET_DAILY_CAP_KEY);
        preferencesEditor.remove(BUDGET_BALANCE_KEY);
        preferencesEditor.remove(BUDGET_MONTH_KEY);
        preferencesEditor.remove(BUDGET_DAY_KEY);
        return preferencesEditor.commit();
    }
}
