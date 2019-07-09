package com.gilgameshtc.traffles;

public interface StorageManagerInterface {
    boolean isValidForTransaction();
    void makeValidForTransaction();
    void makeInvalidForTransaction();
    void setNewBudget(float newBudget, String month, int day);
    boolean setDailyCap(float dailyCap);
    float getDailyCap();
    boolean setBudgetMonth(String month);
    String getBudgetMonth();
    boolean setDayOfCap(int day);
    int getDayOfCap();
    float incurTransaction(float transactionValue);
    boolean setBalance(float balance);
    float getBalance();
    boolean updateBalance(float newBalance, int day);
    boolean resetBudget();
}
