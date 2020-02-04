package au.edu.anu.cs.sparkee.model;

import android.location.Location;

import org.threeten.bp.LocalDateTime;

public class ParticipantCredit extends Location {

    private LocalDateTime ts_credit_update;
    private LocalDateTime ts_participation_update;
    private double availability_value;
    private double credit_value;
    private boolean participation_processed;
    private boolean credit_processed;

    public LocalDateTime getTs_credit_update() {
        return ts_credit_update;
    }

    public void setTs_credit_update(String ts_credit_update) {
        this.ts_credit_update = LocalDateTime.parse(ts_credit_update);
    }

    public LocalDateTime getTs_participation_update() {
        return ts_participation_update;
    }

    public void setTs_participation_update(String ts_participation_update) {
        this.ts_participation_update = LocalDateTime.parse(ts_participation_update);
    }

    public double getAvailability_value() {
        return availability_value;
    }

    public void setAvailability_value(double availability_value) {
        this.availability_value = availability_value;
    }

    public double getCredit_value() {
        return credit_value;
    }

    public void setCredit_value(double credit_value) {
        this.credit_value = credit_value;
    }

    public boolean isParticipation_processed() {
        return participation_processed;
    }

    public void setParticipation_processed(boolean participation_processed) {
        this.participation_processed = participation_processed;
    }

    public boolean isCredit_processed() {
        return credit_processed;
    }

    public void setCredit_processed(boolean credit_processed) {
        this.credit_processed = credit_processed;
    }

    public ParticipantCredit() {
        super("");
    }
}
