package au.edu.anu.cs.sparkee.ui.map.overlay;

import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;

import java.util.HashMap;

import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingSpotMarker;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingZonePolygon;

public class HashFolderOverlay extends FolderOverlay {

    HashMap<Integer, Overlay> hashMap;
    public HashFolderOverlay() {
        super();
        hashMap = new HashMap<Integer, Overlay>();
    }

    public void add(int id, Overlay overlay) {
        hashMap.put(id, overlay);
        add(overlay);
    }

    public Overlay get(int id) {
        return hashMap.get(id);
    }
    public int countHashMap() {
        return hashMap.size();
    }
}
