package com.diy.blelib.profile.bleutils;

import java.util.UUID;

/**
 * @version V1.0 <UUID>
 * @author: Xs
 * @date: 2016-08-13 10:06
 * @email Xs.lin@foxmail.com
 */
public class BleUUID {

    /**
     * 温度服务
     */
    public static final UUID TP_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
    public static final UUID TP_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");

    /**
     * 心率服务
     */
    public static final UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    public static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    public static final UUID HR_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * 电池服务
     */
    public static final  UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public static final  UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    /**
     * 串口服务
     */
    //串口写特征值
    public static final UUID W_RX_CHAR_UUID = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
    //串口读特征值
    public static final UUID R_TX_CHAR_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");

}
