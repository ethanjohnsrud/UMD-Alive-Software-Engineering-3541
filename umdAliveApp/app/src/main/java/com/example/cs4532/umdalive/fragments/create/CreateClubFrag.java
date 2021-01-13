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
import com.example.cs4532.umdalive.UserSingleton;
import com.example.cs4532.umdalive.fragments.base.ClubFrag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class that creates the page for creating a club
 *
 * @author Josh Senst
 *
 * 4/26/2018
 *
 */
public class CreateClubFrag extends Fragment {
    View view;

    private EditText ClubImg;
    private EditText ClubName;
    private EditText ClubDescription;
    private Button save;

    /**
     * Creates the create club page view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return The view of the create club page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Create View
        view = inflater.inflate(R.layout.create_club_layout, container, false);

        getLayoutComponents();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClubName.getText().length() > 0 && ClubDescription.getText().length() > 0) {
                    try {
                        createClub();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return view;
    }

    /**
     * Gets the layout componenets from create_club_layout.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        ClubName = view.findViewById(R.id.createClubName);
        ClubImg = view.findViewById(R.id.createClubImg);
        ClubDescription = view.findViewById(R.id.createClubDes);
        save = view.findViewById(R.id.createClubSave);
    }

    /**
     * Called whenever a user wishes to create a club, and makes the that user and admin
     * @throws JSONException Error in JSON processing
     * @see JSONException
     * @return nothing
     */
    private void createClub() throws JSONException {
        JSONObject members = new JSONObject();
        JSONArray regulars = new JSONArray();
        members.put("admin",UserSingleton.getInstance().getUserID());
        members.put("regular",regulars);

        JSONArray events = new JSONArray();

        JSONObject newClubData = new JSONObject();

        newClubData.put("name", ClubName.getText());
        newClubData.put("description", ClubDescription.getText());



        if(ClubImg.getText().length()>0){
            newClubData.put("profilePic", ClubImg.getText());
        } else{
            newClubData.put("profilePic", null);
        }

        newClubData.put("events", events);
        newClubData.put("members", members);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, RestSingleton.getInstance(view.getContext()).getUrl() + "createClub", newClubData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ClubFrag frag = new ClubFrag();
                        Bundle data = new Bundle();
                        try {
                            data.putString("clubID", response.get("_id").toString());
                            System.out.println(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        frag.setArguments(data);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error connecting", String.valueOf(error));
            }
        });

        RestSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
}
