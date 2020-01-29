package au.edu.anu.cs.sparkee.ui.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;

public class CustomInfoWindow extends InfoWindow {
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();
    private  GeoPoint geoPoint;
    private String routing_key_uuid;
    private String status;
    public CustomInfoWindow(int layoutResId, MapView mapView, GeoPoint geoPoint, String routing_key_uuid, String status) {
        super(layoutResId, mapView);
        this.geoPoint = geoPoint;
        this.routing_key_uuid = routing_key_uuid;
        this.status = status;
    }

    public void onClose() {
    }

    public void onOpen(Object arg0) {

        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
        Button btnAvailable = (Button) mView.findViewById(R.id.bubble_available);
        Button btnUnavailable = (Button) mView.findViewById(R.id.bubble_unavailable);
        TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);
        TextView txtSubdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);

        txtTitle.setText(status);
        txtDescription.setText("Click button to contribute data");
        txtSubdescription.setText("");
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
            Channel channel = amqpConnectionHelper.getChannel();
            JSONObject jo = new JSONObject();
            jo.put("type", "parking_availability");
            jo.put("routing_key_uuid", routing_key_uuid);
            jo.put("msg", msg);
            jo.put("long", geoPoint.getLongitude());
            jo.put("latt", geoPoint.getLatitude());
            String str_json = jo.toString();
            channel.basicPublish(Constants.RABBIT_EXCHANGE_OUTGOING_NAME, "", null, str_json.getBytes("UTF-8"));
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