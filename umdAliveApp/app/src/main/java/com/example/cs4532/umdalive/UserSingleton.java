package com.example.cs4532.umdalive;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * @author Devin Cheek
 *
 * 4/26/2018
 *
 * Class that synchronizes, storing their data and allowing it to be accessed by other
 * users in the app.
 */
public class UserSingleton {

    private GoogleSignInAccount account;

    private static final UserSingleton instance = new UserSingleton();

    private UserSingleton (){}

    /**
     * Gets the user's instance
     * @return instance The user's instance
     */
    public static synchronized UserSingleton getInstance() {
        return instance;
    }

    /**
     * Sets the user's account from their GoogleSignInAccount
     * @param a The account of the user
     */
    public void setAccount(GoogleSignInAccount a) {
        instance.account = a;
    }

    /**
     * Gets the ID of the user
     * @return The instance of the user's id
     */
    public String getUserID () {
        return instance.account.getId();
    }

    /**
     * Gets the name of the user
     * @return The name of the user
     */
    public String getName () {
        return instance.account.getDisplayName();
    }

    /**
     * Gets the email of the user
     * @return The email of the user
     */
    public String getEmail() {
        return instance.account.getEmail();
    }

    /**
     * Gets the URL of the Profile so it can be used as the profile photo
     * @return The URL of the Profile
     */
    public String getProfileUrl() {
        if (instance.account.getPhotoUrl() != null) {
            return instance.account.getPhotoUrl().toString();
        } else {
            return null;
        }
    }
}
