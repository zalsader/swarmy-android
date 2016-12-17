package com.swarmy.swarmy;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

public class AllDevicesActivity extends AbstractBlocklyActivity {
    private static final String TAG = "AllDevicesActivity";
    public static final String ARG_DEVICES = "ARG_DEVICES";
    private static final String STATE_DEVICES = "STATE_DEVICES";

    private static final List<String> ARDUINO_GENERATORS = Arrays.asList(
            "swarmy/generators/io.js"
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
                    for (final ParticleDevice device : devices) {
                        Async.executeAsync(device, new Async.ApiWork<ParticleDevice, Boolean>() {
                            @Override
                            public Boolean callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                                particleDevice.flashCodeFile(new ByteArrayInputStream(generatedCode.getBytes(StandardCharsets.UTF_8)));
                                return null;
                            }

                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Log.i(TAG, "Code successfully written to device:" + device);
                            }

                            @Override
                            public void onFailure(ParticleCloudException exception) {
                                Log.i(TAG, "Error writing to device:" + device);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            };

    private ArrayList<ParticleDevice> devices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            devices = savedInstanceState.getParcelableArrayList(STATE_DEVICES);
        } else {
            devices = getIntent().getParcelableArrayListExtra(ARG_DEVICES);
        }
        ParticleCloudSDK.init(this.getApplicationContext());
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_DEVICES, devices);
    }

    public static Intent buildIntent(Context ctx, ArrayList<ParticleDevice> devices) {
        return new Intent(ctx, AllDevicesActivity.class)
                .putParcelableArrayListExtra(AllDevicesActivity.ARG_DEVICES, devices);
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return "swarmy/toolbox_all_devices.xml";
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
                "arduino/base.json",
                "swarmy/io.json"
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
