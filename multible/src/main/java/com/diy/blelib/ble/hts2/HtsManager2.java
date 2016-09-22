/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.ble.hts2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.diy.blelib.profile.BleManager;
import com.diy.blelib.profile.bleutils.BleUUID;
import com.diy.blelib.profile.bleutils.BleUtil;
import com.diy.blelib.profile.bleutils.exception.BleErrorInfo;

import java.util.List;

/**
 * HTSManager class performs BluetoothGatt operations for connection, service discovery, enabling indication and reading characteristics. All operations required to connect to device with BLE HT
 * Service and reading health thermometer values are performed here. HTSActivity implements HTSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class HtsManager2 implements BleManager<HtsManagerCallbacks2> {
	private final String TAG = "HtsManager2";
	private HtsManagerCallbacks2 mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;

	private BluetoothGattCharacteristic  mBatteryCharacteritsic,mHtsCharacteristic;

	private static HtsManager2 managerInstance = null;

	/**
	 * singleton implementation of HTSManager class
	 */
	public static synchronized HtsManager2 getManager() {
		if (managerInstance == null) {
			managerInstance = new HtsManager2();
		}
		return managerInstance;
	}

	/**
	 * callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(HtsManagerCallbacks2 callbacks) {
		mCallbacks = callbacks;
	}

	@Override
	public void connect(Context context, BluetoothDevice device) {
		mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
		mContext = context;
	}

	@Override
	public void disconnect() {
		Log.d(TAG, "Disconnecting device");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			Log.d(TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

			// skip other devices
			if (!device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			if (bondState == BluetoothDevice.BOND_BONDED) {
				// We've read Battery Level, now enabling HT indications
				if (mHtsCharacteristic != null) {
					BleUtil.enableTPIndication(mBluetoothGatt,mHtsCharacteristic);
				}
				mContext.unregisterReceiver(this);
				mCallbacks.onBonded();
			}
		}
	};

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.e(TAG, "onConnectionStateChange: 77777 : "+status+" "+newState );
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.e(TAG, "onConnectionStateChange:GATT_SUCCESS " );
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					Log.e(TAG, "onConnectionStateChange: 11111");
					mBluetoothGatt.discoverServices();
					//This will send callback to HTSActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					//This will send callback to HTSActivity when device get disconnected
					Log.e(TAG, "onConnectionStateChange: 22222");

					if(mCallbacks != null);
						mCallbacks.onDeviceDisconnected();
				}
			} else {
				if(mCallbacks != null) {
					mCallbacks.onError(BleErrorInfo.ERROR_CONNECTION_STATE_CHANGE, status);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.e(TAG, "onServicesDiscovered: 999999994" );
			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(BleUUID.TP_SERVICE_UUID)) {
						mHtsCharacteristic = service.getCharacteristic(BleUUID.TP_MEASUREMENT_CHARACTERISTIC_UUID);
                    }
				}
				Log.e(TAG, "onServicesDiscovered: 666666666666666" );
				if (mHtsCharacteristic != null) {
					Log.e(TAG, "onServicesDiscovered: 888888" );

					BleUtil.enableTPIndication(mBluetoothGatt,mHtsCharacteristic);
					mCallbacks.onServicesDiscovered(false);
                } else {
					Log.e(TAG, "onServicesDiscovered: 4444" );

					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
			} else {
				Log.e(TAG, "onServicesDiscovered: 33333" );

				mCallbacks.onError(BleErrorInfo.ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//read battery
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					Log.e(TAG, BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				mCallbacks.onError(BleErrorInfo.ERROR_READ_CHARACTERISTIC, status);
			}
		}
        /**
         * @param gatt
         * @param characteristic
         */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if(characteristic.getUuid().equals(BleUUID.TP_MEASUREMENT_CHARACTERISTIC_UUID)) {
				//handler data
				byte[] data = characteristic.getValue();
				try {
					float value = (float) BleUtil.decodeTemperature(characteristic.getValue());
					Log.e(TAG, "hts2 : "+value );
					mCallbacks.onBagReceived(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// HT indications has been enabled
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
					mCallbacks.onBondingRequired();
					final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
					mContext.registerReceiver(mBondingBroadcastReceiver, filter);
				} else {
					Log.w(TAG, BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				Log.e(TAG, BleErrorInfo.ERROR_WRITE_DESCRIPTOR + " (" + status + ")");
				mCallbacks.onError(BleErrorInfo.ERROR_WRITE_DESCRIPTOR, status);
			}
		}
	};



	/**
	 *  发送命令
	 * @param command
	 */
	void sendCommand(byte[] command) {//温度服务没有下发命令的接口
		mHtsCharacteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(mHtsCharacteristic);
	}


	@Override
	public void closeBluetoothGatt() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mBatteryCharacteritsic = null;
			mHtsCharacteristic = null;

		}
	}

	@Override
	public void sendUserCommand(byte[] command) {
		sendCommand(command);
	}

	@Override
	public void sendUserCommand(int command) {
		Log.e(TAG, "sendUserCommand: hello "+command );
	}


}
