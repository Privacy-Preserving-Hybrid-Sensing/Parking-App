package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.threeten.bp.LocalDateTime;

public class ParkingSlot extends Location {
    private int id;
    private LocalDateTime ts_register;
    private LocalDateTime ts_update;
    private double total_available;
    private double total_unavailable;
    private int status;
    private int zone_id;
    private String zone_name;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public ParkingSlot() {
        super("");
    }
}
