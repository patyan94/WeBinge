package Model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.projetinfomobile.MainActivity;
import com.projetinfomobile.UserInfoCompletionActivity;

import java.util.Map;

/**
 * Created by yannd on 2016-03-25.
 */
public class DatabaseInterface {

    private final String path = "https://finalprojectmobile.firebaseio.com/";
    private Firebase firebaseRef;
    GeoQuery geoQuery;
    private GeoFire geofireRef;
    private UserData userData = null;
    private AuthData authData;

    //region getters/setters
    public UserData getUserData() {
        return userData;
    }

    public Firebase GetDatabaseMainNode(){
        return firebaseRef;
    }
    //endregion

    //region singleton
    private DatabaseInterface(){
        firebaseRef = new Firebase(path);
        geofireRef = new GeoFire(firebaseRef.child("positions"));
    }

    private static DatabaseInterface instance = null;
    public static DatabaseInterface Instance(){
        if(instance == null){
            instance = new DatabaseInterface();
        }
        return instance;
    }
    //endregion

    //region authentication

    // Login
    public void LoginWithPassword(final String email, final String password, final Firebase.AuthResultHandler authResultHandler, final Firebase.ValueResultHandler<Map<String, Object>> userCreationHandler){
        firebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Gets user data if login succeded
                DatabaseInterface.this.authData = authData;
                FetchUserData(authData, authResultHandler, userCreationHandler);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Signs up user if account doesnt exist, notify user for other errors
                if (firebaseError.getCode() == FirebaseError.USER_DOES_NOT_EXIST) {
                    SignupWithPassword(email, password, authResultHandler, userCreationHandler);
                } else {
                    authResultHandler.onAuthenticationError(firebaseError);
                }
            }
        });
    }

    // Signup
    public void SignupWithPassword(final String email, final String password, final Firebase.AuthResultHandler authResultHandler, final Firebase.ValueResultHandler<Map<String, Object>> userCreationHandler){
        firebaseRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {
                // Tries to login if signup succeded
                LoginWithPassword(email, password, authResultHandler, userCreationHandler);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                // Notifies user if signup didnt work
                userCreationHandler.onError(firebaseError);
            }
        });
    }

    public void  Logout(){
        firebaseRef.unauth();
        userData = null;
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
    public Firebase GetUserDataNode(String username){
        return GetUsersNode().child(username).child("user_data");
    }

    public Firebase GetUsersIDNode(){
        return firebaseRef.child("user_ids");
    }

    public Firebase GetUserIDNode(String uid){
        return GetUsersIDNode().child(uid);
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

    public Firebase GetCurrentUserReceivedFriendRequestsNode(){
        return GetReceivedFriendRequestsNode(userData.getUsername());
    }

    public Firebase GetCurrentUserFriendListNode(){
        return GetFriendListNode(userData.getUsername());
    }
    public Firebase GetCurrentUserSeriesSuggestionNode(){
        return GetSeriesSuggestionNode(userData.getUsername());
    }

    public Firebase GetCurrentUserSeriesListNode(){
        return GetSeriesListNode(userData.getUsername());
    }

    //endregion

    //region usermanagement
    // Gets the username from its UID
    public void FetchUserData(final AuthData authData, final Firebase.AuthResultHandler authResultHandler, final Firebase.ValueResultHandler<Map<String, Object>> userCreationHandler){
        GetUserIDNode(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If found username then load all the data, if not, the user must set a username
                if (dataSnapshot.getValue() != null) {
                    FetchDetailledUserInfo(dataSnapshot.getValue(String.class), authResultHandler);
                    return;
                } else {
                    userCreationHandler.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    public void FetchDetailledUserInfo(final String username, final Firebase.AuthResultHandler authResultHandler) {
        GetUserDataNode(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Listens for changes on each user info
                userData = dataSnapshot.getValue(UserData.class);
                GetFriendListNode(userData.getUsername()).addChildEventListener(new CustomChildEventListener<String>(String.class, userData.getFriendsList()));
                GetSeriesListNode(userData.getUsername()).addChildEventListener(new CustomChildEventListener<String>(String.class, userData.getSeriesList()));
                GetReceivedFriendRequestsNode(userData.getUsername()).addChildEventListener(new CustomChildEventListener<String>(String.class, userData.getFriendsRequests()));
                GetSeriesSuggestionNode(userData.getUsername()).addChildEventListener(new CustomChildEventListener<Recommendation>(Recommendation.class, userData.getSeriesSuggestions()));
                authResultHandler.onAuthenticated(DatabaseInterface.this.authData);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    // Creates a new user account
    public void AddNewUSer(final String username, final Bitmap image,final ValueEventListener valueEventListener){
       GetUserNode(username).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.getValue()== null) {
                   GetUserIDNode(DatabaseInterface.this.authData.getUid()).setValue(username);
                   userData = new UserData();
                   userData.setUsername(username);
                   userData.setUserProfileImage(image);
                   userData.setProvider(DatabaseInterface.this.authData.getProvider());
                   GetUserDataNode(username).setValue(userData);
               }
               valueEventListener.onDataChange(dataSnapshot);
           }

           @Override
           public void onCancelled(FirebaseError firebaseError) {

           }
       });

    }

    public UserData GetCurrentUserData(){
        return this.userData;
    }

    public void SaveCurrentUserData(){
        GetUserDataNode(userData.getUsername()).setValue(userData);
    }

    //endregion

    //region positionmanagement
    public void UpdateUserPosition(Location position){
        if(userData.isSharePosition())
            geofireRef.setLocation(userData.getUsername(), new GeoLocation(position.getLatitude(), position.getLongitude()));
        else
            geofireRef.removeLocation(userData.getUsername());
    }

    public void StartListeningToCloseUsers(Location position, double radius, GeoQueryEventListener listener){
        if(geoQuery != null || !userData.isSharePosition()) return;
        geoQuery =  geofireRef.queryAtLocation(new GeoLocation(position.getLatitude(), position.getLongitude()), radius);
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
    //endregion

    //region friendsManagement

    public void SendFriendRequest(String username){
        GetReceivedFriendRequestsNode(username).child(this.userData.getUsername()).setValue(this.userData.getUsername());
    }

    public void AcceptFriendRequest(String username){
        GetReceivedFriendRequestsNode(userData.getUsername()).child(username).removeValue();
        GetFriendListNode(userData.getUsername()).child(username).setValue(username);
        GetFriendListNode(username).child(userData.getUsername()).setValue(userData.getUsername());
    }

    public void RefuseFriendRequest(String username){
        GetReceivedFriendRequestsNode(userData.getUsername()).child(username).removeValue();
    }

    public void DeleteFriend(String username){
        GetFriendListNode(userData.getUsername()).child(username).removeValue();
        GetFriendListNode(username).child(userData.getUsername()).removeValue();
    }
    //endregion

    //region seriesManagement
    public void SendSerieSuggestion(final String username, final String suggestionID){
        // Serie with a list of people who suggested it
        GetSeriesSuggestionNode(username).child(suggestionID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Recommendation rec = dataSnapshot.getValue(Recommendation.class);
                if(rec == null){
                    rec = new Recommendation();
                    rec.setSerieID(suggestionID);
                }
                rec.AddRecommendation(userData.getUsername());
                GetSeriesSuggestionNode(username).child(suggestionID).setValue(rec);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void AddSerie(String suggestionID){
        GetSeriesSuggestionNode(userData.getUsername()).child(suggestionID).removeValue();
        GetSeriesListNode(userData.getUsername()).child(suggestionID).setValue(suggestionID);
    }

    public void RefuseSerieSuggestion(String suggestionID){
        GetSeriesSuggestionNode(userData.getUsername()).child(suggestionID).removeValue();
    }

    public void DeleteSerie(String suggestionID){
        GetSeriesListNode(userData.getUsername()).child(suggestionID).removeValue();
    }
    //endregion
}
