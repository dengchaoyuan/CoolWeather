package com.dcy.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dcy.coolweather.db.City;
import com.dcy.coolweather.db.Country;
import com.dcy.coolweather.db.Province;
import com.dcy.coolweather.util.HttpUtil;
import com.dcy.coolweather.util.Utilty;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseFragment extends Fragment {

    private View view;
    private TextView titleText;
    private Button back;
    private ListView areaListView;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chooose_area,container,false);
        init();
        return view;
    }

    private void init() {
        titleText = view.findViewById(R.id.area_tv);
        back = view.findViewById(R.id.back_btn);
        areaListView = view.findViewById(R.id.area_list);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        areaListView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(i);
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(i);
                        queryCounty();
                        break;
                    case LEVEL_COUNTY:
                        String weatherId = countryList.get(i).getWeatherId();
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentLevel){
                    case LEVEL_COUNTY:
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        queryProvince();
                        break;
                }
            }
        });
        queryProvince();
    }

    private void queryCounty() {
        titleText.setText(selectedCity.getCityName());
        back.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size() > 0){
            dataList.clear();
            for (Country country : countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }

    }

    private void queryCity() {
        titleText.setText(selectedProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryProvince() {
        titleText.setText("中国");
        back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utilty.handleProvinceResponse(responseString);
                }else if("city".equals(type)){
                    result = Utilty.handleCityResponse(responseString,selectedProvince.getId());
                }else{
                    result = Utilty.handleCountryResponse(responseString,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvince();
                            }else if("city".equals(type)){
                                queryCity();
                            }else if("country".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
