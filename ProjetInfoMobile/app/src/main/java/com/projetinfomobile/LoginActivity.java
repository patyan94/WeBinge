package com.projetinfomobile;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import Controller.UserController;
import Interfaces.FirebaseInterface;
import Interfaces.FragmentInteractionListener;
import Model.UserModel;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements FragmentInteractionListener {

    UserController userController = UserController.Instance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        FirebaseInterface.Initialize(this);

        LoadFragment(LoginFragment.newInstance());
    }

    void LoadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getTag());
        fragmentTransaction.commit();

    }

    void ShowSnackBar(String message) {
        Snackbar.make(this.findViewById(R.id.login_activity_layout), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onLoginInteraction(Object data) {
        if (data.getClass() == FirebaseError.class) {
            FirebaseError error = (FirebaseError) data;
            ShowSnackBar(error.getMessage());
        }
        else if (data.getClass() == UserModel.class) {
            UserModel userModel = (UserModel) data;
            if (userModel.getUsername() == null) {
                LoadFragment(UserInfoCompletionFragment.newInstance());
            } else {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}

