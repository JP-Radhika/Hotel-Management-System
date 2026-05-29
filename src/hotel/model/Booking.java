package hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {
    private static int counter = 5001;
    private int bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Status status;
    private double totalAmount;

    public enum Status { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.bookingId = counter++;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = Status.CONFIRMED;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        this.totalAmount = nights * room.getPricePerNight();
    }

    public int getBookingId() { return bookingId; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public double getTotalAmount() { return totalAmount; }
    public long getNights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }
}
