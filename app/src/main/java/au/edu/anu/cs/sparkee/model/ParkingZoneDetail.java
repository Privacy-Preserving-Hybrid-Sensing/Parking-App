package au.edu.anu.cs.sparkee.model;

public class ParkingZoneDetail extends ParkingZone {

    private ParkingSpot[] parking_slots;

    public ParkingSpot[] getParking_slots() {
        return parking_slots;
    }

    public void setParking_slots(ParkingSpot[] parking_slots) {
        this.parking_slots = parking_slots;
    }
}
