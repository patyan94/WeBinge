package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserData {
    private String username;

    private String provider;
    private String encodedUserProfileImage;
    private boolean sharePosition = true;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEncodedUserProfileImage() {
        return encodedUserProfileImage;
    }

    public void setEncodedUserProfileImage(String encodedUserProfileImage) {
        this.encodedUserProfileImage = encodedUserProfileImage;
    }

    public boolean isSharePosition() {
        return sharePosition;
    }

    public void setSharePosition(boolean sharePosition) {
        this.sharePosition = sharePosition;
    }

//region jsonignore
    @JsonIgnore
    Bitmap userProfilePicture;

    @JsonIgnore
    HashMap<String, String> friendsList = new HashMap<>();
    @JsonIgnore
    HashMap<String, String> seriesList = new HashMap<>();
    @JsonIgnore
    HashMap<String, String> friendsRequests = new HashMap<>();
    @JsonIgnore
    HashMap<String, Recommendation> seriesSuggestions = new HashMap<>();

    @JsonIgnore
    public HashMap<String, String> getFriendsList() {
        return friendsList;
    }

    @JsonIgnore
    public void setFriendsList(HashMap<String, String> friendsList) {
        this.friendsList = friendsList;
    }

    @JsonIgnore
    public HashMap<String, String> getSeriesList() {
        return seriesList;
    }

    @JsonIgnore
    public void setSeriesList(HashMap<String, String> seriesList) {
        this.seriesList = seriesList;
    }

    @JsonIgnore
    public HashMap<String, String> getFriendsRequests() {
        return friendsRequests;
    }

    @JsonIgnore
    public void setFriendsRequests(HashMap<String, String> friendsRequests) {
        this.friendsRequests = friendsRequests;
    }

    @JsonIgnore
    public HashMap<String, Recommendation> getSeriesSuggestions() {
        return seriesSuggestions;
    }

    @JsonIgnore
    public void setSeriesSuggestions(HashMap<String, Recommendation> seriesSuggestions) {
        this.seriesSuggestions = seriesSuggestions;
    }

    @JsonIgnore
    public void setUserProfileImage(Bitmap image){
        try {
            userProfilePicture = image;
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.WEBP, 100, bYtE);
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = com.firebase.client.utilities.Base64.encodeBytes(byteArray);
            encodedUserProfileImage = imageFile;
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @JsonIgnore
    public Bitmap getUserProfileImage(){
        try {
            if (userProfilePicture == null) {
                byte[] decodedByte = com.firebase.client.utilities.Base64.decode(encodedUserProfileImage);
                userProfilePicture = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            }
            return userProfilePicture;
        } catch(Exception e) {
            return null;
        }
    }
//endregion
}