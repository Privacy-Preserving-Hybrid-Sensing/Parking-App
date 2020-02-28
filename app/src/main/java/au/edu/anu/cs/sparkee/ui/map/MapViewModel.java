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

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.DataHelper;
import au.edu.anu.cs.sparkee.model.ParkingSpot;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.model.Participation;

public class MapViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private GpsMyLocationProvider mGpsMyLocationProvider;
    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private MutableLiveData<Location> mLocation;
    private MutableLiveData<HashMap<Integer,ParkingSpot>> mParkingSpots;
    private MutableLiveData<ParkingSpot> mChangedParkingSpot;
    private MutableLiveData<ParkingZone> mChangedParkingZone;

    private MutableLiveData<Integer> creditBalance;
    private MutableLiveData<HashMap<Integer,ParkingZone>> mParkingZones;

    private MutableLiveData<HashMap<Integer,Participation>> mParticipations;

    private MutableLiveData<Pair<Boolean, String>> serverConnectionEstablished;


    final Context context;
    private String device_uuid;

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
        initData();
        startGPS();
        startListeners();
    }

    public void sendRequestZoneSubscribe(int zone_id) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_ZONES_SUBSCRIBE, zone_id);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);

    }

    public void sendRequestZone(int zone_id) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_ZONES_ID, zone_id);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);
    }

    public void sendRequestZoneSpotsAll(int zone_id) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_ZONES_SPOTS_ALL, zone_id);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);
    }

    public void subscribeAsyncChannel(String subscribe_token) {
        Log.d("NGESUB", subscribe_token);
        DataHelper.getInstance(context).subscribeTopic(subscribe_token);
    }

    public void sendRequestZonesAll() {
        // Get Info All Zone
        String url = Constants.BASE_URL + Constants.URL_API_ZONES_ALL;
        DataHelper.getInstance(context).sendGet(url, device_uuid);

    }

    public void sendRequestProfileSummary() {
        // Get Profile Summary (credit value included)
        String url = Constants.BASE_URL + Constants.URL_API_PROFILE_SUMMARY;
        DataHelper.getInstance(context).sendGet(url, device_uuid);
    }

    public void sendRequestParticipation(int zone_id, int spot_id, String status) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_PARTICIPATE, zone_id, spot_id, status);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);
    }

    public void sendRequestProfileParticipationsLatest() {
        String url = Constants.BASE_URL + Constants.URL_API_PROFILE_PARTICIPATION_LATEST;
        DataHelper.getInstance(context).sendGet(url, device_uuid);
    }

    public void sendRequestProfileParticipationsDaysAgo(int days_ago) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(Constants.URL_API_PROFILE_PARTICIPATION_NUM_LAST, days_ago);
        DataHelper.getInstance(context).sendGet(Constants.BASE_URL + fmt.toString(), device_uuid);
    }

    private void initData() {

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");
        DataHelper.getInstance(context).subscribeTopic(device_uuid);
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
                    mLocation.setValue(location);
                }
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

        creditBalance = new MutableLiveData<>();
        creditBalance.setValue( 0 );

        hashmap_parkingSpot = new HashMap<Integer,ParkingSpot > ();

        mParkingSpots = new MutableLiveData<>();
        mParkingSpots.setValue( hashmap_parkingSpot );


        mChangedParkingSpot = new MutableLiveData<>();
        mChangedParkingSpot.setValue( new ParkingSpot() );

        mChangedParkingZone = new MutableLiveData<>();
        mChangedParkingZone.setValue( new ParkingZone() );

        hashmap_parkingZone = new HashMap<Integer,ParkingZone > ();
        mParkingZones = new MutableLiveData<>();
        mParkingZones.setValue( hashmap_parkingZone );

        hashmap_participation = new HashMap<Integer,Participation> ();
        mParticipations = new MutableLiveData<>();
        mParticipations.setValue( hashmap_participation );

        serverConnectionEstablished = new MutableLiveData<>();
        Pair<Boolean, String> pair = new Pair<Boolean, String>(false, "");
        serverConnectionEstablished.setValue( pair );

        amqpReceiver = new InternalAMQPBroadcaseReceiver();
        amqpIntentFilter = new IntentFilter(Constants.BROADCAST_AMQP_IDENTIFIER);

        httpReceiver = new InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);

    }

    public LiveData<Integer> getCreditBalance() {
        return creditBalance;
    }
    public LiveData<Location> getLocation() {
        return mLocation;
    }
    public LiveData<HashMap<Integer,ParkingSpot>> getParkingSpots() {
        return mParkingSpots;
    }

    public LiveData<ParkingSpot> getChangedParkingSpot() {
        return mChangedParkingSpot;
    }
    public LiveData<ParkingZone> getChangedParkingZone() {
        return mChangedParkingZone;
    }

    public LiveData<HashMap<Integer,Participation>> getParticipations() {
        return mParticipations;
    }

    public LiveData<HashMap<Integer, ParkingZone>> getParkingZones() {
        return mParkingZones;
    }
    public LiveData<Pair<Boolean, String>> getServerConnectionEstablished() {
        return serverConnectionEstablished;
    }

    private InternalAMQPBroadcaseReceiver amqpReceiver;
    private IntentFilter amqpIntentFilter;

    private InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    private HashMap<Integer,ParkingZone > hashmap_parkingZone;
    private HashMap<Integer,ParkingSpot > hashmap_parkingSpot;
    private HashMap<Integer,Participation> hashmap_participation;

    void onSuccess(String msg) throws JSONException{
        JSONObject obj = new JSONObject(msg);
        String path = obj.getString("path");
        if(path.matches("/api/profile/summary")) {
            JSONObject jo = obj.getJSONObject("data");
            process_profile_summary(jo);
        }
        else if(path.matches("/api/zones/all")) {
            JSONArray ja = obj.getJSONArray("data");
            process_zones_all(ja);
        }
        else if(path.matches("/api/zones/\\d+")) {
            JSONObject  ja = obj.getJSONObject("data");
            process_update_zone(ja);
        }
        else if(path.matches("/api/zones/\\d+/subscribe")) {
            JSONObject jo = obj.getJSONObject("data");
            process_zones_subscribe(jo);
            sendRequestProfileSummary();
            sendRequestZonesAll();
        }
        else if(path.matches("/api/zones/\\d+/spots/all")) {
            JSONArray ja = obj.getJSONArray("data");
            process_zones_spots_all(ja);
            sendRequestProfileParticipationsLatest();
        }

        else if(path.equalsIgnoreCase("/api/profile/participations/latest") || path.matches("/api/profile/participations/\\d+")) {
            JSONArray ja = obj.getJSONArray("data");
            process_participations(ja);
        }

        else if(path.matches("/api/participate/\\d+/\\d+/available") || path.matches("/api/participate/\\d+/\\d+/unavailable")) {
            JSONObject jo = obj.getJSONObject("data");
            process_participate(jo);
        }
        else if(path.matches("/api/zones/\\d+/spots/\\d+")) {
            JSONObject jo = obj.getJSONObject("data");
            process_update_parking_spot(jo);
        }
        else {
            Log.d("ELSE", msg);
        }

        serverConnectionEstablished.setValue(new Pair<Boolean, String>(true, "" ));
    }


    void process_update_parking_spot(JSONObject jo) throws  JSONException {
        int spot_id = jo.getInt("id");
        ParkingSpot ps = hashmap_parkingSpot.get(spot_id);
        Log.d("ISI SPOT", "" + spot_id);
        if(ps != null) {
            Log.d("ISI SPOT", "MASSSUUUUUUU");
            ps.setName(jo.getString("name"));
            ps.setTs_update(jo.getString("ts_update"));
            ps.setVote_available(jo.getInt("vote_available"));
            ps.setVote_unavailable(jo.getInt("vote_unavailable"));
            ps.setConfidence_level(jo.getDouble("confidence_level"));
            ps.setParking_status(jo.getInt("parking_status"));
            hashmap_parkingSpot.put(spot_id, ps);

            hashmap_participation.remove(ps.getId());
            mParticipations.setValue(hashmap_participation);

            mParkingSpots.setValue(hashmap_parkingSpot);
            mChangedParkingSpot.setValue(ps);
        }
    }


    void process_profile_summary(JSONObject jo) throws  JSONException {

        int credit_balance = jo.getInt("balance");
        Log.d("PROFIL", "" + credit_balance);
        creditBalance.setValue(credit_balance);
    }


    void process_participate(JSONObject jo) throws  JSONException {
        int participation_id = jo.getInt("id");
        Participation participation = hashmap_participation.get(participation_id);
        if(participation == null) {
            participation = new Participation();
        }
        participation.setTs_update(jo.getString("ts_update"));
        participation.setZone_id(jo.getInt("zone_id"));
        participation.setZone_name(jo.getString("zone_name"));
        participation.setSpot_id(jo.getInt("spot_id"));
        participation.setSpot_name(jo.getString("spot_name"));
        participation.setPrevious_value(jo.getInt("previous_value"));
        participation.setParticipation_value(jo.getInt("participation_value"));
        participation.setIncentive_processed(jo.getBoolean("incentive_processed"));
        participation.setIncentive_value(jo.getInt("incentive_value"));

        hashmap_participation.put(participation_id, participation);
        mParticipations.setValue(hashmap_participation);
    }

    void process_zones_subscribe(JSONObject jo) throws  JSONException {
        Log.d("SUBS", " SSSSSSSSSSSSSSSSSSSSSSSSssss");
    }


    void process_participations(JSONArray ja) throws  JSONException {
        hashmap_participation = new HashMap<Integer,Participation>();
        for(int i=0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            Participation tmp_participation = new Participation();

            tmp_participation.setId(jo.getInt("id"));
            tmp_participation.setTs_update(jo.getString("ts_update"));
            tmp_participation.setZone_id(jo.getInt("zone_id"));
            tmp_participation.setSpot_id(jo.getInt("spot_id"));
            tmp_participation.setParticipation_value(jo.getInt("participation_value"));
            tmp_participation.setIncentive_processed(jo.getBoolean("incentive_processed"));
            tmp_participation.setIncentive_value(jo.getInt("incentive_value"));

            hashmap_participation.put(tmp_participation.getId(), tmp_participation);
        }
        mParticipations.setValue(hashmap_participation);
    }

    void process_zones_spots_all(JSONArray ja) throws  JSONException {
        hashmap_parkingSpot = new HashMap<Integer,ParkingSpot>();
        for(int i=0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            ParkingSpot tmp_parkingSpot = new ParkingSpot();
            tmp_parkingSpot.setId(jo.getInt("id"));
            tmp_parkingSpot.setName(jo.getString("name"));

            tmp_parkingSpot.setTs_register(jo.getString("ts_register"));
            tmp_parkingSpot.setTs_update(jo.getString("ts_update"));

            tmp_parkingSpot.setRegistrar_uuid(jo.getString("registrar_uuid"));
            tmp_parkingSpot.setLongitude( Double.parseDouble(jo.getString("longitude")) );
            tmp_parkingSpot.setLatitude( Double.parseDouble(jo.getString("latitude")) );

            tmp_parkingSpot.setVote_available(jo.getInt("vote_available"));
            tmp_parkingSpot.setVote_unavailable(jo.getInt("vote_unavailable"));
            tmp_parkingSpot.setConfidence_level(jo.getDouble("confidence_level"));

            tmp_parkingSpot.setParking_status(jo.getInt("parking_status"));
            tmp_parkingSpot.setZone_id(jo.getInt("zone_id"));

            ParkingZone pz = hashmap_parkingZone.get(jo.getInt("zone_id"));
            tmp_parkingSpot.setZone_name(pz.getName());

            hashmap_parkingSpot.put(tmp_parkingSpot.getId(),tmp_parkingSpot);
        }
        mParkingSpots.setValue(hashmap_parkingSpot);
    }


    void process_update_zone(JSONObject jo) throws  JSONException {

        int zone_id = jo.getInt("id");
        ParkingZone tmp_parkingZone = hashmap_parkingZone.get(zone_id);
        Log.d("ISI ZONE", "" + zone_id);
        if(tmp_parkingZone != null) {

            tmp_parkingZone.setId(jo.getInt("id"));
            tmp_parkingZone.setName(jo.getString("name"));
            tmp_parkingZone.setSubscription_token(jo.getString("subscription_token"));
            tmp_parkingZone.setDescription(jo.getString("description"));
            tmp_parkingZone.setCenter_longitude(jo.getString("center_longitude"));
            tmp_parkingZone.setCenter_latitude(jo.getString("center_latitude"));
            tmp_parkingZone.setCredit_required(jo.getInt("credit_required"));
            tmp_parkingZone.setTs_update(jo.getString("ts_update"));
            tmp_parkingZone.setSubscribed(jo.getBoolean("subscribed"));
            tmp_parkingZone.setSpot_total(jo.getInt("spot_total"));
            tmp_parkingZone.setSpot_available(jo.getInt("spot_available"));
            tmp_parkingZone.setSpot_unavailable(jo.getInt("spot_unavailable"));
            tmp_parkingZone.setSpot_undefined(jo.getInt("spot_undefined"));

            hashmap_parkingZone.put(zone_id, tmp_parkingZone);
            mParkingZones.setValue(hashmap_parkingZone);

            mChangedParkingZone.setValue(tmp_parkingZone);
        }
    }


    void process_zones_all(JSONArray ja) throws  JSONException {
        hashmap_parkingZone = new HashMap<Integer,ParkingZone >();
        for(int i=0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            ParkingZone tmp_parkingZone = new ParkingZone();
            tmp_parkingZone.setId(jo.getInt("id"));
            tmp_parkingZone.setName(jo.getString("name"));
            tmp_parkingZone.setSubscription_token(jo.getString("subscription_token"));
            tmp_parkingZone.setDescription(jo.getString("description"));
            tmp_parkingZone.setCenter_longitude(jo.getString("center_longitude"));
            tmp_parkingZone.setCenter_latitude(jo.getString("center_latitude"));
            tmp_parkingZone.setCredit_required(jo.getInt("credit_required"));
            tmp_parkingZone.setTs_update(jo.getString("ts_update"));
            tmp_parkingZone.setSubscribed(jo.getBoolean("subscribed"));
            tmp_parkingZone.setSpot_total(jo.getInt("spot_total"));
            tmp_parkingZone.setSpot_available(jo.getInt("spot_available"));
            tmp_parkingZone.setSpot_unavailable(jo.getInt("spot_unavailable"));
            tmp_parkingZone.setSpot_undefined(jo.getInt("spot_undefined"));
            JSONArray ja2 = jo.getJSONArray("geopoints");
            List<GeoPoint> tmp_geopoints = new ArrayList<GeoPoint>();
            for(int j=0; j < ja2.length(); j++) {
                JSONObject jo2 = ja2.getJSONObject(j);
                GeoPoint tmp = new GeoPoint( Double.parseDouble(jo2.getString("latitude") ), Double.parseDouble(jo2.getString("longitude")));
                tmp_geopoints.add(tmp);
            }
            tmp_parkingZone.setGeoPoints(tmp_geopoints);
            hashmap_parkingZone.put(tmp_parkingZone.getId(),tmp_parkingZone);
        }
        mParkingZones.setValue(hashmap_parkingZone);
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
                        onError(jo.getString("msg"));

                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            else {
                onError("Conneting to Server failed ...");
            }
        }


        void onError(String msg) {
            serverConnectionEstablished.setValue(new Pair<Boolean, String>(false, msg ));
        }
    }

    public class InternalAMQPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_AMQP_IDENTIFIER);
            try {
                Log.d("ISI AMQP", msg);
                JSONObject jo = new JSONObject(msg);
                if(jo.getString("status").equalsIgnoreCase("OK"))
                    onSuccess(msg);
                else
                    onError(jo.getString("msg"));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }


        void onError(String msg) {
            serverConnectionEstablished.setValue(new Pair<Boolean, String>(false, msg ));
        }

    }


}