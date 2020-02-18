package au.edu.anu.cs.sparkee.ui.map.marker;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import au.edu.anu.cs.sparkee.model.ParkingZone;

public class ParkingZonePolygon extends Polygon {
    public ParkingZone getParkingZone() {
        return parkingZone;
    }

    public void setParkingZone(ParkingZone parkingZone) {
        this.parkingZone= parkingZone;
    }

    ParkingZone parkingZone;
    public ParkingZonePolygon(MapView mapView, ParkingZone parkingZone) {
        super(mapView);
        this.parkingZone = parkingZone;
    }
}
