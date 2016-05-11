package com.projetinfomobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import Controller.UserController;
import Interfaces.FirebaseInterface;
import Interfaces.FragmentInteractionListener;

public class UserInfoCompletionFragment extends Fragment implements Observer {

    private static final String USER_CONTROLLER_PARAM1 = "userController";

    private UserController userController = UserController.Instance();;


    final static int SELECT_PHOTO = 1;
    Button continueButton;
    ImageView profilePicture;
    Button chooseProfilePictureButton;
    EditText usernameEntry;
    Bitmap selectedImage;

    private FragmentInteractionListener mListener;

    public UserInfoCompletionFragment() {
    }

    public static UserInfoCompletionFragment newInstance(

    ) {
        return new UserInfoCompletionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userController.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_info_completion, container, false);

        profilePicture = (ImageView) view.findViewById(R.id.profile_picture_view);

        // Button to select a profile picture
        chooseProfilePictureButton = (Button) view.findViewById(R.id.choose_profile_picture_button);
        chooseProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        // Button to continue to the main application
        continueButton = (Button) view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue();
            }
        });

        usernameEntry = (EditText) view.findViewById(R.id.username);
        usernameEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                continueButton.setEnabled(s.length() > 3);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userController.deleteObserver(this);
        mListener = null;
    }

    // Called when we click on the continue button
    void Continue() {
        // Checks the username which is mandatory
        final String username = usernameEntry.getText().toString();
        usernameEntry.setError(null);
        if (username.isEmpty()) {
            usernameEntry.setError("You must enter a valid username");
            return;
        }
        FirebaseInterface.Instance().GetUserNode()
        BitmapDrawable profilPictureDrawable = (BitmapDrawable) profilePicture.getDrawable();
        Bitmap profilePictureBMP = null;
        if (profilPictureDrawable != null) {
            profilePictureBMP = profilPictureDrawable.getBitmap();
        }
        userController.CreateNewUser(username, profilePictureBMP);
    }

    // Sets the profile picture
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        profilePicture.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        mListener.onLoginInteraction(data);
    }

}
