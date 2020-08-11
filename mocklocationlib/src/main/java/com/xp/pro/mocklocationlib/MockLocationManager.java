package com.xp.pro.mocklocationlib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MockLocationManager:模拟地址管理类
 * Author: xp
 * Date: 18/7/12 22:01
 * Email: xiexiepro@gmail.com
 * Blog: http://XieXiePro.github.io
 */
public class MockLocationManager extends Service {

    public static final int RunCode = 1;

    public static final int StopCode = 2;



    private String TAG = "MockGpsService";


    private Handler handler;

    private HandlerThread handlerThread;

    private boolean isFloatWindowStart = false;

    private boolean isStop = true;

    private String latLngInfo = "104.06121778639009&30.544111926165282";

    private LocationManager locationManager;

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    private void rmGPSTestProvider() {
        try {
            if (this.locationManager.isProviderEnabled("gps")) {
                Log.d(this.TAG, "now remove GPSProvider");
                this.locationManager.removeTestProvider("gps");
                return;
            }
            Log.d(this.TAG, "GPSProvider is not enabled");

            return;
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(this.TAG, "rmGPSProvider error");

            return;
        }
    }

    private void rmNetworkTestProvider() {
        try {
            if (this.locationManager.isProviderEnabled("network")) {
                Log.d(this.TAG, "now remove NetworkProvider");
                this.locationManager.removeTestProvider("network");
                return;
            }
            Log.d(this.TAG, "NetworkProvider is not enabled");
            return;
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(this.TAG, "rmNetworkProvider error");
            return;
        }
    }

    private void setGPSLocation() {


        String[] arrayOfString = this.latLngInfo.split("&");
        LocationBean latLng = new LocationBean(Double.valueOf(arrayOfString[0]).doubleValue(), Double.valueOf(arrayOfString[1]).doubleValue());
        try {
            this.locationManager.setTestProviderLocation("gps", generateLocation(latLng));
        } catch (Exception exception) {
            Log.d(this.TAG, "setGPSLocation error");
            exception.printStackTrace();
        }
    }

    private void setGPSTestProvider() {
        this.locationManager.getProvider("gps");
        try {
            this.locationManager.addTestProvider("gps", false, true, true, false, true, true, true, 0, 5);
            Log.d(this.TAG, "addTestProvider[GPS_PROVIDER] success");

        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(this.TAG, "addTestProvider[GPS_PROVIDER] error");
        }
        if (!this.locationManager.isProviderEnabled("gps"))
            try {
                this.locationManager.setTestProviderEnabled("gps", true);
            } catch (Exception exception) {
                exception.printStackTrace();
                Log.d(this.TAG, "setTestProviderEnabled[GPS_PROVIDER] error");
            }
        this.locationManager.setTestProviderStatus("gps", 2, null, System.currentTimeMillis());
    }

    private void setNetworkTestProvider() {
        try {
            this.locationManager.addTestProvider("network", false, false, false, false, false, false, false, 1, 1);
            Log.d(this.TAG, "addTestProvider[NETWORK_PROVIDER] success");

        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(this.TAG, "addTestProvider[NETWORK_PROVIDER] error");
        }
        if (!this.locationManager.isProviderEnabled("network"))
            try {
                this.locationManager.setTestProviderEnabled("network", true);
                return;
            } catch (Exception exception) {
                exception.printStackTrace();
                Log.d(this.TAG, "setTestProviderEnabled[NETWORK_PROVIDER] error");
            }
    }

    private void setTestProviderLocation() {
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setNetworkLocation: ");
        stringBuilder.append(this.latLngInfo);
        Log.d(str, stringBuilder.toString());

        String[] arrayOfString = this.latLngInfo.split("&");
        LocationBean latLng = new LocationBean(Double.valueOf(arrayOfString[0]).doubleValue(), Double.valueOf(arrayOfString[1]).doubleValue());
        try {
            this.locationManager.setTestProviderLocation("network", generateLocation(latLng));
            return;
        } catch (Exception exception) {
            Log.d(this.TAG, "setNetworkLocation error");
            exception.printStackTrace();
            return;
        }
    }

    public void DisplayToast(String paramString) {
        Toast toast = Toast.makeText((Context)this, paramString, 1);
        toast.setGravity(48, 0, 220);
        toast.show();
    }

    public Location generateLocation(LocationBean paramLatLng) {
        Location location = new Location("gps");
        location.setAccuracy(2.0F);
        location.setAltitude(55.0D);
        location.setBearing(1.0F);
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", 7);
        location.setExtras(bundle);
        location.setLatitude(paramLatLng.getLatitude());
        location.setLongitude(paramLatLng.getLongitude());
        location.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= 17)
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }

    public void getProviders() {
        for (String str : this.locationManager.getProviders(true)) {
            Log.d("PROV", str);
        }
    }

    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    public void onCreate() {
        Log.d(this.TAG, "onCreate");
        super.onCreate();
        this.locationManager = (LocationManager)getSystemService("location");
        getProviders();
        rmNetworkTestProvider();
        rmGPSTestProvider();
        setNetworkTestProvider();
        setGPSTestProvider();
        HandlerThread handlerThread = new HandlerThread(getUUID(), -2);
        this.handlerThread = handlerThread;
        handlerThread.start();
        Handler handler = new Handler(this.handlerThread.getLooper()) {
            public void handleMessage(Message param1Message) {
                try {
                    Thread.sleep(128L);
                    if (!MockLocationManager.this.isStop) {
                        MockLocationManager.this.setTestProviderLocation();
                        MockLocationManager.this.setGPSLocation();
                        sendEmptyMessage(0);
                        Intent intent = new Intent();
                        intent.putExtra("statusCode", 1);
                        intent.setAction("com.example.service.MockGpsService");
                        MockLocationManager.this.sendBroadcast(intent);
                        return;
                    }
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    Log.d(MockLocationManager.this.TAG, "handleMessage error");
                    Thread.currentThread().interrupt();
                }
            }
        };
        this.handler = handler;
        handler.sendEmptyMessage(0);
    }

    public void onDestroy() {
        Log.d(this.TAG, "onDestroy");

        this.isStop = true;
        try {
           // this.floatWindow.hideFloatWindow();
            this.isFloatWindowStart = false;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        this.handler.removeMessages(0);
        this.handlerThread.quit();
        rmNetworkTestProvider();
        rmGPSTestProvider();
        stopForeground(true);
        Intent intent = new Intent();
        intent.putExtra("statusCode", 2);
        intent.setAction("com.example.service.MockGpsService");
        sendBroadcast(intent);
        super.onDestroy();
    }

    public void onStart(Intent paramIntent, int paramInt) {
        super.onStart(paramIntent, paramInt);
        Log.d(this.TAG, "onStart");
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        Notification notification;
        Log.d(this.TAG, "onStartCommand");

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("channel_01", "channel_name", 2);
            Log.i(this.TAG, notificationChannel.toString());
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
            notification = (new Notification.Builder((Context)this)).setChannelId("channel_01").setContentTitle("模拟定位...").setContentText("MockLocation service is running").setSmallIcon(R.drawable.icon_openmap_mark).build();
        } else {
            notification = (new NotificationCompat.Builder((Context)this)).setContentTitle("模拟定位...").setContentText("MockLocation service is running").setSmallIcon(R.drawable.icon_openmap_mark).setOngoing(true).setChannelId("channel_01").build();
        }
        startForeground(1, notification);
        this.latLngInfo = paramIntent.getStringExtra("key");

        this.isStop = false;
        if (!this.isFloatWindowStart) {
//            FloatWindow floatWindow = new FloatWindow(this);
//            this.floatWindow = floatWindow;
//            try {
//                floatWindow.showFloatWindow();
//                this.isFloatWindowStart = true;
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
        }
        return super.onStartCommand(paramIntent, paramInt1, paramInt2);
    }

    public class ServiceBinder extends Binder {
        public MockLocationManager getService() {
            return MockLocationManager.this;
        }
    }
}