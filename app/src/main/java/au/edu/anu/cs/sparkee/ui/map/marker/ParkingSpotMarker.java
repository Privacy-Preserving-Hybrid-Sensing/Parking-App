package au.edu.anu.cs.sparkee.ui.map.marker;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

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
}
