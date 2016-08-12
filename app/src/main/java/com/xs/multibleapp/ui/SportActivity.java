package com.xs.multibleapp.ui;

import android.os.Bundle;

import com.diy.blelib.ble.sport.SportService;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.profile.BleProfileServiceReadyActivity;

import java.util.UUID;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:24
 * @email Xs.lin@foxmail.com
 */
public class SportActivity extends BleProfileServiceReadyActivity<SportService.RSCBinder> {
    private static final String TAG = "SportActivity";

    @Override
    protected void onServiceBinded(SportService.RSCBinder binder) {

    }

    @Override
    protected void onServiceUnbinded() {

    }

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return null;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {

    }

    @Override
    public void onServicesDiscovered(boolean optionalServicesFound) {

    }

    @Override
    protected void setDefaultUI() {

    }

    @Override
    protected UUID getFilterUUID() {
        return null;
    }
}
