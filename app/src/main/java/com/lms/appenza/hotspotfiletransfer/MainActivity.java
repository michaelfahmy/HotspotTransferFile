package com.lms.appenza.hotspotfiletransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "HotspotActivity";

    WifiManager manager;
    List<ScanResult> scanResults;
    List<String> results;
    ArrayAdapter<String> adapter;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanResults = new ArrayList<ScanResult>();
        results = new ArrayList<String>();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanResults = manager.getScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_item_textview, results);
        list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDeviceClicked(position);
            }
        });


    }


    public void recieve(View view) {
        setWifiApEnabled(null, true);
    }



    public void send(View view) {
        scan();
    }

    private boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) { // disable WiFi in any case
                manager.setWifiEnabled(false);
            }

            Method method = manager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(manager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    private void scan() {

        manager.startScan();
        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show();

        Log.d(LOG_TAG, "size " + scanResults.size());

        for (int i = 0; i < scanResults.size(); i++) {
            results.add(scanResults.get(i).SSID);
            Log.d(LOG_TAG, i + " ============== " + scanResults.get(i).SSID);
        }

        adapter.notifyDataSetChanged();
    }

    private void onDeviceClicked(int pos) {
        manager.disconnect();
        manager.enableNetwork(Integer.parseInt(scanResults.get(pos).SSID), true);
        manager.reconnect();
    }

}
