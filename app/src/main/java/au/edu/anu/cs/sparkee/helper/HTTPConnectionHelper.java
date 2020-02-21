package au.edu.anu.cs.sparkee.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import au.edu.anu.cs.sparkee.Constants;

public class HTTPConnectionHelper extends IntentService {
    private static HTTPConnectionHelper singleton;
    private static Context context;

    public HTTPConnectionHelper() {
        // Used to name the worker thread, important only for debugging.
        super("HTTPConnectionHelper");
    }

    public static HTTPConnectionHelper getInstance(Context ctx) {
        if (singleton == null)
            singleton = new HTTPConnectionHelper();
        context = ctx;
        return singleton;
    }

    private Response.Listener okListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String msg) {
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_DATA_STATUS_IDENTIFIER, Constants.BROADCAST_STATUS_OK);
            intent.putExtra(Constants.BROADCAST_HTTP_BODY_IDENTIFIER, msg);
            Log.d("RESP", msg);
            context.sendBroadcast(intent);
        }
    };

    private Response.ErrorListener errorListener =  new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("EROR", error.toString());
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_DATA_STATUS_IDENTIFIER, Constants.BROADCAST_STATUS_ERR);
            intent.putExtra(Constants.BROADCAST_HTTP_BODY_IDENTIFIER, error.toString());
            context.sendBroadcast(intent);
        }
    };


    public String sendPost(String url, String device_uuid) {
        Map<String, String> arg = new HashMap<>();
        arg.put("subscriber_uuid", device_uuid);
        Random rand = new Random();
        String trx_id = "" + rand.nextInt(10000);
        arg.put("trx_id", trx_id);
        sendPost(url, arg);
        return trx_id;
    }

    public boolean sendPost(String url, final Map<String, String> params) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url, okListener, errorListener) {
            protected Map<String, String> getParams()  {
                return params;
            };
        };
        queue.add(request);
        return true;
    }

    public boolean sendGet(String url) {
        Log.d("GET", url);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, url, okListener, errorListener);
        queue.add(request);
        return true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

    }
}