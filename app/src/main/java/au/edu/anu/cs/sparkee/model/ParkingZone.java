package au.edu.anu.cs.sparkee.model;

import org.osmdroid.util.GeoPoint;
import org.threeten.bp.LocalDateTime;

import java.util.List;

public class ParkingZone {
    private int id;
    private boolean subscribed;
    private String name;
    private String subscription_token;
    private String description;
    private String center_longitude;
    private String center_latitude;
    private int credit_required;
    private int spot_total;
    private int spot_available;
    private int spot_unavailable;
    private int spot_undefined;
    private LocalDateTime ts_update;


    public String getSubscription_token() {
        return subscription_token;
    }

    public void setSubscription_token(String subscription_token) {
        this.subscription_token = subscription_token;
    }

    private List<GeoPoint> geoPoints;

    public int getSpot_total() {
        return spot_total;
    }

    public void setSpot_total(int spot_total) {
        this.spot_total = spot_total;
    }

    public int getSpot_available() {
        return spot_available;
    }

    public void setSpot_available(int spot_available) {
        this.spot_available = spot_available;
    }

    public int getSpot_unavailable() {
        return spot_unavailable;
    }

    public void setSpot_unavailable(int spot_unavailable) {
        this.spot_unavailable = spot_unavailable;
    }

    public int getSpot_undefined() {
        return spot_undefined;
    }

    public void setSpot_undefined(int spot_undefined) {
        this.spot_undefined = spot_undefined;
    }


    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
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

    public GeoPoint getCenterGeopoint() {
        return new GeoPoint( Double.parseDouble( getCenter_latitude() ), Double.parseDouble(getCenter_longitude()));
    }

    public void setCenter_latitude(String center_latitude) {
        this.center_latitude = center_latitude;
    }

    public int getCredit_required() {
        return credit_required;
    }

    public void setCredit_required(int credit_required) {
        this.credit_required = credit_required;
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

    public void setTs_update(LocalDateTime ts_update) {
        this.ts_update = ts_update;
    }

}
