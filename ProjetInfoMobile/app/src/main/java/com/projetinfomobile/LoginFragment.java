package com.projetinfomobile;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.FirebaseError;

import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import Controller.UserController;
import Interfaces.FragmentInteractionListener;

public class LoginFragment extends Fragment implements Observer {

    private UserController userController = UserController.Instance();

    private EditText mUsernameEntry;
    private EditText mPasswordEntry;
    private View mProgressView;

    private FragmentInteractionListener mListener;

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userController.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Puts an email from your contact list in the email field
        String possibleEmail = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                break;
            }
        }

        // Set up the login form.
        mUsernameEntry = (EditText) view.findViewById(R.id.username);
        mUsernameEntry.setText(possibleEmail, TextView.BufferType.EDITABLE);

        mPasswordEntry = (EditText) view.findViewById(R.id.password);
        mPasswordEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(false);
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) view.findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(false);
            }
        });

        mProgressView = view.findViewById(R.id.login_progress);

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

    @Override
    public void onResume() {
        super.onResume();
        showProgress(false);
        mUsernameEntry.requestFocus();
        userController.Logout();
    }

    private void attemptLogin(boolean newAccount) {

        // Reset errors.
        mUsernameEntry.setError(null);
        mPasswordEntry.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameEntry.getText().toString();
        String password = mPasswordEntry.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordEntry.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEntry;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameEntry.setError(getString(R.string.error_field_required));
            focusView = mUsernameEntry;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (newAccount) {
                userController.Signin(email, password);
            } else {
                userController.Login(email, password);
            }
        }
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data.getClass() == FirebaseError.class) {
            FirebaseError error = (FirebaseError) data;
            if (error.getCode() == FirebaseError.USER_DOES_NOT_EXIST) {
                attemptLogin(true);
                return;
            }
        }
        mListener.onLoginInteraction(data);

    }

    public void ShowSnackBar(String message) {
        Snackbar.make(this.getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
