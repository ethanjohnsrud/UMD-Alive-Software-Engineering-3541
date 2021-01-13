package com.example.cs4532.umdalive;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cs4532.umdalive.fragments.base.AllClubsFrag;
import com.example.cs4532.umdalive.fragments.base.ChatFrag;
import com.example.cs4532.umdalive.fragments.base.MyClubsFrag;
import com.example.cs4532.umdalive.fragments.base.ProfileFrag;
import com.example.cs4532.umdalive.fragments.create.CreateClubFrag;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

/**
 * @author Devin Cheek
 * 4/26/2018
 *
 * Class that is the basic framework for all fragment. It includes
 * the basic navigation, handles Google Sign-in, and loads all fragments.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //Fragment
    FrameLayout fragContainer;

    //Google Sign In
    GoogleSignInClient mGoogleSignInClient;
    static final int RC_SIGN_IN = 9001;

    //Nav Header Components
    TextView userName;
    TextView userEmail;
    ImageView userImage;

    /**
     * Loads up the entire app on login
     * @param savedInstanceState The Instance saved by the user from last login
     * @return nothing
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_profile);

        //Frag Holder
        fragContainer = findViewById(R.id.fragment_container);

        //Show loading bar
        findViewById(R.id.PageLoading).setVisibility(View.VISIBLE);

        //Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        //Set Nav Header Comps
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);
        userImage = headerView.findViewById(R.id.user_image);
    }

    /**
     * Allows navigator to be closed in any fragment of the app
     * @return nothing
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Creates the navigation bar and menu
     * @param menu The navigation menu
     * @return true, allowing the bar to created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Allows the switching between fragments via the navigation bar
     * @param item The menu item selected in the navigation bar
     * @return true, or the option selected
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            //Show loading bar
            findViewById(R.id.PageLoading).setVisibility(View.VISIBLE);
            //Add Profile Fragment
            ProfileFrag frag = new ProfileFrag();
            Bundle data = new Bundle();
            data.putString("userID", UserSingleton.getInstance().getUserID());
            frag.setArguments(data);
            getSupportFragmentManager().beginTransaction().replace(fragContainer.getId(),frag).commit();

        } else if (id == R.id.nav_create_club) {
            CreateClubFrag frag = new CreateClubFrag();
            Bundle data = new Bundle();
            frag.setArguments(data);
            getSupportFragmentManager().beginTransaction().replace(fragContainer.getId(),frag).commit();

        } else if (id == R.id.nav_all_clubs) {
            //Show loading bar
            findViewById(R.id.PageLoading).setVisibility(View.VISIBLE);

            //Add All Clubs Fragment
            AllClubsFrag frag = new AllClubsFrag();
            Bundle data = new Bundle();
            frag.setArguments(data);
            getSupportFragmentManager().beginTransaction().replace(fragContainer.getId(),frag).commit();

        } else if (id == R.id.nav_group_chat) {
            launchGroupChat();

        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Used only when the sign-in button pressed, allowing it to be clicked.
     * @param v, the view clicked
     * @return nothing
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    /**
     * Allows a user to sign-in to the app, utilizing the Google Response code of 9001
     * to continue
     * @return nothing
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    /**
     * Allows a user to sign-out of the app when clicking on the sign-out option in the
     * navigation bar.
     * @return nothing
     */
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        findViewById(R.id.Login).setVisibility(View.VISIBLE);
                        findViewById(R.id.appBarLayout).setVisibility(View.GONE);
                        findViewById(R.id.Content).setVisibility(View.GONE);

                        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
                        nv.setCheckedItem(R.id.nav_profile);
                    }
                });
    }

    /**
     * Open the group chat activity when the group chat button is clicked
     */
    private void launchGroupChat() {
        /*Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    */


        //Show loading bar
        findViewById(R.id.PageLoading).setVisibility(View.VISIBLE);

        //Add All Clubs Fragment
        MyClubsFrag frag = new MyClubsFrag();
        Bundle data = new Bundle();
        frag.setArguments(data);
        getSupportFragmentManager().beginTransaction().replace(fragContainer.getId(),frag).commit();


    }

    /**
     * Whenever a user sign-ins, this will be called to sort information given
     * @param requestCode Code requested from sign-in
     * @param resultCode Code acquired from sign-in
     * @param data Data of user that signed-in
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Tests whether or not the sign-in was successful, and handles it on the basis of
     * the result
     * @param completedTask The task of the Google sign-in
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
            
        } catch (ApiException e) {
            try {
                updateUI(null);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the UI depending on the user that has logged-in,
     * and opens their profile page
     * @param account The user's account
     * @throws IOException
     */
    private void updateUI(GoogleSignInAccount account) throws IOException {
        UserSingleton.getInstance().setAccount(account);
        UserSingleton us = UserSingleton.getInstance();

        findViewById(R.id.Login).setVisibility(View.GONE);
        findViewById(R.id.appBarLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.Content).setVisibility(View.VISIBLE);

        //Add Profile Fragment
        ProfileFrag frag = new ProfileFrag();
        Bundle data = new Bundle();
        data.putString("userID", UserSingleton.getInstance().getUserID());
        frag.setArguments(data);
        getSupportFragmentManager().beginTransaction().replace(fragContainer.getId(),frag).commit();

        if (us.getProfileUrl() != null) {
            Glide.with(this)
                    .load(us.getProfileUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImage);
        } else {
            Glide.with(this)
                    .load("https://images.homedepot-static.com/productImages/42613c1a-7427-4557-ada8-ba2a17cca381/svn/gorilla-carts-yard-carts-gormp-12-64_1000.jpg")
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImage);
        }

        userName.setText(us.getName());
        userEmail.setText((us.getEmail()));
    }
}
