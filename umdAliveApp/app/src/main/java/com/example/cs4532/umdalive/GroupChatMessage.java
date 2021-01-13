package com.example.cs4532.umdalive;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class GroupChatMessage {
    private String messageText;
    private String messageUser;
    private long messageTime;
    private String messageImage;
    private String messageKey;

    public GroupChatMessage(String messageText, String messageUser, String  messageImage, String key) {

        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageImage = messageImage;
        this.messageKey = key;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public GroupChatMessage(){
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageImage() {
        return messageImage;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey){ this.messageKey = messageKey; }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

}
