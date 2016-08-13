package com.xs.multibleapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.diy.blelib.ble.hts.HtsService;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.profile.BleProfileServiceReadyActivity;
import com.diy.blelib.profile.bleutils.BleConstant;
import com.diy.blelib.profile.bleutils.BleUUID;
import com.xs.multibleapp.MainViewActivity;
import com.xs.multibleapp.R;

import java.util.UUID;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:29
 * @email Xs.lin@foxmail.com
 */
public class HtsActivity extends BleProfileServiceReadyActivity<HtsService.RSCBinder> {
    private static final String TAG = "HtsActivity";

    public static void runAct(Context context,String title) {
        Intent intent = new Intent(context,HtsActivity.class);
        intent.putExtra(MainViewActivity.TITLE,title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.hts_activity);
        Log.e(TAG, "onCreateView: " );
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getIntent().getStringExtra(MainViewActivity.TITLE));
        Log.e(TAG, "onResume: " );
    }


    /**
     * 指定 绑定服务
     * @return
     */
    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return HtsService.class;
    }

    /**
     * 发现设备服务
     * @param optionalServicesFound if <code>true</code> the secondary services were also found on the device.
     */
    @Override
    public void onServicesDiscovered(boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    /**
     * 根据连接状态更新UI
     * @param status
     */
    @Override
    protected void setUIConnectStatus(int status) {
        switch (status) {
            case BleConstant.STATE_CONNECTED:

                break;
            case BleConstant.STATE_DISCONNECTED:

                break;
        }
    }

    /**
     * 根据服务UUID过滤出可用设备
     * @return
     */
    @Override
    protected UUID getFilterUUID() {
        return BleUUID.TP_SERVICE_UUID;
    }


    /**
     * 根据服务连接状态更新UI等
     * @param binder
     */
    @Override
    protected void onServiceBinded(HtsService.RSCBinder binder) {
        // no use
    }

    @Override
    protected void onServiceUnbinded() {
        // no use
    }

}
