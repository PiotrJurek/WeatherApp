package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.database.WeatherRepository;
import com.example.weatherapp.fragments.AdditionalWeatherFragment;
import com.example.weatherapp.fragments.BasicWeatherFragment;
import com.example.weatherapp.fragments.ForecastWeatherFragment;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String LAST_UPDATE_TIME = "last_update_time";
    private static final long UPDATE_INTERVAL = 4 * 60 * 60 * 1000;
    private static final int REQUEST_CODE_SEARCH = 1;

    private ViewPager viewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private MenuItem favoriteMenuItem;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String cityName;
    private Set<String> favoriteCities;
    private WeatherRepository weatherRepository;
    private RequestQueue requestQueue;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private Timer updateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteCities = new HashSet<>(prefs.getStringSet("favorite_cities", new HashSet<>()));

        weatherRepository = new WeatherRepository(this);
        requestQueue = Volley.newRequestQueue(this);

        cityName = prefs.getString("current_city", null);
        if (cityName == null) {
            if (!favoriteCities.isEmpty()) {
                cityName = favoriteCities.iterator().next();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("current_city", cityName);
                editor.apply();
            } else {
                cityName = "Warsaw";
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("current_city", cityName);
                editor.apply();
            }
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (cityName != null) {
            getSupportActionBar().setTitle(cityName);
        } else {
            getSupportActionBar().setTitle("WeatherApp");
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshWeatherData());

        if (!isTablet(this)) {
            viewPager = findViewById(R.id.viewPager);
            fragmentPagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), cityName);
            viewPager.setAdapter(fragmentPagerAdapter);
        } else {
            loadTabletFragments();
        }

        preferenceChangeListener = (sharedPreferences, key) -> {
            if (key.equals("units") || key.equals("current_city")) {
                refreshCurrentWeatherFragment();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        startUpdateTimer();
    }

    private void loadTabletFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_basic_container, BasicWeatherFragment.newInstance(cityName))
                .add(R.id.fragment_additional_container, AdditionalWeatherFragment.newInstance(cityName))
                .add(R.id.fragment_forecast_container, ForecastWeatherFragment.newInstance(cityName))
                .commit();
    }

    private boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK)
                >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdateTimer();
        refreshFavoriteCities();
        refreshCurrentWeatherFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdateTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        favoriteMenuItem = menu.findItem(R.id.action_add_favorite);
        updateFavoriteMenuItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add_favorite) {
            addCityToFavorites(cityName);
            return true;
        } else if (itemId == R.id.action_search) {
            startActivityForResult(new Intent(this, SearchActivity.class), REQUEST_CODE_SEARCH);
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopUpdateTimer();
        super.onDestroy();
    }

    private void refreshFavoriteCities() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteCities = new HashSet<>(prefs.getStringSet("favorite_cities", new HashSet<>()));
    }

    private void updateFavoriteMenuItem() {
        if (favoriteMenuItem != null && cityName != null) {
            if (favoriteCities.contains(cityName)) {
                favoriteMenuItem.setVisible(false);
            } else {
                favoriteMenuItem.setVisible(true);
            }
        }
    }

    private void addCityToFavorites(String city) {
        favoriteCities.add(city);
        saveFavoriteCities();
        updateFavoriteMenuItem();
        Toast.makeText(this, city + " added to favorites", Toast.LENGTH_SHORT).show();
    }

    private void saveFavoriteCities() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("favorite_cities", favoriteCities);
        editor.apply();
    }

    private void refreshWeatherData() {
        if (isInternetAvailable()) {
            updateAllWeatherData();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(LAST_UPDATE_TIME, System.currentTimeMillis());
            editor.apply();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAllWeatherData() {
        fetchWeatherData(cityName, "metric");
        fetchForecastData(cityName, "imperial");
        for (String city : favoriteCities) {
            fetchWeatherData(city, "metric");
            fetchWeatherData(city, "imperial");
            fetchForecastData(city, "metric");
            fetchForecastData(city, "imperial");
        }
    }

    private void fetchWeatherData(final String city, final String units) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.OPENWEATHER_API_KEY + "&units=" + units;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    weatherRepository.saveWeatherData(city + "_" + units, response.toString());
                    if (city.equals(cityName)) {
                        refreshCurrentWeatherFragment();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    error.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                });

        requestQueue.add(request);
    }

    private void fetchForecastData(final String city, final String units) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + BuildConfig.OPENWEATHER_API_KEY + "&units=" + units;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    weatherRepository.saveWeatherData(city + "_forecast_" + units, response.toString());
                    if (city.equals(cityName)) {
                        refreshCurrentWeatherFragment();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    error.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                });

        requestQueue.add(request);
    }

    private void refreshCurrentWeatherFragment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityName = prefs.getString("current_city", "Warsaw");
        getSupportActionBar().setTitle(cityName);

        if (isTablet(this)) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            BasicWeatherFragment basicFragment = (BasicWeatherFragment) fragmentManager.findFragmentById(R.id.fragment_basic_container);
            AdditionalWeatherFragment additionalFragment = (AdditionalWeatherFragment) fragmentManager.findFragmentById(R.id.fragment_additional_container);
            ForecastWeatherFragment forecastFragment = (ForecastWeatherFragment) fragmentManager.findFragmentById(R.id.fragment_forecast_container);

            if (basicFragment != null) {
                basicFragment.updateWeatherData(cityName);
            }
            if (additionalFragment != null) {
                additionalFragment.updateWeatherData(cityName);
            }
            if (forecastFragment != null) {
                forecastFragment.updateWeatherData(cityName);
            }
        } else {
            fragmentPagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), cityName);
            viewPager.setAdapter(fragmentPagerAdapter);
        }

        updateFavoriteMenuItem();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void startUpdateTimer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastUpdateTime = prefs.getLong(LAST_UPDATE_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long initialDelay = Math.max(0, UPDATE_INTERVAL - (currentTime - lastUpdateTime));

        updateTimer = new Timer();
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    refreshWeatherData();
                    startUpdateTimer();
                });
            }
        };
        updateTimer.schedule(updateTask, initialDelay);
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK) {
            refreshCurrentWeatherFragment();
        }
    }

    private class WeatherPagerAdapter extends FragmentPagerAdapter {

        private String cityName;

        public WeatherPagerAdapter(FragmentManager fm, String cityName) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.cityName = cityName;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BasicWeatherFragment.newInstance(cityName);
                case 1:
                    return AdditionalWeatherFragment.newInstance(cityName);
                case 2:
                    return ForecastWeatherFragment.newInstance(cityName);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
