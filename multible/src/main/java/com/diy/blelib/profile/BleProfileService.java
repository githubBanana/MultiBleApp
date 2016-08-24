/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.profile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.diy.blelib.profile.bleutils.BleConstant;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
	private static final String TAG = "BleProfileService";



	private BleManager<BleManagerCallbacks> mBleManager;
	private Handler mHandler;

	public static boolean mConnected;
	private String mDeviceAddress;
	private String mDeviceName;
	private boolean serviceLife = false;
	private BluetoothAdapter mBlutoothAdapter;

	public class LocalBinder extends Binder {
		/**
		 * Disconnects from the sensor.
		 */
		public final void disconnect() {
			if (!mConnected) {
				onDeviceDisconnected();
				return;
			}

			// notify user about changing the state to DISCONNECTING
			final Intent broadcast = new Intent(BleConstant.BROADCAST_CONNECTION_STATE);
			broadcast.putExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_DISCONNECTING);
			LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

			mBleManager.disconnect();
		}

		/**
		 * Returns the device address
		 *
		 * @return device address
		 */
		public String getDeviceAddress() {
			return mDeviceAddress;
		}

		/**
		 * Returns the device name
		 *
		 * @return the device name
		 */
		public String getDeviceName() {
			return mDeviceName;
		}

		/**
		 * Returns <code>true</code> if the device is connected to the sensor.
		 *
		 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
		 */
		public boolean isConnected() {
			return mConnected;
		}

		/**
		 * notify from service to manager
		 * @param command
		 */
		public void sendUserCommand(byte[] command) {
			mBleManager.sendUserCommand(command);
		}
		public void sendUserCommand(int command) {
			mBleManager.sendUserCommand(command);
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		Log.e(TAG, "onBind: " );
		return getBinder();
	}

	/**
	 * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the binded activity.
	 *
	 * @return the service binder
	 */
	protected LocalBinder getBinder() {
		// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
		Log.e(TAG, "getBinder: " );
		return new LocalBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// we must allow to rebind to the same service
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		super.onCreate();
		serviceLife = true;
		mHandler = new Handler();

		// initialize the manager
		mBleManager = initializeManager();
		mBleManager.setGattCallbacks(this);
		mBlutoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.e(TAG, "onCreate: " );
		Log.e(TAG, "ggggggggggggggggonCreate: " );
	}


	@SuppressWarnings("rawtypes")
	protected abstract BleManager initializeManager();



	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Log.e(TAG, "ggggggggggggggggononStartCommand: " );
		if (intent == null || !intent.hasExtra(BleConstant.EXTRA_DEVICE_ADDRESS))
			throw new UnsupportedOperationException("No device address at EXTRA_DEVICE_ADDRESS key");

		mDeviceAddress = intent.getStringExtra(BleConstant.EXTRA_DEVICE_ADDRESS);
		Log.e(TAG, "Service started");
		//每次打开服务再次确认蓝牙是否打开
		if (!mBlutoothAdapter.isEnabled())
			mBlutoothAdapter.enable();
		// notify user about changing the state to CONNECTING
		final Intent broadcast = new Intent(BleConstant.BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_CONNECTING);
		LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		final BluetoothDevice device = adapter.getRemoteDevice(mDeviceAddress);
		mDeviceName = device.getName();
		onServiceStarted();

		Log.v(TAG, "Connecting...");
		mBleManager.connect(BleProfileService.this, device);
		return START_REDELIVER_INTENT;
	}

	/**
	 * Called when the service has been started. The device name and address are set. It nRF Logger is installed than logger was also initialized.
	 */
	protected void onServiceStarted() {
		// empty default implementation
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final Intent broadcast = new Intent(BleConstant.EXTRA_DESTORY_SERVICE);
		LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);
		// shutdown the manager
//		if(mBleManager == null)
		mBleManager.closeBluetoothGatt();
		mBleManager = null;
		mDeviceAddress = null;
		mDeviceName = null;
		mConnected = false;
		serviceLife = false;
	}


	@Override
	public void onDeviceConnected() {
		mConnected = true;

		final Intent broadcast = new Intent(BleConstant.BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_CONNECTED);
		broadcast.putExtra(BleConstant.EXTRA_DEVICE_ADDRESS, mDeviceAddress);
		broadcast.putExtra(BleConstant.EXTRA_DEVICE_NAME, mDeviceName);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}


	@Override
	public void onLinklossOccur() {
		Log.w(TAG, "Connection lost");
		mConnected = false;

		final Intent broadcast = new Intent(BleConstant.BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_LINK_LOSS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		Log.i(TAG, "Services Discovered");
		Log.v(TAG, "Primary service found");
		if (optionalServicesFound)
			Log.v(TAG, "Secondary service found");

		final Intent broadcast = new Intent(BleConstant.BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(BleConstant.EXTRA_SERVICE_PRIMARY, true);
		broadcast.putExtra(BleConstant.EXTRA_SERVICE_SECONDARY, optionalServicesFound);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceNotSupported() {
		Log.i(TAG, "Services Discovered");
		Log.w(TAG, "Device is not supported");

		final Intent broadcast = new Intent(BleConstant.BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(BleConstant.EXTRA_SERVICE_PRIMARY, false);
		broadcast.putExtra(BleConstant.EXTRA_SERVICE_SECONDARY, false);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// no need for disconnecting, it will be disconnected by the manager automatically
	}

	@Override
	public void onBatteryValueReceived(final int value) {
		Log.i(TAG, "Battery level received: " + value + "%");

		final Intent broadcast = new Intent(BleConstant.BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(BleConstant.EXTRA_BATTERY_LEVEL, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingRequired() {
		Log.v(TAG, "Bond state: Bonding...");
//		showToast(R.string.bonding);

		final Intent broadcast = new Intent(BleConstant.BROADCAST_BOND_STATE);
		broadcast.putExtra(BleConstant.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBonded() {
		Log.i(TAG, "Bond state: Bonded");
//		showToast(R.string.bonded);

		final Intent broadcast = new Intent(BleConstant.BROADCAST_BOND_STATE);
		broadcast.putExtra(BleConstant.EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	@Override
	public void onError(String message, int errorCode) {
		// TODO Auto-generated method stub
		Log.e(TAG, message + " (" + errorCode + ")");
		if(serviceLife) {
			final Intent broadcast = new Intent(BleConstant.BROADCAST_ERROR);
			broadcast.putExtra(BleConstant.EXTRA_ERROR_MESSAGE, message);
			broadcast.putExtra(BleConstant.EXTRA_ERROR_CODE, errorCode);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
			mBleManager.disconnect();
		}
		stopSelf();
	}
	@Override
	public void onDeviceDisconnected() {
		// TODO Auto-generated method stub
		Log.i(TAG, "Disconnected");
		mConnected = false;
		mDeviceAddress = null;
		mDeviceName = null;
		if(serviceLife) {
			final Intent broadcast = new Intent(BleConstant.BROADCAST_CONNECTION_STATE);
			broadcast.putExtra(BleConstant.EXTRA_CONNECTION_STATE, BleConstant.STATE_DISCONNECTED);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		}
		// user requested disconnection. We must stop the service
		stopSelf();
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param messageResId
	 *            an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}


	/**
	 * Returns the device address
	 *
	 * @return device address
	 */
	protected String getDeviceAddress() {
		return mDeviceAddress;
	}

	/**
	 * Returns the device name
	 *
	 * @return the device name
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Returns <code>true</code> if the device is connected to the sensor.
	 *
	 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
	 */
	protected boolean isConnected() {
		return mConnected;
	}
}
