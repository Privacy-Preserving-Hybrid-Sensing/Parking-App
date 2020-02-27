package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.threeten.bp.LocalDateTime;

public class Participation  {
    private int id;
    private LocalDateTime ts_update;
    private int zone_id;
    private String zone_name;

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    public String getSpot_name() {
        return spot_name;
    }

    public void setSpot_name(String spot_name) {
        this.spot_name = spot_name;
    }

    private String spot_name;

    private int spot_id;

    public int getPrevious_value() {
        return previous_value;
    }

    public void setPrevious_value(int previous_value) {
        this.previous_value = previous_value;
    }

    private int previous_value;
    private int participation_value;
    private boolean incentive_processed;
    private int incentive_value;

    public void setSpot_id(int spot_id) {
        this.spot_id = spot_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTs_update() {
        return ts_update;
    }

    public void setTs_update(String ts_update) {
        this.ts_update = LocalDateTime.parse(ts_update);
    }

    public int getZone_id() {
        return zone_id;
    }

    public void setZone_id(int zone_id) {
        this.zone_id = zone_id;

    }

    public int getSpot_id() {
        return spot_id;
    }


    public int getParticipation_value() {
        return participation_value;
    }

    public void setParticipation_value(int participation_value) {
        this.participation_value = participation_value;
    }

    public boolean isIncentive_processed() {
        return incentive_processed;
    }

    public void setIncentive_processed(boolean incentive_processed) {
        this.incentive_processed = incentive_processed;
    }

    public int getIncentive_value() {
        return incentive_value;
    }

    public void setIncentive_value(int incentive_value) {
        this.incentive_value = incentive_value;
    }


}
