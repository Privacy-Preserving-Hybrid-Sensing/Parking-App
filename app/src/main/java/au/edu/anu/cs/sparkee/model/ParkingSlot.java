package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.threeten.bp.LocalDateTime;

public class ParkingSlot extends Location {
    private int id;
    private LocalDateTime ts_register;
    private LocalDateTime ts_update;
    private double total_available;
    private double total_unavailable;
    private int parking_status;
    private int participation_status;
    private int marker_status;
    private int zone_id;
    private double confidence_level;
    private String zone_name;

    public ParkingSlot() {
        super("");
    }


    public double getConfidence_level() {
        return confidence_level;
    }

    public void setConfidence_level(double confidence_level) {
        this.confidence_level = confidence_level;
    }

    public int getMarker_status() {
        return marker_status;
    }

    public void setMarker_status(int marker_status) {
        this.marker_status = marker_status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParticipation_status() {
        return participation_status;
    }

    public void setParticipation_status(int participation_status) {
        this.participation_status = participation_status;
    }


    public int getZone_id() {
        return zone_id;
    }

    public void setZone_id(int zone_id) {
        this.zone_id = zone_id;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
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

    public double getTotal_available() {
        return total_available;
    }

    public void setTotal_available(double total_available) {
        this.total_available = total_available;
    }

    public double getTotal_unavailable() {
        return total_unavailable;
    }

    public void setTotal_unavailable(double total_unavailable) {
        this.total_unavailable = total_unavailable;
    }

}
