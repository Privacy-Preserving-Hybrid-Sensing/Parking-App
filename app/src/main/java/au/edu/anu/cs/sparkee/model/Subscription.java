package au.edu.anu.cs.sparkee.model;


import org.threeten.bp.LocalDateTime;

import au.edu.anu.cs.sparkee.Constants;

public class Subscription extends  History {

    public Subscription() {
        super(Constants.HISTORY_TYPE_SUBSCRIPTION);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTs_subscription() {
        return ts_subscription;
    }

    public void setTs_subscription(String ts_update) {
        this.ts_subscription = org.threeten.bp.LocalDateTime.parse(ts_update);
    }

    public void setTs(LocalDateTime ts) {
        this.ts_subscription = ts;
    }

    public int getCharged() {
        return charged;
    }

    public void setCharged(int charged) {
        this.charged = charged;
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

    private int id;
    private LocalDateTime ts_subscription;
    private int charged;
    private int zone_id;
    private String zone_name;
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
