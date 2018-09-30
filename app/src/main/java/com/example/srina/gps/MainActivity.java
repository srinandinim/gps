package com.example.srina.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    URL url;
    URLConnection connection;
    InputStream stream;
    BufferedReader reader;
    String readerString;
    JSONObject location;

    double longitude;
    double latitude;
    LocationListener locationListener;
    LocationManager locationManager;
    DecimalFormat decimalFormat = new DecimalFormat("0.######");

    TextView textLat, textLong, textAdd, textDis;
    final List<Location> list = new ArrayList<>();

    /* Api Key:
    AIzaSyBO1lI5EAVV2PxgDn1I09bQXoxH3hFCk7s
     */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLat = (TextView) (findViewById(R.id.latitude));
        textLong = (TextView) findViewById(R.id.longitude);
        textAdd = (TextView) findViewById(R.id.address);
        textDis = (TextView) findViewById(R.id.totalDistance);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                list.add(location);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                textLat.setText("Latitude: " + decimalFormat.format(latitude));
                textLong.setText("Longitude: " + decimalFormat.format(longitude));
                if (list.size() == 1) {
                    textDis.setText ("Total Distance: " + "0.0 miles");
                }
                if (list.size() > 1) {
                    float totalDistance = 0;
                    for (int i = 1; i < list.size(); i++) {
                        Location locA = list.get(i - 1);
                        Location locB = list.get(i);
                        totalDistance += locA.distanceTo(locB);
                        Log.d("msg", totalDistance + "");
                    }
                    double tot = 0.000621371 * totalDistance;
                    textDis.setText("Total Distance: " + decimalFormat.format(tot) + " miles");
                }
                AsyncThread locationThread = new AsyncThread(longitude, latitude);
                locationThread.execute();
            }

            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            public void onProviderEnabled(String s) {
            }

            public void onProviderDisabled(String s) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public class AsyncThread extends AsyncTask<Void, Void, JSONObject> {

        double lon, lat;

        public AsyncThread(double x, double y) {
            lon = x;
            lat = y;
        }

        protected JSONObject doInBackground(Void... voids) {
            try {
                url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&key=AIzaSyBO1lI5EAVV2PxgDn1I09bQXoxH3hFCk7s");
                try {
                    connection = url.openConnection();
                    stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    readerString = stringBuffer.toString();
                    location = new JSONObject(readerString);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return location;
        }

        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            JSONArray results = null;
            try {
                results = location.getJSONArray("results");
                String number = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("long_name");
                String street = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("long_name");
                String city = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(2).getString("long_name");
                String state = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(5).getString("short_name");
                textAdd.setText(number + " " + street + ", " + city + ", " + state);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
