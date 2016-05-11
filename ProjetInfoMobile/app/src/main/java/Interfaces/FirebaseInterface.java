package Interfaces;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.projetinfomobile.R;

/**
 * Created by yannd on 2016-04-22.
 */
public class FirebaseInterface {

    private Firebase firebaseRef;
    private GeoFire geofireRef;
    GeoQuery geoQuery;

    //region getter/setter
    public GeoFire getGeofireRef() {
        return geofireRef;
    }

    public void setGeofireRef(GeoFire geofireRef) {
        this.geofireRef = geofireRef;
    }

    public Firebase getFirebaseRef() {
        return firebaseRef;
    }

    public void setFirebaseRef(Firebase firebaseRef) {
        this.firebaseRef = firebaseRef;
    }
    //endregion

    //region nodes

    // All functions of this region return the URL address on the database of their field
    public Firebase GetUsersNode(){
        return firebaseRef.child("users");
    }

    public Firebase GetUserNode(String username){
        return GetUsersNode().child(username);
    }

    public Firebase GetUserIDNode(String uid){
        return GetUsersNode().child(uid);
    }

    public Firebase GetReceivedFriendRequestsNode(String username){
        return GetUserNode(username).child("friends_requests");
    }

    public Firebase GetFriendListNode(String username){
        return GetUserNode(username).child("friends");
    }
    public Firebase GetSeriesSuggestionNode(String username){
        return GetUserNode(username).child("series_suggestions");
    }

    public Firebase GetSeriesListNode(String username){
        return GetUserNode(username).child("series");
    }

    public Firebase GetProfilePictureNode(String username){
        return GetUserNode(username).child("encodedProfilePicture");
    }

    public Firebase GetSharePositionNode(String username){
        return GetUserNode(username).child("sharePosition");
    }
    //endregion


    private static FirebaseInterface instance = null;
    private FirebaseInterface(){}

    public static void Initialize(Context context){
        Resources resources = context.getResources();
        String databasePath = resources.getString(R.string.database_path);
        String positionPath = resources.getString(R.string.user_positions);

        instance = new FirebaseInterface();
        instance.firebaseRef = new Firebase(databasePath);
        instance.geofireRef = new GeoFire(instance.firebaseRef.child(positionPath));
    }

    public static FirebaseInterface Instance() throws NullPointerException {
        if(instance == null){
            throw new NullPointerException("Call FirebaseInstance.Initialize(...) first");
        }
        return instance;
    }

    public void StartListeningToCloseUsers(Location position, double radius, GeoQueryEventListener listener){
        if(geoQuery != null) return;
        geoQuery = geofireRef.queryAtLocation(new GeoLocation(position.getLatitude(), position.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(listener);
    }
    public void StopListeningToCloseUsers(){
        if(geoQuery == null) return;
        geoQuery.removeAllListeners();
        geoQuery = null;
    }

    public void UpdateGeoQueryPosition(Location position){
        geoQuery.setCenter(new GeoLocation(position.getLatitude(), position.getLongitude()));
    }
}
