package com.example.cs4532.umdalive;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;



public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter<GroupChatMessage> adapter;
    private ColorDiagram match = new ColorDiagram();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        displayGroupChatMessages();

        Button sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageInput = (EditText) findViewById(R.id.messageInput);

                String key = String.valueOf ((Math.random()*((1000000-1)+1))+1);

                FirebaseDatabase.getInstance().getReference().child("messages").push().setValue(new GroupChatMessage(messageInput.getText().toString(),
                        UserSingleton.getInstance().getName(), UserSingleton.getInstance().getProfileUrl(), "holder" ));

                adapter.getItem(adapter.getCount() -1 ).setMessageKey(adapter.getRef(adapter.getCount() -1 ).getKey());
                //adapter.getItem(adapter.getCount() -1 ).getMessageKey();

                messageInput.setText("");
            }
        });


    }

    private void displayGroupChatMessages() {

        ListView displayOfAllMessages = (ListView)findViewById(R.id.chatDisplayBox);

        adapter = new FirebaseListAdapter<GroupChatMessage>(this, GroupChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("messages").getRef()) {
            @Override
            protected void populateView(View v, final GroupChatMessage model, int position) {
                final TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                final TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                ImageView messageImage = (ImageView)v.findViewById(R.id.message_image);
                final TextView messageKeyValue = (TextView)v.findViewById(R.id.message_key);

                messageText.setText(model.getMessageText());
                messageKeyValue.setText(String.valueOf(model.getMessageKey()));
                System.out.println("model message key = " + model.getMessageKey());
                messageUser.setText(model.getMessageUser());
                messageUser.setTextColor(ColorDiagram.getColor());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                final ImageButton removeMessageButton = (ImageButton) v.findViewById(R.id.remove_message);


                // Set listener on delete button and handle logic of indexes to ensure access
                // and deletion of objects in the realtime db is done within bounds.

                removeMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("key value: " + messageKeyValue.getText().toString());
                        FirebaseDatabase.getInstance().getReference().orderByKey().equalTo(messageKeyValue.getText().toString()).getRef().removeValue();
//                        if (model.getMessageKey() != adapter.getCount() && model.getMessageKey() < adapter.getCount()) {
//                            adapter.getRef(model.getMessageKey()).removeValue();
//                        }
//                        else if (model.getMessageKey() -1 >= adapter.getCount()) {
//                            adapter.getRef(adapter.getCount() -1).removeValue();
//                        }
//                        else if (adapter.getCount() == 1) {
//                            adapter.getRef(0).removeValue();
//                        }
//                        else {
//                            adapter.getRef(model.getMessageKey() - 1).removeValue();
//                        }
                    }
                });

                    messageUser.setTextColor(match.matchColor(model.getMessageUser()));
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));

                //insert picture

                Picasso.Builder builder = new Picasso.Builder(getApplicationContext());

                if(model.getMessageImage() != null) {
                        builder.build()
                                .load(model.getMessageImage())
//                                .placeholder(R.drawable.ic_menu_profile)
                                .resize(30, 30)
                                .centerCrop()
                                .into(messageImage);

                } else {        //model.getMessageImage() == null
                    builder.build()
                            .load("null")
                                .placeholder(R.drawable.ic_menu_profile)
                            .resize(30, 30)
                            .centerCrop()
                            .into(messageImage);

                }

            }
        };

        displayOfAllMessages.setAdapter(adapter);
    }

    public ChatActivity() {

    }

}

