package Model;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yannd on 2016-04-06.
 */
public class CustomChildEventListener<T> implements ChildEventListener {
    Class<T> typeParameterClass;
    HashMap<String, T> data;

    public CustomChildEventListener(Class<T> typeParameterClass, HashMap<String, T> data){
        this.typeParameterClass= typeParameterClass;
        this.data = data;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        data.put(dataSnapshot.getKey(), dataSnapshot.getValue(typeParameterClass));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        data.remove(dataSnapshot.getKey());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
