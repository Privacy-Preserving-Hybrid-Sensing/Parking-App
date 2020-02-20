package au.edu.anu.cs.sparkee.ui.map.infowindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.HTTPConnectionHelper;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.model.SParkeeJSONObject;
import au.edu.anu.cs.sparkee.ui.map.marker.ParkingZonePolygon;


public class ParkingZoneInfoWindow extends InfoWindow {

    private String device_uuid;
    private ParkingZonePolygon polygon;
    private ParkingZone parkingZone;

    public ParkingZoneInfoWindow(int layoutResId, MapView mapView, String device_uuid, ParkingZonePolygon polygon) {
        super(layoutResId, mapView);
        Log.d("RESID", "" + layoutResId);
        this.device_uuid = device_uuid;
        this.polygon = polygon;
        this.parkingZone = polygon.getParkingZone();

        httpReceiver = new ParkingZoneInfoWindow.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_HTTP_RESPONSE_IDENTIFIER);
    }

    public void onClose() {
        super.close();
        try {
            mMapView.getContext().unregisterReceiver(httpReceiver);
        }
        catch(Exception e) {
//            e.printStackTrace();
        }

    }

    private String url_identifier_detail;
    private String url_identifier_credit;

    String trx_id_subscribe = "";
    String trx_id_detail = "";
    String trx_id_credit = "";
    private long DEFAULT_DELAY_ANIMATION = 500;

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
            Log.d("PATH", jo.getPath() + " vs " + url_identifier_detail);
            Log.d("ZONE TRX ID", jo.getTrx_id() + " vs " + trx_id_detail);
            if(jo.getPath().startsWith(Constants.URL_API_ZONES_DETAIL) && jo.getTrx_id().equalsIgnoreCase(trx_id_detail)) {
                processZoneDetail(jo);
            }
            else if(jo.getPath().startsWith(Constants.URL_API_ZONES_SUBSCRIBE) && jo.getTrx_id().equalsIgnoreCase(trx_id_subscribe)) {
                processZoneSubscribe(jo);
            }

        }

        void processZoneSubscribe(SParkeeJSONObject jo) throws  JSONException {
            Log.d("JSON SUBSCRIBE", jo.getStatus() );
            if(jo.getStatus().equalsIgnoreCase("OK")) {
                JSONArray ja = jo.getData();
                JSONObject obj_zone = ja.getJSONObject(0);
                Log.d("OBJ ZONE", obj_zone.toString());
                parkingZone.setAuthorized(true);

                url_identifier_detail = Constants.BASE_URL + Constants.URL_API_ZONES_DETAIL + "/" + parkingZone.getId();
                trx_id_detail  = HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(url_identifier_detail, device_uuid);
                Log.d("trx_id_detail", trx_id_detail);


                url_identifier_credit = Constants.BASE_URL + Constants.URL_API_PROFILE_CREDIT;
                trx_id_credit = HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(url_identifier_credit , device_uuid);


            }
            else {
                Toast.makeText(getMapView().getContext(), jo.getMsg(), Toast.LENGTH_LONG).show();
            }
        }

        void processZoneDetail(SParkeeJSONObject jo) throws  JSONException{

            Log.d("DEB", "PPPPPPPPPPPPPPPPPPPP");
            if (mView==null) {
                Log.d("ERR", "Error trapped, BasicInfoWindow.open, mView is null!");
                return;
            }

            Log.d("SUCCESS", jo.toString());
            JSONArray data = jo.getData();
            JSONObject obj = data.getJSONObject(0);

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
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT:
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT:
                    case Constants.MARKER_PARKING_UNCONFIRMED_DEFAULT:
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT:
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT:
                        undefined++;
                        break;
                    case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT:
                        available++;
                        break;
                    case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT:
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

            int polygon_color = ParkingZonePolygon.getPolygonColor(total, available);
            int colorColor = ContextCompat.getColor(mView.getContext(), polygon_color);
//            polygon.setFillColor(polygon_color);
//            polygon.getOutlinePaint().setColor(polygon_color);
//            polygon.getOutlinePaint().setStrokeWidth(2);
            polygon.getFillPaint().setColor( colorColor );

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
        txtName.setText(textNewlinePrettyFormatting(parkingZone.getName(), 15));
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parkingZone != null) {
                    mMapView.getController().animateTo(parkingZone.getCenterGeopoint(), Constants.DEFAULT_ZOOM_PARKING_ZONE_MED, DEFAULT_DELAY_ANIMATION);
                    onClose();
                }
            }
        });

        txtDescription.setText(textNewlinePrettyFormatting(parkingZone.getDescription(), 20 ));
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

                    url_identifier_detail = Constants.URL_API_ZONES_SUBSCRIBE+ "/" + parkingZone.getId();
                    trx_id_subscribe = HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(Constants.BASE_URL + url_identifier_detail, device_uuid);

                    onClose();
                }
            });
            layout_table_information.setVisibility(View.GONE);
            layout_btn.setVisibility(View.VISIBLE);
        }

        bubble_parking_zone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parkingZone != null) {
                    mMapView.getController().animateTo(parkingZone.getCenterGeopoint(), Constants.DEFAULT_ZOOM_PARKING_ZONE_MED, DEFAULT_DELAY_ANIMATION);
                    onClose();
                }

            }
        });

        mMapView.getContext().registerReceiver(httpReceiver, httpIntentFilter);


        if(parkingZone.getParking_spots() == null ) {
            url_identifier_detail = Constants.URL_API_ZONES_DETAIL + "/" + parkingZone.getId();
            trx_id_detail = HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(Constants.BASE_URL + url_identifier_detail, device_uuid);
        }
    }

    private String textNewlinePrettyFormatting(String txt, int length_per_line) {
        StringTokenizer st = new StringTokenizer(txt);
        int cnt_tokens = st.countTokens();
        List<String> list_str = new ArrayList<String>();
        String candidate = new String();
        do {
            String tmp = st.nextToken();
            if( (candidate.length() + tmp.length()) > length_per_line) {
                list_str.add(candidate);
                candidate = tmp;
            }
            else {
                candidate += " " + tmp ;
            }

        }
        while(st.hasMoreTokens());

        list_str.add(candidate);

        String ret = TextUtils.join("\n", list_str);
        return ret.trim();
    }

    private ParkingZoneInfoWindow.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;

    public void send(String msg, GeoPoint geoPoint) {
    }


}