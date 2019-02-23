package com.dcy.coolweather.util;

import android.text.TextUtils;

import com.dcy.coolweather.db.City;
import com.dcy.coolweather.db.Country;
import com.dcy.coolweather.db.Province;
import com.dcy.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class Utilty {

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinceObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response,int provinceId){
        try {
            JSONArray cityJSONArray = new JSONArray(response);
            for (int i = 0; i < cityJSONArray.length(); i++) {
                JSONObject cityJSONObject = cityJSONArray.getJSONObject(i);
                City city = new City();
                city.setCityCode(cityJSONObject.getInt("id"));
                city.setCityName(cityJSONObject.getString("name"));
                city.setProvinceId(provinceId);
                city.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean handleCountryResponse(String response, int cityId) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Country country = new Country();
                country.setCountryName(jsonObject.getString("name"));
                country.setWeatherId(jsonObject.getString("weather_id"));
                country.setCityId(cityId);
                country.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
