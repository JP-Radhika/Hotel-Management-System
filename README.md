
# 🏨 Grand Vista Hotel — Management System
### JavaFX + Collections Framework

---

## Project Structure

```
HotelApp/
└── src/
    ├── module-info.java
    └── hotel/
        ├── HotelApp.java          ← Main entry point
        ├── style.css              ← UI stylesheet
        ├── model/
        │   ├── Room.java          ← Room entity
        │   ├── Guest.java         ← Guest entity
        │   └── Booking.java       ← Booking entity
        ├── service/
        │   └── HotelService.java  ← Business logic + Collections
        └── ui/
            └── MainUI.java        ← All JavaFX UI code
```

---

## Collections Used

| Collection | Purpose |
|---|---|
| `LinkedHashMap<Integer, Room>` | Stores rooms — preserves insertion order |
| `HashMap<Integer, Guest>` | Stores guests by ID — O(1) lookup |
| `ArrayList<Booking>` | Stores all bookings in order |
| Stream API | Filtering available rooms, bookings by status |

---

## Features

### 🛏 Rooms Tab
- View all rooms in a table with room number, type, price, status, amenities
- Filter by: All / Available / Occupied / Room Type
- Click any row to see full room details in the side panel
- **Add new rooms** via dialog (room number, type, price, amenities)

### 📋 Book Room Tab
- Register a guest (name, phone, email)
- Choose from available rooms only
- Select check-in and check-out dates
- **Live price preview** (nights × rate = total)
- Confirm booking — room is immediately marked Occupied

### ✅ Manage Bookings Tab
- View all bookings with full details
- Filter by status: All / CONFIRMED / CHECKED_IN / CHECKED_OUT / CANCELLED
- **Check In** — move booking to CHECKED_IN
- **Check Out** — marks room Available again
- **Cancel** — with confirmation dialog, frees the room
- **Details** — popup with complete booking summary

### 📊 Live Stats Header
- Total rooms / Available / Occupied / Total Revenue
- Updates automatically after every operation

---

## How to Run

### Prerequisites
- Java 17+
- JavaFX SDK 17+ (download from https://openjfx.io)

### Compile & Run (from `src/` directory)

```bash
# Set your JavaFX path
JAVAFX=/path/to/javafx-sdk/lib

# Compile
javac --module-path $JAVAFX --add-modules javafx.controls,javafx.fxml \
  -d out $(find . -name "*.java")

# Copy CSS resource
cp hotel/style.css out/hotel/

# Run
java --module-path $JAVAFX --add-modules javafx.controls,javafx.fxml \
  -cp out hotel.HotelApp
```

### Using IntelliJ IDEA (recommended)
1. Open the `HotelApp` folder as a project
2. Go to **File → Project Structure → Libraries** → Add JavaFX SDK
3. Go to **Run → Edit Configurations** → Add VM options:
   ```
   --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
   ```
4. Set main class to `hotel.HotelApp`
5. Run!

---

## Sample Data (pre-loaded)

| Room | Type | Price/Night |
|------|------|-------------|
| 101–103 | STANDARD | $79–89 |
| 201–202 | DELUXE | $139 |
| 301–302 | SUITE | $249–279 |
| 401 | PRESIDENTIAL | $499 |
