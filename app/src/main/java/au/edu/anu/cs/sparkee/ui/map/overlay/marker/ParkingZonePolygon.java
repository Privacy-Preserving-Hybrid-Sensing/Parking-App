package au.edu.anu.cs.sparkee.ui.map.overlay.marker;


import android.graphics.Color;
import android.graphics.ColorSpace;
import android.util.Log;

import androidx.annotation.ColorInt;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
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

    public static int getPolygonColor(int parking_spots_cnt, int parking_spots_available) {
        int marker = R.color.white;
        if(parking_spots_cnt <= 0)
            return marker;
        int parking_spots_unavailable_undefined = parking_spots_cnt - parking_spots_available;
        double occupation =  (parking_spots_unavailable_undefined / (double) parking_spots_cnt) * 100;
        Log.d("OCCUPATION", "" + parking_spots_unavailable_undefined + " / " + parking_spots_cnt );
        Log.d("OCCUPATION2", "" + occupation );
        if(occupation > 90) {
            marker = R.color.light_red;
        }
        else if(occupation > 60) {
            marker = R.color.orange;
        }
        else if(occupation > 30) {
            marker = R.color.light_yellow;
        }
        else {
            marker = R.color.light_green;
        }
        return marker;
    }


}
