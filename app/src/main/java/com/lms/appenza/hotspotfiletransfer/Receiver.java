package com.lms.appenza.hotspotfiletransfer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver extends AppCompatActivity {
    private static boolean server_running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
            new FileServerTask().execute();
            //server_running=true;

    }

    private class FileServerTask extends AsyncTask<Void, Void, File> {

        public static final String LOG_TAG = "HOTSPOTMM server";

        @Override
        protected File doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                Log.d(LOG_TAG, "Server: socket opened");
                Socket client = serverSocket.accept();
                Log.d(LOG_TAG, "Server: connection accepted");


                File file = new File(Environment.getExternalStorageDirectory() + "/HotspotSharedFiles/file" +System.currentTimeMillis() + ".jpg");
                File dirs = new File(file.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                if (file.createNewFile())
                    Log.d(LOG_TAG,"file created");
                else
                    Log.d(LOG_TAG, "file not created");



                InputStream inputStream = client.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(file);

                if(copyFile(inputStream, outputStream)) {
                    Log.d(LOG_TAG, "File received");
                } else {
                    Log.d(LOG_TAG, "File not copied");
                }
                //client.close();
                //server_running = false;
                Log.d(LOG_TAG,"Server Conn closed");
                finish();
                return file;

            } catch (IOException e) {
                e.printStackTrace();
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
                Log.d(LOG_TAG, e.toString());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(File f) {
            Log.d(LOG_TAG, "File Uri: " + Uri.fromFile(f));
            if (f != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(f), "image/*");
                startActivity(intent);
            }
        }

    }


}
