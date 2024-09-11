package com.example.weatherapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weatherapp.R;
import com.example.weatherapp.database.WeatherRepository;

import org.json.JSONException;
import org.json.JSONObject;

public class AdditionalWeatherFragment extends Fragment {

    private static final String ARG_CITY_NAME = "city_name";
    private String cityName;
    private TextView windSpeedTextView, windDirectionTextView, humidityTextView, visibilityTextView;
    private WeatherRepository weatherRepository;

    public static AdditionalWeatherFragment newInstance(String cityName) {
        AdditionalWeatherFragment fragment = new AdditionalWeatherFragment();
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
        View view = inflater.inflate(R.layout.fragment_additional_weather, container, false);

        windSpeedTextView = view.findViewById(R.id.wind_speed);
        windDirectionTextView = view.findViewById(R.id.wind_direction);
        humidityTextView = view.findViewById(R.id.humidity);
        visibilityTextView = view.findViewById(R.id.visibility);

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
                JSONObject wind = response.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");
                int windDeg = wind.getInt("deg");
                JSONObject main = response.getJSONObject("main");
                int humidity = main.getInt("humidity");
                int visibility = response.getInt("visibility");

                String speedUnit = units.equals("imperial") ? "mph" : "m/s";
                String visibilityUnit = units.equals("imperial") ? "mi" : "m";

                windSpeedTextView.setText(windSpeed + " " + speedUnit);
                windDirectionTextView.setText(windDeg + "°");
                humidityTextView.setText(humidity + "%");
                visibilityTextView.setText(visibility + " " + visibilityUnit);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateWeatherData(String city){
        this.cityName = city;
        loadWeatherData(city);
    }
}
