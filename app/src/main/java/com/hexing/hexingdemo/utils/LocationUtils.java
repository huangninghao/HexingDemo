package com.hexing.hexingdemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.hexing.libhexbase.log.HexBaseLog;
import com.hexing.libhexbase.log.HexLog;


/**
 * @author caibinglong
 *         date 2018/4/2.
 *         desc desc
 */

public class LocationUtils {
    //Location信息变化时调用的接口
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude() + "";
                longitude = location.getLongitude() + "";
                HexLog.debug("Location", latitude + "||" + longitude);
            }
        }
    };

    private Activity mActivity;
    /**
     * 定位相关
     */
    private long intervalTime = 5000;
    private long locationRefreshMinTime = 5000;
    private long locationRefreshMinDistance = 0;
    private String latitude;
    private String longitude;
    public static final int PERMISSION_REQUESTCODE = 0x1;
    private LocationManager locationManager;
    private boolean isLocationEnable = true, isGoogleApiEnable = true, isBaiDuApiEnable = true, isCancel = false;
    private String country, administrative, locality;
    //private LocationUtilCallback mCallback;
    public static final int RESULT_OK = 0, RESULT_LOCATION_ERROR = 1, RESULT_GOOGLE_API_ERROR = 2, RESULT_PERMISSION_ERROR = 3;

    public LocationUtils(Activity mActivity) {
        this.mActivity = mActivity;
        locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * 获取手机当前位置的经纬度
     */
    public void getLocalInformation() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUESTCODE);
            return;
        }
        isLocationEnable = false;
        isCancel = false;
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null && location.getLatitude() > 0 && location.getLongitude() > 0) {
            latitude = location.getLatitude() + "";
            longitude = location.getLongitude() + "";
            HexLog.debug("Location", latitude + "||" + longitude);

        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationRefreshMinTime, locationRefreshMinDistance, locationListener);
        }
    }

    public void onDestroy() {
        isCancel = true;
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    public boolean isLocationEnable() {
        return isLocationEnable;
    }

    public LocationUtils setLocationEnable(boolean locationEnable) {
        isLocationEnable = locationEnable;
        return this;
    }

    public boolean isGoogleApiEnable() {
        return isGoogleApiEnable;
    }

    public LocationUtils setGoogleApiEnable(boolean googleApiEnable) {
        isGoogleApiEnable = googleApiEnable;
        return this;
    }

    private void openGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//系统设置界面
        mActivity.startActivity(intent);
    }
}
