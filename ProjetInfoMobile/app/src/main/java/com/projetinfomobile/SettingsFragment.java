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
import android.provider.ContactsContract;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Model.DatabaseInterface;


public class SettingsFragment extends Fragment {

    public static final int ACTIVITY_FOR_RESULOT_PHOTO_PICKER = 1;
    Button profilePictureSelectionButton;
    ImageView profilePictureView;
    CheckBox sharePosition;
    ArrayList<String> background_colors;

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
        sharePosition.setChecked(DatabaseInterface.Instance().getUserData().isSharePosition());
        sharePosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseInterface.Instance().getUserData().setSharePosition(isChecked);
                DatabaseInterface.Instance().SaveCurrentUserData();
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
        Bitmap profilePicture = DatabaseInterface.Instance().getUserData().getUserProfileImage();
        if(profilePicture != null)
            profilePictureView.setImageBitmap(profilePicture);

        // Setup the background color selection spinner
        background_colors = new ArrayList<String>(Arrays.asList(getActivity().getResources().getStringArray(R.array.background_colors)));
        Spinner spinner = (Spinner) view.findViewById(R.id.color_picking_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.background_colors, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String currentBackgroundColor = pref.getString("background-color", adapter.getItem(0).toString());
        spinner.setSelection(background_colors.indexOf(currentBackgroundColor));

        // Saves the selected item
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                ThemeUtils.SetBackgroundColor(PreferenceManager.getDefaultSharedPreferences(getContext()), item);
                ((MainActivity)getActivity()).SetTheme();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        DatabaseInterface.Instance().getUserData().setUserProfileImage(image);
                        DatabaseInterface.Instance().SaveCurrentUserData();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    public static class ThemeUtils {

        public static int GetBackgroundColor(SharedPreferences pref, final Resources res){
            HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
            colorMap.put(res.getString(R.string.default_background_color), Color.rgb(200,200,200));
            colorMap.put(res.getString(R.string.white_color), Color.WHITE);
            colorMap.put(res.getString(R.string.grey_color), Color.LTGRAY);
            colorMap.put(res.getString(R.string.dark_color), Color.DKGRAY);
            String currentBackgroundColor = pref.getString("background-color", res.getString(R.string.default_background_color));
            if(currentBackgroundColor == null || !colorMap.containsKey(currentBackgroundColor))
                return colorMap.get(res.getString(R.string.default_background_color));
            return colorMap.get(currentBackgroundColor);
        }

        public static void SetBackgroundColor(SharedPreferences pref, String color){
            // Saving selected theme color
            SharedPreferences.Editor edt = pref.edit();
            edt.putString("background-color", color);
            edt.commit();
        }
    }
}
