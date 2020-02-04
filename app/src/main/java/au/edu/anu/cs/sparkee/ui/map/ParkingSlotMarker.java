package au.edu.anu.cs.sparkee.ui.map;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import au.edu.anu.cs.sparkee.model.ParkingSlot;

public class ParkingSlotMarker extends Marker {
    public ParkingSlot getParkingSlot() {
        return parkingSlot;
    }

    public void setParkingSlot(ParkingSlot parkingSlot) {
        this.parkingSlot = parkingSlot;
    }

    ParkingSlot parkingSlot;
    public ParkingSlotMarker(MapView mapView, ParkingSlot parkingSlot) {
        super(mapView);
        this.parkingSlot = parkingSlot;
    }
}
