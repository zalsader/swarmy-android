package io.particle.android.sdk.devicesetup.setupsteps;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.utils.EZ;


public class WaitForCloudConnectivityStep extends SetupStep {

    private static final int MAX_RETRIES_REACHABILITY = 1;


    private final ParticleCloud cloud;
    private final Context ctx;

    public WaitForCloudConnectivityStep(StepConfig stepConfig, ParticleCloud cloud, Context ctx) {
        super(stepConfig);
        this.cloud = cloud;
        this.ctx = ctx;
    }

    @Override
    protected void onRunStep() throws SetupStepException {
        // Wait for just a couple seconds for a WiFi connection if possible, in case we
        // flip from the soft AP, to mobile data, and then to WiFi in rapid succession.
        EZ.threadSleep(2000);
        int reachabilityRetries = 0;
        boolean isAPIHostReachable = checkIsApiHostAvailable();
        while (!isAPIHostReachable && reachabilityRetries <= MAX_RETRIES_REACHABILITY) {
            EZ.threadSleep(2000);
            isAPIHostReachable = checkIsApiHostAvailable();
            log.d("Checked for reachability " + reachabilityRetries + " times");
            reachabilityRetries++;
        }
        if (!isAPIHostReachable) {
            throw new SetupStepException("Unable to reach API host");
        }
    }

    @Override
    public boolean isStepFulfilled() {
        return checkIsApiHostAvailable();
    }

    private boolean checkIsApiHostAvailable() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return false;
        }

//        try {
//            cloud.getDevices();
//        } catch (Exception e) {
//            log.e("error checking availability: ", e);
//            // FIXME:
//            return false;
//            // At this stage we're technically OK with other types of errors
//            if (set(Kind.NETWORK, Kind.UNEXPECTED).contains(e.getKind())) {
//                return false;
//            }
//        }

        return true;
    }

}
