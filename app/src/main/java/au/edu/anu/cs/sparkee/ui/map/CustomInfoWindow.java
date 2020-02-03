package au.edu.anu.cs.sparkee.ui.map;

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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;

public class CustomInfoWindow extends InfoWindow {
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();
    private  GeoPoint geoPoint;
    private String device_uuid;
    private String status;
    private LocalDateTime ts_update;

    public CustomInfoWindow(int layoutResId, MapView mapView, GeoPoint geoPoint, String device_uuid, String status, LocalDateTime ts_update) {
        super(layoutResId, mapView);
        this.geoPoint = geoPoint;
        this.device_uuid = device_uuid;
        this.status = status;
        this.ts_update = ts_update;
    }

    public void onClose() {
    }

    public void onOpen(Object arg0) {

        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
        Button btnAvailable = (Button) mView.findViewById(R.id.bubble_available);
        Button btnUnavailable = (Button) mView.findViewById(R.id.bubble_unavailable);
        TextView txtBubbleStatus = (TextView) mView.findViewById(R.id.bubble_status);
        TextView txtLastUpdate = (TextView) mView.findViewById(R.id.bubble_last_update);
        TextView txtLocation = (TextView) mView.findViewById(R.id.bubble_location);

        txtBubbleStatus.setText(status);
        txtLastUpdate.setText( ts_update.toString());
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
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Unavailable");
                close();
                send("Unavailable", geoPoint);
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
            jo.put("long", geoPoint.getLongitude());
            jo.put("latt", geoPoint.getLatitude());
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