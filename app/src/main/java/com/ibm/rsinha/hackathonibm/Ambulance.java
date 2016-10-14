package com.ibm.rsinha.hackathonibm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ambulance extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String imei;
    private double x,y,xt=0.0,yt=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance);

        readIMEINo();
        initializeLocationService();
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 2, locationListener);
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
            //todo change the values and send request;
            x=location.getLatitude();
            y=location.getLongitude();
            //xt=location.get
            new SendData().execute();
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

    private class SendData extends AsyncTask<Void, Void, Void>{
        private String domain="http://ibmmsrit.hol.es/";
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("Check", "Uploading Data");
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpGet = new HttpPost(domain + "sendAmbulanceData.php");
                List<NameValuePair> ln1 = new ArrayList<NameValuePair>();
                ln1.add(new BasicNameValuePair("imei", imei));
                ln1.add(new BasicNameValuePair("x", String.valueOf(x)));
                ln1.add(new BasicNameValuePair("y", String.valueOf(y)));
                ln1.add(new BasicNameValuePair("xt", String.valueOf(xt)));
                ln1.add(new BasicNameValuePair("yt", String.valueOf(yt)));
                httpGet.setEntity(new UrlEncodedFormEntity(ln1));
                HttpResponse response = httpClient.execute(httpGet);
                //publishProgress("Reading Data...");
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = br.readLine();
                if(line.equals("Successful"))
                    Log.i("Check","SuccessFully updated");
                else
                    Log.i("Check","Cannot Upload");
            }catch (Exception ex){
                Log.i("Check",ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("Check", "Uploading Done");
        }
    }
}
