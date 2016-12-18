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

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;

public class DeviceActivity extends AbstractBlocklyActivity {
    private static final String TAG = "DeviceActivity";
    public static final String ARG_DEVICE = "ARG_DEVICE";
    private static final String STATE_DEVICE = "STATE_DEVICE";

    private static final List<String> ARDUINO_GENERATORS = Arrays.asList(
            "swarmy/generators/io.js",
            "swarmy/generators/events.js"
    );

    private final Handler mHandler = new Handler();
    private final CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    Log.i(TAG, "generatedCode:\n" + generatedCode);
                    Map<String, String> valuesMap = new HashMap<>();
                    valuesMap.put("myName", device.getName());
                    StrSubstitutor sub = new StrSubstitutor(valuesMap);
                    final String resolvedCode = sub.replace(generatedCode);
                    Log.i(TAG, "resolvedCode:\n" + resolvedCode);
                    Toast.makeText(getApplicationContext(), resolvedCode,
                            Toast.LENGTH_LONG).show();
                    Async.executeAsync(device, new Async.ApiWork<ParticleDevice, Boolean>() {
                        @Override
                        public Boolean callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                            particleDevice.flashCodeFile(new ByteArrayInputStream(resolvedCode.getBytes(StandardCharsets.UTF_8)));
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
        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(STATE_DEVICE);
        } else {
            device = getIntent().getParcelableExtra(ARG_DEVICE);
        }
        ParticleCloudSDK.init(this.getApplicationContext());
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DEVICE, device);
    }

    public static Intent buildIntent(Context ctx, ParticleDevice device) {
        return new Intent(ctx, DeviceActivity.class)
                .putExtra(DeviceActivity.ARG_DEVICE, device);
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return "swarmy/toolbox_full.xml";
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
                "swarmy/io.json",
                "swarmy/events.json"
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
