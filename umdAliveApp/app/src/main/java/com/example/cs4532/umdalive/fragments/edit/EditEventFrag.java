package com.example.cs4532.umdalive.fragments.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.cs4532.umdalive.R;
import com.example.cs4532.umdalive.RestSingleton;
import com.example.cs4532.umdalive.fragments.base.ClubFrag;
import com.example.cs4532.umdalive.fragments.base.EventFrag;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Josh on 4/25/2018.
 */

/**
 * @author Josh Senst
 *
 * 4/26/2018
 *
 * Class that allows for the editing of events on the edit events page
 */

public class EditEventFrag  extends Fragment implements View.OnClickListener {
    //View
    View view;

    //Layout Components
    private TextView EditingEvent;
    private EditText NewEventName;
    private EditText NewEventDescription;
    private EditText NewEventTime;
    private EditText NewEventDate;
    private Button SaveButton;
    private Button DeleteEvent;

    private JSONObject eventData;

    /**
     * Creates the page for Editing Events when the edit events button is pressed
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view The View of the edit events page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Create View
        view = inflater.inflate(R.layout.edit_event_layout, container, false);

        //Get Layout Components
        getLayoutComponents();

        //Use Volley Singleton to Update Page UI
        RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, restSingleton.getUrl() + "getEvent/" + getArguments().getString("eventID"),
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

        //Return View
        return view;
    }

    /**
     * Allows for the clicked to edit on the text box
     * @param v The textView clicked
     * @return nothing
     */
    @Override
    public void onClick(View v) {
        if(v.getTag()=="DELETE"){
            String clubid = new String();
            try {
                clubid =  eventData.getString("_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
            StringRequest stringRequest = null;
            
                stringRequest = new StringRequest(Request.Method.DELETE, restSingleton.getUrl() + "deleteEvent/" + clubid,
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
            ClubFrag frag = new ClubFrag();
            Bundle data = new Bundle();
            data.putString("clubID", clubid);
            frag.setArguments(data);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            
        }else {
            if (NewEventName.getText().toString().trim().length() != 0) {
                try {
                    eventData.put("name", NewEventName.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (NewEventDescription.getText().toString().trim().length() != 0) {
                try {
                    eventData.put("description", NewEventDescription.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (NewEventDate.getText().toString().trim().length() != 0) {
                try {
                    eventData.put("date", NewEventDate.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (NewEventTime.getText().toString().trim().length() != 0) {
                try {
                    eventData.put("time", NewEventTime.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, restSingleton.getUrl() + "editEvent/", eventData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error connecting", String.valueOf(error));
                }
            });
            restSingleton.addToRequestQueue(jsonObjectRequest);
            EventFrag frag = new EventFrag();
            Bundle data = new Bundle();
            data.putString("eventID", EditingEvent.getTag().toString());
            frag.setArguments(data);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
        }

    }

    /**
     * Gets the layout components from edit_event_layout.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        EditingEvent = view.findViewById(R.id.EventEditing);
        NewEventName = view.findViewById(R.id.EventName);
        NewEventDescription = view.findViewById(R.id.EventDescription);
        NewEventDate = view.findViewById(R.id.EventDate);
        NewEventTime = view.findViewById(R.id.EventTime);
        SaveButton = view.findViewById(R.id.SaveEvent);
        SaveButton.setOnClickListener(this);
        DeleteEvent.setOnClickListener(this);
        DeleteEvent.setTag("DELETE");
    }

    /**
     * Adds the textView boxes from the database into the page
     * @param res The response from the database
     * @throws JSONException Error in JSON processing
     * @see JSONException
     */
    private void updateUI(JSONObject res) throws JSONException{
        EditingEvent.setText("Editing Event:\n"+res.getString("name"));
        EditingEvent.setTag(res.getString("_id"));
        NewEventName.setText(res.getString("name"));
        NewEventDescription.setText(res.getString("description"));
        NewEventTime.setText(res.getString("time"));
        NewEventDate.setText(res.getString("date"));
        eventData = res;
    }
}
