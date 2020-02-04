package au.edu.anu.cs.sparkee.ui.map;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.model.ParkingSlot;
import au.edu.anu.cs.sparkee.model.ParticipantCredit;

public class MapViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private GpsMyLocationProvider mGpsMyLocationProvider;
    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private MutableLiveData<Location> mLocation;
    private MutableLiveData<ParkingSlot []> mParkingSlots;
    private MutableLiveData<ParticipantCredit[]> mParticipantCredit;

    final Context context;

    public void stopViewModel() {
        stopGPS();
        stopAMQPListener();
    }

    private void stopAMQPListener() {
        context.unregisterReceiver(receiver);
    }

    private void stopGPS() {
        if(mGpsMyLocationProvider != null) {
            mGpsMyLocationProvider.stopLocationProvider();
        }
    }

    public void startViewModel() {
        startGPS();
        startAMQPListener();
    }

    private void startGPS() {
        mGpsMyLocationProvider = new GpsMyLocationProvider(context);
        mGpsMyLocationProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                String date_time = formatter.format(date);
                if(location != null) {
                    Log.d("Long", "" + location.getLongitude());
                    Log.d("Lat", "" + location.getLatitude());
                    mLocation.setValue(location);
                }
                Log.d("Wkt", date_time);
            }
        });

    }

    private void startAMQPListener() {
        context.registerReceiver(receiver, intentFilter);
    }

    public MapViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        mLocation = new MutableLiveData<>();
        mLocation.setValue( null );

        mParkingSlots = new MutableLiveData<>();
        mParkingSlots.setValue( null );

        mParticipantCredit= new MutableLiveData<>();
        mParticipantCredit.setValue( null );

        receiver = new InternalAMQPBroadcaseReceiver();
        intentFilter = new IntentFilter(Constants.BROADCAST_ACTION_IDENTIFIER);

    }

    public LiveData<Location> getLocation() {
        return mLocation;
    }


    public LiveData<ParkingSlot []> getParkingSlots() {
        return mParkingSlots;
    }

    public LiveData<ParticipantCredit[]> getParticipantCredit() {
        return mParticipantCredit;
    }

    public InternalAMQPBroadcaseReceiver receiver;
    private IntentFilter intentFilter;


    public class InternalAMQPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_ACTION_IDENTIFIER);
            try {
                JSONObject jo = new JSONObject(msg);
                Log.d("InternapAMQP", msg);
                String payload_type = jo.getString("type");
                if(payload_type.equalsIgnoreCase("parking_slots")) {
                    process_parking_slots(jo.getJSONArray("payload"));
                }
                else if(payload_type.equalsIgnoreCase("participant_credit")) {
                    process_participant_credit(jo.getJSONArray("payload"));
                }
            }
            catch(JSONException je) {
                je.printStackTrace();
            }
        }

        void process_participant_credit(JSONArray ja) throws  JSONException {
            // TODO:
        }

        void process_parking_slots(JSONArray ja) throws  JSONException{

            ParkingSlot [] tmp_parkingSlots = new ParkingSlot[ja.length()];
            for(int i=0; i < ja.length() ; i++) {
                JSONObject jo = ja.getJSONObject(i);
                tmp_parkingSlots[i] = new ParkingSlot();
                tmp_parkingSlots[i].setId (jo.getInt("id"));
                tmp_parkingSlots[i].setLatitude(jo.getDouble("lattitude"));
                tmp_parkingSlots[i].setLongitude(jo.getDouble("longitude"));
                tmp_parkingSlots[i].setTs_register(jo.getString("ts_register"));
                tmp_parkingSlots[i].setTs_update(jo.getString("ts_update"));
                tmp_parkingSlots[i].setTotal_available(jo.getDouble("total_available"));
                tmp_parkingSlots[i].setTotal_unavailable(jo.getDouble("total_unavailable"));
                tmp_parkingSlots[i].setStatus(jo.getInt("status"));
                tmp_parkingSlots[i].setZone_id(jo.getInt("zone_id"));
                tmp_parkingSlots[i].setZone_name(jo.getString("zone_name"));
            }
            mParkingSlots.setValue(tmp_parkingSlots);

        }
    }
}