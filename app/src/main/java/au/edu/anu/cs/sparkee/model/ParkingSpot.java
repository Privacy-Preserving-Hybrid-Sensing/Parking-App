package au.edu.anu.cs.sparkee.model;

import android.location.Location;
import android.os.Parcelable;

import org.threeten.bp.LocalDateTime;

public class ParkingSpot extends Location  {
    private int id;
    private LocalDateTime ts_register;
    private LocalDateTime ts_update;
    private double voting_available;
    private double voting_unavailable;
    private int parking_status;
    private boolean participation_status;
    private int marker_status;
    private int zone_id;
    private double confidence_level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private String zone_name;

    public ParkingSpot() {
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

    public boolean getParticipation_status() {
        return participation_status;
    }

    public void setParticipation_status(boolean participation_status) {
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

    public double getVoting_available() {
        return voting_available;
    }

    public void setVoting_available(double voting_available) {
        this.voting_available = voting_available;
    }

    public double getVoting_unavailable() {
        return voting_unavailable;
    }

    public void setVoting_unavailable(double voting_unavailable) {
        this.voting_unavailable = voting_unavailable;
    }

}
