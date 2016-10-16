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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ambulance extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LinearLayout holder;
    private TextView txt;
    private Button btn;
    private String imei;
    private double x,y,xt=0.0,yt=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance);

        bindListener();

        Log.i("Check","1");
        readIMEINo();
        Log.i("Check","2");
        initializeLocationService();
        new Wait2Sec().start();
    }

    private void bindListener() {
        holder=(LinearLayout)findViewById(R.id.holder_ambulance);
        txt=(TextView)findViewById(R.id.txt_line);
        btn=(Button)findViewById(R.id.btn_seen);
        holder.setVisibility(View.INVISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.setVisibility(View.INVISIBLE);
                new Wait2Sec().start();
            }
        });
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 3, locationListener);
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
            Log.i("Check","3");
            x=location.getLatitude();
            y=location.getLongitude();
            Toast.makeText(Ambulance.this,"Got the location "+x+" : "+y,Toast.LENGTH_LONG).show();
            //xt=location.get
            new SendData().execute();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
            Log.i("Check","4");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.i("Check","5");
        }
    }
    private String domain="http://ibmmsrit.hol.es/";
    private class SendData extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("Check", "Uploading Data");
                //Toast.makeText(Ambulance.this,"Uploading Data",Toast.LENGTH_SHORT).show();
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(domain + "sendAmbulanceData.php");
                List<NameValuePair> ln1 = new ArrayList<NameValuePair>();
                ln1.add(new BasicNameValuePair("imei", imei));
                ln1.add(new BasicNameValuePair("x", String.valueOf(x)));
                ln1.add(new BasicNameValuePair("y", String.valueOf(y)));
                ln1.add(new BasicNameValuePair("xt", String.valueOf(xt)));
                ln1.add(new BasicNameValuePair("yt", String.valueOf(yt)));
                httpPost.setEntity(new UrlEncodedFormEntity(ln1));
                HttpResponse response = httpClient.execute(httpPost);
                //publishProgress("Reading Data...");
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = br.readLine();
                if(line.equals("Successful")) {
                    Log.i("Check", "SuccessFully updated");
                    //Toast.makeText(Ambulance.this,"Successfull",Toast.LENGTH_SHORT).show();

                }
                else {
                    Log.i("Check", "Cannot Upload");
                    //Toast.makeText(Ambulance.this,"Error",Toast.LENGTH_SHORT).show();
                }
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
    private void showMessage(String line){
        holder.setVisibility(View.VISIBLE);
        txt.setText(line);
    }
    class GetData extends AsyncTask<Void,Void,Void>{
        boolean b=false;
        String line;
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("Check", "XUploading Data");
                //Toast.makeText(Ambulance.this,"Uploading Data",Toast.LENGTH_SHORT).show();
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(domain + "getMyData.php");
                List<NameValuePair> ln1 = new ArrayList<NameValuePair>();
                ln1.add(new BasicNameValuePair("imei", imei));
                httpPost.setEntity(new UrlEncodedFormEntity(ln1));
                HttpResponse response = httpClient.execute(httpPost);
                //publishProgress("Reading Data...");
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                line= br.readLine();
                Log.i("Check",line);
                if(line.equals("NULL")) {
                    Log.i("Check", "XSuccessFully updated");
                    b=true;
                    //Toast.makeText(Ambulance.this,"Successfull",Toast.LENGTH_SHORT).show();
                }
                else {
                    b=false;
                    Log.i("Check", "XCannot Upload");
                    //Toast.makeText(Ambulance.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception ex){
                Log.i("Check",ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(b)
                new Wait2Sec().start();
            else
                showMessage(line);
        }
    }
    class Wait2Sec implements Runnable{
        private Thread t;
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                new GetData().execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public void start(){
            t=new Thread(this);
            t.start();
        }
        public void stop(){
            if(t!=null)
                t.stop();
        }
    }
}
