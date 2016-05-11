package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class UserModel {

    private String username;
    @JsonIgnore
    private Bitmap userProfilePicture;
    private String encodedUserProfilePicture;
    private boolean sharePosition = true;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public Bitmap getUserProfilePicture() {
        if(userProfilePicture == null) {
            try {
                byte[] decodedByte = com.firebase.client.utilities.Base64.decode(encodedUserProfilePicture);
                this.userProfilePicture = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            } catch (Exception e) {
            }
        }
        return userProfilePicture;
    }

    @JsonIgnore
    public void setUserProfilePicture(Bitmap userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
        try {
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            userProfilePicture.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = com.firebase.client.utilities.Base64.encodeBytes(byteArray);
            this.encodedUserProfilePicture = imageFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSharePosition() {
        return sharePosition;
    }

    public void setSharePosition(boolean sharePosition) {
        this.sharePosition = sharePosition;
    }

    public String getEncodedUserProfilePicture() {
        return encodedUserProfilePicture;
    }

    public void setEncodedUserProfilePicture(String encodedUserProfileImage) {
        this.encodedUserProfilePicture = encodedUserProfileImage;
    }

    @JsonIgnore
    HashSet<String> friendsList = new HashSet<>();
    @JsonIgnore
    HashSet<String> seriesList = new HashSet<>();
    @JsonIgnore
    HashSet<String> friendsRequests = new HashSet<>();
    @JsonIgnore
    HashMap<String, RecommendationModel> seriesSuggestions = new HashMap<>();

    @JsonIgnore
    public HashSet<String> getFriendsList() {
        return friendsList;
    }

    @JsonIgnore
    public void setFriendsList(HashSet<String> friendsList) {
        this.friendsList = friendsList;
    }

    @JsonIgnore
    public HashSet<String> getSeriesList() {
        return seriesList;
    }

    @JsonIgnore
    public void setSeriesList(HashSet<String> seriesList) {
        this.seriesList = seriesList;
    }

    @JsonIgnore
    public HashSet<String> getFriendsRequests() {
        return friendsRequests;
    }

    @JsonIgnore
    public void setFriendsRequests(HashSet<String> friendsRequests) {
        this.friendsRequests = friendsRequests;
    }

    @JsonIgnore
    public HashMap<String, RecommendationModel> getSeriesSuggestions() {
        return seriesSuggestions;
    }

    @JsonIgnore
    public void setSeriesSuggestions(HashMap<String, RecommendationModel> seriesSuggestions) {
        this.seriesSuggestions = seriesSuggestions;
    }
}