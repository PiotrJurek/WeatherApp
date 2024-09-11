package com.example.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.database.WeatherRepository;
import com.example.weatherapp.favorites.FavoritesAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private EditText searchCity;

    private FavoritesAdapter favoritesAdapter;
    private ArrayList<String> favoriteCitiesList;
    private Set<String> favoriteCities;
    private WeatherRepository weatherRepository;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchCity = findViewById(R.id.search_city);
        Button searchButton = findViewById(R.id.search_button);
        RecyclerView favoritesList = findViewById(R.id.favorites_list);

        weatherRepository = new WeatherRepository(this);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteCities = new HashSet<>(prefs.getStringSet("favorite_cities", new HashSet<>()));
        favoriteCitiesList = new ArrayList<>(favoriteCities);

        favoritesAdapter = new FavoritesAdapter(favoriteCitiesList, new FavoritesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String city) {
                setCityAndFinish(city);
            }

            @Override
            public void onDeleteClick(String city) {
                favoriteCities.remove(city);
                favoriteCitiesList.remove(city);
                saveFavoriteCities();
                favoritesAdapter.notifyDataSetChanged();
            }
        });
        favoritesList.setLayoutManager(new LinearLayoutManager(this));
        favoritesList.setAdapter(favoritesAdapter);

        searchButton.setOnClickListener(v -> {
            String city = searchCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherData(city);
            } else {
                Toast.makeText(SearchActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherData(final String city) {
        String[] unitsArray = {"metric", "imperial"};
        for (final String units : unitsArray) {
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.OPENWEATHER_API_KEY + "&units=" + units;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        weatherRepository.saveWeatherData(city + "_" + units, response.toString());
                        if (units.equals("metric")) {
                            fetchForecastData(city);
                        }
                    }, error -> {
                        error.printStackTrace();
                        Toast.makeText(SearchActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    });

            requestQueue.add(request);
        }
    }

    private void fetchForecastData(final String city) {
        String[] unitsArray = {"metric", "imperial"};
        for (final String units : unitsArray) {
            String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + BuildConfig.OPENWEATHER_API_KEY + "&units=" + units;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        weatherRepository.saveWeatherData(city + "_forecast_" + units, response.toString());
                        if (units.equals("metric")) {
                            setCityAndFinish(city);
                        }
                    }, error -> {
                        error.printStackTrace();
                        Toast.makeText(SearchActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    });

            requestQueue.add(request);
        }
    }

    private void setCityAndFinish(String city) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_city", city);
        editor.apply();
        setResult(RESULT_OK);
        finish();
    }

    private void saveFavoriteCities() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("favorite_cities", favoriteCities);
        editor.apply();
    }
}
