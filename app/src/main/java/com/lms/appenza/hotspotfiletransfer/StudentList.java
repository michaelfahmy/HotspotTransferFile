package com.lms.appenza.hotspotfiletransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentList extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.LOG_TAG + "/" + StudentList.class.getSimpleName();

    Map<String, String> json = new HashMap<>();
    WifiManager manager;
    List<ScanResult> scanResults;
    StudentAdapter onlineAdapter, offlineAdapter;
    ListView onlineList, offlineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        json.put("tmp", "eg:gg:df:gd");
        json.put("huawei", "58:2a:f7:a9:7f:20");
        json.put("lenovo", "ee:89:f5:3c:f7:3c");

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanResults = new ArrayList<ScanResult>();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanResults = manager.getScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        onlineList = (ListView) findViewById(R.id.online_list);
        offlineList = (ListView) findViewById(R.id.offline_list);
        onlineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StudentItem student = onlineAdapter.getItem(position);
                student.toggleChecked();
                StudentAdapter.ViewHolder viewHolder = (StudentAdapter.ViewHolder) view.getTag();
                viewHolder.getCheckBox().setChecked(student.isChecked());
            }
        });
        onlineAdapter = new StudentAdapter(this, R.layout.list_item, R.id.student, new ArrayList<StudentItem>());
        offlineAdapter = new StudentAdapter(this, R.layout.list_item, R.id.student, new ArrayList<StudentItem>());
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

        Log.d(LOG_TAG, "size ============= " + scanResults.size());

        onlineAdapter.clear();
        offlineAdapter.clear();

        boolean f;

        for (Map.Entry<String, String> entry : json.entrySet()) {
            f = false;
            String studentMAC = entry.getValue();
            for (int j = 0; j < scanResults.size(); j++) {
                Log.d(LOG_TAG, scanResults.get(j).SSID + " : " + scanResults.get(j).BSSID);
                if(studentMAC.equals(scanResults.get(j).BSSID)) {
                    onlineAdapter.add(new StudentItem(entry.getKey(), entry.getValue(), false));
                    f = true;
                    break;
                }
            }
            if (!f) {
                offlineAdapter.add(new StudentItem(entry.getKey(), entry.getValue(), false));
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
            holder.getCheckBox().setChecked(true);
        }
    }

}
