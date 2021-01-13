package com.example.cs4532.umdalive.fragments.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.cs4532.umdalive.ColorDiagram;
import com.example.cs4532.umdalive.GroupChatMessage;
import com.example.cs4532.umdalive.R;
import com.example.cs4532.umdalive.RestSingleton;
import com.example.cs4532.umdalive.UserSingleton;
import com.example.cs4532.umdalive.fragments.create.CreateEventFrag;
import com.example.cs4532.umdalive.fragments.edit.EditClubFrag;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;

public class ChatFrag extends Fragment {
    //View
    View view;

    //Layout Components
    private Button sendButton;
    private EditText editText;
    private ListView listView;
    private ImageButton removeMessage;

    private FirebaseListAdapter<GroupChatMessage> adapter;
    private ColorDiagram match = new ColorDiagram();

    private String userID;
    private JSONObject memberJSON;
    private JSONObject admins;

    /**
     * Creates the page whenever the club page is clicked on
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view The view of the club page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Create View
        view = inflater.inflate(R.layout.chat_activity, container, false);
        //view.setVisibility(View.GONE);
        //Get Layout Components
        //getLayoutComponents();




        Button sendButton = (Button) view.findViewById(R.id.sendButton);
        final Button eraseAllMessages = (Button) view.findViewById(R.id.eraseAllButton);

        eraseAllMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseAllMessages();
            }
        });
        eraseAllMessages.setVisibility(View.GONE);

        try {
            displayGroupChatMessages();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageInput = (EditText) view.findViewById(R.id.messageInput);

                FirebaseDatabase.getInstance().getReference().child(getArguments().getString("clubID")).push().setValue(new GroupChatMessage(messageInput.getText().toString(),
                        UserSingleton.getInstance().getName(), UserSingleton.getInstance().getProfileUrl(), "holder" )).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        /*
                            Pulling the unique key from the db and storing it in the model GroupChatMessage
                            so it can be referenced specifically and easily using the class's methods.
                        */
                        String thisKey = adapter.getRef(adapter.getCount() -1).getKey();
                        System.out.println("thisKey == " + thisKey);
                        FirebaseDatabase.getInstance().getReference().child(getArguments().getString("clubID")).child(thisKey).child("messageKey").setValue(thisKey);                        //adapter.getItem(adapter.getCount() -1 ).setMessageKey(thisKey);
                    }
                });

                messageInput.setText("");
            }
        });

        //updateUI();

        RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, restSingleton.getUrl() + "getClub/" + getArguments().getString("clubID"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            updateUI(new JSONObject(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error connecting", String.valueOf(error));
            }
        });
        restSingleton.addToRequestQueue(stringRequest);

        try {
            displayGroupChatMessages();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Return View
        return view;
    }

    private void displayGroupChatMessages() throws JSONException{

        ListView displayOfAllMessages = (ListView)view.findViewById(R.id.chatDisplayBox);

        adapter = new FirebaseListAdapter<GroupChatMessage>(getActivity(), GroupChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child(getArguments().getString("clubID")).getRef()) {
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
                messageUser.setTextColor(match.matchColor(model.getMessageUser()));
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                final ImageButton removeMessageButton = (ImageButton) v.findViewById(R.id.remove_message);
                removeMessage = (ImageButton) v.findViewById(R.id.remove_message);

                removeMessage.setVisibility(View.INVISIBLE);

                String userName = model.getMessageUser();



                if (userName.equals(getArguments().getString("userName"))) {
                    removeMessage.setVisibility(View.VISIBLE);
                }


                try {
                    if (admins != null && admins.getString("userID").equals(userID))
                        removeMessage.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                removeMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                            Remove message by using the message's unique key to reference it as a child
                            of the chat block.
                        */
                        System.out.println("remove key value: " + messageKeyValue.getText().toString());
                        FirebaseDatabase.getInstance().getReference().child(getArguments().getString("clubID")).child(messageKeyValue.getText().toString()).getRef().removeValue();
                    }
                });



                //insert picture

                Picasso.Builder builder = new Picasso.Builder(mActivity.getApplicationContext());

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


    /**
     * Gets the layout components from club_layout.xml
     * @return nothing
     */
    /*private void getLayoutComponents() {

        sendButton = (Button) view.findViewById(R.id.sendButton);
        editText = (EditText) view.findViewById(R.id.messageInput);
        listView = (ListView) view.findViewById(R.id.chatDisplayBox);
    }
*/

    private void updateUI(JSONObject res) throws JSONException{
        view.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.PageLoading).setVisibility(View.GONE);

        userID = UserSingleton.getInstance().getUserID();
        ImageButton removeMessageButton = view.findViewById(R.id.remove_message);

        memberJSON = res.getJSONObject("members");

        JSONArray regulars = memberJSON.getJSONArray("regular");
        admins = memberJSON.getJSONObject("admin");


        if (admins.getString("userID").equals(userID)) {
            Button eraseButton = view.findViewById(R.id.eraseAllButton);
            eraseButton.setVisibility(View.VISIBLE);

            if (removeMessage != null)
                removeMessage.setVisibility(View.VISIBLE);



        }


        /*String userName = UserSingleton.getInstance().getName();



            if (userName.equals(getArguments().getString("userName"))) {
                //removeMessageButton.setVisibility(View.VISIBLE);
            }

         */
    }

    public void eraseAllMessages () {
        FirebaseDatabase.getInstance().getReference().child(getArguments().getString("clubID")).removeValue();
    }


}
