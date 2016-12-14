package com.swarmy.swarmy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.ui.BaseActivity;
import io.particle.android.sdk.ui.NextActivitySelector;
import io.particle.android.sdk.utils.EZ;
import io.particle.android.sdk.utils.TLog;


/**
 * More than just a way to display a splash screen; this also wraps
 * FirstRealActivitySelector for you, and launches the appropriate activity
 * from there.
 */
public class SplashActivity extends BaseActivity {

    private static final TLog log = TLog.get(SplashActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(this);
        ParticleDeviceSetupLibrary.init(this.getApplicationContext(), DeviceListActivity.class);
        onShowingSplashComplete();
    }

    private void onShowingSplashComplete() {
        if (isFinishing()) {
            log.i("Activity is already finished/finishing, not launching next Activity");

        } else {
            Intent intent;
            if (SDKGlobals.getAppDataStorage().getUserHasClaimedDevices()) {
                intent = NextActivitySelector.getNextActivityIntent(this,
                        ParticleCloudSDK.getCloud(),
                        SDKGlobals.getSensitiveDataStorage(), null);
            } else {
                intent = new Intent(this, IntroActivity.class);
            }

            log.d("Splash screen done, moving to next Activity: " + intent);
            startActivity(intent);
            finish();
        }
    }

}
