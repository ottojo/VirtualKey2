package com.jonasotto.virtualkey2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabUnlock;
    RequestQueue queue;
    IPAddress espIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        fabUnlock = (FloatingActionButton) findViewById(R.id.fabUnlock);
        fabUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.unlock();
            }
        });
        espIp = currentESPIp();
        discoverEsp();
    }

    public void unlock(){
        String url = "http://" + espIp.toString() + "/open?pin=" +  "1234";//pinText.getText().toString();
        Log.d("OPENING", "requesting " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("OPENING", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("OPENING", "That didn't work!");

            }
        });
        queue.add(stringRequest);
    }

    void discoverEsp() {
        try {
            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int localIp = wifiInfo.getIpAddress();
            byte[] ipBytes = BigInteger.valueOf(localIp).toByteArray();
            IPAddress ip =  new IPAddress(ipBytes);
            String ipString = ip.toString();
            Log.d("Own IP", ipString);
            String ipRange = ipString.substring(0, ipString.lastIndexOf('.'));
            Log.d("RANGE", ipRange);
            for (int i = 0; i < 256; i++) {
                testForEsp(ipRange + "." + i);
            }
        } catch (Exception e){
            //e.printStackTrace();
            throw e;
            //Toast.makeText(getApplicationContext(), "You fucked up", Toast.LENGTH_LONG).show();
        }
    }

    IPAddress currentESPIp()
    {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String ipString = sharedPreferences.getString("espIp", "0.0.0.0");
        return new IPAddress(ipString);
    }

    void testForEsp(final String ipAddress) {
        final String url = "http://" + ipAddress + "/testEsp";
        Log.d("ESPSEARCH", "Requesting " + url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("ESPSEARCH", "Response is: " + response);
                        if (response.equals("ESP")) {
                            Log.d("ESPSEARCH", "ESP ON  " + url);
                            espIp = new IPAddress(ipAddress);
                            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                            sharedPref.edit().putString("espIp", ipAddress).apply();
                            queue.cancelAll(new RequestQueue.RequestFilter() {
                                @Override
                                public boolean apply(Request<?> request) {
                                    return true;
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ESPSEARCH", "No response from " + url);
                Log.d("ESPSEARCH", "Volley Error from " + url + ": " + error.toString());
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                200,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
}
