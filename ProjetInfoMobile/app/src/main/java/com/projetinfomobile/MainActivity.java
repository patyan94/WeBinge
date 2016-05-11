package com.projetinfomobile;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Controller.UserController;
import Interfaces.FirebaseInterface;
import Model.OMDBInterface;
import Model.SerieModel;
import Model.UserModel;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    UserController userController = UserController.Instance();
    ImageView profilePictureView;
    TextView usernameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup the drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Setup the navigation view in the drawer
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(3).setChecked(true);//Set the item menu "Your series" checked by default

        View drawerHeader = navigationView.getHeaderView(0);

        final UserModel userModel = userController.GetUserModel();
        // Sets the user photo
        profilePictureView = (ImageView) drawerHeader.findViewById(R.id.profile_picture_view);
        profilePictureView.setImageBitmap(userModel.getUserProfilePicture());

        // Sets the username
        usernameView = (TextView) drawerHeader.findViewById(R.id.user_display_name);
        usernameView.setText(userModel.getUsername());


        // Setup the floating button to open the search fragment
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_serie);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowRandomSerie();
            }
        });

        // Starts the position updater service
        Intent intent = new Intent(this, Services.PositionUpdaterService.class);
        startService(intent);

        //Loading the "Your series" fragment at application start
        LoadFragment(new SeriesFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.action_settings:
                LoadFragment(new SettingsFragment());
                return true;

            //Add any new button's action in a new case if needed

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LoadFragment(new CloseUsersMapFragment());
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (navigationView.getMenu().getItem(1).isChecked()) {
                LoadLastFragment();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {

            case R.id.nav_friends_recommendations:
                fragment = new RecommandationsFragment();
                break;
            case R.id.nav_friends:
                fragment = new FriendsFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.nav_map:
                fragment = new CloseUsersMapFragment();
                break;
            case R.id.nav_your_series:
                fragment = new SeriesFragment();
                break;
        }

        if (fragment != null) {
            LoadFragment(fragment);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    void LoadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment, fragment.getTag());
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.commit();
    }

    void LoadLastFragment() {
        getSupportFragmentManager().popBackStack();
    }

    //Static method used to pop up an altert dialog with the specified user series, it's used in the map and friend activity
    public static void PromptUserSeries(final String username, Context ctx) {
        final OMDBInterface omdbInterface;
        omdbInterface = OMDBInterface.Start(ctx);

        //We build the window
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(username);
        View seriesView = View.inflate(ctx, R.layout.alert_dialog_series, null);
        builder.setView(seriesView);

        RecyclerView seriesListview = (RecyclerView) seriesView.findViewById(R.id.series_listview_alert);

        seriesListview.setHasFixedSize(true);
        seriesListview.setLayoutManager(new LinearLayoutManager(ctx));

        FirebaseRecyclerAdapter<String, SeriesViewHolder> seriesAdapter = new FirebaseRecyclerAdapter<String, SeriesViewHolder>(String.class, R.layout.alert_series_listview_item, SeriesViewHolder.class, FirebaseInterface.Instance().GetSeriesListNode(username)) {
            @Override
            protected void populateViewHolder(final SeriesViewHolder view, final String serieID, int position) {
                omdbInterface.GetSerieInfo(serieID, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            SerieModel serie = SerieModel.FromJSONObject(response);
                            view.title.setText(serie.getName());
                            view.description.setText(serie.getDescription());
                            if (!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                                omdbInterface.GetPoster(serie.getPhotoURL(), view.posterView);
                            }
                            if (UserController.Instance().GetUserModel().getSeriesList().contains(serie.getID())) {
                                view.itemView.setBackgroundColor(Color.LTGRAY);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
            }
        };
        seriesListview.setAdapter(seriesAdapter);


        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.show();
    }

    public void ShowRandomSerie() {

        //Getting the friend of the current user
        Set<String> m = userController.GetUserModel().getFriendsList();
        final BitSet serieSearchStatus = new BitSet(m.size());
        serieSearchStatus.clear();

        if (m.size() > 0) {
            List<String> friends = new ArrayList<>(m);
            Collections.shuffle(friends, new Random(System.currentTimeMillis()));

            final boolean[] flag = new boolean[2];

            for (int i = 0; i< friends.size(); ++i) {
                final int idx = i;
                //For each serie of the friend, we add it to an array if we don't already own this serie
                final List<String> unknownSeries = new ArrayList<>();

                //Getting the series of the selected friend
                FirebaseInterface.Instance().GetSeriesListNode(friends.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (!userController.GetUserModel().getSeriesList().contains(ds.getValue(String.class))) {
                                unknownSeries.add(ds.getValue(String.class));
                            }
                        }

                        if (unknownSeries.isEmpty()) {
                            //Then we try again with another friend if possible
                        } else if (!flag[0]) {
                            //We then randomly select an unknown serie from the array and prompt the message
                            Random random = new Random();
                            String serieName = unknownSeries.get(random.nextInt(unknownSeries.size()));

                            final OMDBInterface omdbInterface = OMDBInterface.Start(getApplicationContext());
                            omdbInterface.GetSerieInfo(serieName, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getString("Response").equalsIgnoreCase("True") && response.getString("Type").equalsIgnoreCase("series")) {
                                                    SerieModel serie = SerieModel.FromJSONObject(response);
                                                    ShowSerieAlertDialog(serie, omdbInterface);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    });

                            flag[0] = true;
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            //Countdown timer used the print the toast message after 10 seconds if none of the listeners have flipped the flag before that
            new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    flag[1] = true;
                }
            }.start();

            new Thread(new Runnable() {
                public void run() {

                    //Wait for one of the flag the flip
                    while (!flag[0] && !flag[1]) ;

                    if (flag[0]) {
                        //This means that one of the friend was able to make a recommendation
                    } else {
                        //this means the cunt down ended, so no one could make a recommendation
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Sorry, none of your friends can recommend anything...", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        } else {
            Toast.makeText(getApplicationContext(), "Sorry, you must have at least one friend to get recommendations...", Toast.LENGTH_LONG).show();
        }
    }

    void ShowSerieAlertDialog(final SerieModel serie, OMDBInterface omdb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Would you like to add '" + serie.getName() + "' ?");

        final ImageView poster = new ImageView(this);
        omdb.GetPoster(serie.getPhotoURL(), poster);
        poster.setMaxHeight(100);
        poster.setMaxWidth(100);
        builder.setView(poster);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserController.Instance().AddSerie(serie.getID());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.show();
    }

    public static class SeriesViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;

        public SeriesViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.serie_name);
            description = (TextView) itemView.findViewById(R.id.serie_description);
            posterView = (ImageView) itemView.findViewById(R.id.serie_poster);
        }
    }
}
