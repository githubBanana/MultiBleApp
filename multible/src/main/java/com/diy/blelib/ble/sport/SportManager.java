/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.ble.sport;

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

import com.diy.blelib.bag.ActiveSend;
import com.diy.blelib.bag.BagCommand;
import com.diy.blelib.bag.BagHandler;
import com.diy.blelib.profile.bleutils.exception.BleErrorInfo;
import com.diy.blelib.profile.BleManager;
import com.diy.blelib.profile.bleutils.BleUUID;

import java.util.List;

/**
 * HTSManager class performs BluetoothGatt operations for connection, service discovery, enabling indication and reading characteristics. All operations required to connect to device with BLE HT
 * Service and reading health thermometer values are performed here. HTSActivity implements HTSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class SportManager implements BleManager<SportManagerCallbacks> {
	private final String TAG = "RopeSkipManager";
	private SportManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;



	private BluetoothGattCharacteristic  mBatteryCharacteritsic,mHrsCharacteristic,mHistoryCharacteristic;

	private final int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
	private final int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
	private final int SHIFT_LEFT_8BITS = 8;
	private final int SHIFT_LEFT_16BITS = 16;
	private final int GET_BIT24 = 0x00400000;
	private static final int FIRST_BIT_MASK = 0x01;
	private static SportManager managerInstance = null;

	/**
	 * singleton implementation of HTSManager class
	 */
	public static synchronized SportManager getHTSManager() {
		if (managerInstance == null) {
			managerInstance = new SportManager();
		}
		return managerInstance;
	}

	/**
	 * callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(SportManagerCallbacks callbacks) {
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
				if (mHrsCharacteristic != null) {
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
					Log.d(TAG, "Device connected");
					mBluetoothGatt.discoverServices();
					syncInit = true;
					//This will send callback to HTSActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					Log.e(TAG, "Device disconnected1");
					//This will send callback to HTSActivity when device get disconnected
					if(mCallbacks != null);
						mCallbacks.onDeviceDisconnected();
				}
			} else {
				if(mCallbacks != null) {
					mCallbacks.onError(BleErrorInfo.ERROR_CONNECTION_STATE_CHANGE, status);
					Log.e(TAG, "Device disconnected2");
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(BleUUID.HR_SERVICE_UUID)) {
                        mHrsCharacteristic = service.getCharacteristic(BleUUID.HR_MEASUREMENT_CHARACTERISTIC_UUID);
						mHistoryCharacteristic = service.getCharacteristic(BleUUID.HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
                    }
				}

				if (mHistoryCharacteristic != null)
					Log.e(TAG,"mHistoryCharacteristic:"+mHistoryCharacteristic);

				if (mHrsCharacteristic != null) {
					enableHRNotification();
					mCallbacks.onServicesDiscovered(false);
                } else {
					Log.e(TAG,"mHrsCharacteristic == null");
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
				/*if (mBatteryCharacteritsic != null) {
					readBatteryLevel();
                    enableHRNotification();
                } else {
                    enableHRNotification();
				}*/
			} else {
				mCallbacks.onError(BleErrorInfo.ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(BleUUID.BATTERY_LEVEL_CHARACTERISTIC)) {
					int batteryValue = characteristic.getValue()[0];
					mCallbacks.onBatteryValueReceived(batteryValue);
				}

			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					Log.w(TAG, BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(BleErrorInfo.ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				mCallbacks.onError(BleErrorInfo.ERROR_READ_CHARACTERISTIC, status);
			}
		}
        private boolean syncInit = false;
        /**
         * 转速数据获取通道
         * @param gatt
         * @param characteristic
         */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if(characteristic.getUuid().equals(BleUUID.HR_MEASUREMENT_CHARACTERISTIC_UUID)) {
				if (syncInit) {
					syncInit = false;
					sendCommand(ActiveSend.toSyncTime());
				}

				byte[] data = characteristic.getValue();
				byte by = BagHandler.doBag(data);
				// 1.sync time  2.get history data  3.upload history data  4.clear device history data
				switch (by) {
					case BagCommand.BAG_RECEIVE_SYNC_TIME:
						sendCommand(ActiveSend.toGetHistorySpeedData());
						break;
					case BagCommand.BAG_RECEIVE_HISTORY_END:
						//upload history data here... if success , to clear device history data
						mCallbacks.onUpGameArr();

						//Just test: modifyDeviceName
//				sendCommand(ActiveSend.toModifyDeviceName("1ZFWD"));
						break;
				}
				try {
                    mCallbacks.onBagReceived(by);
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


	public void readBatteryLevel() {
		if (mBatteryCharacteritsic != null) {
			mBluetoothGatt.readCharacteristic(mBatteryCharacteritsic);
		} else {
			Log.e(TAG, "Battery Level Characteristic is null");
		}
	}
	/**
	 * Enabling notification on Heart Rate Characteristic
	 */
	private void enableHRNotification() {
		Log.d(TAG, "Enabling heart rate notifications");
		mBluetoothGatt.setCharacteristicNotification(mHrsCharacteristic, true);
		BluetoothGattDescriptor descriptor = mHrsCharacteristic.getDescriptor(BleUUID.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 *  发送命令
	 * @param command
	 */
	void sendCommand(byte[] command) {
		mHistoryCharacteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(mHistoryCharacteristic);
	}

	private short convertNegativeByteToPositiveShort(byte octet) {
		if (octet < 0) {
			return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
		} else {
			return octet;
		}
	}

	private int getTwosComplimentOfNegativeMantissa(int mantissa) {
		if ((mantissa & GET_BIT24) != 0) {
			return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
		} else {
			return mantissa;
		}
	}

	/**
	 * This method will check if Heart rate value is in 8 bits or 16 bits
	 */
	private boolean isHeartRateInUINT16(byte value) {
		if ((value & FIRST_BIT_MASK) != 0)
			return true;
		return false;
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
			mHrsCharacteristic = null;

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
