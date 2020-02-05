package au.edu.anu.cs.sparkee.ui.map;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

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
import org.threeten.bp.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
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
                String payload_type = jo.getString("type");
                if(payload_type.equalsIgnoreCase("parking_slots")) {
                    process_parking_slots(jo.getJSONArray("payload"));
                }
                else if(payload_type.equalsIgnoreCase("participant_credits")) {
                    process_participant_credit(jo.getJSONArray("payload"));
                }
            }
            catch(JSONException je) {
                je.printStackTrace();
            }
        }

        void process_participant_credit(JSONArray ja) throws  JSONException {
            // TODO:
            ParticipantCredit [] tmp_participant_credit = new ParticipantCredit[ja.length()];
            for(int i=0; i < ja.length() ; i++) {
                JSONObject jo = ja.getJSONObject(i);
                tmp_participant_credit[i] = new ParticipantCredit();
                tmp_participant_credit[i].setId_participation(jo.getInt("id_participation"));
                tmp_participant_credit[i].setId_credit(jo.getInt("id_credit"));
                tmp_participant_credit[i].setTs_credit_update(jo.getString("ts_credit"));
                tmp_participant_credit[i].setTs_participation_update(jo.getString("ts_participation"));
                tmp_participant_credit[i].setLongitude( Double.parseDouble(jo.getString("longitude")));
                tmp_participant_credit[i].setLatitude( Double.parseDouble(jo.getString("latitude")));
                tmp_participant_credit[i].setAvailability_value(jo.getDouble("availability_value"));
                tmp_participant_credit[i].setCredit_value(jo.getDouble("credit_value"));
                tmp_participant_credit[i].setParticipation_processed(jo.getBoolean("participation_processed"));
                tmp_participant_credit[i].setCredit_processed(jo.getBoolean("credit_processed"));

            }
            mParticipantCredit.setValue(tmp_participant_credit);
        }

        void process_parking_slots(JSONArray ja) throws  JSONException{

            ParkingSlot [] tmp_parkingSlots = new ParkingSlot[ja.length()];
            for(int i=0; i < ja.length() ; i++) {
                JSONObject jo = ja.getJSONObject(i);
                tmp_parkingSlots[i] = new ParkingSlot();
                tmp_parkingSlots[i].setId(jo.getInt("id"));

                tmp_parkingSlots[i].setLongitude( Double.parseDouble(jo.getString("longitude")));
                tmp_parkingSlots[i].setLatitude( Double.parseDouble(jo.getString("latitude")));

                tmp_parkingSlots[i].setTs_register(jo.getString("ts_register"));
                tmp_parkingSlots[i].setTs_update(jo.getString("ts_update"));
                tmp_parkingSlots[i].setTotal_available(jo.getDouble("total_available"));
                tmp_parkingSlots[i].setTotal_unavailable(jo.getDouble("total_unavailable"));
                tmp_parkingSlots[i].setConfidence_level(jo.getDouble("confidence_level"));
                tmp_parkingSlots[i].setParking_status(jo.getInt("status"));
                tmp_parkingSlots[i].setZone_id(jo.getInt("zone_id"));
                tmp_parkingSlots[i].setZone_name(jo.getString("zone_name"));

                Pair <Integer, Integer> participation_data= getParticipationAndMarkerValue(
                        jo.getString("ts_update"),
                        jo.getInt("status"),
                        Double.parseDouble(jo.getString("latitude")),
                        Double.parseDouble(jo.getString("longitude"))
                );
                int participation_value = participation_data.first.intValue();
                int marker_value = participation_data.second.intValue();

                tmp_parkingSlots[i].setParticipation_status(participation_value);
                tmp_parkingSlots[i].setMarker_status(marker_value);
            }
            mParkingSlots.setValue(tmp_parkingSlots);
        }

        Pair <Integer, Integer> getParticipationAndMarkerValue(String last_parking_update_str, int parking_status, double latitude, double longitude) {
            ParticipantCredit [] participation = mParticipantCredit.getValue();
            int participation_value = 0;
            int marker_value = 0;
            LocalDateTime last_participation = LocalDateTime.MIN;
            LocalDateTime last_parking_update = LocalDateTime.parse(last_parking_update_str);

            if(participation != null) {
                for (int i = 0; i < participation.length; i++) {
                    if (participation[i].getLatitude() == latitude && participation[i].getLongitude() == longitude) {
                        participation_value += participation[i].getAvailability_value();
                        last_participation = participation[i].getTs_participation_update();
                        break;
                    }
                }
            }

//            Log.d("LAST_PARTICIPATION", last_participation.toString());
//            Log.d("LAST_PARKING UPDATE", last_parking_update.toString());
//            Log.d("PARKING STATUS", "" + parking_status);
            if(last_participation.isAfter(last_parking_update)) {
                if(participation_value > 0)
                    marker_value = Constants.MARKER_PARTICIPATION_AVAILABLE_RECEIVED;
                else
                    marker_value = Constants.MARKER_PARTICIPATION_UNAVAILABLE_RECEIVED;
            }
            else {
                switch (parking_status) {
                    case -3:
                        marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIRMED;
                        break;
                    case -2:
                        marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2;
                        break;
                    case -1:
                        marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1;
                        break;
                    case 0:
                        marker_value = Constants.MARKER_PARKING_UNCONFIRMED;
                        break;
                    case 1:
                        marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1;
                        break;
                    case 2:
                        marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2;
                        break;
                    case 3:
                        marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIRMED;
                        break;
                }
            }
            return new Pair<>(participation_value, marker_value);
        }
    }
}