package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.threeten.bp.LocalDateTime;

public class ParkingSlot extends Location {
    private int id;
    private LocalDateTime ts_register;
    private LocalDateTime ts_update;
    private double confidence_available;
    private double confidence_unavailable;
    private int status;

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

    public double getConfidence_available() {
        return confidence_available;
    }

    public void setConfidence_available(double confidence_available) {
        this.confidence_available = confidence_available;
    }

    public double getConfidence_unavailable() {
        return confidence_unavailable;
    }

    public void setConfidence_unavailable(double confidence_unavailable) {
        this.confidence_unavailable = confidence_unavailable;
    }

    public ParkingSlot() {
        super("");
    }
}
