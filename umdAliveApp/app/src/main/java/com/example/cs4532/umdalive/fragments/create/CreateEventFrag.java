package com.example.cs4532.umdalive.fragments.create;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.cs4532.umdalive.R;
import com.example.cs4532.umdalive.RestSingleton;
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

public class CreateEventFrag  extends Fragment implements View.OnClickListener {
    //View
    View view;

    //Layout Components
    private EditText EventName;
    private EditText EventDescription;
    private EditText EventTime;
    private EditText EventDate;
    private Button CreateEventButton;
    private JSONObject clubData;

    /**
     * Creates the page for Editing Events when the edit events button is pressed
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view The View of the create events page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Create View
        view = inflater.inflate(R.layout.create_event_layout, container, false);

        //Set Event Data
        try {
            clubData = new JSONObject();
            clubData.put("clubID", getArguments().getString("clubID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get Layout Components
        getLayoutComponents();

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
        JSONObject newEventData = new JSONObject();
        try {
            newEventData.put("name", EventName.getText());
            newEventData.put("description", EventDescription.getText());
            newEventData.put("time",EventTime.getText());
            newEventData.put("date", EventDate.getText());
            newEventData.put("club",clubData.getString("clubID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, RestSingleton.getInstance(view.getContext()).getUrl() + "createEvent", newEventData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            EventFrag frag = new EventFrag();
                            Bundle data = new Bundle();
                            data.putString("eventID", response.getString("eventID"));
                            data.putString("clubID", clubData.getString("clubID"));
                            frag.setArguments(data);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
                        } catch (JSONException e){
                            Log.d("Error getting eventID", String.valueOf(e));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error connecting", String.valueOf(error));
            }
        });

        RestSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Gets the layout components from edit_event_layout.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        EventName = view.findViewById(R.id.EventName);
        EventDescription = view.findViewById(R.id.EventDescription);
        EventTime = view.findViewById(R.id.EventTime);
        EventDate = view.findViewById(R.id.EventDate);
        CreateEventButton = view.findViewById(R.id.CreateEvent);
        CreateEventButton.setOnClickListener(this);
    }

}
