package au.edu.anu.cs.sparkee.ui.summary;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.DataHelper;

public class SummaryViewModel extends AndroidViewModel {

    private MutableLiveData<Map<String, String>> mSummary;

    private SummaryViewModel.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();

        mSummary = new MutableLiveData<>();
        mSummary.setValue(null);

        httpReceiver = new SummaryViewModel.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
        initData();
        startListeners();
        sendRequestProfileSummary();
    }

    public void stopViewModel() {
        stopListeners();
    }

    public void sendRequestProfileSummary() {
        // Get Profile Summary (credit value included)
        String url = Constants.URL_API_PROFILE_SUMMARY;
        DataHelper.getInstance(context).sendGet(url, device_uuid);
    }

    private void stopListeners() {
        try {
            context.unregisterReceiver(httpReceiver);
        }
        catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    public LiveData<Map<String, String>> getSummary() {
        return mSummary;
    }

    String device_uuid;
    Context context;
    private void initData() {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");
//        DataHelper.getInstance(context).subscribeTopic(device_uuid);
    }

    private void startListeners() {
        context.registerReceiver(httpReceiver, httpIntentFilter);
    }

    private void process_summary(JSONObject obj) throws  JSONException{
        Map<String, String> summary = new HashMap<String, String>();
        summary.put("participation", obj.getString("participation"));
        summary.put("balance", obj.getString("balance"));
        summary.put("incentive", obj.getString("incentive"));
        summary.put("charged", obj.getString("charged"));
        mSummary.setValue(summary);
    }

    public class InternalHTTPBroadcaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_HTTP_BODY_IDENTIFIER);
            String status = bundle.getString(Constants.BROADCAST_DATA_STATUS_IDENTIFIER);
            if(status.equalsIgnoreCase(Constants.BROADCAST_STATUS_OK)) {
                try {
                    Log.d("MSG", msg);
                    JSONObject jo = new JSONObject(msg);
                    if(jo.getString("status").equalsIgnoreCase("OK"))
                        onSuccess(msg);
                    else    // jo.getString("status").equalsIgnoreCase("ERR")
                        onError(context, msg);

                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            else {
                onError(context, msg);
            }
        }

        void onSuccess(String msg) throws JSONException {
            JSONObject jo = new JSONObject(msg);
            String path = jo.getString("path");

            Log.d("PATH", path);
            if(path.matches("/api/profile/summary")) {
                JSONObject obj = jo.getJSONObject("data");
                process_summary(obj);
            }

        }

        void onError(Context ctx, String msg) {
            Log.d("ERROR", msg);
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG ).show();
        }
    }
}