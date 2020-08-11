package com.xp.pro.mocklocation;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.xp.pro.mocklocationlib.LocationBean;

public class LocationDialogDemo extends Activity {
    LocationBean mLocationBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMockLocationData();
      //  createLocationDialog();
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

    }

//    /**
//     * 创建模拟定位对话框
//     */
//    private void createLocationDialog() {
//        LocationDialog.Builder builder = new LocationDialog.Builder(this);
//        builder.setLatitude(mLocationBean.getLatitude());
//        builder.setLongitude(mLocationBean.getLongitude());
//        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.create().show();
//    }
}
