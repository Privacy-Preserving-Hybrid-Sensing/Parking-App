package au.edu.anu.cs.sparkee.ui.map;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.threeten.bp.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.HTTPConnectionHelper;
import au.edu.anu.cs.sparkee.model.ParkingSpot;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.model.ParticipantCredit;
import au.edu.anu.cs.sparkee.model.SParkeeJSONObject;

public class MapViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private GpsMyLocationProvider mGpsMyLocationProvider;
    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private MutableLiveData<Location> mLocation;
    private MutableLiveData<ParkingSpot[]> mParkingSlots;
    private MutableLiveData<Integer> creditValue;
    private MutableLiveData<ParkingZone []> mParkingZones;

    private MutableLiveData<Boolean> httpConnectionEstablished;
    private MutableLiveData<ParticipantCredit[]> mParticipantCredit;

    final Context context;

    public void stopViewModel() {
        stopGPS();
        stopListeners();
    }

    private void stopListeners() {
        try {
            context.unregisterReceiver(amqpReceiver);
            context.unregisterReceiver(httpReceiver);
        }
        catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    private void stopGPS() {
        if(mGpsMyLocationProvider != null) {
            mGpsMyLocationProvider.stopLocationProvider();
        }
    }

    public void startViewModel() {

        startGPS();
        startListeners();

        initData();
    }

    private void initData() {
        String url = "";

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        String device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");

        // Get Info All Zone
        url = Constants.BASE_URL + Constants.URL_API_ZONES_INFO_ALL;
        HTTPConnectionHelper.getInstance(context).sendPost(url, device_uuid);

        // Get Profile Credit Value

        url = Constants.BASE_URL + Constants.URL_API_PROFILE_CREDIT;
        HTTPConnectionHelper.getInstance(context).sendPost(url, device_uuid);
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
//                    Log.d("Long", "" + location.getLongitude());
//                    Log.d("Lat", "" + location.getLatitude());
                    mLocation.setValue(location);
                }
//                Log.d("Wkt", date_time);
            }
        });

    }

    private void startListeners() {
        context.registerReceiver(amqpReceiver, amqpIntentFilter);
        context.registerReceiver(httpReceiver, httpIntentFilter);
    }

    public MapViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        mLocation = new MutableLiveData<>();
        mLocation.setValue( null );

        creditValue= new MutableLiveData<>();
        creditValue.setValue( 0 );

        mParkingSlots = new MutableLiveData<>();
        mParkingSlots.setValue( null );

        mParticipantCredit= new MutableLiveData<>();
        mParticipantCredit.setValue( null );

        mParkingZones = new MutableLiveData<>();
        mParkingZones.setValue( null );

        httpConnectionEstablished= new MutableLiveData<>();
        httpConnectionEstablished.setValue( Boolean.TRUE );

        amqpReceiver = new InternalAMQPBroadcaseReceiver();
        amqpIntentFilter = new IntentFilter(Constants.BROADCAST_AMQP_IDENTIFIER);

        httpReceiver = new InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER);
    }

    public LiveData<Integer> getCreditValue() {
        return creditValue;
    }
    public LiveData<Location> getLocation() {
        return mLocation;
    }
    public LiveData<ParkingSpot[]> getParkingSlots() {
        return mParkingSlots;
    }
    public LiveData<ParticipantCredit[]> getParticipantCredit() {
        return mParticipantCredit;
    }
    public LiveData<ParkingZone[]> getParkingZones() {
        return mParkingZones;
    }
    public LiveData<Boolean> getHttpConnectionEstablished() {
        return httpConnectionEstablished;
    }

    private InternalAMQPBroadcaseReceiver amqpReceiver;
    private IntentFilter amqpIntentFilter;

    private InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    public class InternalHTTPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_HTTP_BODY_IDENTIFIER);
            String status = bundle.getString(Constants.BROADCAST_HTTP_STATUS_IDENTIFIER);
            if(status.equalsIgnoreCase(Constants.BROADCAST_HTTP_STATUS_OK)) {
                try {
                    Log.d("MSG", msg);
                    onSuccess(SParkeeJSONObject.parse(msg));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            else {
                onError(msg);
            }
        }

        void onSuccess(SParkeeJSONObject jo) throws JSONException{
            JSONArray data = jo.getData();
            if(jo.getPath().equalsIgnoreCase(Constants.URL_API_ZONES_INFO_ALL)) {
                process_parking_zones(data);
            }
            else if(jo.getPath().equalsIgnoreCase(Constants.URL_API_PROFILE_CREDIT)) {
                process_profile_credit(data);
            }
            else if(jo.getPath().startsWith(Constants.URL_API_ZONES_DETAIL)) {
                process_parking_zone_details(data);
            }
            httpConnectionEstablished.setValue(Boolean.TRUE);
        }

        void onError(String msg) {
            Log.d("ERROR", msg);
            httpConnectionEstablished.setValue(Boolean.FALSE);
        }

        void process_profile_credit(JSONArray ja) throws  JSONException {
            JSONObject jo = ja.getJSONObject(0);
            int credit_value = jo.getInt("credit_value");
            creditValue.setValue(credit_value);
        }

        void process_parking_zone_details(JSONArray ja) throws JSONException{

            ParkingZone [] update_parking_zone = mParkingZones.getValue();
            if(update_parking_zone == null)
                return;

            for(int k=0; k < ja.length() ; k++) {

                JSONObject parking_data_obj = ja.getJSONObject(k);

                int id = parking_data_obj.getInt("id");
                boolean authorized = parking_data_obj.getBoolean("authorized");
                LocalDateTime ts_update = LocalDateTime.parse(parking_data_obj.getString("ts_update"));
                ParkingZone existing_pz = findParkingZoneByID(id);

                JSONArray parking_spots = parking_data_obj.getJSONArray("parking_spots");
//                Log.d("VS VS", existing_pz.getTs_update().toString() + " vs " + ts_update.toString() + " STATUS: " + existing_pz.getTs_update().isBefore(ts_update));
                if(existing_pz  != null && parking_spots.length() > 0) { // && existing_pz.getTs_update().isBefore(ts_update) ) {
                    existing_pz.setAuthorized(authorized);
                    existing_pz.setTs_update(ts_update);

                    Log.d("ZZON2", existing_pz.getCenter_latitude() + existing_pz.getCenter_longitude());

                    ParkingSpot [] new_parking_spots = new ParkingSpot[parking_spots.length()];
                    for(int j = 0; j< parking_spots.length(); j++ ) {
                        ParkingSpot tmp_parking_spot = new ParkingSpot();
                        JSONObject obj_parking_spot = parking_spots.getJSONObject(j);
                        tmp_parking_spot.setId( obj_parking_spot.getInt("id") );
                        tmp_parking_spot.setTs_register( obj_parking_spot.getString("ts_register") );
                        tmp_parking_spot.setTs_update( obj_parking_spot.getString("ts_update") );
                        tmp_parking_spot.setVoting_available( obj_parking_spot.getDouble("voting_available") );
                        tmp_parking_spot.setVoting_unavailable( obj_parking_spot.getDouble("voting_unavailable") );

                        tmp_parking_spot.setName( obj_parking_spot.getString("name") );
                        tmp_parking_spot.setZone_name( existing_pz.getName() );
                        tmp_parking_spot.setZone_id( existing_pz.getId() );

                        tmp_parking_spot.setLatitude(Double.parseDouble(obj_parking_spot.getString("latitude")));
                        tmp_parking_spot.setLongitude(Double.parseDouble(obj_parking_spot.getString("longitude")));

                        tmp_parking_spot.setVoting_unavailable( obj_parking_spot.getDouble("voting_unavailable") );

                        Pair <Integer, Integer> participation_data= getParticipationAndMarkerValue(
                                obj_parking_spot.getString("ts_update"),
                                obj_parking_spot.getInt("status"),
                                Double.parseDouble(obj_parking_spot.getString("latitude")),
                                Double.parseDouble(obj_parking_spot.getString("longitude"))
                        );
                        int participation_value = participation_data.first.intValue();
                        int marker_value = participation_data.second.intValue();

                        tmp_parking_spot.setParticipation_status(participation_value);
                        tmp_parking_spot.setMarker_status(marker_value);
                        tmp_parking_spot.setParking_status( obj_parking_spot.getInt("status") );
                        tmp_parking_spot.setConfidence_level(obj_parking_spot.getDouble("confidence_level") );
                        new_parking_spots[j] = tmp_parking_spot;

                    }
                    Log.d("PARKING SPOT CNT", "" + new_parking_spots.length);
                    existing_pz.setParking_spots(new_parking_spots);
                }

                update_parking_zone [k] = existing_pz;
            }
            mParkingZones.setValue(update_parking_zone);
        }

        private ParkingZone findParkingZoneByID(int id) {
            ParkingZone ret = null;
            ParkingZone [] parkingZones = mParkingZones.getValue();
            if(parkingZones != null) {
                for (ParkingZone tmp: parkingZones) {
                    if(tmp != null && tmp.getId() == id) {
                        ret = tmp;
                        break;
                    }
                }
            }
            return ret;
        }

        void process_parking_zones(JSONArray ja) throws  JSONException {
            ParkingZone [] tmp_parkingZone = new ParkingZone[ja.length()];
            for(int i=0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                Log.d("ZONZON", jo.getString("center_longitude") + jo.getString("center_latitude"));
                tmp_parkingZone[i] = new ParkingZone();
                tmp_parkingZone[i].setId(jo.getInt("id"));
                tmp_parkingZone[i].setName(jo.getString("name"));
                tmp_parkingZone[i].setDescription(jo.getString("description"));
                tmp_parkingZone[i].setCenter_longitude(jo.getString("center_longitude"));
                tmp_parkingZone[i].setCenter_latitude(jo.getString("center_latitude"));
                tmp_parkingZone[i].setCredit_charge(jo.getInt("credit_charge"));
                tmp_parkingZone[i].setTs_update(jo.getString("ts_update"));
                tmp_parkingZone[i].setAuthorized(jo.getBoolean("authorized"));

                JSONArray ja2 = jo.getJSONArray("geopoints");
                List<GeoPoint> tmp_geopoints = new ArrayList<GeoPoint>();
                for(int j=0; j < ja2.length(); j++) {
                    JSONObject jo2 = ja2.getJSONObject(j);
                    GeoPoint tmp = new GeoPoint( Double.parseDouble(jo2.getString("latitude") ), Double.parseDouble(jo2.getString("longitude")));
                    tmp_geopoints.add(tmp);
                }
                tmp_parkingZone[i].setGeoPoints(tmp_geopoints);
//                Log.d("NAME", tmp_parkingZone[i].getName());
            }
            mParkingZones.setValue(tmp_parkingZone);
        }


    }


    public class InternalAMQPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_AMQP_IDENTIFIER);
            try {
                JSONObject jo = new JSONObject(msg);
                String payload_type = jo.getString("type");
                if(payload_type.equalsIgnoreCase("parking_slots")) {
                    process_parking_slots(jo.getJSONArray("payload"));
                }
                else if(payload_type.equalsIgnoreCase("participant_credits")) {
                    process_participant_credit(jo.getJSONArray("payload"));
                }
//                else if(payload_type.equalsIgnoreCase("parking_zones")) {
//                    process_parking_zones(jo.getJSONArray("payload"));
//                }
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

            ParkingSpot[] tmp_parkingSpots = new ParkingSpot[ja.length()];
            for(int i=0; i < ja.length() ; i++) {
                JSONObject jo = ja.getJSONObject(i);
                tmp_parkingSpots[i] = new ParkingSpot();
                tmp_parkingSpots[i].setId(jo.getInt("id"));

                tmp_parkingSpots[i].setLongitude( Double.parseDouble(jo.getString("longitude")));
                tmp_parkingSpots[i].setLatitude( Double.parseDouble(jo.getString("latitude")));

                tmp_parkingSpots[i].setTs_register(jo.getString("ts_register"));
                tmp_parkingSpots[i].setTs_update(jo.getString("ts_update"));
                tmp_parkingSpots[i].setVoting_available(jo.getDouble("total_available"));
                tmp_parkingSpots[i].setVoting_unavailable(jo.getDouble("total_unavailable"));
                tmp_parkingSpots[i].setConfidence_level(jo.getDouble("confidence_level"));
                tmp_parkingSpots[i].setParking_status(jo.getInt("status"));
                tmp_parkingSpots[i].setZone_id(jo.getInt("zone_id"));
                tmp_parkingSpots[i].setZone_name(jo.getString("zone_name"));

                Pair <Integer, Integer> participation_data=getParticipationAndMarkerValue(
                        jo.getString("ts_update"),
                        jo.getInt("status"),
                        Double.parseDouble(jo.getString("latitude")),
                        Double.parseDouble(jo.getString("longitude"))
                );
                int participation_value = participation_data.first.intValue();
                int marker_value = participation_data.second.intValue();

                tmp_parkingSpots[i].setParticipation_status(participation_value);
                tmp_parkingSpots[i].setMarker_status(marker_value);
            }
            mParkingSlots.setValue(tmp_parkingSpots);
        }

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
                    marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT;
                    break;
                case -2:
                    marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT;
                    break;
                case -1:
                    marker_value = Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT;
                    break;
                case 0:
                    marker_value = Constants.MARKER_PARKING_UNCONFIRMED_DEFAULT;
                    break;
                case 1:
                    marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT;
                    break;
                case 2:
                    marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT;
                    break;
                case 3:
                    marker_value = Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT;
                    break;
            }
        }
        return new Pair<>(participation_value, marker_value);
    }

}