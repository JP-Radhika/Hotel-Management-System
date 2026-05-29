package hotel.model;

public class Room {
    private int roomNumber;
    private RoomType type;
    private double pricePerNight;
    private boolean isAvailable;
    private String amenities;

    public enum RoomType {
        STANDARD, DELUXE, SUITE, PRESIDENTIAL
    }

    public Room(int roomNumber, RoomType type, double pricePerNight, String amenities) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
        this.amenities = amenities;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double p) { this.pricePerNight = p; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String a) { this.amenities = a; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " | " + type + " | $" + pricePerNight + "/night";
    }
}
