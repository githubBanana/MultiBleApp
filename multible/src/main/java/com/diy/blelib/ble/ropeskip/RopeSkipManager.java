/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.ble.ropeskip;

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

import com.diy.blelib.profile.bleutils.exception.BleErrorInfo;
import com.diy.blelib.profile.BleManager;
import com.diy.blelib.profile.bleutils.BleUUID;

import java.util.List;

/**
 * HTSManager class performs BluetoothGatt operations for connection, service discovery, enabling indication and reading characteristics. All operations required to connect to device with BLE HT
 * Service and reading health thermometer values are performed here. HTSActivity implements HTSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class RopeSkipManager implements BleManager<RopeSkipManagerCallbacks> {
	private final String TAG = "RopeSkipManager";
	private RopeSkipManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;

	private BluetoothGattCharacteristic  mBatteryCharacteritsic,mRopeSkipReadCharacteristic,mRopeSkipWriteCharacteristic;

	private static RopeSkipManager managerInstance = null;

	/**
	 * singleton implementation of HTSManager class
	 */
	public static synchronized RopeSkipManager getHTSManager() {
		if (managerInstance == null) {
			managerInstance = new RopeSkipManager();
		}
		return managerInstance;
	}

	/**
	 * callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(RopeSkipManagerCallbacks callbacks) {
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
				if (mRopeSkipReadCharacteristic != null) {
					enableHRNotification();
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
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					mBluetoothGatt.discoverServices();
					//This will send callback to HTSActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					//This will send callback to HTSActivity when device get disconnected
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
			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(BleUUID.HR_SERVICE_UUID)) {
						mRopeSkipReadCharacteristic = service.getCharacteristic(BleUUID.HR_MEASUREMENT_CHARACTERISTIC_UUID);
						mRopeSkipWriteCharacteristic = service.getCharacteristic(BleUUID.HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
                    }
				}

				if (mRopeSkipReadCharacteristic != null) {
					enableHRNotification();
					mCallbacks.onServicesDiscovered(false);
                } else {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
			} else {
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
			if(characteristic.getUuid().equals(BleUUID.HR_MEASUREMENT_CHARACTERISTIC_UUID)) {
				//handler data
				byte[] data = characteristic.getValue();
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
	 * Enabling notification on Heart Rate Characteristic
	 */
	private void enableHRNotification() {
		mBluetoothGatt.setCharacteristicNotification(mRopeSkipReadCharacteristic, true);
		BluetoothGattDescriptor descriptor = mRopeSkipReadCharacteristic.getDescriptor(BleUUID.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 *  发送命令
	 * @param command
	 */
	void sendCommand(byte[] command) {
		mRopeSkipWriteCharacteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(mRopeSkipWriteCharacteristic);
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
			mRopeSkipReadCharacteristic = null;

		}
	}

	@Override
	public void sendUserCommand(byte[] command) {
		sendCommand(command);
	}

	@Override
	public void sendUserCommand(int command) {

	}


}
