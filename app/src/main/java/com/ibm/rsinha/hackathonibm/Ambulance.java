package com.ibm.rsinha.hackathonibm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class Ambulance extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance);

        readIMEINo();

    }

    private void readIMEINo() {
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = tel.getDeviceId();
    }


    private void initializeLocationService() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationListener == null)
            locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void stopLocationService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

}
