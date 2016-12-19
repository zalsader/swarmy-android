package com.swarmy.swarmy;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Async;

public class ChatActivity extends AppCompatActivity implements ParticleEventHandler{
    private static final String TAG = "ChatActivity";

    private ListView mListView;
    private Button mButtonSend;
    private EditText mEditTextMessage;


    private ChatMessageAdapter mAdapter;
    private long eventSubscriptionId;

    public static Intent buildIntent(Context ctx) {
        return new Intent(ctx, ChatActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (Button) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);

        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);


        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendMessage(message);
                mEditTextMessage.setText("");
            }
        });
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Long>() {

            @Override
            public Long callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                return particleCloud.subscribeToMyDevicesEvents(
                        "say", ChatActivity.this);
            }

            @Override
            public void onSuccess(Long subscriptionId) {
                eventSubscriptionId = subscriptionId;
                Log.i(TAG, "SubscriptionId: " + subscriptionId);
            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }
        });
    }

    private void sendMessage(final String message) {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Boolean>() {

            @Override
            public Boolean callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.publishEvent("say/" + message, "me;",
                        ParticleEventVisibility.PRIVATE, 60);
                return true;
            }

            @Override
            public void onSuccess(Boolean success) {
                Log.i(TAG, "Sent message from me" + message);
            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }
        });
        mAdapter.add(new ChatMessage(message, "me", ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Boolean>() {

            @Override
            public Boolean callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.unsubscribeFromEventWithID(eventSubscriptionId);
                return true;
            }

            @Override
            public void onSuccess(Boolean success) {
                Log.i(TAG, "Unsubscribed from " + eventSubscriptionId);
            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }
        });
    }

    @Override
    public void onEvent(String eventName, ParticleEvent event) {
        int nameIndex = event.dataPayload.indexOf(';');
        String senderName = event.dataPayload.substring(0, nameIndex);
        String data = event.dataPayload.substring(nameIndex + 1);
        ChatMessage chatMessage = new ChatMessage(eventName.substring(4), senderName, data);
        if (!chatMessage.isMine()) {
            mAdapter.add(chatMessage);
        }
        Log.i(TAG, "Received event " + eventName + " with payload: " + event.dataPayload);
    }

    @Override
    public void onEventError(Exception e) {
        Log.e(TAG, "Event error: ", e);
    }
}
