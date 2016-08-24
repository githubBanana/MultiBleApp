package com.diy.blelib.profile.bleutils;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-13 09:50
 * @email Xs.lin@foxmail.com
 */
public class BleConstant {

    public static final String BROADCAST_CONNECTION_STATE = "com.diy.blelib.BROADCAST_CONNECTION_STATE";
    public static final String BROADCAST_SERVICES_DISCOVERED = "com.diy.blelib.BROADCAST_SERVICES_DISCOVERED";
    public static final String BROADCAST_BOND_STATE = "com.diy.blelib.BROADCAST_BOND_STATE";
    public static final String BROADCAST_BATTERY_LEVEL = "com.diy.blelib.BROADCAST_BATTERY_LEVEL";
    public static final String BROADCAST_ERROR = "com.diy.blelib.BROADCAST_ERROR";

    /** The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to */
    public static final String EXTRA_DEVICE_ADDRESS = "com.diy.blelib.EXTRA_DEVICE_ADDRESS";
    /** The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}. */
    public static final String EXTRA_DEVICE_NAME = "com.diy.blelib.EXTRA_DEVICE_NAME";
    public static final String EXTRA_CONNECTION_STATE = "com.diy.blelib.EXTRA_CONNECTION_STATE";
    public static final String EXTRA_BOND_STATE = "com.diy.blelib.EXTRA_BOND_STATE";
    public static final String EXTRA_SERVICE_PRIMARY = "com.diy.blelib.EXTRA_SERVICE_PRIMARY";
    public static final String EXTRA_SERVICE_SECONDARY = "com.diy.blelib.EXTRA_SERVICE_SECONDARY";
    public static final String EXTRA_BATTERY_LEVEL = "com.diy.blelib.EXTRA_BATTERY_LEVEL";
    public static final String EXTRA_ERROR_MESSAGE = "com.diy.blelib.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_ERROR_CODE = "com.diy.blelib.EXTRA_ERROR_CODE";
    public static final String EXTRA_DESTORY_SERVICE = "com.diy.blelib.EXTRA_DESTORY_SERVICE";

    public static final int STATE_LINK_LOSS = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTING = 3;


}
