package Model;


import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.projetinfomobile.R;

import org.json.JSONObject;

/**
 * Created by yannd on 2016-04-05.
 * Interface to search on the series database
 */
public class OMDBInterface {
    final String URLSearchSerie = "http://www.omdbapi.com/?type=series&r=json&s=";
    final String URLSearchID = "http://www.omdbapi.com/?plot=short&r=json&i=";

    RequestQueue requestQueue;

    OMDBInterface(){

    }

    public static OMDBInterface Start(Context context){
        OMDBInterface instance = new OMDBInterface();
        instance.requestQueue = Volley.newRequestQueue(context);
        return instance;
    }

    // Finds a list of series matching the search
    public void SearchSerie(String title, int page, Response.Listener<JSONObject> responselistener, Response.ErrorListener errorListener){
        StringBuilder urlString = new StringBuilder(URLSearchSerie);
        urlString.append(title.replace(' ', '+'));
        urlString.append("&page="+page);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, urlString.toString(), null, responselistener, errorListener);
        requestQueue.add(jsObjRequest);
    }

    // Find s specific infos
    public void GetSerieInfo(String serieID, Response.Listener<JSONObject> responselistener, Response.ErrorListener errorListener){
        StringBuilder urlString = new StringBuilder(URLSearchID);
        urlString.append(serieID);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, urlString.toString(), null, responselistener, errorListener);
        requestQueue.add(jsObjRequest);
    }

    // Find a serie image
    public void GetPoster(String url, final ImageView view){
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(request);
    }
}
