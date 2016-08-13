package com.diy.blelib.profile.bleutils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:14
 * @email Xs.lin@foxmail.com
 */
public class BleUtil {

    private final int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
    private final int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
    private final int SHIFT_LEFT_8BITS = 8;
    private final int SHIFT_LEFT_16BITS = 16;
    private final int GET_BIT24 = 0x00400000;
    private static final int FIRST_BIT_MASK = 0x01;

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


    /**
     * Enabling notification on Heart Rate Characteristic
     * @param mBluetoothGatt
     * @param mCharacteristic
     */
    public static void enableHRNotification(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic mCharacteristic) {
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(BleUUID.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Enabling notification on Uart characteristic
     * @param mBluetoothGatt
     * @param mCharacteristic
     */
    public static void enableUartReci(BluetoothGatt mBluetoothGatt,BluetoothGattCharacteristic mCharacteristic) {
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic,true);
        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(BleUUID.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * enable Health Thermometer indication on Health Thermometer Measurement characteristic
     * @param mBluetoothGatt
     * @param mCharacteristic
     */
    public static void enableTPIndication(BluetoothGatt mBluetoothGatt,BluetoothGattCharacteristic mCharacteristic) {
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(BleUUID.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }


}
