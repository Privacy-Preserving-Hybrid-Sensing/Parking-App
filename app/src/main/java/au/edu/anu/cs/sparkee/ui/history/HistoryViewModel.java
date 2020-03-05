package au.edu.anu.cs.sparkee.ui.history;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.DataHelper;
import au.edu.anu.cs.sparkee.model.History;
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.model.Subscription;

public class HistoryViewModel extends AndroidViewModel {

    private MutableLiveData<List<History>> mHistory;
    String device_uuid;
    public HistoryViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        mHistory = new MutableLiveData<>();
        mHistory.setValue(new ArrayList<History>());

        httpReceiver = new HistoryViewModel.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);

        initData();
        startListeners();
    }

    private HistoryViewModel.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    private void startListeners() {
        context.registerReceiver(httpReceiver, httpIntentFilter);
    }

    public void stopViewModel() {
        stopListeners();
    }
    private void stopListeners() {
        try {
            context.unregisterReceiver(httpReceiver);
        }
        catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    private void initData() {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");
        DataHelper.getInstance(context).subscribeTopic(device_uuid);
    }

    Context context;
    public void sendRequestHistoryNumLast(int num_last_history) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_PROFILE_HISTORY_NUM_LAST, num_last_history);
        DataHelper.getInstance(context).sendGet(fmt.toString(), device_uuid);
    }

    public LiveData<List<History>> getHistory() {
        return mHistory;
    }

    private void process_history(JSONArray arr) throws  JSONException{
        ArrayList<History> pa = new ArrayList<History>();
        for (int i=0; i < arr.length(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            String type = jo.getString("type");
            if(type.equalsIgnoreCase("subscription")) {
                Subscription item = new Subscription();
                item.setId(jo.getInt("id_subscription"));
                item.setTs_subscription(jo.getString("ts_subscription"));
                item.setZone_id(jo.getInt("zone_id"));
                item.setZone_name(jo.getString("zone_name"));
                item.setCharged(jo.getInt("charged"));
                item.setBalance(jo.getInt("balance"));
                pa.add(item);
            }
            else if(type.equalsIgnoreCase("participation")) {
                Participation item = new Participation();
                item.setId(jo.getInt("id_participation"));
                item.setTs_update(jo.getString("ts_update"));
                item.setZone_id(jo.getInt("zone_id"));
                item.setZone_name(jo.getString("zone_name"));
                item.setSpot_id(jo.getInt("spot_id"));
                item.setSpot_name(jo.getString("spot_name"));
                item.setPrevious_value(jo.getInt("previous_value"));
                item.setParticipation_value(jo.getInt("participation_value"));
                item.setIncentive_processed(jo.getBoolean("incentive_processed"));
                item.setIncentive_value(jo.getInt("incentive_value"));
                item.setBalance(jo.getInt("balance"));
                pa.add(item);
            }

        }
        mHistory.setValue(pa);
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
            if(path.matches("/api/profile/history/\\d+")) {
                JSONArray ja = jo.getJSONArray("data");
                process_history(ja);
            }

        }

        void onError(Context ctx, String msg) {
            Log.d("ERROR", msg);
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG ).show();
        }
    }

}