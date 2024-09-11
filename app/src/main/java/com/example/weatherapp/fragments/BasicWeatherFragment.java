package com.example.weatherapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weatherapp.R;
import com.example.weatherapp.database.WeatherRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BasicWeatherFragment extends Fragment {

    private static final String ARG_CITY_NAME = "city_name";
    private String cityName;
    private TextView coordinatesTextView, timeTextView, temperatureTextView, pressureTextView, weatherDescriptionTextView;
    private ImageView weatherIcon;
    private WeatherRepository weatherRepository;

    public static BasicWeatherFragment newInstance(String cityName) {
        BasicWeatherFragment fragment = new BasicWeatherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CITY_NAME, cityName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        weatherRepository = new WeatherRepository(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic_weather, container, false);

        coordinatesTextView = view.findViewById(R.id.coordinates);
        timeTextView = view.findViewById(R.id.time);
        temperatureTextView = view.findViewById(R.id.temperature);
        pressureTextView = view.findViewById(R.id.pressure);
        weatherDescriptionTextView = view.findViewById(R.id.weather_description);
        weatherIcon = view.findViewById(R.id.weather_icon);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cityName = prefs.getString("current_city", "Warsaw");

        loadWeatherData(cityName);

        return view;
    }

    private void loadWeatherData(String city) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String units = prefs.getString("units", "metric");
        String data = weatherRepository.getWeatherData(city + "_" + units);
        if (data != null) {
            try {
                JSONObject response = new JSONObject(data);
                JSONObject coord = response.getJSONObject("coord");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");
                JSONObject main = response.getJSONObject("main");
                double temp = main.getDouble("temp");
                int pressure = main.getInt("pressure");
                String description = response.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = response.getJSONArray("weather").getJSONObject(0).getString("icon");
                long lastUpdated = weatherRepository.getLastUpdated(city + "_" + units);
                String lastUpdatedTime = formatTime(lastUpdated);

                String tempUnit = units.equals("imperial") ? "°F" : "°C";

                coordinatesTextView.setText("Lat: " + lat + ", Lon: " + lon);
                timeTextView.setText("Last Updated: " + lastUpdatedTime);
                temperatureTextView.setText(temp + tempUnit);
                pressureTextView.setText(pressure + " hPa");
                weatherDescriptionTextView.setText(description);
                setWeatherIcon(icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private void setWeatherIcon(String icon) {
        int iconResId = getResources().getIdentifier("icon_" + icon, "drawable", getContext().getPackageName());
        weatherIcon.setImageResource(iconResId);
    }

    public void updateWeatherData(String city){
        this.cityName = city;
        loadWeatherData(city);
    }
}
