package au.edu.anu.cs.sparkee.ui.map.infowindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.HTTPConnectionHelper;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.model.ParkingZoneDetail;
import au.edu.anu.cs.sparkee.model.SParkeeJSONObject;


public class ParkingZoneInfoWindow extends InfoWindow {

    private String device_uuid;
    private ParkingZone parkingZone;

    public ParkingZoneInfoWindow(int layoutResId, MapView mapView, String device_uuid, ParkingZone parkingZone) {
        super(layoutResId, mapView);
        Log.d("RESID", "" + layoutResId);
        this.device_uuid = device_uuid;
        this.parkingZone = parkingZone;

        httpReceiver = new ParkingZoneInfoWindow.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER);
    }

    public void onClose() {
        super.close();
    }

    private String url_identifier;


    public class InternalHTTPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_HTTP_BODY_IDENTIFIER);
            String status = bundle.getString(Constants.BROADCAST_HTTP_STATUS_IDENTIFIER);
            if (status.equalsIgnoreCase(Constants.BROADCAST_HTTP_STATUS_OK)) {
                try {
                    onSuccess(SParkeeJSONObject.parse(msg));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                onError(msg);
            }
        }

        void onSuccess(SParkeeJSONObject jo) throws JSONException {
            if(jo.getPath().startsWith(Constants.URL_API_ZONES_DETAIL)) {
                processZoneDetail(jo);
            }
            else if(jo.getPath().startsWith(Constants.URL_API_ZONES_SUBSCRIBE)) {
                processZoneSubscribe(jo);
            }

        }

        void processZoneSubscribe(SParkeeJSONObject jo) throws  JSONException {

        }
        void processZoneDetail(SParkeeJSONObject jo) throws  JSONException{

            if (mView==null) {
                Log.d("ERR", "Error trapped, BasicInfoWindow.open, mView is null!");
                return;
            }

            Log.d("SUCCESS", jo.toString());
            JSONArray data = jo.getData();
            JSONObject obj = data.getJSONObject(0);

            ParkingZoneDetail pzd = new ParkingZoneDetail();
            JSONArray parkingSpots = obj.getJSONArray("parking_spots");
            LocalDateTime ts_update = LocalDateTime.parse(obj.getString("ts_update"));

            int total = 0;
            int available = 0;
            int unavailable = 0;
            int undefined = 0;
            for (int i=0; i < parkingSpots.length(); i++) {
                JSONObject item = parkingSpots.getJSONObject(i);
                total++;
                switch (item.getInt("status")) {
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1:
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2:
                    case Constants.MARKER_PARKING_UNCONFIRMED:
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1:
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2:
                        undefined++;
                        break;
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIRMED:
                        available++;
                        break;
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIRMED:
                        unavailable++;
                        break;
                }
            }

            TextView txtParkingSpotTotal        = (TextView) mView.findViewById(R.id.total);
            TextView txtParkingSpotAvailable    = (TextView) mView.findViewById(R.id.bubble_parking_spot_available);
            TextView txtParkingSpotUnavailable  = (TextView) mView.findViewById(R.id.bubble_parking_spot_unavailable);
            TextView txtParkingSpotUndefined    = (TextView) mView.findViewById(R.id.bubble_parking_spot_undefined);
            TextView txtLastUpdate              = (TextView) mView.findViewById(R.id.bubble_last_update);

            PrettyTime p = new PrettyTime();
            String date_str = ts_update.atZone(ZoneId.systemDefault()).toLocalDate().toString();
            String time_str = ts_update.atZone(ZoneId.systemDefault()).toLocalTime().toString();
            String time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));

            txtLastUpdate.setText(time_pretty);

            txtParkingSpotTotal.setText("" +  total);
            txtParkingSpotAvailable.setText("" + available);
            txtParkingSpotUnavailable.setText("" + unavailable);
            txtParkingSpotUndefined.setText("" + undefined);

        }

        void onError(String msg) {
            Log.d("ERROR", msg);
        }
    }

    @Override
    public void onOpen(Object arg0) {

        if (mView==null) {
            Log.d("ERR", "Error trapped, BasicInfoWindow.open, mView is null!");
            return;
        }

        Log.d("MVIEW", mView.toString());
        mIsVisible = true;
        LinearLayout bubble_parking_zone_layout = (LinearLayout) mView.findViewById(R.id.bubble_parking_zone_layout);
        LinearLayout layout_btn = (LinearLayout) mView.findViewById(R.id.layout_btn);

        TextView txtName                    = (TextView) mView.findViewById(R.id.bubble_name);
        TextView txtDescription             = (TextView) mView.findViewById(R.id.bubble_description);
        Button btnUseCredit                 = (Button) mView.findViewById(R.id.bubble_use_credit);
        TextView txtLastUpdate              = (TextView) mView.findViewById(R.id.bubble_last_update);
        TableLayout layout_table_information = (TableLayout) mView.findViewById(R.id.layout_table_information);

        PrettyTime p = new PrettyTime();
        String date_str = parkingZone.getTs_update().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String time_str = parkingZone.getTs_update().atZone(ZoneId.systemDefault()).toLocalTime().toString();
        String time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));

        txtLastUpdate.setText(time_pretty);

        txtName.setText(parkingZone.getName());
        txtDescription.setText(parkingZone.getDescription());
        if(parkingZone.isAuthorized()) {
            layout_table_information.setVisibility(View.VISIBLE);
            layout_btn.setVisibility(View.GONE);
        }
        else {
            btnUseCredit.setText("Use Credit (" + parkingZone.getCredit_charge()+ ")");
            btnUseCredit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.d("Btn", "Use Credit");

                    Map<String, String> arg = new HashMap<>();
                    arg.put("subscriber_uuid", device_uuid);
                    url_identifier = Constants.URL_API_ZONES_SUBSCRIBE+ "/" + parkingZone.getId();
                    HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(Constants.BASE_URL + url_identifier, arg);

                    onClose();
                }
            });
            layout_table_information.setVisibility(View.GONE);
            layout_btn.setVisibility(View.VISIBLE);
        }

        mMapView.getContext().registerReceiver(httpReceiver, httpIntentFilter);


        Map<String, String> arg = new HashMap<>();
        arg.put("subscriber_uuid", device_uuid);
        url_identifier = Constants.URL_API_ZONES_DETAIL + "/" + parkingZone.getId();
        HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(Constants.BASE_URL + url_identifier, arg);

    }

    private ParkingZoneInfoWindow.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    public void send(String msg, GeoPoint geoPoint) {
    }


}