package com.diy.blelib.profile;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diy.blelib.expand.base.BaseFragment;
import com.diy.blelib.profile.bleutils.BleConstant;
import com.diy.blelib.scanner.ScannerFragment;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-26 17:50
 * @email Xs.lin@foxmail.com
 */
public abstract class BleProfileServiceReadyFragment <E extends BleProfileService.LocalBinder> extends BaseFragment implements
        ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
    private static final String TAG = "BleProfileServiceReadyFragment";

    private static final String DEVICE_NAME 				= "device_name";
    protected static final int REQUEST_ENABLE_BT 			= 2;

    private E mService;

    private String mDeviceName;

    private final BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BleConstant.BROADCAST_CONNECTION_STATE: {
                    final int state = intent.getIntExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_DISCONNECTED);

                    switch (state) {
                        case BleConstant.STATE_CONNECTED: {
                            mDeviceName = intent.getStringExtra(BleConstant.EXTRA_DEVICE_NAME);
                            onDeviceConnected();
                            break;
                        }
                        case BleConstant.STATE_DISCONNECTED: {
                            onDeviceDisconnected();
                            mDeviceName = null;
                            break;
                        }
                        case BleConstant.STATE_LINK_LOSS: {
                            onLinklossOccur();
                            break;
                        }
                        case BleConstant.STATE_CONNECTING: {
//                            onDeviceConnecting();111
                            break;
                        }
                        case BleConstant.STATE_DISCONNECTING: {
//                            onDeviceDisconnecting();11
                            break;
                        }
                        // current implementation does nothing in this states
                        default:
                            // there should be no other actions
                            break;
                    }
                    break;
                }
                case BleConstant.BROADCAST_SERVICES_DISCOVERED: {
                    final boolean primaryService = intent.getBooleanExtra(BleConstant.EXTRA_SERVICE_PRIMARY, false);
                    final boolean secondaryService = intent.getBooleanExtra(BleConstant.EXTRA_SERVICE_SECONDARY, false);

                    if (primaryService) {
                        onServicesDiscovered(secondaryService);
                    } else {
                        onDeviceNotSupported();
                    }
                    break;
                }
                case BleConstant.BROADCAST_BOND_STATE: {
                    final int state = intent.getIntExtra(BleConstant.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                    switch (state) {
                        case BluetoothDevice.BOND_BONDING:
                            onBondingRequired();
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            onBonded();
                            break;
                    }
                    break;
                }
                case BleConstant.BROADCAST_BATTERY_LEVEL: {
                    final int value = intent.getIntExtra(BleConstant.EXTRA_BATTERY_LEVEL, -1);
                    if (value > 0)
                        onBatteryValueReceived(value);
                    break;
                }
                case BleConstant.BROADCAST_ERROR: {
                    final String message = intent.getStringExtra(BleConstant.EXTRA_ERROR_MESSAGE);
                    final int errorCode = intent.getIntExtra(BleConstant.EXTRA_ERROR_CODE, 0);
                    onError(message, errorCode);
                    onDeviceDisconnected();
                    break;
                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final E bleService = mService = (E) service;
//            Log.e(TAG, "Activity binded to the service");
//            onServiceBinded(bleService);111

            mDeviceName = bleService.getDeviceName();

            // and notify user if device is connected
            if (bleService.isConnected())
                onDeviceConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
//            Log.e(TAG, "Activity disconnected from the service");

            mService = null;
            mDeviceName = null;
//            onServiceUnbinded();111
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getContentView(),container,false);
        return view;
    }

    protected abstract int getContentView();
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}
