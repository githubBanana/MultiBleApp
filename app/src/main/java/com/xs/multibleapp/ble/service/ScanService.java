package com.xs.multibleapp.ble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-11 16:16
 * @email Xs.lin@foxmail.com
 */
public class ScanService extends Service {
    private static final String TAG = "ScanService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        Log.e(TAG, "onCreate: " );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " );
        startScan();
        return START_REDELIVER_INTENT;
    }
    private boolean mIsScanning;
    private void startScan() {
        Log.e(TAG, "startScan: " );
        mBluetoothAdapter.startLeScan(mLEScanCallback);

        mIsScanning = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                }
            }
        }, 10000);
    }
    private void stopScan() {
        Log.e(TAG, "stopScan: " );
        if (mIsScanning) {
            mBluetoothAdapter.stopLeScan(mLEScanCallback);
            mIsScanning = false;
        }
    }
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device != null) {
                Log.e(TAG, "TEST: "+device.getName()+ " "+device.getAddress()+" "+rssi);

        /*        try {
                    if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, mUuid, mDiscoverableRequired)) {
                        // On some devices device.getName() is always null. We have to parse the name manually :(
                        // This bug has been found on Sony Xperia Z1 (C6903) with Android 4.3.
                        // https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
                        addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);
                    }
//                    addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);


                } catch (Exception e) {
                    Log.e(TAG, "Invalid data in Advertisement packet " + e.toString());
                }*/
            }
        }
    };

}
