package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private TextView textCityName, textViewTemperature, textViewCondition;
    private TextInputEditText textInputEditText;
    private ImageView imageViewTemperature;
    private ImageView imageViewBack;
    private RecyclerView recyclerView;
    private ImageView imageViewSearch;
    private ArrayList<WeatherModel> weatherModels;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout = findViewById(R.id.homeLayout);
        progressBar = findViewById(R.id.progressBar);
        textCityName = findViewById(R.id.cityName);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewCondition = findViewById(R.id.textViewCondition);
        textInputEditText = findViewById(R.id.editText);
        imageViewSearch = findViewById(R.id.searchButton);
        imageViewTemperature = findViewById(R.id.showWeather);
        imageViewBack = findViewById(R.id.backImage);
        recyclerView = findViewById(R.id.recycler);
        weatherModels = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModels);
        recyclerView.setAdapter(weatherAdapter);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            cityName = getCityName(location.getLongitude(), location.getLatitude());
            getWeatherInfo(cityName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot access location", Toast.LENGTH_SHORT).show();
        }
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = textInputEditText.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter a city name first", Toast.LENGTH_SHORT).show();
                } else {
                    getWeatherInfo(city);
                }
            }
        });
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not Known";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
            for (Address adr : addressList) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Log.d("Tag", "City NOT FOUND");
                        Toast.makeText(this, "User city Not Found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=975e22b188ca4ff4a79102642232003&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        textCityName.setText(cityName);
        AndroidNetworking.initialize(MainActivity.this);
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RES", response.toString());
                        progressBar.setVisibility(View.GONE);
                        relativeLayout.setVisibility(View.VISIBLE);
                        weatherModels.clear();
                        try {
                            String temperature = response.getJSONObject("current").getString("temp_c");
                            textViewTemperature.setText(temperature + "°C");
                            int isDay = response.getJSONObject("current").getInt("is_day");
                            if (isDay == 1) {
                                Picasso.get().load("https://img.freepik.com/free-photo/white-cloud-blue-sky_74190-2381.jpg?w=996&t=st=1679496758~exp=1679497358~hmac=4497359f51f01776a4e0cc694a16cf7bbbe8cff7c91424bdf103854525840350").into(imageViewBack);
                            } else {
                                Picasso.get().load("https://ubuntuhandbook.org/wp-content/uploads/2021/07/M-Maggs-pixabay.jpg").into(imageViewBack);
                            }
                            String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                            textViewCondition.setText(condition);
                            String icon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                            Picasso.get().load("https:".concat(icon)).into(imageViewTemperature);

                            JSONObject jsonObject = response.getJSONObject("forecast");
                            JSONObject foreCastObj = jsonObject.getJSONArray("forecastday").getJSONObject(0);
                            JSONArray jsonArray = foreCastObj.getJSONArray("hour");
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject hourObj = jsonArray.getJSONObject(i);
                                String time = hourObj.getString("time");
                                String temp = hourObj.getString("temp_c");
                                String iconn = hourObj.getJSONObject("condition").getString("icon");
                                String windSpeed = hourObj.getString("wind_kph");
                                weatherModels.add(new WeatherModel(time, temp, iconn, windSpeed));
                            }
                            weatherAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Some Error", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("ERROR", anError.toString());
                        anError.printStackTrace();
                        Toast.makeText(MainActivity.this, "Enter a valid city name", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission has given by user", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission is necessary", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}