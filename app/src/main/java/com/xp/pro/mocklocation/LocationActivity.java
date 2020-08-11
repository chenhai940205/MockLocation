package com.xp.pro.mocklocation;

import android.app.Activity;
import android.os.Bundle;

import com.xp.pro.mocklocationlib.LocationBean;

public class LocationActivity extends Activity {
    LocationWidget idLocationWidget;
    LocationBean mLocationBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_content_view);
        initView();
        initMockLocationData();
    }

    private void initMockLocationData() {
        double latitude;
        double longitude;
        try {
            latitude = getIntent().getDoubleExtra("latitude", 22.568431);
            longitude = getIntent().getDoubleExtra("longitude", 113.960533);

        } catch (Exception e) {
            latitude = 0;
            longitude = 0;
        }
        mLocationBean = new LocationBean(latitude,longitude);
        idLocationWidget.setLocation(mLocationBean);
    }

    private void initView() {
        idLocationWidget = (LocationWidget) findViewById(R.id.id_location_wigdet);
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        idLocationWidget.refreshData();
    }

    @Override
    protected void onPause() {
        idLocationWidget.removeUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        idLocationWidget.stopMock();
        super.onDestroy();
    }
}