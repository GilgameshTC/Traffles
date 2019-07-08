package com.gilgameshtc.traffles;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences savedSettings;
    private boolean validForTransaction;

    // UI
    private Button budget;
    private FloatingActionButton addButton;
    private FloatingActionButton resetButton;
    private AlertDialog setBudgetAlert;
    private AlertDialog updateBudgetAlert;
    private AlertDialog invalidInputAlert;
    private AlertDialog confirmationAlert;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    // ----------===== Logic =====----------
    // Precondition: Should only be called after budget button and userPref have been initialized.
    private void loadBudgetUI() {
        int expenditureSoFar;
        // Retrieve the budget balance
        int balance = savedSettings.getInt(StorageManager.BUDGET_BALANCE_KEY, -1);
        if (balance < 0) {
            // User has not set up budget cap
            expenditureSoFar = 0;
            validForTransaction = false;
            budget.setText(String.valueOf(expenditureSoFar));
            return;
        }
        // Check whether the budget cap has expired
        String monthOfCap = savedSettings.getString(StorageManager.BUDGET_MONTH_KEY, null);
        int dateOfCap = savedSettings.getInt(StorageManager.BUDGET_DAY_KEY, -1);
        if (monthOfCap != null && monthOfCap.equalsIgnoreCase(getCurrentMonth())) {
            // budget cap is up to date
            expenditureSoFar = balance;
            if (dateOfCap > 0 && dateOfCap != getTodayDate()) {
                // budget needs to be updated
                int dailyCap = savedSettings.getInt(StorageManager.BUDGET_DAILY_CAP_KEY, 0);
                expenditureSoFar += dailyCap * (getTodayDate() - dateOfCap);
                updateBudgetBalance(expenditureSoFar);
            }
            validForTransaction = true;
        } else {
            // budget is out of date
            expenditureSoFar = 0;
            validForTransaction = false;
        }
        // Update UI with user's budget
        budget.setText(String.valueOf(expenditureSoFar));
    }

    private void updateBudgetUI() {
        // Retrieve the budget balance
        int balance = savedSettings.getInt(StorageManager.BUDGET_BALANCE_KEY, -1);
        budget.setText(String.valueOf(balance));
    }

    private String getCurrentMonth() {
        Date currentDate = new Date();
        return (String)DateFormat.format("MMM", currentDate); // e.g. Jun
    }

    private int getTodayDate() {
        Date currentDate = new Date();
        String day = (String)DateFormat.format("dd", currentDate);
        return Integer.parseInt(day); // e.g. 20
    }

    // Save user's new budget into user preference
    private void setBudget(int budgetValue) {
        SharedPreferences.Editor preferencesEditor = savedSettings.edit();
        // Save new budget
        preferencesEditor.putInt(StorageManager.BUDGET_DAILY_CAP_KEY, budgetValue);
        preferencesEditor.putInt(StorageManager.BUDGET_BALANCE_KEY, budgetValue);
        // Save current month
        preferencesEditor.putString(StorageManager.BUDGET_MONTH_KEY, getCurrentMonth());
        preferencesEditor.putInt(StorageManager.BUDGET_DAY_KEY, getTodayDate());
        preferencesEditor.commit();
        validForTransaction = true;
    }

    // Pre-condition: There is valid balance in database.
    // Returns new balance value
    private int incurTransaction(int transactionValue) {
        int balance = savedSettings.getInt(StorageManager.BUDGET_BALANCE_KEY, -1);
        return balance - transactionValue;
    }

    private void updateBudgetBalance(int newBalance) {
        SharedPreferences.Editor preferencesEditor = savedSettings.edit();
        preferencesEditor.putInt(StorageManager.BUDGET_BALANCE_KEY, newBalance);
        preferencesEditor.putInt(StorageManager.BUDGET_DAY_KEY, getTodayDate());
        preferencesEditor.commit();
    }

    private void resetBudget() {
        SharedPreferences.Editor preferencesEditor = savedSettings.edit();
        preferencesEditor.remove(StorageManager.BUDGET_DAILY_CAP_KEY);
        preferencesEditor.remove(StorageManager.BUDGET_BALANCE_KEY);
        preferencesEditor.remove(StorageManager.BUDGET_MONTH_KEY);
        preferencesEditor.remove(StorageManager.BUDGET_DAY_KEY);
        preferencesEditor.commit();
    }
    // ----------=================----------

    // ----------===== Button Listener =====----------
    // Listen to budget text button to save the daily cap onto storage.
    public View.OnClickListener budgetTextButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setBudgetAlert.show();
        }
    };

    // Listen to add button to update the budget balance.
    public View.OnClickListener addButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            updateBudgetAlert.show();
        }
    };

    // Listen to reset button to reset database.
    public View.OnClickListener resetButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            confirmationAlert.show();
        }
    };
    // ----------===========================----------
    // ----------===== UI =====----------
    private void setupSetBudgetAlert(AlertDialog.Builder setBudgetAlertBuilder) {
        setBudgetAlertBuilder.setTitle("Set Daily Cap");
        setBudgetAlertBuilder.setCancelable(true);
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.requestFocus();
        setBudgetAlertBuilder.setView(input);
        setBudgetAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Check that user only inputs positive integer
                if (value.matches("[0-9]+")) {
                    int budgetValue = Integer.parseInt(value);
                    setBudget(budgetValue);
                    // Update UI
                    validForTransaction = true;
                    updateBudgetUI();
                } else {
                    invalidInputAlert.setTitle("Invalid input");
                    invalidInputAlert.show();
                }
            }
        });

        setBudgetAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        });
        setBudgetAlert = setBudgetAlertBuilder.create();
        setBudgetAlert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setupUpdateBudgetAlert(AlertDialog.Builder updateBudgetAlertBuilder) {
        updateBudgetAlertBuilder.setTitle("Add new transaction");
        updateBudgetAlertBuilder.setCancelable(true);
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.requestFocus();
        updateBudgetAlertBuilder.setView(input);
        updateBudgetAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Check that user only inputs positive integer
                if (value.matches("[0-9]+") && validForTransaction) {
                    int transactionValue = Integer.parseInt(value);
                    int newBalance = incurTransaction(transactionValue);
                    updateBudgetBalance(newBalance);
                    // Update UI
                    updateBudgetUI();
                } else {
                    invalidInputAlert.setTitle("Invalid input / no budget is set!");
                    invalidInputAlert.show();
                }
            }
        });

        updateBudgetAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        });
        updateBudgetAlert = updateBudgetAlertBuilder.create();
        updateBudgetAlert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setupInvalidInputAlert(AlertDialog.Builder invalidInputAlertBuilder) {
        invalidInputAlertBuilder.setTitle("Invalid input");
        invalidInputAlertBuilder.setCancelable(true);
        invalidInputAlertBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        invalidInputAlert = invalidInputAlertBuilder.create();
    }

    private void setupConfirmationAlert(AlertDialog.Builder confirmationAlertBuilder) {
        confirmationAlertBuilder.setTitle("Reset cannot be undone.");
        confirmationAlertBuilder.setCancelable(true);
        confirmationAlertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetBudget();
                loadBudgetUI();
            }
        });

        confirmationAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Canceled.
                dialog.cancel();
            }
        });
        confirmationAlert = confirmationAlertBuilder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureUILayout();
        configureStorageManager();
        configureBudgetUI();
    }

    private void configureUILayout() {
        // Title Bar configuration
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        getSupportActionBar().hide(); // hide the title bar
        // Set up layout
        setContentView(R.layout.activity_main);
        configureButtonsUI();
        // Set up alerts
        AlertDialog.Builder setBudgetAlertBuilder = new AlertDialog.Builder(this);
        setupSetBudgetAlert(setBudgetAlertBuilder);
        AlertDialog.Builder updateBudgetAlertBuilder = new AlertDialog.Builder(this);
        setupUpdateBudgetAlert(updateBudgetAlertBuilder);
        AlertDialog.Builder invalidInputAlertBuilder = new AlertDialog.Builder(this);
        setupInvalidInputAlert(invalidInputAlertBuilder);
        AlertDialog.Builder confirmationAlertBuilder = new AlertDialog.Builder(this);
        setupConfirmationAlert(confirmationAlertBuilder);
        // Capture Bottom Navigation Bar (currently hidden)
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void configureStorageManager() {
        // Get user saved data
        savedSettings = getSharedPreferences("userPref", MODE_PRIVATE);
    }

    private void configureBudgetUI() {
        // Set up Budget Text button
        budget = findViewById(R.id.budgetText);
        loadBudgetUI();
        budget.setOnClickListener(budgetTextButtonListener);
    }

    private void configureButtonsUI() {
        // Set up Add button
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(addButtonListener);
        // Set up Reset button
        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(resetButtonListener);
    }
    // ----------==============----------
}
