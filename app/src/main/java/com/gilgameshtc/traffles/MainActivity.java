package com.gilgameshtc.traffles;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
    private StorageManager database;

    // UI
    private Button budget;
    private CircleProgressBar progressBar;
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
        float expenditureSoFar = 0;
        // Retrieve the budget balance
        float balance = database.getBalance();
        if (balance < 0) {
            // User has not set up budget cap
            database.makeInvalidForTransaction();
            budget.setText(String.valueOf(expenditureSoFar));
            return;
        }
        // Check whether the budget cap has expired
        String monthOfCap = database.getBudgetMonth();
        int dateOfCap = database.getDayOfCap();
        if (monthOfCap != null && monthOfCap.equalsIgnoreCase(getCurrentMonth())) {
            // budget cap is up to date
            expenditureSoFar = balance;
            if (dateOfCap > 0 && dateOfCap != getTodayDate()) {
                // budget needs to be updated
                float dailyCap = database.getDailyCap();
                expenditureSoFar += dailyCap * (getTodayDate() - dateOfCap);
                database.updateBalance(expenditureSoFar, getTodayDate());
            }
            database.makeValidForTransaction();
        } else {
            // budget is out of date
            database.makeInvalidForTransaction();
        }
        // Update UI with user's budget
        budget.setText(String.valueOf(expenditureSoFar));
    }

    private void updateBudgetUI() {
        // Retrieve the budget balance
        float balance = database.getBalance();
        budget.setText(String.valueOf(balance));
    }

    private void updateProgressBar() {
        float balance = database.getBalance();
        float dailyCap = database.getDailyCap();
        // Invert progress
        float progress = (-(balance / dailyCap) * 100) + 100;
        if (progress < 40) {
            // Budget balance is healthy
            progressBar.setColor(Color.GREEN);
            if (progress < 0) {
                progress = 0;
            }
        } else if (progress >= 40 && progress <= 80) {
            // Budget balance is diminishing
            progressBar.setColor(Color.YELLOW);
        } else {
            // Budget balance is critical
            progressBar.setColor(Color.RED);
        }
        progressBar.setProgress(progress);
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
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.requestFocus();
        setBudgetAlertBuilder.setView(input);
        setBudgetAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Check that user only inputs positive integer
                if (value.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                    int budgetValue = Integer.parseInt(value);
                    database.setNewBudget(budgetValue, getCurrentMonth(), getTodayDate());
                    // Update UI
                    database.makeValidForTransaction();
                    updateBudgetUI();
                    updateProgressBar();
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
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.requestFocus();
        updateBudgetAlertBuilder.setView(input);
        updateBudgetAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Check that user only inputs positive integer
                if (value.matches("[0-9]+(\\.[0-9]{1,2})?") &&
                        database.isValidForTransaction()) {
                    float transactionValue = Float.parseFloat(value);
                    float newBalance = database.incurTransaction(transactionValue);
                    database.setBalance(newBalance);
                    // Update UI
                    updateBudgetUI();
                    updateProgressBar();
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
                database.resetBudget();
                loadBudgetUI();
                updateProgressBar();
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
        database = new StorageManager(getSharedPreferences("userPref", MODE_PRIVATE));
    }

    private void configureBudgetUI() {
        // Set up Budget Text button
        budget = findViewById(R.id.budgetText);
        loadBudgetUI();
        budget.setOnClickListener(budgetTextButtonListener);
        // Set up Circular Progress Bar
        progressBar = findViewById(R.id.custom_progressBar);
        updateProgressBar();
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
