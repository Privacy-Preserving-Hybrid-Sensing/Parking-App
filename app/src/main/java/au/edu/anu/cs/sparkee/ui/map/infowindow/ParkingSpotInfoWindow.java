package au.edu.anu.cs.sparkee.ui.map.infowindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rabbitmq.client.AlreadyClosedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.sql.Timestamp;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.helper.HTTPConnectionHelper;
import au.edu.anu.cs.sparkee.model.SParkeeJSONObject;
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
    }


    private ParkingSpotInfoWindow.InternalHTTPBroadcaseReceiver httpReceiver;
    private IntentFilter httpIntentFilter;
    private String trx_id_detail;
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
        TextView txtName= (TextView) mView.findViewById(R.id.bubble_name);

        PrettyTime p = new PrettyTime();
        String date_str = ts_update.atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String time_str = ts_update.atZone(ZoneId.systemDefault()).toLocalTime().toString();
        String time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));
        txtName.setText( parkingSpotMarker.getParkingSpot().getName() );
        txtBubbleStatus.setText(status);
        txtLastUpdate.setText( time_pretty );

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
                close();
                send("available");
                parkingSpotMarker.setIcon(mMapView.getResources().getDrawable(R.drawable.participate_plus_1));
                mMapView.invalidate();
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Unavailable");
                close();
                send("unavailable");
                parkingSpotMarker.setIcon(mMapView.getResources().getDrawable(R.drawable.participate_minus_1));
                mMapView.invalidate();

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
            Log.d("TRX ID", jo.getTrx_id() + " vs " + trx_id_detail);
            if(jo.getPath().startsWith(Constants.URL_API_PARTICIPATE) && jo.getTrx_id().equalsIgnoreCase(trx_id_detail)) {
                processParticipation(jo);
            }
        }

        void processParticipation(SParkeeJSONObject jo) throws  JSONException {
            Log.d("JSON SUBSCRIBE", jo.getStatus() );
            if(jo.getStatus().equalsIgnoreCase("OK")) {
                JSONArray ja = jo.getData();
                Toast.makeText(getMapView().getContext(), jo.getMsg(), Toast.LENGTH_LONG).show();
            }
        }
        void onError(String msg) {
            Log.d("ERROR", msg);
        }
    }

}