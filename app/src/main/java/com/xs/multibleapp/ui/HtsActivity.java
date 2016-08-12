package com.xs.multibleapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.diy.blelib.ble.hts.HtsService;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.profile.BleProfileServiceReadyActivity;
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
    protected Class<? extends BleProfileService> getServiceClass() {
        return HtsService.class;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.hts_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(getIntent().getStringExtra(MainViewActivity.TITLE));
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

    @Override
    protected void onServiceBinded(HtsService.RSCBinder binder) {
        // no use
    }

    @Override
    protected void onServiceUnbinded() {
        // no use
    }

}
