package com.example.cs4532.umdalive;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * @author Devin Cheek
 *
 * 4/26/2018
 *
 * A class that synchronizes all requests across the app
 */
public class RestSingleton {

    //Volley
    private static RestSingleton instance;
    private RequestQueue requestQueue;
    private static Context context;

    //Server address
    private String url = "http://ukko.d.umn.edu:22643/";


    /**
     * Constructor for the RestSingleton
     * @param c The context of the call being made
     */
    private RestSingleton (Context c) {
            context = c;
            requestQueue = getRequestQueue();
    }

    /**
     * Gets the instance of the RestSingleton, and if one does not exist, it creates a new one
     * @param c The Context of the instance
     * @return The instance
     */
    public static synchronized  RestSingleton getInstance(Context c) {
        if (instance == null) {
            instance = new RestSingleton(c);
        }
        return instance;
    }

    /**
     * Gets the Queue of all the requests being made, if none exists, it makes one
     * @return The requestQueue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds requests to the requestQueue
     * @param req The request being passed in
     * @param <T> Allows any type of response
     */
    public <T> void addToRequestQueue(Request req) {
        getRequestQueue().add(req);
    }

    /**
     * Gets the server address
     * @return The url of the server
     */
    public String getUrl () {
        return url;
    }
}
