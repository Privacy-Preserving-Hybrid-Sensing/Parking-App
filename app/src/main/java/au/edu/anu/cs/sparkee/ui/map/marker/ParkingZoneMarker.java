package au.edu.anu.cs.sparkee.ui.map.marker;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import au.edu.anu.cs.sparkee.model.ParkingSlot;
import au.edu.anu.cs.sparkee.model.ParkingZone;

public class ParkingZoneMarker extends Marker {
    public ParkingZone getParkingSlot() {
        return parkingZone;
    }

    public void setParkingZone(ParkingZone parkingZone) {
        this.parkingZone= parkingZone;
    }

    ParkingZone parkingZone;
    public ParkingZoneMarker(MapView mapView, ParkingZone parkingZone) {
        super(mapView);
        this.parkingZone = parkingZone;
    }
}
