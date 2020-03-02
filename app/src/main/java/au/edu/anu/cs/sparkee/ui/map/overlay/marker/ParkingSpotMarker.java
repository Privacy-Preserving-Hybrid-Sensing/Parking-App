package au.edu.anu.cs.sparkee.ui.map.overlay.marker;

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

    public static int getMarkerIcon(int marker_status, int category) {
        int marker = R.drawable.unconfirmed_0;
        if(category == Constants.MARKER_PARKING_CATEGORY_DEFAULT) {
            switch (marker_status) {
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT:
                    marker = R.drawable.default_unavailable_3;
                    break;
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT:
                    marker = R.drawable.default_unavailable_2;
                    break;
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT:
                    marker = R.drawable.default_unavailable_1;
                    break;
                case Constants.MARKER_PARKING_UNCONFIRMED_DEFAULT:
                    marker = R.drawable.default_unconfirmed_0;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT:
                    marker = R.drawable.default_available_1;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT:
                    marker = R.drawable.default_available_2;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT:
                    marker = R.drawable.default_available_3;
                    break;
            }
        }
        else if(category == Constants.MARKER_PARKING_CATEGORY_PARTICIPATION) {
            switch (marker_status) {
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT:
                    marker = R.drawable.participate_unavailable_3;
                    break;
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT:
                    marker = R.drawable.participate_unavailable_2;
                    break;
                case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT:
                    marker = R.drawable.participate_unavailable_1;
                    break;
                case Constants.MARKER_PARKING_UNCONFIRMED_DEFAULT:
                    marker = R.drawable.participate_unconfirmed_0;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT:
                    marker = R.drawable.participate_available_1;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT:
                    marker = R.drawable.participate_available_2;
                    break;
                case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT:
                    marker = R.drawable.participate_available_3;
                    break;
            }
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
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT:
                tmp_status = "Unavailable";
                break;
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT:
            case Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT:
                tmp_status = "Unavailable ("+ tmp_confidence +"%)";
                tmp_status = "Unavailable ("+ tmp_confidence +"%)";
                break;
            case Constants.MARKER_PARKING_UNCONFIRMED_DEFAULT:
                tmp_status = "Unconfirmed";
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT:
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT:
                tmp_status = "Available ("+ tmp_confidence +"%)";
                break;
            case Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT:
                tmp_status = "Available";
                break;
        }
        return tmp_status;
    }
}
