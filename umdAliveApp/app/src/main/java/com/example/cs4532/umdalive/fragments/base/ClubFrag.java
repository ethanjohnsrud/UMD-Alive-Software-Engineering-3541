package com.example.cs4532.umdalive.fragments.base;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cs4532.umdalive.ColorDiagram;
import com.example.cs4532.umdalive.R;
import com.example.cs4532.umdalive.RestSingleton;
import com.example.cs4532.umdalive.UserSingleton;
import com.example.cs4532.umdalive.fragments.create.CreateEventFrag;
import com.example.cs4532.umdalive.fragments.edit.EditClubFrag;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Requires argument with key of clubID to be passed into it before it is added to the frame layout
 */

/**
 * @author Josh Senst
 *
 * 4/26/2018
 *
 * Class that creates the club page
 */
public class ClubFrag extends Fragment {

    //View
    View view;

    //Layout Components
    private ImageView clubImage;
    private TextView clubName;

    private Button joinLeave;
    private TextView clubDescription;

    private LinearLayout members;
    private LinearLayout eventsList;

    private FloatingActionButton editClub;
    private FloatingActionButton addEvent;

    private JSONObject joinLeaveObj;

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
        view = inflater.inflate(R.layout.club_layout, container, false);
        view.setVisibility(View.GONE);

        joinLeaveObj = new JSONObject();

        try {
            joinLeaveObj.put("clubID", getArguments().getString("clubID"));
            joinLeaveObj.put("userID", UserSingleton.getInstance().getUserID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get Layout Components
        getLayoutComponents();

        //On Click
        editClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditClubFrag frag = new EditClubFrag();
                Bundle data = new Bundle();
                data.putString("clubID", clubName.getTag().toString());
                frag.setArguments(data);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            }
        });

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateEventFrag frag = new CreateEventFrag();
                Bundle data = new Bundle();
                data.putString("clubID", clubName.getTag().toString());
                frag.setArguments(data);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            }
        });

        //Use Volley Singleton to Update Page UI
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

        //Return View
        return view;
    }


    /**
     * Gets the layout components from club_layout.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        clubImage = view.findViewById(R.id.clubImage);
        clubName = (TextView) view.findViewById(R.id.ClubNameView);
        clubDescription = (TextView) view.findViewById(R.id.DescriptionView);
        joinLeave= (Button) view.findViewById(R.id.ClubJoinLeave);
        members = (LinearLayout) view.findViewById(R.id.memberList);
        eventsList = (LinearLayout) view.findViewById(R.id.eventsList);
        editClub = (FloatingActionButton) view.findViewById(R.id.EditClub);
        addEvent = (FloatingActionButton) view.findViewById(R.id.AddEvent);
    }

    /**
     * {"name":"",
     * "description":"",
     * "members":{
     * "admin":{"name":"","userID":""},
     * "regular":[]
     * },
     * "events":[]}
     */

    /**
     * Adds the club information to the page depending on which club was clicked. Inside there are several onClicks relevant to different items in lists being
     * clicked within the club layout.
     * @param res The response from the database
     * @throws JSONException Error in JSON processing
     * @see JSONException
     */
    private void updateUI(JSONObject res) throws JSONException{
        view.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.PageLoading).setVisibility(View.GONE);

        clubName.setText(res.getString("name"));
        clubName.setTag(res.getString("_id"));

        if (UserSingleton.getInstance().getProfileUrl() != null) {
            Glide.with(this)
                    .load(res.getString("profilePic"))
                    .into(clubImage);
        }

        clubDescription.setText(res.getString("description"));

        JSONObject memberJson = res.getJSONObject("members");

        JSONArray regulars = memberJson.getJSONArray("regular");
        final JSONObject admins = memberJson.getJSONObject("admin");

        JSONArray events = res.getJSONArray("events");

        String userID = UserSingleton.getInstance().getUserID();


        joinLeave.setText("Join CLub");
        joinLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinClub();
            }
        });

            for (int i = 0; i < events.length(); i++) {
                final JSONObject event = events.getJSONObject(i);

                String name = event.getString("name");
                String day = event.getString("date");

                TextView eventText = new TextView(view.getContext());

                eventText.setText(name + " : " +day);
                eventText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                eventText.setTextColor(ColorDiagram.getColor());
                eventText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventFrag frag = new EventFrag();
                        Bundle data = new Bundle();
                        try {
                            data.putString("eventID", event.get("_id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        frag.setArguments(data);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
                    }       });

                eventsList.addView(eventText);
            }



        if(admins.getString("userID").equals(userID)){
            editClub.setVisibility(View.VISIBLE);
            addEvent.setVisibility(view.VISIBLE);
            joinLeave.setVisibility(View.GONE);
        }

        TextView adminText = new TextView(view.getContext());
        adminText.setText(admins.get("name").toString() + " : ADMIN");
        adminText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        adminText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFrag frag = new ProfileFrag();
                Bundle data = new Bundle();
                try {
                    data.putString("userID", admins.get("userID").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                frag.setArguments(data);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
            }
        });
        members.addView(adminText);

        for (int i=0;i<regulars.length();i++){
            final JSONObject member = regulars.getJSONObject(i);
            TextView memberText = new TextView(view.getContext());
            memberText.setText(member.get("name").toString());
            memberText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            memberText.setTextColor(ColorDiagram.getColor());
            System.out.println(member.get("userID").toString() +" : "+ userID);
            if(member.get("userID").toString().equals(userID)){
                joinLeave.setText("Leave Club");
                joinLeave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveClub();
                    }
                });
            }

            memberText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFrag frag = new ProfileFrag();
                    Bundle data = new Bundle();
                    try {
                        data.putString("userID", member.get("userID").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    frag.setArguments(data);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
                }
            });
            members.addView(memberText);
        }
    }

    private void joinClub() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, RestSingleton.getInstance(view.getContext()).getUrl() + "joinClub", joinLeaveObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        joinLeave.setText("Leave Club");
                        joinLeave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                leaveClub();
                            }
                        });

                        TextView userText = new TextView(view.getContext());
                        userText.setText(UserSingleton.getInstance().getName());
                        userText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                        members.addView(userText);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error connecting", String.valueOf(error));
            }
        });

        RestSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void leaveClub() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, RestSingleton.getInstance(view.getContext()).getUrl() + "leaveClub", joinLeaveObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ProfileFrag frag = new ProfileFrag();
                        Bundle data = new Bundle();
                        data.putString("userID", UserSingleton.getInstance().getUserID());
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