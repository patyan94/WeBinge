package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannd on 2016-03-25.
 */
public class Recommendation {

    String serieID;
    List<String> users;

    Recommendation(){
        users = new ArrayList<>();
        serieID = "";
    }

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

    public void AddRecommendation(String user){
        RemoveRecommendation(user);
        users.add(user);
    }

    public void RemoveRecommendation(String user){
        users.remove(user);
    }
}
