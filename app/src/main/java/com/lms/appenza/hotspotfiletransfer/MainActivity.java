package com.lms.appenza.hotspotfiletransfer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "HotspotActivity";
    public static final int CHOOSE_FILE_REQUEST_CODE = 10;

    WifiManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


    }



    public void send(View view) {
        chooseFile();
    }

    public void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CHOOSE_FILE_REQUEST_CODE) {
            Uri uri = data.getData();
            Log.d(LOG_TAG, "Uri: " + uri.toString());
            startActivity(new Intent(this, StudentList.class));
//            Intent serviceIntent = new Intent(this, FileTransferService.class);
//            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
//            serviceIntent.setData(uri);
//            startService(serviceIntent);
        }
    }


    public void recieve(View view) {
        setWifiApEnabled(null, true);
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



}
