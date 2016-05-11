package com.projetinfomobile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.util.ArrayList;

import Controller.UserController;
import Interfaces.FirebaseInterface;
import Model.UserModel;

// Fragment to show the friends and friend resuqests received
public class FriendsFragment extends Fragment {
    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView profilePicture;
        Button acceptButton;
        Button denyButton;

        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            profilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
            acceptButton = (Button) itemView.findViewById(R.id.accept);
            denyButton = (Button) itemView.findViewById(R.id.deny);
        }
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView profilePicture;
        Button deleteButton;

        public FriendViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            profilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
            deleteButton = (Button) itemView.findViewById(R.id.delete_friend_button);
        }
    }

    private UserController userController = UserController.Instance();
    RecyclerView friendRequestsListview;
    RecyclerView friendListview;
    FirebaseRecyclerAdapter<String, FriendViewHolder> friendsListAdapter;
    FirebaseRecyclerAdapter<String, FriendRequestViewHolder> friendsRequestListAdapter;
    ArrayAdapter<String> autoCompleteAdapter;

    com.firebase.client.Query usernameQuery;

    ArrayList<String> autoCompleteSuggestions = new ArrayList<>();

    AutoCompleteTextView friendSearchAutocomplete;
    Button addFriendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_friends, container, false);

        // Sends a friend request
        addFriendButton = (Button) fragmentView.findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < friendsListAdapter.getItemCount(); i++) {
                    if (friendsListAdapter.getItem(i).toString().equalsIgnoreCase(friendSearchAutocomplete.getText().toString())) {
                        Toast.makeText(getContext(), "This user is already in your friend list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                userController.SendFriendRequest(friendSearchAutocomplete.getText().toString());
                friendSearchAutocomplete.setText("");
                Toast.makeText(getContext(), "Invitation sent", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the autocomplete for friend search
        friendSearchAutocomplete = (AutoCompleteTextView) fragmentView.findViewById(R.id.friend_search_entry);
        autoCompleteAdapter = new ArrayAdapter<String>(FriendsFragment.this.getActivity(),
                android.R.layout.simple_dropdown_item_1line, autoCompleteSuggestions);
        friendSearchAutocomplete.setAdapter(autoCompleteAdapter);
        friendSearchAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addFriendButton.setEnabled(s.length() > 3);
            }
        });


        // Setup the recycler views
        friendListview = (RecyclerView) fragmentView.findViewById(R.id.friends_listview);
        friendListview.setHasFixedSize(true);
        friendListview.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup the view for the list of friend
        friendsListAdapter = new FirebaseRecyclerAdapter<String, FriendViewHolder>(String.class, R.layout.friends_listview_item, FriendViewHolder.class, FirebaseInterface.Instance().GetFriendListNode(userController.GetUserModel().getUsername())) {
            @Override
            protected void populateViewHolder(final FriendViewHolder view, final String username, int position) {
                Log.i("Populate", username);
                view.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.PromptUserSeries(username, getContext());
                    }
                });
                view.username.setText(username);
                view.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userController.DeleteFriend(username);
                    }
                });
                // Fetch the user photo
                FirebaseInterface.Instance().GetUserNode(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel userData = dataSnapshot.getValue(UserModel.class);
                        if (userData.getUserProfilePicture() != null) {
                            view.profilePicture.setImageBitmap(userData.getUserProfilePicture());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        };
        friendListview.setAdapter(friendsListAdapter);


        // Setup the recycler views
        friendRequestsListview = (RecyclerView) fragmentView.findViewById(R.id.friends_requests_listview);
        friendRequestsListview.setHasFixedSize(true);
        friendRequestsListview.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setups the view for the friend requests
        friendsRequestListAdapter = new FirebaseRecyclerAdapter<String, FriendRequestViewHolder>(String.class, R.layout.friend_request_listview_item, FriendRequestViewHolder.class, FirebaseInterface.Instance().GetReceivedFriendRequestsNode(userController.GetUserModel().getUsername())) {
            @Override
            protected void populateViewHolder(final FriendRequestViewHolder view, final String username, int position) {
                view.username.setText(username);
                view.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userController.AcceptFriendRequest(username);
                    }
                });
                view.denyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userController.RefuseFriendRequest(username);
                    }
                });

                // Fetch the user photo
                FirebaseInterface.Instance().GetUserNode(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel userData = dataSnapshot.getValue(UserModel.class);
                        if (userData.getUserProfilePicture() != null) {
                            view.profilePicture.setImageBitmap(userData.getUserProfilePicture());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        };
        friendRequestsListview.setAdapter(friendsRequestListAdapter);

        // Keeps the users list up to date for the autocomplete
        usernameQuery = FirebaseInterface.Instance().GetUsersNode();
        usernameQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    String val = (String) dataSnapshot.getValue();
                    if (!userController.GetUserModel().getUsername().equals(val))
                        autoCompleteAdapter.add(val);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    autoCompleteAdapter.remove((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        friendsListAdapter.cleanup();
        friendsRequestListAdapter.cleanup();
    }
}
