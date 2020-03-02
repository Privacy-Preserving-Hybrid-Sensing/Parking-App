package au.edu.anu.cs.sparkee.ui.map.overlay.infowindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.ui.map.MapViewModel;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingSpotMarker;

public class ParkingSpotInfoWindow extends InfoWindow {

    private  GeoPoint geoPoint;
    private String device_uuid;
    private String status;
    private LocalDateTime ts_update;
    private ParkingSpotMarker parkingSpotMarker;
    private MapViewModel mapViewModel;

    public ParkingSpotInfoWindow(int layoutResId, MapView mapView, GeoPoint geoPoint, String device_uuid, String status, LocalDateTime ts_update, ParkingSpotMarker parkingSpotMarker, MapViewModel mapViewModel) {
        super(layoutResId, mapView);
        this.geoPoint = geoPoint;
        this.device_uuid = device_uuid;
        this.status = status;
        this.ts_update = ts_update;
        this.parkingSpotMarker = parkingSpotMarker;
        this.mapViewModel = mapViewModel;
    }

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

        int participation = Constants.MARKER_PARKING_CATEGORY_DEFAULT;
        int iconId = ParkingSpotMarker.getMarkerIcon(parkingSpotMarker.getParkingSpot().getParking_status(), participation);
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
                mapViewModel.sendRequestParticipation(parkingSpotMarker.getParkingSpot().getZone_id(), parkingSpotMarker.getParkingSpot().getId(), "available");
                close();
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapViewModel.sendRequestParticipation(parkingSpotMarker.getParkingSpot().getZone_id(), parkingSpotMarker.getParkingSpot().getId(), "unavailable");
                close();
            }
        });
    }


    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();
        }
    }
}