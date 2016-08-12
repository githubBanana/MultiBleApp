package com.diy.blelib.ble.sport;

import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.diy.blelib.profile.BleManager;
import com.diy.blelib.profile.BleProfileService;

public class SportService extends BleProfileService implements SportManagerCallbacks{
	private static final String TAG = SportService.class.getSimpleName();

	public static final String BROADCAST_HRS_MEASUREMENT = "com.diy.blelib.ble.BROADCAST_HRS_MEASUREMENT";
	public static final String EXTRA_HEART = "com.diy.blelib.ble.EXTRA_HEART";
	public static final String BROADCAST_UPGAMEARR = "com.diy.blelib.ble.BROADCAST_UPGAMEARR";


	private SportManager mManager;
	private boolean mBinded;

	private final LocalBinder mBinder = new RSCBinder();

	/**
	 * This local binder is an interface for the binded activity to operate with the HTS sensor
	 */
	public class RSCBinder extends LocalBinder {
		// empty
	}

	@Override
	protected LocalBinder getBinder() {
		return mBinder;
	}

	@Override
	protected BleManager<SportManagerCallbacks> initializeManager() {
		return mManager = SportManager.getHTSManager();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		mBinded = true;
		return super.onBind(intent);
	}

	@Override
	public void onRebind(final Intent intent) {
		mBinded = true;
		// read the battery level when back in the Activity
		/*if (isConnected())
			mManager.readBatteryLevel();*/
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		mBinded = false;
		return super.onUnbind(intent);
	}


	@Override
	public void onBagReceived(byte config) {
		final Intent broadcast = new Intent(BROADCAST_HRS_MEASUREMENT);
		broadcast.putExtra(EXTRA_HEART,config);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onUpGameArr() {
		final Intent broadcast = new Intent(BROADCAST_UPGAMEARR);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

}
