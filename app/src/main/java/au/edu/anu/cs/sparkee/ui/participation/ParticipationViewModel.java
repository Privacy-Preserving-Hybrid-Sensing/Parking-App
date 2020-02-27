package au.edu.anu.cs.sparkee.ui.participation;

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
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.DataHelper;
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.ui.map.MapViewModel;

public class ParticipationViewModel extends AndroidViewModel {

    private MutableLiveData<List<Participation>> mParticipation;
    String device_uuid;
    public ParticipationViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        mParticipation = new MutableLiveData<>();
        mParticipation.setValue(new ArrayList<Participation>());

        httpReceiver = new ParticipationViewModel.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);

        initData();
        startListeners();
    }

    private ParticipationViewModel.InternalHTTPBroadcaseReceiver httpReceiver;
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
    public void sendRequestParticipationsNumLast(int num_last_participation) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_PROFILE_PARTICIPATION_NUM_LAST, num_last_participation);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);
    }

    public LiveData<List<Participation>> getParticipation() {
        return mParticipation;
    }

    private void process_participations(JSONArray arr) throws  JSONException{
        ArrayList<Participation> pa = new ArrayList<Participation>();
        for (int i=0; i < arr.length(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            Participation item = new Participation();
            item.setId(jo.getInt("id"));
            item.setTs_update(jo.getString("ts_update"));
            item.setZone_id(jo.getInt("zone_id"));
            item.setZone_name(jo.getString("zone_name"));
            item.setSpot_id(jo.getInt("spot_id"));
            item.setSpot_name(jo.getString("spot_name"));
            item.setParticipation_value(jo.getInt("participation_value"));
            item.setIncentive_processed(jo.getBoolean("incentive_processed"));
            item.setIncentive_value(jo.getInt("incentive_value"));
            pa.add(item);

        }
        mParticipation.setValue(pa);
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
            if(path.matches("/api/profile/participations/\\d+")) {
                JSONArray ja = jo.getJSONArray("data");
                process_participations(ja);
            }

        }

        void onError(Context ctx, String msg) {
            Log.d("ERROR", msg);
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG );
        }
    }

}