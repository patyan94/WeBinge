package Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yannd on 2016-03-25.
 */
public class Serie {
    String name;
    String ID;
    String photoURL;
    String description;

    Serie(){}
    public Serie(String name, String ID, String photoURL) {
        this.name = name;
        this.ID = ID;
        this.photoURL = photoURL;
    }

    public Serie(String name, String ID, String photoURL, String description) {
        this.name = name;
        this.ID = ID;
        this.photoURL = photoURL;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Serie FromJSONObject(JSONObject json){
        Serie serie = new Serie();
        try {
            serie.name = json.getString("Title");
            serie.ID = json.getString("imdbID");
            serie.photoURL = json.getString("Poster");
            serie.description = json.getString("Plot");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return serie;
    }
}
