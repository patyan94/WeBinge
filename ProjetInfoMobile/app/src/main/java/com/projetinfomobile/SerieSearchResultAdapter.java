package com.projetinfomobile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import Model.DatabaseInterface;
import Model.Serie;

/**
 * Created by yannd on 2016-04-05.
 * Adapter for the serie search results
 */
public class SerieSearchResultAdapter extends RecyclerArrayAdapter<Serie, SerieSearchResultAdapter.ViewHolder> {

    RequestQueue imageRequests;
    private LayoutInflater inflater = null;
    private Context ctx;

    public SerieSearchResultAdapter(Context context){
        super(new ArrayList<Serie>());
        imageRequests = Volley.newRequestQueue(context);
        ctx = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.serie_search_result_listview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.BindSerie(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView posterView;
        Button addSerieButton;
        EditText searchSerieTitle ;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.serie_name);
            posterView = (ImageView)itemView.findViewById(R.id.serie_poster);
            addSerieButton =(Button)itemView.findViewById(R.id.add_serie_button);
            searchSerieTitle = (EditText) ((Activity) ctx).findViewById(R.id.search_serie_title);
        }
        public void BindSerie(final Serie serie) {
            title.setText(serie.getName());
            addSerieButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseInterface.Instance().AddSerie(serie.getID());

                    //Clearing the results when a serie is selected.
                    SerieSearchResultAdapter.this.clear();
                    searchSerieTitle.setText("");

                }
            });
            // Fetch the serie photo if there is one
            if (!serie.getPhotoURL().equalsIgnoreCase("N/A")) {
                ImageRequest request = new ImageRequest(serie.getPhotoURL(),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                posterView.setImageBitmap(bitmap);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                imageRequests.add(request);
            }
        }
    }
}
