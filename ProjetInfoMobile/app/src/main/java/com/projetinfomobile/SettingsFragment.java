package com.projetinfomobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Controller.UserController;


public class SettingsFragment extends Fragment {

    public static final int ACTIVITY_FOR_RESULOT_PHOTO_PICKER = 1;
    Button profilePictureSelectionButton;
    ImageView profilePictureView;
    CheckBox sharePosition;
    ArrayList<String> background_colors;
    private UserController userController = UserController.Instance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profilePictureView = (ImageView)view.findViewById(R.id.profile_picture);

        sharePosition = (CheckBox) view.findViewById(R.id.share_position_checkbox);
        sharePosition.setChecked(userController.GetUserModel().isSharePosition());
        sharePosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userController.SharePosition(isChecked);
            }
        });

        profilePictureSelectionButton = (Button)view.findViewById(R.id.profile_picture_selection);
        profilePictureSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, ACTIVITY_FOR_RESULOT_PHOTO_PICKER);
            }
        });
        Bitmap profilePicture = userController.GetUserModel().getUserProfilePicture();
        if(profilePicture != null)
            profilePictureView.setImageBitmap(profilePicture);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Used for settings and the photo selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case ACTIVITY_FOR_RESULOT_PHOTO_PICKER:
                if(resultCode == getActivity().RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        final Bitmap image = BitmapFactory.decodeStream(imageStream);
                        profilePictureView.setImageBitmap(image);
                        userController.SetProfilePicture(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
