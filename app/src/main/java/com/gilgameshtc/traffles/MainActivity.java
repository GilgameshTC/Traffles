package com.gilgameshtc.traffles;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button budget;
    private SharedPreferences savedSettings;

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

    // Precondition: Should only be called after budget button and userPref have been initialized.
    private void setupBudget() {
        String expenditureSoFar = savedSettings.getString("budget", "0");
        budget.setText(expenditureSoFar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Title Bar configuration
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title
        getSupportActionBar().hide(); // hide the title bar
        // Set up lay out
        setContentView(R.layout.activity_main);
        // Get user saved data
        savedSettings = getSharedPreferences("userPref", MODE_PRIVATE);
        // Capture Budget Text button
        budget = findViewById(R.id.budgetText);
        setupBudget();
        // Capture Bottom Navigation Bar (currently hidden)
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
