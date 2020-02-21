package au.edu.anu.cs.sparkee.ui.map.infowindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rabbitmq.client.AlreadyClosedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.helper.HTTPConnectionHelper;
import au.edu.anu.cs.sparkee.model.SParkeeJSONArrayData;
import au.edu.anu.cs.sparkee.ui.map.marker.ParkingSpotMarker;

public class ParkingSpotInfoWindow extends InfoWindow {
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();
    private  GeoPoint geoPoint;
    private String device_uuid;
    private String status;
    private LocalDateTime ts_update;
    private ParkingSpotMarker parkingSpotMarker;

    public ParkingSpotInfoWindow(int layoutResId, MapView mapView, GeoPoint geoPoint, String device_uuid, String status, LocalDateTime ts_update, ParkingSpotMarker parkingSpotMarker) {
        super(layoutResId, mapView);
        this.geoPoint = geoPoint;
        this.device_uuid = device_uuid;
        this.status = status;
        this.ts_update = ts_update;
        this.parkingSpotMarker = parkingSpotMarker;

        httpReceiver = new ParkingSpotInfoWindow.InternalHTTPBroadcaseReceiver();
        httpIntentFilter = new IntentFilter(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
    }


    private ParkingSpotInfoWindow.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;
    private String trx_id_detail;
    private boolean participate;
    public void onClose() {
        super.close();
    }

    public void onOpen(Object arg0) {
        if(! parkingSpotMarker.isEnabled())
            return;

        mIsVisible = true;
        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_parking_spot_layout);
        Button btnAvailable = (Button) mView.findViewById(R.id.bubble_available);
        Button btnUnavailable = (Button) mView.findViewById(R.id.bubble_unavailable);
        TextView txtBubbleStatus = (TextView) mView.findViewById(R.id.bubble_status);
        TextView txtLastUpdate = (TextView) mView.findViewById(R.id.bubble_last_update);
        TextView txtSlotName = (TextView) mView.findViewById(R.id.bubble_name);
        TextView txtZoneName = (TextView) mView.findViewById(R.id.bubble_parking_zone_name);
        ImageView imgBubbleParkingSpot = (ImageView) mView.findViewById(R.id.bubble_parking_spot_icon);

        int participation = ( parkingSpotMarker.getParkingSpot().getParticipation_status() ? Constants.MARKER_PARKING_CATEGORY_PARTICIPATION: Constants.MARKER_PARKING_CATEGORY_DEFAULT);
        int iconId = ParkingSpotMarker.getMarkerIcon(parkingSpotMarker.getParkingSpot().getMarker_status(), participation);
        imgBubbleParkingSpot.setImageDrawable( mMapView.getResources().getDrawable( iconId ));

        PrettyTime p = new PrettyTime();
        String date_str = ts_update.atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String time_str = ts_update.atZone(ZoneId.systemDefault()).toLocalTime().toString();
        String time_pretty = "-";
        try {
            time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));
        }
        catch(IllegalArgumentException iae) {
            iae.printStackTrace();
            Log.d("ERR", "Time Problem");
        }

        txtSlotName.setText( parkingSpotMarker.getParkingSpot().getName() );
        txtZoneName.setText( "at " + parkingSpotMarker.getParkingSpot().getZone_name() );
        txtBubbleStatus.setText("Status: " + status);
        txtLastUpdate.setText( "Last change " + time_pretty );

        layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Override Marker's onClick behaviour here
                close();
            }
        });

        btnAvailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Available");
                mMapView.getContext().registerReceiver(httpReceiver, httpIntentFilter);
                send("available");
                close();
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Unavailable");
                mMapView.getContext().registerReceiver(httpReceiver, httpIntentFilter);
                send("unavailable");
                close();

            }
        });

    }

    String url_identifier_detail = "";
    public void send(String participation) {
        try {
            url_identifier_detail = Constants.BASE_URL + Constants.URL_API_PARTICIPATE + "/" + participation + "/" + parkingSpotMarker.getParkingSpot().getId();
            trx_id_detail  = HTTPConnectionHelper.getInstance(mMapView.getContext()).sendPost(url_identifier_detail, device_uuid);
            Log.d("trx_id_detail", trx_id_detail);
        }
        catch (AlreadyClosedException ace) {
            ace.printStackTrace();
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();

        }
    }

    public class InternalHTTPBroadcaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(Constants.BROADCAST_HTTP_BODY_IDENTIFIER);
            String status = bundle.getString(Constants.BROADCAST_DATA_STATUS_IDENTIFIER);
            Log.d("SPOT", msg);
            if (status.equalsIgnoreCase(Constants.BROADCAST_STATUS_OK)) {
                try {
                    onSuccess(SParkeeJSONArrayData.parse(msg));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                onError(msg);
            }

            try {
                mMapView.getContext().unregisterReceiver(httpReceiver);
            }
            catch(Exception e) {

            }
        }

        void onSuccess(SParkeeJSONArrayData jo) throws JSONException {
            Log.d("SPOT TRX ID", jo.getTrx_id() + " vs " + trx_id_detail);
            if(jo.getPath().startsWith(Constants.URL_API_PARTICIPATE) && jo.getTrx_id().equalsIgnoreCase(trx_id_detail)) {
                processParticipation(jo);
            }
        }

        void processParticipation(SParkeeJSONArrayData jo) throws  JSONException {
            Log.d("JSON SUBSCRIBE", jo.getStatus() );
            if(jo.getStatus().equalsIgnoreCase("OK")) {
                JSONArray ja = jo.getData();

                ImageView imgBubbleParkingSpot = (ImageView) mView.findViewById(R.id.bubble_parking_spot_icon);
                parkingSpotMarker.getParkingSpot().setParticipation_status(true);
                Log.d("PARTICIPATE", "" + parkingSpotMarker.getParkingSpot().getParticipation_status());

                int participation = (parkingSpotMarker.getParkingSpot().getParticipation_status() ? Constants.MARKER_PARKING_CATEGORY_PARTICIPATION: Constants.MARKER_PARKING_CATEGORY_PARTICIPATION);
                int iconId = ParkingSpotMarker.getMarkerIcon(parkingSpotMarker.getParkingSpot().getMarker_status(), participation);
                Log.d("ICON 2", "" + iconId);
                Drawable iconDrawable = mMapView.getResources().getDrawable( iconId );
                imgBubbleParkingSpot.setImageDrawable( iconDrawable );

                parkingSpotMarker.setIcon(iconDrawable);
                mMapView.invalidate();

//                Toast.makeText(getMapView().getContext(), jo.getMsg(), Toast.LENGTH_LONG).show();
            }
        }
        void onError(String msg) {
            Log.d("ERROR", msg);
        }
    }

}