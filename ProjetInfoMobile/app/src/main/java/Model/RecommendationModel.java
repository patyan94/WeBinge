package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannd on 2016-03-25.
 */
public class RecommendationModel {

    String serieID = "";
    List<String> users = new ArrayList<>();

    public String getSerieID() {
        return serieID;
    }

    public void setSerieID(String serieID) {
        this.serieID = serieID;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public void AddRecommendation(String user) {
        RemoveRecommendation(user);
        users.add(user);
    }

    public void RemoveRecommendation(String user) {
        users.remove(user);
    }
}
