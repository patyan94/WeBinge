package com.projetinfomobile;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Controller.UserController;
import Interfaces.FirebaseInterface;
import Model.OMDBInterface;
import Model.SerieModel;

public class SeriesFragment extends Fragment {

    public static class WatchedSerieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView posterView;
        Button removeSerieButton;
        Button recommendSerieButton;

        public WatchedSerieViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.serie_name);
            description = (TextView) itemView.findViewById(R.id.serie_description);
            posterView = (ImageView) itemView.findViewById(R.id.serie_poster);
            removeSerieButton = (Button) itemView.findViewById(R.id.remove_serie_button);
            recommendSerieButton = (Button) itemView.findViewById(R.id.recommend_serie_button);
        }
    }

    private UserController userController = UserController.Instance();
    Button searchSeriesButton;
    EditText searchSerieTitle;
    RecyclerView searchSerieResults;
    RecyclerView watchedSeriesListview;
    OMDBInterface omdbInterface;
    FirebaseRecyclerAdapter<String, WatchedSerieViewHolder> watchedSeriesAdapter;
    SerieSearchResultAdapter serieSearchResultAdapter;
    ArrayAdapter<String> autoCompleteFriendAdapter;
    List<String> autoCompleteFriendsSuggestions = new ArrayList<>();
    int currentSearchPage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Setup the autocomplete friend finder
        autoCompleteFriendAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, autoCompleteFriendsSuggestions);
        FirebaseInterface.Instance().GetFriendListNode(userController.GetUserModel().getUsername()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                autoCompleteFriendAdapter.add(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                autoCompleteFriendAdapter.remove(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        serieSearchResultAdapter = new SerieSearchResultAdapter(getContext());
        omdbInterface = OMDBInterface.Start(getContext());

        View view = inflater.inflate(R.layout.fragment_series, container, false);

        // Search serie button
        searchSeriesButton = (Button) view.findViewById(R.id.search_serie_button);
        searchSeriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serieSearchResultAdapter.clear();
                currentSearchPage = 1;
                omdbInterface.SearchSerie(searchSerieTitle.getText().toString().trim(), currentSearchPage, onSerieSearchResponse, onSerieSearchError);
            }
        });

        // The title of the serie to search
        searchSerieTitle = (EditText) view.findViewById(R.id.search_serie_title);
        searchSerieTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    serieSearchResultAdapter.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchSerieTitle.requestFocus();
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }

        // Setup the recycler views
        watchedSeriesListview = (RecyclerView) view.findViewById(R.id.series_listview);
        watchedSeriesListview.setHasFixedSize(true);
        watchedSeriesListview.setLayoutManager(new LinearLayoutManager(getContext()));
        searchSerieResults = (RecyclerView) view.findViewById(R.id.search_results_recyclerview);
        searchSerieResults.setHasFixedSize(true);
        searchSerieResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchSerieResults.setAdapter(serieSearchResultAdapter);


        // Populates the list with each serie watched
        watchedSeriesAdapter = new FirebaseRecyclerAdapter<String, WatchedSerieViewHolder>(String.class, R.layout.series_listview_item, WatchedSerieViewHolder.class, FirebaseInterface.Instance().GetSeriesListNode(userController.GetUserModel().getUsername())) {
            @Override
            protected void populateViewHolder(final WatchedSerieViewHolder view, final String serieID, int position) {
                Log.i("Populate", serieID);
                view.recommendSerieButton.setEnabled(false);
                view.removeSerieButton.setEnabled(false);
                view.removeSerieButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserController.Instance().DeleteSerie(serieID);
                    }
                });
                view.recommendSerieButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecommendSerie(serieID, view.title.getText().toString());
                    }
                });
                omdbInterface.GetSerieInfo(serieID, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("Response", serieID);

                            SerieModel serie = SerieModel.FromJSONObject(response);
                            view.title.setText(serie.getName());
                            view.description.setText(serie.getDescription());
                            if (!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                                omdbInterface.GetPoster(serie.getPhotoURL(), view.posterView);
                            }
                            view.recommendSerieButton.setEnabled(true);
                            view.removeSerieButton.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
            }
        };
        watchedSeriesListview.setAdapter(watchedSeriesAdapter);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        watchedSeriesAdapter.cleanup();
    }

    // Callback when we received the results of a serie search
    Response.Listener<JSONObject> onSerieSearchResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONArray results = response.getJSONArray("Search");
                for (int i = 0; i < results.length(); ++i) {
                    serieSearchResultAdapter.add(SerieModel.FromJSONObject(results.getJSONObject(i)));
                }
                int nbResults = response.getInt("totalResults");
                //Continue the search if we didnt get all the results
                if (serieSearchResultAdapter.getItemCount() < nbResults) {
                    omdbInterface.SearchSerie(searchSerieTitle.getText().toString(), ++currentSearchPage, onSerieSearchResponse, onSerieSearchError);
                } else {
                    if (getActivity().getCurrentFocus() != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS, 0);
                        inputMethodManager.toggleSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                    searchSeriesButton.setEnabled(true);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener onSerieSearchError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };

    // Shows a dialog to send a serie recommendation to multiple friends at the time
    void RecommendSerie(final String serieID, String serieName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Recommend " + serieName);

        final MultiAutoCompleteTextView input = new MultiAutoCompleteTextView(getContext());
        input.setAdapter(autoCompleteFriendAdapter);
        input.setThreshold(2);
        input.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] friends = input.getText().toString().split(", ");
                for (int i = 0; i < friends.length; ++i) {
                    userController.SendSerieSuggestion(friends[i], serieID);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.show();
    }
}
