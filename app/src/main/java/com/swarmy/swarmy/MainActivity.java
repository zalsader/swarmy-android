package com.swarmy.swarmy;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.devicesetup.SetupCompleteIntentBuilder;
import io.particle.android.sdk.devicesetup.SetupResult;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AbstractBlocklyActivity {
    private static final String TAG = "MainActivity";

    private static final List<String> ARDUINO_GENERATORS = Arrays.asList(
    );

    private final Handler mHandler = new Handler();
    private final CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {

                    // Sample callback.
                    Log.i(TAG, "generatedCode:\n" + generatedCode);
                    Toast.makeText(getApplicationContext(), generatedCode,
                            Toast.LENGTH_LONG).show();
                    Async.executeAsync(device, new Async.ApiWork<ParticleDevice, Boolean>() {
                        @Override
                        public Boolean callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                            particleDevice.flashCodeFile(new ByteArrayInputStream(generatedCode.getBytes(StandardCharsets.UTF_8)));
                            return null;
                        }

                        @Override
                        public void onSuccess(Boolean aBoolean) {

                        }

                        @Override
                        public void onFailure(ParticleCloudException exception) {
                            Log.i(TAG, "generatedCode:\n" + generatedCode);
                        }
                    });
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            };

    private ParticleDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(this.getApplicationContext());
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, ParticleDevice>() {
            @Override
            public ParticleDevice callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();
                if (!devices.isEmpty()) {
                    return devices.get(0);
                }
                return null;
            }

            @Override
            public void onSuccess(ParticleDevice particleDevice) {
                device = particleDevice;
            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }
        });
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return "default/toolbox.xml";
    }

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        return Arrays.asList(
                "default/list_blocks.json",
                "default/logic_blocks.json",
                "default/loop_blocks.json",
                "default/math_blocks.json",
                "default/text_blocks.json",
                "default/variable_blocks.json",
                "default/colour_blocks.json",
                "arduino/base.json"
        );
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        return ARDUINO_GENERATORS;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCodeGeneratorCallback;
    }
}
