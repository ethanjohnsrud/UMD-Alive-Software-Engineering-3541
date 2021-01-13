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
import com.example.cs4532.umdalive.fragments.base.AllClubsFrag;
import com.example.cs4532.umdalive.fragments.base.ClubFrag;

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
 * Class that creates the Edit Club Page
 */
public class EditClubFrag extends Fragment implements View.OnClickListener {
    //View
    View view;

    //Layout Components
    private TextView EditingClub;
    private EditText NewClubName;
    private EditText NewClubDescription;
    private EditText NewImageURL;
    private Button SaveButton;
    private Button DeleteButton;

    private JSONObject clubData;

    /**
     * Creates the page whenever the button for editing a club is pressed
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view The view of the edit club page
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Create View
        view = inflater.inflate(R.layout.edit_club_layou, container, false);

        //Get Layout Components
        getLayoutComponents();

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
     * Allows the clicking of the editText boxes
     * @param v The text editing area that has been clicked
     * @return nothing
     */
    @Override
    public void onClick(View v) {
        if (v.getTag().toString() == "DELETE") {
            RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
            StringRequest stringRequest = null;
            try {
                stringRequest = new StringRequest(Request.Method.DELETE, restSingleton.getUrl() + "deleteClub/" + clubData.getString("_id"),
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            restSingleton.addToRequestQueue(stringRequest);
            AllClubsFrag frag = new AllClubsFrag();
            Bundle data = new Bundle();
            frag.setArguments(data);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frag).commit();
        } else {
            if (NewClubName.getText().toString().trim().length() != 0) {
                try {
                    clubData.put("name", NewClubName.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (NewClubDescription.getText().toString().trim().length() != 0) {
                try {
                    clubData.put("description", NewClubDescription.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (NewImageURL.getText().toString().trim().length() != 0) {
                try {
                    clubData.put("profilePic", NewImageURL.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            RestSingleton restSingleton = RestSingleton.getInstance(view.getContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, restSingleton.getUrl() + "editClub/", clubData,
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
            ClubFrag frag = new ClubFrag();
            Bundle data = new Bundle();
            data.putString("clubID", EditingClub.getTag().toString());
            frag.setArguments(data);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
        }
    }

    /**
     * Gets the layout components from edit_club_layou.xml
     * @return nothing
     */
    private void getLayoutComponents() {
        EditingClub = view.findViewById(R.id.ClubEditing);
        NewClubName = view.findViewById(R.id.NewClubName);
        NewClubDescription = view.findViewById(R.id.NewClubDescription);
        NewImageURL = view.findViewById(R.id.NewImageURL);
        SaveButton = view.findViewById(R.id.SaveClub);
        SaveButton.setOnClickListener(this);
        DeleteButton = view.findViewById(R.id.DeleteClub);
    }

    /**
     * Adds the layout components and sets them in editTexts
     * @param res The response from the database
     * @throws JSONException Error in JSON processing
     * @see JSONException
     */
    private void updateUI(JSONObject res) throws JSONException {
        EditingClub.setText("Editing Club:\n" + res.getString("name"));
        EditingClub.setTag(res.getString("_id"));
        DeleteButton.setTag("DELETE");
        NewClubName.setText(res.getString("name"));
        NewClubDescription.setText(res.getString("description"));
        NewImageURL.setText(res.getString("profilePic"));
        clubData = res;
    }
}
