package com.example.cs4532.umdalive.fragments.base;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.cs4532.umdalive.R;
import com.example.cs4532.umdalive.RestSingleton;
import com.example.cs4532.umdalive.UserSingleton;
import com.example.cs4532.umdalive.fragments.edit.EditEventFrag;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Josh Senst
 *
 * 4/26/2018
 *
 * This class holds the page for the events
 */
public class EventFrag extends Fragment {

    //View
    View view;

    //Layout Components
    private TextView eventName;
    private TextView eventDescription;
    private TextView eventDate;
    private TextView eventTime;
    private Button goTo;
    private FloatingActionButton editEventFAB;

    //


    /**
     * Creates the event page when navigating to it
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view The view of the event page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Create View
        view = inflater.inflate(R.layout.event_layout, container, false);

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
     * Gets the layout components from event_layout.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        eventName=view.findViewById(R.id.EventNameView);
        eventDate=view.findViewById(R.id.EventDateView);
        eventDescription=view.findViewById(R.id.EventDescriptionView);
        eventTime=view.findViewById(R.id.EventTimeView);
        goTo=view.findViewById(R.id.GoToClub);
        goTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String TAG = (String) goTo.getTag();
                ClubFrag frag = new ClubFrag();
                Bundle data = new Bundle();
                data.putString("clubID", TAG);
                frag.setArguments(data);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            }
        });
        editEventFAB=view.findViewById(R.id.EditEventFAB);
        editEventFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String TAG = (String) eventName.getTag();
                EditEventFrag frag = new EditEventFrag();
                Bundle data = new Bundle();
                data.putString("eventID", TAG);
                frag.setArguments(data);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            }
        });

    }

    /**
     * Updates the UI of the Event page depending on which event had been clicked
     * @param res The response from the database
     * @throws JSONException Error in JSON processing
     * @see JSONException
     * @return nothing
     */
    private void updateUI(JSONObject res) throws JSONException{
        getActivity().findViewById(R.id.PageLoading).setVisibility(View.GONE);
        eventName.setText(res.getString("name"));
        eventName.setTag(res.getString("_id"));
        eventDate.setText(res.getString("date"));
        eventDescription.setText(res.getString("description"));
        eventTime.setText(res.getString("time"));
        goTo.setTag(res.getJSONObject("club").getString("_id").toString());
        if(UserSingleton.getInstance().getUserID()!=res.getJSONObject("club").getJSONObject("members").getString("admin")){
            editEventFAB.setVisibility(View.GONE);
        }
    }

}