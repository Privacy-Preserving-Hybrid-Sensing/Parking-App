package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.threeten.bp.LocalDateTime;

public class ParkingSpot extends Location  {
    private int id;
    private String name;
    private LocalDateTime ts_register;
    private LocalDateTime ts_update;
    private String registrar_uuid;
    private int vote_available;
    private int vote_unavailable;
    private double confidence_level;
    private int parking_status;
    private int zone_id;

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    private String zone_name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ParkingSpot() {
        super("");
    }


    public double getConfidence_level() {
        return confidence_level;
    }

    public void setConfidence_level(double confidence_level) {
        this.confidence_level = confidence_level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getZone_id() {
        return zone_id;
    }

    public void setZone_id(int zone_id) {
        this.zone_id = zone_id;
    }

    public String getRegistrar_uuid() {
        return registrar_uuid;
    }

    public void setRegistrar_uuid(String registrar_uuid) {
        this.registrar_uuid = registrar_uuid;
    }

    public int getParking_status() {
        return parking_status;
    }

    public void setParking_status(int parking_status) {
        this.parking_status = parking_status;
    }



    public LocalDateTime getTs_register() {
        return ts_register;
    }

    public void setTs_register(String ts_register)  {

        this.ts_register = LocalDateTime.parse(ts_register);
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

    public int getVote_available() {
        return vote_available;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(getLatitude(), getLongitude());
    }

    public void setVote_available(int vote_available) {
        this.vote_available = vote_available;
    }

    public int getVote_unavailable() {
        return vote_unavailable;
    }

    public void setVote_unavailable(int vote_unavailable) {
        this.vote_unavailable = vote_unavailable;
    }

}
