package au.edu.anu.cs.sparkee.ui.map.infowindow;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rabbitmq.client.AlreadyClosedException;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Date;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.ui.map.marker.ParkingSlotMarker;

public class ParkingSlotInfoWindow extends InfoWindow {
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();
    private  GeoPoint geoPoint;
    private String device_uuid;
    private String status;
    private LocalDateTime ts_update;
    private ParkingSlotMarker marker;

    public ParkingSlotInfoWindow(int layoutResId, MapView mapView, GeoPoint geoPoint, String device_uuid, String status, LocalDateTime ts_update, ParkingSlotMarker marker) {
        super(layoutResId, mapView);
        this.geoPoint = geoPoint;
        this.device_uuid = device_uuid;
        this.status = status;
        this.ts_update = ts_update;
        this.marker = marker;
    }

    public void onClose() {
        super.close();
    }

    public void onOpen(Object arg0) {
        if(! marker.isEnabled())
            return;

        mIsVisible = true;
        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
        Button btnAvailable = (Button) mView.findViewById(R.id.bubble_available);
        Button btnUnavailable = (Button) mView.findViewById(R.id.bubble_unavailable);
        TextView txtBubbleStatus = (TextView) mView.findViewById(R.id.bubble_status);
        TextView txtLastUpdate = (TextView) mView.findViewById(R.id.bubble_last_update);
        TextView txtLocation = (TextView) mView.findViewById(R.id.bubble_location);

        PrettyTime p = new PrettyTime();
        String date_str = ts_update.atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String time_str = ts_update.atZone(ZoneId.systemDefault()).toLocalTime().toString();
        String time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));
        txtBubbleStatus.setText(status);
        txtLastUpdate.setText( time_pretty );
        txtLocation.setText(geoPoint.getLatitude() + ", " + geoPoint.getLongitude());
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
                send("Available", geoPoint);
                marker.setIcon(mMapView.getResources().getDrawable(R.drawable.participate_plus_1));
                mMapView.invalidate();
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Unavailable");
                close();
                send("Unavailable", geoPoint);
                marker.setIcon(mMapView.getResources().getDrawable(R.drawable.participate_minus_1));
                mMapView.invalidate();

            }
        });
    }

    public void send(String msg, GeoPoint geoPoint) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("action", "parking_availability");
            jo.put("device_uuid", device_uuid);
            jo.put("device_type", Constants.DEVICE_TYPE);
            jo.put("msg", msg);
            jo.put("lon", geoPoint.getLongitude());
            jo.put("lat", geoPoint.getLatitude());
            amqpConnectionHelper.send(jo);
        }
        catch (AlreadyClosedException ace) {
            ace.printStackTrace();
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();
        }
    }

}