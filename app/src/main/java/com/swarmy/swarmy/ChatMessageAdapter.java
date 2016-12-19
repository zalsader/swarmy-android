package com.swarmy.swarmy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
    }

    @Override
    public int getViewTypeCount() {
        // my message, other message
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);

        if (item.isMine()) return MY_MESSAGE;
        else return OTHER_MESSAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        ChatMessage message = getItem(position);
        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
        } else if (viewType == OTHER_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
            TextView speakerView = (TextView) convertView.findViewById(R.id.message_speaker);
            speakerView.setText(message.getSpeaker());
        }
        TextView textView = (TextView) convertView.findViewById(R.id.message_text);
        String text = message.getContent();
        if (!message.getData().isEmpty()) {
            text += "\nData: " + message.getData();
        }
        textView.setText(text);
        return convertView;
    }

    @Override
    public void add(ChatMessage object) {
        super.add(object);
        Log.i("ChatActivity", "Added Chat message " + object.getContent()  + object.getSpeaker());
    }
}
