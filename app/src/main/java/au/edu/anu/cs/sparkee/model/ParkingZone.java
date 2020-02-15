package au.edu.anu.cs.sparkee.model;

import org.osmdroid.util.GeoPoint;
import org.threeten.bp.LocalDateTime;

import java.util.List;

public class ParkingZone {
    private int id;
    private String name;
    private String description;
    private String center_longitude;
    private String center_latitude;
    private int credit_charge;

    private boolean authorized;
    private List<GeoPoint> geoPoints;
    private LocalDateTime ts_update;

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }


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

    public int getCredit_charge() {
        return credit_charge;
    }

    public void setCredit_charge(int credit_charge) {
        this.credit_charge = credit_charge;
    }

    public List<GeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public void setGeoPoints(List<GeoPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }

    public LocalDateTime getTs_update() {
        return ts_update;
    }

    public void setTs_update(String ts_update) {
        this.ts_update = LocalDateTime.parse(ts_update);
    }

}
