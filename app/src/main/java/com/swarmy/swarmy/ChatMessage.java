package com.swarmy.swarmy;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable{
    private String speaker;
    private String content;
    private String data;

    public ChatMessage(String message, String speaker, String data) {
        this.content = message;
        this.speaker = speaker;
        this.data = data;
    }

    public ChatMessage(Parcel in) {
        this.speaker = in.readString();
        this.content = in.readString();
        this.data = in.readString();
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel source) {
            return new ChatMessage(source);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public String getContent() {
        return content;
    }

    public boolean isMine() {
        return this.speaker.equals("me");
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(speaker);
        dest.writeString(content);
        dest.writeString(data);
    }
}