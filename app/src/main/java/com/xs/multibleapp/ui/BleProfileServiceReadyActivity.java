/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.xs.multibleapp.ui;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.diy.blelib.R;
import com.diy.blelib.expand.base.BaseActivity;
import com.diy.blelib.profile.BleManagerCallbacks;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.profile.bleutils.BleConstant;
import com.diy.blelib.scanner.ScannerFragment;

import java.util.UUID;


/**
 * <p>
 * The {@link BleProfileServiceReadyActivity} activity is designed to be the base class for profile activities that uses services in order to connect to the
 * device. When user press CONNECT button a service is created and the activity binds to it. The service tries to connect to the service and notifies the
 * activity using Local Broadcasts ({@link LocalBroadcastManager}). See {@link BleProfileService} for messages. If the device is not in range it will listen for
 * it and connect when it become visible. The service exists until user will press DISCONNECT button.
 * </p>
 * <p>
 * When user closes the activity (f.e. by pressing Back button) while being connected, the Service remains working. It's still connected to the device or still
 * listens for it. When entering back to the activity, activity will to bind to the service and refresh UI.
 * </p>
 */
public abstract class BleProfileServiceReadyActivity<E extends BleProfileService.LocalBinder> extends BaseActivity implements
		ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
	private static final String TAG = "BleProSerReadyAct";

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
							onDeviceConnecting();
							break;
						}
						case BleConstant.STATE_DISCONNECTING: {
							onDeviceDisconnecting();
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
			Log.e(TAG, "Activity binded to the service");
			onServiceBinded(bleService);

			mDeviceName = bleService.getDeviceName();

			// and notify user if device is connected
			if (bleService.isConnected())
				onDeviceConnected();
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			Log.e(TAG, "Activity disconnected from the service");

			mService = null;
			mDeviceName = null;
			onServiceUnbinded();
		}
	};


	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ensureBLESupported();
		if (!isBLEEnabled()) {
			showBLEDialog();
		}

		onCreateView(savedInstanceState);

		LocalBroadcastManager.getInstance(this).registerReceiver(mCommonBroadcastReceiver, makeIntentFilter());
		Log.e(TAG, "onCreate: " );
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e(TAG, "onStart: " );
		/*
		 * If the service has not been started before, the following lines will not start it. However, if it's running, the Activity will be binded to it and
		 * notified via mServiceConnection.
		 */
		final Intent service = new Intent(this, getServiceClass());
		if (bindService(service, mServiceConnection, BIND_AUTO_CREATE)) // we pass 0 as a flag so the service will not be created if not exists
			Log.e(TAG, "Binding to the service..."); // (* - see the comment below)
	}

	@Override
	protected void onStop() {
		super.onStop();

		try {
			// We don't want to perform some operations (e.g. disable Battery Level notifications) in the service if we are just rotating the screen.
			// However, when the activity is finishing, we may want to disable some device features to reduce the battery consumption.

			Log.e(TAG, "Unbinding from the service...");
			unbindService(mServiceConnection);
			mService = null;

			Log.e(TAG, "Activity unbinded from the service");
			onServiceUnbinded();
			mDeviceName = null;
		} catch (final IllegalArgumentException e) {
			// do nothing, we were not connected to the sensor
		}
		Log.e(TAG, "onStop: " );
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommonBroadcastReceiver);
		Log.e(TAG, "onDestroy: " );
	}

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BleConstant.BROADCAST_CONNECTION_STATE);
		intentFilter.addAction(BleConstant.BROADCAST_SERVICES_DISCOVERED);
		intentFilter.addAction(BleConstant.BROADCAST_BOND_STATE);
		intentFilter.addAction(BleConstant.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(BleConstant.BROADCAST_ERROR);
		return intentFilter;
	}

	/**
	 * Called when activity binds to the service. The parameter is the object returned in {@link Service#onBind(Intent)} method in your service. The method is
	 * called when device gets connected or is created while sensor was connected before. You may use the binder as a sensor interface.
	 */
	protected abstract void onServiceBinded(E binder);

	/**
	 * Called when activity unbinds from the service. You may no longer use this binder because the sensor was disconnected. This method is also called when you
	 * leave the activity being connected to the sensor in the background.
	 */
	protected abstract void onServiceUnbinded();

	/**
	 * Returns the service class for sensor communication. The service class must derive from {@link BleProfileService} in order to operate with this class.
	 *
	 * @return the service class
	 */
	protected abstract Class<? extends BleProfileService> getServiceClass();

	/**
	 * Returns the service interface that may be used to communicate with the sensor. This will return <code>null</code> if the device is disconnected from the
	 * sensor.
	 *
	 * @return the service binder or <code>null</code>
	 */
	protected E getService() {
		return mService;
	}

	/**
	 * Called from {@link #onCreate(Bundle)}. This method should build the activity UI, f.e. using {@link #setContentView(int)}. Use to obtain references to
	 * views. Connect/Disconnect button, the device name view and battery level view are manager automatically.
	 *
	 * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
	 */
	protected abstract void onCreateView(final Bundle savedInstanceState);

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DEVICE_NAME, mDeviceName);
		Log.e(TAG, "onSaveInstanceState: " );
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mDeviceName = savedInstanceState.getString(DEVICE_NAME);
		Log.e(TAG, "onRestoreInstanceState: " );
	}




	/**
	 * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
	 */
	public void onConnectClicked(final View view) {
		if (isBLEEnabled()) {
			if (mService == null) {
				showDeviceScanningDialog();
			} else {
				Log.e(TAG, "Disconnecting...");
				mService.disconnect();
			}
		} else {
			showBLEDialog();
		}
	}

	/**
	 * Returns the title resource id that will be used to create logger session. If 0 is returned (default) logger will not be used.
	 *
	 * @return the title resource id
	 */
	protected int getLoggerProfileTitle() {
		return 0;
	}

	/**
	 * This method may return the local log content provider authority if local log sessions are supported.
	 *
	 * @return local log session content provider URI
	 */
	protected Uri getLocalAuthorityLogger() {
		return null;
	}

	@Override
	public void onDeviceSelected(final BluetoothDevice device, final String name) {
		final int titleId = getLoggerProfileTitle();
		if (titleId > 0) {
			Log.e(TAG, "onDeviceSelected: "+getString(titleId)+" "+device.getAddress()+"  "+name );
		}
		mDeviceName = name;

		// The device may not be in the range but the service will try to connect to it if it reach it
		Log.e(TAG, "Creating service...");
		final Intent service = new Intent(this, getServiceClass());
		service.putExtra(BleConstant.EXTRA_DEVICE_ADDRESS, device.getAddress());
		startService(service);
		Log.e(TAG, "Binding to the service...");
		bindService(service, mServiceConnection, 0);
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}

	/**
	 * Called when the device has been connected. This does not mean that the application may start communication. A service discovery will be handled
	 * automatically after this call. Service discovery may ends up with calling {@link #onServicesDiscovered(boolean)} or {@link #onDeviceNotSupported()} if required
	 * services have not been found.
	 */
	public void onDeviceConnected() {
		setUIConnectStatus(BleConstant.STATE_CONNECTED);
	}

	/**
	 * Called when the device has disconnected (when the callback returned
	 * {@link BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} with state DISCONNECTED.
	 */
	public void onDeviceDisconnected() {

		try {
			Log.e(TAG, "Unbinding from the service...");
			unbindService(mServiceConnection);
			mService = null;

			Log.e(TAG, "Activity unbinded from the service");
			onServiceUnbinded();
			mDeviceName = null;
			setUIConnectStatus(BleConstant.STATE_DISCONNECTED);
		} catch (final IllegalArgumentException e) {
			// do nothing. This should never happen but does...
		}
	}
	public void onDeviceDisconnecting() {
		setUIConnectStatus(BleConstant.STATE_DISCONNECTING);
	}
	public void onDeviceConnecting() {
		setUIConnectStatus(BleConstant.STATE_CONNECTING);
	}

	/**
	 * Some profiles may use this method to notify user that the link was lost. You must call this method in your Ble Manager instead of
	 * {@link #onDeviceDisconnected()} while you discover disconnection not initiated by the user.
	 */
	public void onLinklossOccur() {
	}

	/**
	 * Called when service discovery has finished and primary services has been found. The device is ready to operate. This method is not called if the primary,
	 * mandatory services were not found during service discovery. For example in the Blood Pressure Monitor, a Blood Pressure service is a primary service and
	 * Intermediate Cuff Pressure service is a optional secondary service. Existence of battery service is not notified by this call.
	 *
	 * @param optionalServicesFound if <code>true</code> the secondary services were also found on the device.
	 */
	public abstract void onServicesDiscovered(final boolean optionalServicesFound);


	/**
	 * Called when the device has started bonding process
	 */
	public void onBondingRequired() {
		// empty default implementation
	}

	/**
	 * Called when the device has finished bonding process successfully
	 */
	public void onBonded() {
		// empty default implementation
	}

	/**
	 * Called when service discovery has finished but the main services were not found on the device. This may occur when connecting to bonded device that does
	 * not support required services.
	 */
	public void onDeviceNotSupported() {
		showToast(R.string.not_supported);
	}

	/**
	 * Called when battery value has been received from the device
	 *
	 * @param value the battery value in percent
	 */
	public void onBatteryValueReceived(final int value) {
	}

	/**
	 * Called when a BLE error has occurred
	 *
	 * @param message   the error message
	 * @param errorCode the error code
	 */
	public void onError(final String message, final int errorCode) {
		Log.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
		showToast(message + " (" + errorCode + ")");
	}

	/**
	 * Returns <code>true</code> if the device is connected. Services may not have been discovered yet.
	 */
	protected boolean isDeviceConnected() {
		return mService != null;
	}

	/**
	 * Returns the name of the device that the phone is currently connected to or was connected last time
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Restores the default UI before reconnecting
	 */
	protected abstract void setUIConnectStatus(int status);


	/**
	 * The UUID filter is used to filter out available devices that does not have such UUID in their advertisement packet. See also:
	 * {@link #isChangingConfigurations()}.
	 *
	 * @return the required UUID or <code>null</code>
	 */
	protected abstract UUID getFilterUUID();

	/**
	 * Shows the scanner fragment.
	 * filter the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
	 * services
	 * @see #getFilterUUID()
	 */
	private void showDeviceScanningDialog() {
		final ScannerFragment dialog = ScannerFragment.getInstance(getFilterUUID());
		dialog.show(getSupportFragmentManager(), ScannerFragment.TAG);
	}

	private void ensureBLESupported() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	protected boolean isBLEEnabled() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		return adapter != null && adapter.isEnabled();
	}

	protected void showBLEDialog() {
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
}
