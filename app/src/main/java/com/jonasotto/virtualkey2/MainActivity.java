package com.jonasotto.virtualkey2;

import android.graphics.Outline;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabUnlock;
    RequestQueue queue;
    IPAddress ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabUnlock = (FloatingActionButton) findViewById(R.id.fabUnlock);
        fabUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.unlock();
            }
        });
        discoverEsp();
    }

    public void unlock(){
        //String url = "http://" + espIp.toString() + "/open?pin=" + pinText.getText().toString();
        String url = "http://example.com";
        Log.d("OPENING", "requesting " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("RESPONSE", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RESPONSE", "That didn't work!");

            }
        });

        queue.add(stringRequest);
    }

    void discoverEsp() {
        try {
            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int localIp = wifiInfo.getIpAddress();
            Log.d("INT", Integer.toString(localIp));
            byte[] ipBytes = BigInteger.valueOf(localIp).toByteArray();
            String hexString = Integer.toHexString(localIp);
            Log.d("hex String", hexString);
            Log.d("decimal String", toDecimalString(hexString));
            Log.d("byte Array", ipBytes.toString());
            ip =  new IPAddress(ipBytes);

            Log.d("IP: ", ip.toString());
            for (int i = 0; i < 256; i++) {
                Log.d("ESPSEARCH", "starting test for " + i);
                //testForEsp(ipRange + "." + i);
            }
        } catch (Exception e){
            //e.printStackTrace();
            throw e;
            //Toast.makeText(getApplicationContext(), "You fucked up", Toast.LENGTH_LONG).show();
        }

    }


    /*void testForEsp(final String ipAddress) {
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
                            espIp = new InetAddress(ipAddress);
                            localIpText.setText(ipAddress);
                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
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
            }
        });

        queue.add(stringRequest);
    }*/


    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        for(int i = 0; i < bytesPrim.length; i++){
            bytes[i] = Byte.valueOf(bytesPrim[i]);
        }
        return bytes;
    }

    String toDecimalString(String hexString){
        int intArray[] = new int[hexString.length()/2];
        for(int i = 0; i < hexString.length(); i+=2)
        {
            Log.d("toDecimalString", "parsing " + hexString.substring(i, i+2));
            intArray[i/2] = Integer.decode("0x" +hexString.substring(i, i+2));
        }
        String result = "";
        for(int i:intArray){
            result += Integer.toString(i) + ".";
        }
        return result;
       // return result.substring(0, result.length()-2);
    }
}
