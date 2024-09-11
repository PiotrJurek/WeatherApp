package com.example.weatherapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import com.example.weatherapp.R;
import com.example.weatherapp.database.WeatherRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForecastWeatherFragment extends Fragment {

    private static final String ARG_CITY_NAME = "city_name";
    private String cityName;
    private GridLayout gridLayout;
    private WeatherRepository weatherRepository;

    public static ForecastWeatherFragment newInstance(String cityName) {
        ForecastWeatherFragment fragment = new ForecastWeatherFragment();
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
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);

        gridLayout = view.findViewById(R.id.grid_layout);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cityName = prefs.getString("current_city", "Warsaw");

        loadWeatherData(cityName);

        return view;
    }

    private void loadWeatherData(String city) {
        gridLayout.removeAllViews();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String units = prefs.getString("units", "metric");
        String data = weatherRepository.getWeatherData(city + "_forecast_" + units);
        if (data != null) {
            try {
                JSONObject response = new JSONObject(data);
                JSONArray list = response.getJSONArray("list");

                String tempUnit = units.equals("imperial") ? "°F" : "°C";

                for (int i = 0; i < 5; i++) {
                    JSONObject dayForecast = list.getJSONObject(i * 8);
                    String date = dayForecast.getString("dt_txt").split(" ")[0];
                    double temp = dayForecast.getJSONObject("main").getDouble("temp");
                    String description = dayForecast.getJSONArray("weather").getJSONObject(0).getString("description");
                    String icon = dayForecast.getJSONArray("weather").getJSONObject(0).getString("icon");

                    addForecastRow(date, icon, description, temp, tempUnit);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addForecastRow(String date, String icon, String description, double temp, String tempUnit) {
        TextView dateTextView = new TextView(getContext());
        dateTextView.setText(date);
        dateTextView.setTextSize(16);
        dateTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        GridLayout.LayoutParams dateParams = new GridLayout.LayoutParams();
        dateParams.columnSpec = GridLayout.spec(0);
        dateParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        dateParams.setGravity(View.TEXT_ALIGNMENT_CENTER);
        gridLayout.addView(dateTextView, dateParams);

        LinearLayout weatherLayout = new LinearLayout(getContext());
        weatherLayout.setOrientation(LinearLayout.VERTICAL);
        weatherLayout.setGravity(Gravity.CENTER);

        ImageView weatherIcon = new ImageView(getContext());
        int iconResId = getResources().getIdentifier("icon_" + icon, "drawable", getContext().getPackageName());
        weatherIcon.setImageResource(iconResId);
        int iconHeight = (int) (64 * getResources().getDisplayMetrics().density);
        weatherIcon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, iconHeight));

        TextView descriptionTextView = new TextView(getContext());
        descriptionTextView.setText(description);
        descriptionTextView.setTextSize(16);
        descriptionTextView.setGravity(Gravity.CENTER);

        weatherLayout.addView(weatherIcon);
        weatherLayout.addView(descriptionTextView);

        GridLayout.LayoutParams weatherParams = new GridLayout.LayoutParams();
        weatherParams.columnSpec = GridLayout.spec(1);
        weatherParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        weatherParams.setGravity(View.TEXT_ALIGNMENT_CENTER);
        gridLayout.addView(weatherLayout, weatherParams);

        TextView tempTextView = new TextView(getContext());
        tempTextView.setText(temp + tempUnit);
        tempTextView.setTextSize(16);
        tempTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        GridLayout.LayoutParams tempParams = new GridLayout.LayoutParams();
        tempParams.columnSpec = GridLayout.spec(2);
        tempParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        tempParams.setGravity(View.TEXT_ALIGNMENT_CENTER);
        gridLayout.addView(tempTextView, tempParams);
    }

    public void updateWeatherData(String city){
        this.cityName = city;
        loadWeatherData(city);
    }
}
