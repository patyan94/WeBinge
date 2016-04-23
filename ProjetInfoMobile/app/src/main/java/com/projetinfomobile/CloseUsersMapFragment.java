package com.projetinfomobile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.DatabaseInterface;
import Model.OMDBInterface;
import Model.Serie;

public class CloseUsersMapFragment extends SupportMapFragment
        implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private HashMap<String, MarkerOptions> closeUsersLocations = new HashMap<>();
    private HashMap<String, Marker> actualMarkersUsers = new HashMap<>();
    private GoogleMap map;
    private  Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private HashMap<String, List<String>> closeUsersSeries;

    public CloseUsersMapFragment() {
        closeUsersSeries = new HashMap<>();
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO : show position of user AND surroundings only if the option is enabled - Also, do not update position in firebase at all if not enable
        if(!DatabaseInterface.Instance().getUserData().isSharePosition()){
            Toast.makeText(getContext(), "In order to be able to use this functionality, please enable the location sharing setting", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        //Inflate the layout for this fragment
        //View v = inflater.inflate(R.layout.fragment_map, container, false);

        getMapAsync(this);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DatabaseInterface.Instance().StopListeningToCloseUsers();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getContext(), "Please allow this application to use your location from Android/Applications settings", Toast.LENGTH_LONG).show();
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Creates location requests every 5-60 sec
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(5000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //if(mLastLocation != null && location.distanceTo(mLastLocation)<50.f)return;
        DatabaseInterface.Instance().StartListeningToCloseUsers(location, 1000.f, geoQueryEventListener);
        DatabaseInterface.Instance().UpdateGeoQueryPosition(location);
        DatabaseInterface.Instance().UpdateUserPosition(location);
        mLastLocation = location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStart(){
        super.onStart();
        RessourceMonitor.getInstance().SaveCurrentBatteryLevel();
    }

    @Override
    public void onStop(){
        super.onStop();
        Toast.makeText(getContext(), "Map battery usage : " + String.valueOf(RessourceMonitor.getInstance().GetLastBatteryUsage()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String username = marker.getTitle();

                //If we clicked on ourselves we don't do anything special
                if (DatabaseInterface.Instance().getUserData().getUsername().equalsIgnoreCase(username)) {
                    return true;
                } else {
                    MainActivity.PromptUserSeries(username, getContext());
                    return true;
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
    }

    private GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            closeUsersLocations.put(key, new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(key));
            ShowUserLocationOnMap(closeUsersLocations.get(key));
        }

        @Override
        public void onKeyExited(String key) {
            closeUsersLocations.remove(key);
            actualMarkersUsers.get(key).remove();
            actualMarkersUsers.remove(key);
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Marker m = actualMarkersUsers.get(key);
            m.setPosition(new LatLng(location.latitude, location.longitude));
        }

        @Override
        public void onGeoQueryReady() {
        }

        @Override
        public void onGeoQueryError(FirebaseError error) {

        }
    };
    private void ShowUserLocationOnMap(MarkerOptions pos) {
        final Marker m = map.addMarker(pos);
        actualMarkersUsers.put(pos.getTitle(), m);

        //For the marker of the current user, show it in a distinctive colour
        if (pos.getTitle().equalsIgnoreCase(DatabaseInterface.Instance().getUserData().getUsername())) {
            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        } else {
            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            DatabaseInterface.Instance().GetSeriesListNode(pos.getTitle()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (!closeUsersSeries.containsKey(dataSnapshot.getKey())) {
                        closeUsersSeries.put(dataSnapshot.getKey(), new ArrayList<String>());
                    }
                    closeUsersSeries.get(dataSnapshot.getKey()).add(dataSnapshot.getValue(String.class));
                    if (DatabaseInterface.Instance().GetCurrentUserData().getSeriesList().containsKey(dataSnapshot.getKey())) {
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    if (!closeUsersSeries.containsKey(dataSnapshot.getKey())) {
                        closeUsersSeries.get(dataSnapshot.getKey()).remove(dataSnapshot.getValue(String.class));
                    }
                    for (String s : closeUsersSeries.get(dataSnapshot.getKey())) {
                        if (DatabaseInterface.Instance().GetCurrentUserData().getSeriesList().containsKey(s)) {
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            return;
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }
}
