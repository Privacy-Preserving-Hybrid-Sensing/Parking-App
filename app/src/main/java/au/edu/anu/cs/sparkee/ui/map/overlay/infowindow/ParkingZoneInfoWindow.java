package au.edu.anu.cs.sparkee.ui.map.overlay.infowindow;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.ui.map.MapViewModel;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingZonePolygon;


public class ParkingZoneInfoWindow extends InfoWindow {

    private String device_uuid;
    private ParkingZonePolygon polygon;
    private ParkingZone parkingZone;
    private MapViewModel mapViewModel;

    public void setParkingZone(ParkingZone parkingZone) {
        this.parkingZone = parkingZone;
    }

    public ParkingZoneInfoWindow(int layoutResId, MapView mapView, String device_uuid, ParkingZonePolygon polygon, MapViewModel mapViewModel) {
        super(layoutResId, mapView);
        Log.d("RESID", "" + layoutResId);
        this.device_uuid = device_uuid;
        this.polygon = polygon;
        this.parkingZone = polygon.getParkingZone();
        this.mapViewModel = mapViewModel;
    }

    public void onClose() {
        super.close();
    }

    private long DEFAULT_DELAY_ANIMATION = 500;

    @Override
    public void onOpen(Object arg0) {

        if (mView==null) {
            Log.d("ERR", "Error trapped, BasicInfoWindow.open, mView is null!");
            return;
        }

        Log.d("MVIEW", mView.toString());
        mIsVisible = true;
        updateInfoWindow();
    }

    public void updateInfoWindow() {

        LinearLayout bubble_parking_zone_layout = (LinearLayout) mView.findViewById(R.id.bubble_parking_zone_layout);
        LinearLayout layout_btn = (LinearLayout) mView.findViewById(R.id.layout_btn);

        TextView txtName                    = (TextView) mView.findViewById(R.id.bubble_name);
        TextView txtDescription             = (TextView) mView.findViewById(R.id.bubble_description);
        TextView txtParkingSpotTotal        = (TextView) mView.findViewById(R.id.bubble_parking_spot_total);
        TextView txtParkingSpotAvailable    = (TextView) mView.findViewById(R.id.bubble_parking_spot_available);
        TextView txtParkingSpotUnavailable  = (TextView) mView.findViewById(R.id.bubble_parking_spot_unavailable);
        TextView txtParkingSpotUndefined    = (TextView) mView.findViewById(R.id.bubble_parking_spot_undefined);


        TextView txtLastUpdate              = (TextView) mView.findViewById(R.id.bubble_last_update);

        Button btnUseCredit                 = (Button) mView.findViewById(R.id.bubble_use_credit);
        TableLayout layout_table_information = (TableLayout) mView.findViewById(R.id.layout_table_information);

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
        if(parkingZone.isSubscribed()) {
            layout_table_information.setVisibility(View.VISIBLE);
            layout_btn.setVisibility(View.GONE);
        }
        else {
            btnUseCredit.setText("Use Credit (" + parkingZone.getCredit_required()+ ")");
            btnUseCredit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.d("Btn", "Use Credit");
                    requestZoneSubscribe(parkingZone.getId());
                    onClose();
                }
            });
            layout_table_information.setVisibility(View.GONE);
            layout_btn.setVisibility(View.VISIBLE);
        }

        bubble_parking_zone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestZoneDetail(parkingZone.getId());
                mMapView.getController().animateTo(parkingZone.getCenterGeopoint(), Constants.DEFAULT_ZOOM_PARKING_ZONE_MED, DEFAULT_DELAY_ANIMATION);
                onClose();

            }
        });

        PrettyTime p = new PrettyTime();
        String date_str = parkingZone.getTs_update().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String time_str = parkingZone.getTs_update().atZone(ZoneId.systemDefault()).toLocalTime().toString();
        String time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));

        txtLastUpdate.setText(time_pretty);
        txtName.setText(textNewlinePrettyFormatting(parkingZone.getName(), 15));

        txtParkingSpotTotal.setText("" + parkingZone.getSpot_total());
        txtParkingSpotAvailable.setText("" + parkingZone.getSpot_available());
        txtParkingSpotUnavailable.setText("" + parkingZone.getSpot_unavailable());
        txtParkingSpotUndefined.setText("" + parkingZone.getSpot_undefined());

    }

    private void requestZoneDetail(int zone_id) {
//        mapViewModel.sendRequestZone(parkingZone.getId());

        mapViewModel.sendRequestZoneSpotsAll(zone_id);
        String subscription_token = parkingZone.getSubscription_token();
        mapViewModel.subscribeAsyncChannel(subscription_token);

    }

    private void requestZoneSubscribe(int zone_id) {
        mapViewModel.sendRequestZoneSubscribe(zone_id);
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

    public ParkingZone getParkingZone() {
        return parkingZone;
    }

}