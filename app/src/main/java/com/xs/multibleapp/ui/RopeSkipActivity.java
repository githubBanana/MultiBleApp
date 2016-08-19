package com.xs.multibleapp.ui;

import android.os.Bundle;

import com.diy.blelib.ble.ropeskip.RopeSkipService;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.profile.BleProfileServiceReadyActivity;
import com.diy.blelib.profile.bleutils.BleUUID;

import java.util.UUID;

/**
 * @version V1.0 <跳绳>
 * @author: Xs
 * @date: 2016-08-12 15:13
 * @email Xs.lin@foxmail.com
 */
public class RopeSkipActivity extends BleProfileServiceReadyActivity<RopeSkipService.RSCBinder> {
    private static final String TAG = "RopeSkipActivity";

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return RopeSkipService.class;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {

    }


    @Override
    public void onServicesDiscovered(boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    protected void setUIConnectStatus(int status) {

    }

    @Override
    protected UUID[] getFilterUUID() {
        UUID[] uuids = new UUID[]{BleUUID.TP_SERVICE_UUID};
        return uuids;
    }

    @Override
    protected void onServiceBinded(RopeSkipService.RSCBinder binder) {
        // no use
    }

    @Override
    protected void onServiceUnbinded() {
        // no use
    }

}
