package com.xs.multibleapp.ui;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.scanner.ScannerFragment;
import com.xs.multibleapp.R;
import com.xs.multibleapp.ble.service.ScanService;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
,ScannerFragment.OnDeviceSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private BleProfileService.LocalBinder mSportService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_test).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test:

                final Intent intent = new Intent(MainActivity.this,ScanService.class);
                startService(intent);
                bindService(intent,mSportServiceConn,0);

                ScannerFragment.getInstance(this, UUID.fromString("uuid"),true).show(getSupportFragmentManager(),"tag");

                break;
        }
    }

    private ServiceConnection mSportServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mSportService = (BleProfileService.LocalBinder) service;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mSportService = null;
        }
    };

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {

    }

    @Override
    public void onDialogCanceled() {

    }
}
