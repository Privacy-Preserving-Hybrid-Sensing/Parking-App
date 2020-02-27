package au.edu.anu.cs.sparkee.model;

import org.threeten.bp.LocalDateTime;

public class ActivityModel {
    String imageUrl;
    String spotID;
    String zoneName;

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    String credit;
    LocalDateTime tsUpdate;

    public ActivityModel(String imageUrl, String spotID, String zoneName, String credit, LocalDateTime tsUpdate) {
        this.imageUrl = imageUrl;
        this.spotID = spotID;
        this.zoneName = zoneName;
        this.credit = credit;
        this.tsUpdate = tsUpdate;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSpotID() {
        return spotID;
    }

    public void setSpotID(String spotID) {
        this.spotID = spotID;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public LocalDateTime getTsUpdate() {
        return tsUpdate;
    }

    public void setTsUpdate(LocalDateTime tsUpdate) {
        this.tsUpdate = tsUpdate;
    }
}
