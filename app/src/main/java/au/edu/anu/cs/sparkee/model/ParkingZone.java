package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.threeten.bp.LocalDateTime;

import java.util.List;

/*
{'id': 1, 'name': 'Zone A', 'description': 'ACT', 'center_longitude': '149.12859', 'center_latitude': '-35.281851', 'allowed_minimum_credit': 100, 'geopoints': [{'id': 7, 'longitude': '149.118965', 'latitude': '-35.276534'}, {'id': 6, 'longitude': '149.119223', 'latitude': '-35.276667'}, {'id': 5, 'longitude': '149.119299', 'latitude': '-35.276579'}, {'id': 4, 'longitude': '149.119304', 'latitude': '-35.276537'}, {'id': 3, 'longitude': '149.119074', 'latitude': '-35.276411'}, {'id': 2, 'longitude': '149.119', 'latitude': '-35.276431'}, {'id': 1, 'longitude': '149.118958', 'latitude': '-35.276534'}]}}

 */
public class ParkingZone {
    private int id;
    private String name;
    private String description;
    private String center_longitude;
    private String center_latitude;

    private int allowed_minimum_credit;
    private List<GeoPoint> geoPoints;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCenter_longitude() {
        return center_longitude;
    }

    public void setCenter_longitude(String center_longitude) {
        this.center_longitude = center_longitude;
    }

    public String getCenter_latitude() {
        return center_latitude;
    }

    public void setCenter_latitude(String center_latitude) {
        this.center_latitude = center_latitude;
    }

    public int getAllowed_minimum_credit() {
        return allowed_minimum_credit;
    }

    public void setAllowed_minimum_credit(int allowed_minimum_credit) {
        this.allowed_minimum_credit = allowed_minimum_credit;
    }

    public List<GeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public void setGeoPoints(List<GeoPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }
}
