package com.xs.multibleapp.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

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

    private TextView mTvHtsShow;
    public static void runAct(Context context,String title) {
        Intent intent = new Intent(context,HtsActivity.class);
        intent.putExtra(MainViewActivity.TITLE,title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.hts_activity);
        LocalBroadcastManager.getInstance(this).registerReceiver(_receiver,new IntentFilter(HtsService.BROADCAST_HTS_MEASUREMENT));
        mTvHtsShow = (TextView) findViewById(R.id.tv_hts_show);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getIntent().getStringExtra(MainViewActivity.TITLE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_receiver);
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
                mTvHtsShow.setText("已连");
                break;
            case BleConstant.STATE_DISCONNECTED:
                mTvHtsShow.setText("断连");
                break;
        }
    }

    /**
     * 根据服务UUID过滤出可用设备
     * @return
     */
    @Override
    protected UUID[] getFilterUUID() {
        UUID[] uuids = new UUID[]{BleUUID.TP_SERVICE_UUID,BleUUID.BATTERY_SERVICE,BleUUID.R_TX_CHAR_UUID,BleUUID.W_RX_CHAR_UUID,BleUUID.HR_SERVICE_UUID};
        return uuids;
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

    private BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HtsService.BROADCAST_HTS_MEASUREMENT.equals(intent.getAction())) {
                final float value = intent.getFloatExtra(HtsService.EXTRA_HTS,0.0f);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvHtsShow.setText(String.valueOf(value)+"℃");
                    }
                });
            }
        }
    };
}
