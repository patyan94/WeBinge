package com.projetinfomobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;

import Model.DatabaseInterface;
import Model.UserData;

public class UserInfoCompletionActivity extends AppCompatActivity {

    final static int SELECT_PHOTO = 1;
    Button continueButton;
    ImageView profilePicture;
    Button chooseProfilePictureButton;
    EditText usernameEntry;
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_completion);
        Firebase.setAndroidContext(this);

        profilePicture = (ImageView) findViewById(R.id.profile_picture_view);

        // Button to select a profile picture
        chooseProfilePictureButton = (Button)findViewById(R.id.choose_profile_picture_button);
        chooseProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        // Button to continue to the main application
        continueButton = (Button)findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue();
            }
        });

        usernameEntry = (EditText)findViewById(R.id.username);
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
    }

    @Override
    protected void onResume(){
        super.onResume();
        SetTheme();
    }

    // Called when we click on the continue button
    void Continue(){
        // Checks the username which is mandatory
        final String username = usernameEntry.getText().toString();
        usernameEntry.setError(null);
        if(username.isEmpty()) {
            usernameEntry.setError("You must enter a valid username");
            return;
        }
        // Creates a new user account
        DatabaseInterface.Instance().AddNewUSer(username, selectedImage, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    usernameEntry.setError("Username already used");
                } else {
                    // Go to main activity once the setup is complete
                    Intent intent = new Intent(UserInfoCompletionActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // Sets the profile picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        profilePicture.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public void SetTheme(){
        int color = SettingsFragment.ThemeUtils.GetBackgroundColor(PreferenceManager.getDefaultSharedPreferences(this), getResources());

        View layout = (View)findViewById(R.id.user_info_completion_layout);
        layout.setBackgroundColor(color);
    }

}
