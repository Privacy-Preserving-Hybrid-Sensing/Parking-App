package au.edu.anu.cs.sparkee.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import au.edu.anu.cs.sparkee.Constants;

public class HTTPConnectionHelper extends IntentService {
    private static HTTPConnectionHelper singleton;

    public HTTPConnectionHelper() {
        // Used to name the worker thread, important only for debugging.
        super("HTTPConnectionHelper");
    }

    public static HTTPConnectionHelper getInstance() {
        if (singleton == null)
            singleton = new HTTPConnectionHelper();
        return singleton;
    }

    private Response.Listener okListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_HTTP_STATUS_IDENTIFIER, Constants.BROADCAST_HTTP_STATUS_OK);
            intent.putExtra(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER, response.toString());
            sendBroadcast(intent);
        }
    };

    private Response.ErrorListener errorListener =  new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_HTTP_STATUS_IDENTIFIER, Constants.BROADCAST_HTTP_STATUS_ERR);
            intent.putExtra(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER, error.getMessage());
            sendBroadcast(intent);
        }
    };

    public boolean sendPost(Context context, String url, final String device_uuid, String tag) {
        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject postparams = new JSONObject();
            postparams.put("city", "london");
            postparams.put("timestamp", "1500134255");
            JsonObjectRequest request = new JsonObjectRequest(url, postparams, okListener, errorListener) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Device-UUID", device_uuid);
                    return params;
                }
            };
            request.setTag(tag);
            queue.add(request);
        }
        catch(JSONException je) {
            je.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

    }
}