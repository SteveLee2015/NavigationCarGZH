package com.novsky.map.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class InstallLogService extends Service {

    private boolean isStop = false;
    private ServerSocket serverSocket = null;


    private Runnable startServiceRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                Log.i("TEST", "=====================>start Log Service");
                while (!isStop) {
                    Socket socket = serverSocket.accept();
                    Log.i("TEST", "============server accept ");
                    PrintStream printStream = null;
                    BufferedReader reader = null;
                    try {
                        printStream = new PrintStream(socket.getOutputStream());

                        Log.i("TEST", "===================>THDConstants.LOG_FLAG");
                        Process exec = Runtime.getRuntime().exec("logcat");
                        reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));

                        String log = "";
                        int count = 0;
                        Runtime.getRuntime().exec("logcat -c");
                        while (!isStop && (log = reader.readLine()) != null) {
                            count++;
                            printStream.println(log);
                            if (count > 20) {
                                printStream.flush();
                                count = 0;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        reader.close();
                        printStream.close();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStop = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread startThread = new Thread(startServiceRunnable);
        startThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
    }
}
