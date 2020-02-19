package au.edu.anu.cs.sparkee.ui.map.marker;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.model.ParkingSpot;

public class ParkingSpotMarker extends Marker {
    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    ParkingSpot parkingSpot;
    public ParkingSpotMarker(MapView mapView, ParkingSpot parkingSpot) {
        super(mapView);
        this.parkingSpot = parkingSpot;
    }

    public static int getMarkerIcon(int marker_status) {
        int marker = R.drawable.unconfirmed_0;
        switch (marker_status) {
            case Constants.MARKER_PARTICIPATION_UNAVAILABLE_RECEIVED:
                marker = R.drawable.participate_minus_1_confirmed;
                break;
            case Constants.MARKER_PARTICIPATION_AVAILABLE_RECEIVED:
                marker = R.drawable.participate_plus_1_confirmed;
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIRMED:
                marker = R.drawable.confirmed_unavailable;
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2:
                marker = R.drawable.unconfirmed_minus_2;
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1:
                marker = R.drawable.unconfirmed_minus_1;
                break;
            case Constants.MARKER_PARKING_UNCONFIRMED:
                marker = R.drawable.unconfirmed_0;
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1:
                marker = R.drawable.unconfirmed_plus_1;
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2:
                marker = R.drawable.unconfirmed_plus_2;
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIRMED:
                marker = R.drawable.confirmed_available;
                break;
        }
        return marker;
    }

    public static String getMarkerStatusTxt(int marker_status, double tmp_confidence) {
        String tmp_status = "";
        switch (marker_status) {
            case Constants.MARKER_PARTICIPATION_UNAVAILABLE_RECEIVED:
                tmp_status = "Participate: Unvailable";
                break;
            case Constants.MARKER_PARTICIPATION_AVAILABLE_RECEIVED:
                tmp_status = "Participate: Available";
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIRMED:
                tmp_status = "Unavailable";
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2:
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1:
                tmp_status = "Unavailable ("+ tmp_confidence +"%)";
                tmp_status = "Unavailable ("+ tmp_confidence +"%)";
                break;
            case Constants.MARKER_PARKING_UNCONFIRMED:
                tmp_status = "Unconfirmed";
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1:
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2:
                tmp_status = "Available ("+ tmp_confidence +"%)";
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIRMED:
                tmp_status = "Available";
                break;
        }
        return tmp_status;
    }
}
