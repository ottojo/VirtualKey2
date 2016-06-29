package com.jonasotto.virtualkey2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fabUnlock;
    RequestQueue searchQueue;
    RequestQueue comQueue;
    IPAddress espIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchQueue = Volley.newRequestQueue(this);
        comQueue = Volley.newRequestQueue(this);
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


    public void unlock() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setTitle("PIN");
        alertBuilder.setCancelable(true);


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        alertBuilder.setView(input);

        alertBuilder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String url = "http://" + espIp.toString() + "/open?pin=" + input.getText().toString();
                Log.d("OPENING", "requesting " + url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("OPENING", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("OPENING", "That didn't work!");

                    }
                });
                comQueue.add(stringRequest);
            }
        });

        alertBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.getWindow().setLayout(500, 400);
        alert.show();


    }

    void discoverEsp() {
        try {
            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int localIp = wifiInfo.getIpAddress();
            byte[] ipBytes = BigInteger.valueOf(localIp).toByteArray();
            IPAddress ip = new IPAddress(ipBytes);
            //String ipString = ip.toString();
            String ipString = getWifiApIpAddress();
            Log.d("Own IP", ipString);
            String ipRange = ipString.substring(0, ipString.lastIndexOf('.'));
            Log.d("RANGE", ipRange);
            for (int i = 0; i < 256; i++) {
                testForEsp(ipRange + "." + i);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw e;
            //Toast.makeText(getApplicationContext(), "You fucked up", Toast.LENGTH_LONG).show();
        }
    }

    IPAddress currentESPIp() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String ipString = sharedPreferences.getString("espIp", "0.0.0.0");
        return new IPAddress(ipString);
    }

    void testForEsp(final String ipAddress) {
        final String url = "http://" + ipAddress + "/testEsp";
        Log.d("ESPSEARCH", "Requesting " + url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                Log.d("ESPSEARCH", "Response is: " + response);
                if (response.equals("ESP")) {
                    Log.d("ESPSEARCH", "ESP ON  " + url);
                    espIp = new IPAddress(ipAddress);
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    sharedPref.edit().putString("espIp", ipAddress).apply();
                    searchQueue.cancelAll(new RequestQueue.RequestFilter() {
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(200, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        searchQueue.add(stringRequest);
    }

    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                            Log.d("BLA", inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("BLA", ex.toString());
        }
        return null;
    }

}
