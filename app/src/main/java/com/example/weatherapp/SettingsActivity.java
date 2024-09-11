package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        Spinner unitsSpinner = findViewById(R.id.units_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitsSpinner.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String units = prefs.getString("units", "metric");
        int spinnerPosition = adapter.getPosition(units);
        unitsSpinner.setSelection(spinnerPosition);

        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedUnits = parentView.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("units", selectedUnits);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
