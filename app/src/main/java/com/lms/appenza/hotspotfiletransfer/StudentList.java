package com.lms.appenza.hotspotfiletransfer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentList extends AppCompatActivity {

    public static final String LOG_TAG = "HOTSPOTMM";

    Map<String, String> json = new HashMap<>();
    WifiManager manager;
    List<ScanResult> scanResults;
    StudentAdapter onlineAdapter, offlineAdapter;
    ListView onlineList, offlineList;
    WifiConfiguration conf ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        json.put("tmp", "xx:xx:xx:xx:xx:xx");
        json.put("huawei", "58:2a:f7:a9:7f:20");
        json.put("lenovo1", "ee:89:f5:3c:f7:3c");
        json.put("lenovo2", "16:36:c6:a8:45:87");

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanResults = new ArrayList<>();
        conf = new WifiConfiguration();
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanResults = manager.getScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        onlineList = (ListView) findViewById(R.id.online_list);
        offlineList = (ListView) findViewById(R.id.offline_list);
//        onlineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                StudentItem student = onlineAdapter.getItem(position);
//                student.toggleChecked();
//                StudentAdapter.ViewHolder viewHolder = (StudentAdapter.ViewHolder) view.getTag();
//                viewHolder.getCheckedTextView().setChecked(student.isChecked());
//            }
//        });
        onlineAdapter = new StudentAdapter(this, R.layout.list_item, R.id.checkedText, new ArrayList<StudentItem>());
        offlineAdapter = new StudentAdapter(this, R.layout.list_item, R.id.checkedText, new ArrayList<StudentItem>());
        onlineList.setAdapter(onlineAdapter);
        offlineList.setAdapter(offlineAdapter);

        scan();

    }


    public void scanBtn(View view) {
        scan();
    }

    private void scan() {
        if(!manager.isWifiEnabled())
            manager.setWifiEnabled(true);

        manager.startScan();

        Log.d(LOG_TAG, "Open networks ============= " + scanResults.size());

        onlineAdapter.clear();
        offlineAdapter.clear();

        boolean f;

        for (Map.Entry<String, String> entry : json.entrySet()) {
            f = false;
            String studentBSSID = entry.getValue();
            for (int j = 0; j < scanResults.size(); j++) {
                String networkBSSID = scanResults.get(j).BSSID;
                if(studentBSSID.equals(networkBSSID)) {
                    onlineAdapter.add(new StudentItem(entry.getKey(), scanResults.get(j).SSID, studentBSSID, false));
                    f = true;
                    break;
                }
            }
            if (!f) {
                offlineAdapter.add(new StudentItem(entry.getKey(), null, studentBSSID, false));
            }
        }

        onlineAdapter.notifyDataSetChanged();
        offlineAdapter.notifyDataSetChanged();
    }

    public void selectAll(View view) {
        StudentItem student;
        StudentAdapter.ViewHolder holder;
        for (int i= 0; i < onlineList.getCount(); i++) {
            student = onlineAdapter.getItem(i);
            student.setChecked(true);
            holder = (StudentAdapter.ViewHolder) onlineList.getChildAt(i).getTag();
            holder.getCheckedTextView().setChecked(true);
        }
    }

    public void sendQuizToStudents(View view){
        for(int i = 0; i < onlineAdapter.getCount(); i++){
            if(onlineAdapter.getItem(i).isChecked()){
                conf.SSID = "\"" + onlineAdapter.getItem(i).getSSID() + "\"";
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                manager.addNetwork(conf);
                List<WifiConfiguration> list = manager.getConfiguredNetworks();
                for( WifiConfiguration j : list ) {
                    if(j.SSID != null && j.SSID.equals("\"" + onlineAdapter.getItem(i).getSSID() + "\"")) {
                        manager.disconnect();
                        manager.enableNetwork(j.networkId, true);
                        manager.reconnect();
                        Log.d(LOG_TAG, "CONNECTED to " + j.SSID);
                        new ClientTask().execute(MainActivity.uri);
                        break;
                    }
                }
            }
        }
    }

    public static final String HOST = "192.168.43.1";
    public static final int PORT = 8000;

    private class ClientTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... params) {
            Context context = getApplicationContext();
            Socket socket = new Socket();

            try {
                Log.d(LOG_TAG, "Client: socket opened");
                socket.bind(null);
                Log.d(LOG_TAG, "Client: connection requested");
                socket.connect(new InetSocketAddress(HOST, PORT));
                Log.d(LOG_TAG, "Client: socket connected");


                ContentResolver cr = context.getContentResolver();
                InputStream inputStream = cr.openInputStream(params[0]);
                OutputStream outputStream = socket.getOutputStream();

                DataOutputStream dos = new DataOutputStream(outputStream);
                dos.writeUTF(params[0].getLastPathSegment());

                if(copyFile(inputStream, outputStream)) {
                    Log.d(LOG_TAG, "File sent");
                } else {
                    Log.d(LOG_TAG, "File not sent");
                }

                if (inputStream != null) {
                    inputStream.close();
                }
                outputStream.close();
                socket.close();
                Log.d(LOG_TAG, "Client: socket closed");
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
            return null;
        }

        private boolean copyFile(InputStream inputStream, OutputStream out) {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
                return false;
            }
            return true;
        }
    }
}
