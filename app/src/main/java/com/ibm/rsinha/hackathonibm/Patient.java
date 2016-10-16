package com.ibm.rsinha.hackathonibm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.Language;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Patient extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String message="";
    private String msg="";
    private EditText txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        txt=(EditText) findViewById(R.id.editText);
        initializeLocationService();
    }

    public void callTheAmbulance(View v){
        //EntityUtils.toString(httpEntity);
        message=txt.getText().toString();
        new FetchLanguage().execute();
    }

    class FetchLanguage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                LanguageTranslation service = new LanguageTranslation();
                service.setEndPoint("https://gateway.watsonplatform.net/language-translator/api");
                service.setUsernameAndPassword("d2b87de0-a552-46e4-82f4-9bbebc896fc5", "GeUodnASmtNt");
                TranslationResult result = service.translate(message, Language.ENGLISH, Language.FRENCH).execute();
                msg=result.getFirstTranslation();
                Log.i("Check", result.getFirstTranslation());
            }catch (Exception ex){
                Log.i("Check","Error : "+ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new SendRequest().execute();
        }
    }

    private void initializeLocationService() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationListener == null)
            locationListener = new Patient.MyLocationListener();

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
    private double x,y;
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //todo change the values and send request;
            Log.i("Check","3");
            x=location.getLatitude();
            y=location.getLongitude();
            Toast.makeText(Patient.this,"Got the location "+x+" : "+y,Toast.LENGTH_LONG).show();
            //xt=location.get
            //new Ambulance.SendData().execute();
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




    class SendRequest extends AsyncTask<Void,Void,Void>{
        private String domain="http://ibmmsrit.hol.es/";
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("Check", "Uploading Data");
                //Toast.makeText(Ambulance.this,"Uploading Data",Toast.LENGTH_SHORT).show();
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(domain + "getAmbulanceData.php");
                List<NameValuePair> ln1 = new ArrayList<NameValuePair>();
                httpPost.setEntity(new UrlEncodedFormEntity(ln1));
                HttpResponse response = httpClient.execute(httpPost);
                //publishProgress("Reading Data...");
                String s=EntityUtils.toString(response.getEntity());

                JSONArray jsonArray=new JSONArray(s);

                ArrayList<Locations> arrayList=new ArrayList<Locations>();
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    Locations loc=new Locations();
                    loc.imei=jsonObject.getString("imei");
                    loc.x=jsonObject.getDouble("x");
                    loc.y=jsonObject.getDouble("y");
                    arrayList.add(loc);
                }
                String imei=getClosestLocation(arrayList);
                httpPost = new HttpPost(domain + "updateAmbulanceData.php");
                ln1 = new ArrayList<NameValuePair>();
                ln1.add(new BasicNameValuePair("imei", imei));
                ln1.add(new BasicNameValuePair("xt", String.valueOf(x)));
                ln1.add(new BasicNameValuePair("yt", String.valueOf(y)));
                ln1.add(new BasicNameValuePair("message", msg));
                httpPost.setEntity(new UrlEncodedFormEntity(ln1));
                response = httpClient.execute(httpPost);
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = br.readLine();
                if(line.equals("Successful")) {
                    Log.i("Check", "SuccessFully updated");
                }
                else {
                    Log.i("Check", "Cannot Upload");
                }
            }catch (Exception ex){
                Log.i("Check",ex.getMessage());
            }
            return null;
        }
    }
    private String getClosestLocation(ArrayList<Locations> arr){
        double dist=(arr.get(0).x-x)*(arr.get(0).x-x)+(arr.get(0).y-y)*(arr.get(0).y-y);
        String imei=arr.get(0).imei;
        for(int i=1;i<arr.size();i++){
            double dist2=(arr.get(i).x-x)*(arr.get(i).x-x)+(arr.get(i).y-y)*(arr.get(i).y-y);
            if(dist2<dist)
                imei=arr.get(i).imei;
        }
        return imei;
    }
    class Locations{
        public double x,y;
        public String imei;
    }
}
