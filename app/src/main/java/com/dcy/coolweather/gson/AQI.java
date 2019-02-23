package com.dcy.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {

    public AQICity aqiCity;

    public class AQICity{

        public String aqi;

        public String pm25;
    }
}
