package com.xp.pro.mocklocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xp.pro.mocklocationlib.LocationBean;
import com.xp.pro.mocklocationlib.MockLocationManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LocationWigdet:模拟位置信息提示控件
 * Author: xp
 * Date: 18/7/12 22:22
 * Email: xiexiepro@gmail.com
 * Blog: http://XieXiePro.github.io
 */
public class LocationWidget extends LinearLayout {
    private Context context;
    private TextView tvProvider = null;
    private TextView tvTime = null;
    private EditText tvLatitude = null;
    private EditText tvLongitude = null;
    private TextView tvSystemMockPositionStatus = null;
    private Button btnStartMock = null;
    private Button btnStopMock = null;
    private ImageView locationWigdetTipIv;
    private LinearLayout locationWigdetDataLl;
    private LocationManager locationManager;
    private boolean bRun;
    private boolean canMock;


    public LocationWidget(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public LocationWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public LocationWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    private void init(final Context context) {
        View layout = LayoutInflater.from(context).inflate(com.xp.pro.mocklocationlib.R.layout.location_wiget_layout, this, true);
        tvProvider = (TextView) layout.findViewById(com.xp.pro.mocklocationlib.R.id.tv_provider);
        tvTime = (TextView) layout.findViewById(com.xp.pro.mocklocationlib.R.id.tv_time);
        tvLatitude = (EditText) layout.findViewById(com.xp.pro.mocklocationlib.R.id.tv_latitude);
        tvLongitude = (EditText) layout.findViewById(com.xp.pro.mocklocationlib.R.id.tv_longitude);
        tvSystemMockPositionStatus = (TextView) findViewById(com.xp.pro.mocklocationlib.R.id.tv_system_mock_position_status);
        locationWigdetTipIv = (ImageView) findViewById(com.xp.pro.mocklocationlib.R.id.location_wigdet_tip_iv);
        locationWigdetDataLl = (LinearLayout) findViewById(com.xp.pro.mocklocationlib.R.id.location_wigdet_data_ll);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        btnStartMock = (Button) findViewById(com.xp.pro.mocklocationlib.R.id.btn_start_mock);
        btnStopMock = (Button) findViewById(com.xp.pro.mocklocationlib.R.id.btn_stop_mock);
        canMock = isAllowMockLocation();
        btnStartMock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMock();
            }
        });
        btnStopMock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMock();
            }
        });


    }

    /**
     * 停止模拟定位
     */
    public void stopMock() {
        LocationActivity activity = (LocationActivity) context;
        Intent intent = new Intent(context, MockLocationManager.class);
        activity.stopService(intent);
        bRun = false;
        btnStartMock.setEnabled(true);
        btnStopMock.setEnabled(false);
    }

    /**
     * 模拟定位
     */
    public void startMock() {
        LocationActivity activity = (LocationActivity) context;
        if (canMock) {
            Intent intent = new Intent(context, MockLocationManager.class);
            intent.putExtra("latitude", Double.valueOf(tvLatitude.getText().toString()));
            intent.putExtra("longitude", Double.valueOf(tvLongitude.getText().toString()));
            intent.putExtra("key",tvLatitude.getText().toString() + "&" + tvLongitude.getText().toString());
            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(intent);
                Log.d("DEBUG", "startForegroundService: MOCK_GPS");
            } else {
                activity.startService(intent);
                Log.d("DEBUG", "startService: MOCK_GPS");
            }
            btnStartMock.setEnabled(false);
            btnStopMock.setEnabled(true);
            bRun = true;
        }
    }

    public void refreshData() {

        LocationActivity activity = (LocationActivity) context;
        // 判断系统是否允许模拟位置，并addTestProvider
        if (canMock) {
            if (bRun) {
                btnStartMock.setEnabled(false);
                btnStopMock.setEnabled(true);
            } else {
                btnStartMock.setEnabled(true);
                btnStopMock.setEnabled(false);
            }
            tvSystemMockPositionStatus.setText("已开启");
            locationWigdetTipIv.setVisibility(View.GONE);
            locationWigdetDataLl.setVisibility(View.VISIBLE);
        } else {
            btnStartMock.setEnabled(false);
            btnStopMock.setEnabled(false);
            tvSystemMockPositionStatus.setText("未开启");
            locationWigdetTipIv.setVisibility(View.VISIBLE);
            locationWigdetDataLl.setVisibility(View.GONE);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
            }
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void removeUpdates() {
        locationManager.removeUpdates(locationListener);
    }


    public boolean isAllowMockLocation() {
        int i = Build.VERSION.SDK_INT;
        LocationActivity activity = (LocationActivity) context;
        boolean bool = true;
        if (i <= 22) {
            if (Settings.Secure.getInt(activity.getContentResolver(), "mock_location", 0) == 0)
                bool = false;
            return bool;
        }
        try {
            LocationProvider provider = locationManager.getProvider("gps");
            if (provider != null) {
                    try {
                        locationManager.addTestProvider(
                                provider.getName()
                                , provider.requiresNetwork()
                                , provider.requiresSatellite()
                                , provider.requiresCell()
                                , provider.hasMonetaryCost()
                                , provider.supportsAltitude()
                                , provider.supportsSpeed()
                                , provider.supportsBearing()
                                , provider.getPowerRequirement()
                                , provider.getAccuracy());
                        bool = true;
                    } catch (Exception exception) {
                        Log.e("FUCK", "add origin gps test provider error");
                        exception.printStackTrace();
                    }
            }else{
                try {
                    locationManager.addTestProvider("gps", true, true, false, false, true, true, true, 3, 1);
                    bool = true;
                } catch (Exception exception) {
                    Log.e("FUCK", "add gps test provider error");
                    exception.printStackTrace();
                }
            }
            if (bool) {
                locationManager.setTestProviderEnabled("gps", true);
                locationManager.setTestProviderStatus("gps", 2, null, System.currentTimeMillis());
                locationManager.setTestProviderEnabled("gps", false);
                locationManager.removeTestProvider("gps");
            }
            return bool;
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
            return false;
        }
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            setLocationData(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 获取到模拟定位信息，并显示
     *
     * @param location 定位信息
     */
    private void setLocationData(Location location) {
        tvProvider.setText(location.getProvider());
        tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime())));
      //  tvLatitude.setText(String.format("%s", location.getLatitude()));
      //  tvLongitude.setText(String.format("%s", location.getLongitude()));
    }


    public void setLocation(LocationBean mLocationBean) {
        tvLatitude.setText(String.format("%s", mLocationBean.getLatitude()));
        tvLongitude.setText(String.format("%s", mLocationBean.getLongitude()));
    }
}