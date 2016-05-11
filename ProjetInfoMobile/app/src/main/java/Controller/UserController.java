package Controller;

import android.graphics.Bitmap;
import android.location.Location;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;

import java.util.Map;
import java.util.Observable;

import Interfaces.FirebaseInterface;
import Model.RecommendationModel;
import Model.UserModel;

/**
 * Created by yannd on 2016-04-22.
 */
public class UserController extends Observable {
    private UserModel userModel = new UserModel();
    private String uid;

    public final UserModel GetUserModel() {
        return userModel;
    }

    //region singleton
    private static UserController instance;
    public static UserController Instance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    private UserController() {
    }
    //endregion

    //region login/out
    public void Logout(){
        FirebaseInterface.Instance().getFirebaseRef().unauth();
    }

    public void Login(String email, String password) {
        Firebase firebaseRef = FirebaseInterface.Instance().getFirebaseRef();
        firebaseRef.authWithPassword(email, password, loginResultHandler);
    }

    public void Signin(String email, String password) {
        Firebase firebaseRef = FirebaseInterface.Instance().getFirebaseRef();
        firebaseRef.createUser(email, password, signinResponseHandler);
    }

    private void FetchUserData(final String uid) {
        FirebaseInterface.Instance().GetUserIDNode(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        setChanged();
                        // If found username then load all the data, if not, the user must set a username
                        if (dataSnapshot.getValue() != null) {
                            UserController.this.userModel = dataSnapshot.getValue(UserModel.class);
                        }
                        notifyObservers(UserController.this.userModel);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        setChanged();
                        notifyObservers(firebaseError);
                    }
                });
    }

    public void CreateNewUser(final String username, final Bitmap profilePicture) {
        Query usernameQuery = FirebaseInterface.Instance().GetUsersNode().equalTo(username, "username");
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    userModel = new UserModel();
                    userModel.setUsername(username);
                    userModel.setUserProfilePicture(profilePicture);
                    FirebaseInterface.Instance().GetUserIDNode(uid).setValue(userModel, userProfilCreationResponseHandler);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                setChanged();
                notifyObservers(firebaseError);
            }
        });
    }
    //endregion

    //region friendsmanagement
    public void SendFriendRequest(String username) {
        FirebaseInterface.Instance().GetReceivedFriendRequestsNode(username).child(userModel.getUsername()).setValue(userModel.getUsername());
    }

    public void AcceptFriendRequest(String username) {
        FirebaseInterface.Instance().GetReceivedFriendRequestsNode(userModel.getUsername()).child(username).removeValue();
        FirebaseInterface.Instance().GetFriendListNode(userModel.getUsername()).child(username).setValue(username);
        FirebaseInterface.Instance().GetFriendListNode(username).child(userModel.getUsername()).setValue(userModel.getUsername());
    }

    public void RefuseFriendRequest(String username) {
        FirebaseInterface.Instance().GetReceivedFriendRequestsNode(userModel.getUsername()).child(username).removeValue();
    }

    public void DeleteFriend(String username) {
        FirebaseInterface.Instance().GetFriendListNode(userModel.getUsername()).child(username).removeValue();
        FirebaseInterface.Instance().GetFriendListNode(username).child(userModel.getUsername()).removeValue();
    }
    //endregion

    //region seriesManagement
    public void SendSerieSuggestion(final String username, final String suggestionID) {
        // SerieModel with a list of people who suggested it
        FirebaseInterface.Instance().GetSeriesSuggestionNode(username).child(suggestionID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RecommendationModel rec = dataSnapshot.getValue(RecommendationModel.class);
                if (rec == null) {
                    rec = new RecommendationModel();
                    rec.setSerieID(suggestionID);
                }
                rec.AddRecommendation(userModel.getUsername());
                FirebaseInterface.Instance().GetSeriesSuggestionNode(username).child(suggestionID).setValue(rec);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void AddSerie(String suggestionID) {
        FirebaseInterface.Instance().GetSeriesSuggestionNode(userModel.getUsername()).child(suggestionID).removeValue();
        FirebaseInterface.Instance().GetSeriesListNode(userModel.getUsername()).child(suggestionID).setValue(suggestionID);
    }

    public void RefuseSerieSuggestion(String suggestionID) {
        FirebaseInterface.Instance().GetSeriesSuggestionNode(userModel.getUsername()).child(suggestionID).removeValue();
    }

    public void DeleteSerie(String suggestionID) {
        FirebaseInterface.Instance().GetSeriesListNode(userModel.getUsername()).child(suggestionID).removeValue();
    }
    //endregion

    //region positionmanagement
    public void SharePosition(boolean share){
        FirebaseInterface.Instance().GetSharePositionNode(userModel.getUsername()).setValue(share);
    }

    public void UpdateUserPosition(Location position){
        if(userModel.isSharePosition())
            FirebaseInterface.Instance().getGeofireRef().setLocation(userModel.getUsername(), new GeoLocation(position.getLatitude(), position.getLongitude()));
        else
            FirebaseInterface.Instance().getGeofireRef().removeLocation(userModel.getUsername());
    }
    //endregion

    //region profilepicturemanagement
    public void SetProfilePicture(Bitmap picture){
        userModel.setUserProfilePicture(picture);
        FirebaseInterface.Instance().GetSharePositionNode(userModel.getEncodedUserProfilePicture());
    }
    //endregion

    //region authenticationcallbacks
    Firebase.AuthResultHandler loginResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            uid = authData.getUid();
            FetchUserData(authData.getUid());
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            UserController.this.setChanged();
            UserController.this.notifyObservers(firebaseError);
        }
    };

    Firebase.ValueResultHandler<Map<String, Object>> signinResponseHandler = new Firebase.ValueResultHandler<Map<String, Object>>() {
        @Override
        public void onSuccess(Map<String, Object> result) {
            uid = (String)result.get("uid");
            FetchUserData((String) result.get("uid"));
        }

        @Override
        public void onError(FirebaseError firebaseError) {
            UserController.this.setChanged();
            UserController.this.notifyObservers(firebaseError);
        }
    };

    Firebase.CompletionListener userProfilCreationResponseHandler = new Firebase.CompletionListener() {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            UserController.this.setChanged();
            if (firebaseError == null) {
                UserController.this.notifyObservers(userModel);
            } else {
                UserController.this.notifyObservers(firebaseError);
            }
        }
    };
    //endregion
}