package com.swarmy.swarmy;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import io.particle.android.sdk.accountsetup.LoginActivity;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.ui.BaseActivity;
import io.particle.android.sdk.utils.SoftAPConfigRemover;
import io.particle.android.sdk.utils.ui.Ui;

public class DeviceListActivity extends BaseActivity implements DeviceListFragment.Callbacks {

    private SoftAPConfigRemover softAPConfigRemover;
    private DeviceListFragment deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        softAPConfigRemover = new SoftAPConfigRemover(this);
        deviceList = Ui.findFrag(this, R.id.fragment_device_list);

        final ParticleCloud cloud = ParticleCloud.get(this);
        Ui.findView(this, R.id.action_log_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloud.logOut();
                startActivity(new Intent(DeviceListActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        softAPConfigRemover.removeAllSoftApConfigs();
        softAPConfigRemover.reenableWifiNetworks();
    }

    @Override
    public void onBackPressed() {
        if (deviceList == null || !deviceList.onBackPressed()) {
            super.onBackPressed();
        }
    }

    //region DeviceListFragment.Callbacks
    @Override
    public void onDeviceSelected(ParticleDevice device) {
        startActivity(MainActivity.buildIntent(this, device));
    }
    //endregion
}

