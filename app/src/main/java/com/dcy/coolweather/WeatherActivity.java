package com.dcy.coolweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.mbms.MbmsErrors;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.dcy.coolweather.gson.Forecast;
import com.dcy.coolweather.gson.Weather;
import com.dcy.coolweather.util.HttpUtil;
import com.dcy.coolweather.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.dcy.coolweather.R.id.date_text;

public class WeatherActivity extends Activity {

    private ScrollView weatherLayout;
    private TextView cityName;
    private TextView updateTime;
    private TextView degree;
    private TextView weatherInfo;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWash;
    private TextView sportText;
    private SharedPreferences preferences;
    private String weatherString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        init();
    }

    private void init() {
        weatherLayout = findViewById(R.id.weather_layout);
        cityName = findViewById(R.id.title_city);
        updateTime = findViewById(R.id.title_update_time);
        degree = findViewById(R.id.degree_text);
        weatherInfo = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWash = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherString = preferences.getString("weather",null);
        if(weatherString != null){
            Weather weather = Utilty.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=710de9a898434dddad7eadf1a7bd1707";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utilty.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String city = weather.basic.city;
        String update = weather.basic.update.updateTime.split(" ")[1];
        String degreeText = weather.now.tmp + "℃";
        String weatherInfoText = weather.now.nowCond.txt;
        cityName.setText(city);
        updateTime.setText(update);
        degree.setText(degreeText);
        weatherInfo.setText(weatherInfoText);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecasts) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);

        }
//        if(weather.aqi != null){
//            aqiText.setText(weather.aqi.aqiCity.aqi);
//            pm25Text.setText(weather.aqi.aqiCity.pm25);
//        }
        String comfort = "舒适度："+weather.suggestion.comfort.txt;
        String carWashText = "洗车指数："+weather.suggestion.carWash.cw;
        String sport = "运动指数："+weather.suggestion.sport.txt;
        comfortText.setText(comfort);
        carWash.setText(carWashText);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
