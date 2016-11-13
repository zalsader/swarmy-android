package com.swarmy.swarmy;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AbstractBlocklyActivity {
    private static final String TAG = "MainActivity";

    private static final List<String> JAVASCRIPT_GENERATORS = Arrays.asList(
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
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return JAVASCRIPT_GENERATORS;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        return mCodeGeneratorCallback;
    }
}
