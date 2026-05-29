package hotel.service;

import hotel.model.Booking;
import hotel.model.Booking.Status;
import hotel.model.Guest;
import hotel.model.Room;
import hotel.model.Room.RoomType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelService {

    // Collections Framework - different collection types used intentionally
    private final Map<Integer, Room>    rooms    = new LinkedHashMap<>(); // preserves insertion order
    private final Map<Integer, Guest>   guests   = new HashMap<>();
    private final List<Booking>         bookings = new ArrayList<>();

    public HotelService() {
        seedRooms();
    }

    // ─── SEED DATA ────────────────────────────────────────────────────────────
    private void seedRooms() {
        addRoom(new Room(101, RoomType.STANDARD,     79.99,  "AC, TV, WiFi"));
        addRoom(new Room(102, RoomType.STANDARD,     79.99,  "AC, TV, WiFi"));
        addRoom(new Room(103, RoomType.STANDARD,     89.99,  "AC, TV, WiFi, Garden View"));
        addRoom(new Room(201, RoomType.DELUXE,      139.99,  "AC, Smart TV, WiFi, Mini-bar, Balcony"));
        addRoom(new Room(202, RoomType.DELUXE,      139.99,  "AC, Smart TV, WiFi, Mini-bar, Balcony"));
        addRoom(new Room(301, RoomType.SUITE,       249.99,  "AC, Smart TV, WiFi, Jacuzzi, Kitchenette"));
        addRoom(new Room(302, RoomType.SUITE,       279.99,  "AC, Smart TV, WiFi, Jacuzzi, Ocean View"));
        addRoom(new Room(401, RoomType.PRESIDENTIAL,499.99,  "All amenities, Butler, Private Pool"));
    }

    // ─── ROOM OPERATIONS ─────────────────────────────────────────────────────
    public void addRoom(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public boolean roomExists(int number) {
        return rooms.containsKey(number);
    }

    public Room getRoom(int number) {
        return rooms.get(number);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public List<Room> getAvailableRooms() {
        return rooms.values().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByType(RoomType type) {
        return rooms.values().stream()
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());
    }

    // ─── GUEST OPERATIONS ────────────────────────────────────────────────────
    public Guest registerGuest(String name, String phone, String email) {
        Guest g = new Guest(name, phone, email);
        guests.put(g.getGuestId(), g);
        return g;
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests.values());
    }

    public Guest getGuest(int id) {
        return guests.get(id);
    }

    // ─── BOOKING OPERATIONS ──────────────────────────────────────────────────
    /** Book a room. Returns the new Booking or throws if unavailable. */
    public Booking bookRoom(int guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Guest guest = guests.get(guestId);
        if (guest == null) throw new IllegalArgumentException("Guest not found.");

        Room room = rooms.get(roomNumber);
        if (room == null)         throw new IllegalArgumentException("Room not found.");
        if (!room.isAvailable())  throw new IllegalStateException("Room is already booked.");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Check-out must be after check-in.");

        room.setAvailable(false);
        Booking booking = new Booking(guest, room, checkIn, checkOut);
        bookings.add(booking);
        return booking;
    }

    /** Check in: move booking to CHECKED_IN */
    public void checkIn(int bookingId) {
        Booking b = findBooking(bookingId);
        if (b.getStatus() != Status.CONFIRMED)
            throw new IllegalStateException("Booking is not in CONFIRMED state.");
        b.setStatus(Status.CHECKED_IN);
    }

    /** Check out: mark booking CHECKED_OUT and free the room */
    public void checkOut(int bookingId) {
        Booking b = findBooking(bookingId);
        if (b.getStatus() != Status.CHECKED_IN && b.getStatus() != Status.CONFIRMED)
            throw new IllegalStateException("Booking is not active.");
        b.setStatus(Status.CHECKED_OUT);
        b.getRoom().setAvailable(true);
    }

    /** Cancel a booking and free the room */
    public void cancelBooking(int bookingId) {
        Booking b = findBooking(bookingId);
        if (b.getStatus() == Status.CHECKED_OUT || b.getStatus() == Status.CANCELLED)
            throw new IllegalStateException("Cannot cancel a completed or already cancelled booking.");
        b.setStatus(Status.CANCELLED);
        b.getRoom().setAvailable(true);
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
                .filter(b -> b.getStatus() == Status.CONFIRMED || b.getStatus() == Status.CHECKED_IN)
                .collect(Collectors.toList());
    }

    // ─── STATS ───────────────────────────────────────────────────────────────
    public long totalRooms()     { return rooms.size(); }
    public long availableRooms() { return rooms.values().stream().filter(Room::isAvailable).count(); }
    public long occupiedRooms()  { return rooms.values().stream().filter(r -> !r.isAvailable()).count(); }
    public double totalRevenue() {
        return bookings.stream()
                .filter(b -> b.getStatus() != Status.CANCELLED)
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────
    private Booking findBooking(int id) {
        return bookings.stream()
                .filter(b -> b.getBookingId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Booking #" + id + " not found."));
    }
}
