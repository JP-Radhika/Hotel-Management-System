package hotel.ui;

import hotel.model.Booking;
import hotel.model.Guest;
import hotel.model.Room;
import hotel.model.Room.RoomType;
import hotel.service.HotelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class MainUI {

    private final HotelService service;
    private final ObservableList<RoomRow>    roomData    = FXCollections.observableArrayList();
    private final ObservableList<BookingRow> bookingData = FXCollections.observableArrayList();
    private Label statsLabel;

    public MainUI(HotelService service) { this.service = service; }

    public BorderPane buildRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color:#f4f4f4; -fx-font-family:'Segoe UI';");

        statsLabel = new Label();
        statsLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");
        HBox header = new HBox(
            new Label("🏨 Hotel Manager") {{ setStyle("-fx-font-size:18px;-fx-font-weight:bold;"); }},
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            statsLabel
        );
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            new Tab("Rooms",    roomsTab()),
            new Tab("Book",     bookTab()),
            new Tab("Bookings", bookingsTab())
        );

        root.setTop(header);
        root.setCenter(tabs);
        refreshStats();
        return root;
    }

    // ── TAB 1: ROOMS ─────────────────────────────────────────────────────────
    private BorderPane roomsTab() {
        TableView<RoomRow> table = makeTable(roomData,
            col("Room #",    "roomNumber", 70),
            col("Type",      "type",       100),
            col("Price",     "price",      90),
            col("Status",    "status",     90),
            col("Amenities", "amenities",  999)
        );
        refreshRoomTable();

        Button btnAdd = new Button("+ Add Room");
        btnAdd.setOnAction(e -> {
            TextField num   = new TextField(); num.setPromptText("Room number");
            TextField price = new TextField(); price.setPromptText("Price/night");
            TextField amen  = new TextField(); amen.setPromptText("Amenities");
            ComboBox<RoomType> type = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
            type.setValue(RoomType.STANDARD);

            showDialog("Add Room", new VBox(8, row("Number:", num), row("Type:", type),
                row("Price:", price), row("Amenities:", amen)), () -> {
                try {
                    int n = Integer.parseInt(num.getText().trim());
                    if (service.roomExists(n)) { alert("Room already exists."); return; }
                    service.addRoom(new Room(n, type.getValue(),
                        Double.parseDouble(price.getText().trim()), amen.getText().trim()));
                    refreshRoomTable(); refreshStats();
                } catch (NumberFormatException ex) { alert("Invalid number format."); }
            });
        });

        BorderPane pane = new BorderPane(table);
        pane.setBottom(hbox(btnAdd));
        pane.setPadding(new Insets(10));
        return pane;
    }

    // ── TAB 2: BOOK ──────────────────────────────────────────────────────────
    private VBox bookTab() {
        TextField name  = new TextField(); name.setPromptText("Guest name");
        TextField phone = new TextField(); phone.setPromptText("Phone");
        TextField email = new TextField(); email.setPromptText("Email");
        ComboBox<Room> roomBox = new ComboBox<>(FXCollections.observableArrayList(service.getAvailableRooms()));
        roomBox.setMaxWidth(Double.MAX_VALUE);
        roomBox.setPromptText("Select available room…");
        DatePicker checkIn  = new DatePicker(LocalDate.now());
        DatePicker checkOut = new DatePicker(LocalDate.now().plusDays(1));

        Label preview = new Label();
        preview.setStyle("-fx-text-fill:#1a6b2a; -fx-font-weight:bold;");
        Runnable updatePreview = () -> {
            Room r = roomBox.getValue();
            if (r == null || checkOut.getValue() == null || !checkOut.getValue().isAfter(checkIn.getValue()))
                { preview.setText(""); return; }
            long n = java.time.temporal.ChronoUnit.DAYS.between(checkIn.getValue(), checkOut.getValue());
            preview.setText(String.format("%d night(s) × $%.2f = $%.2f", n, r.getPricePerNight(), n * r.getPricePerNight()));
        };
        roomBox.setOnAction(e -> updatePreview.run());
        checkIn.setOnAction(e  -> updatePreview.run());
        checkOut.setOnAction(e -> updatePreview.run());

        Button btnBook = new Button("Confirm Booking");
        btnBook.setStyle("-fx-background-color:#2d6a9f;-fx-text-fill:white;-fx-font-weight:bold;");
        btnBook.setMaxWidth(Double.MAX_VALUE);
        btnBook.setOnAction(e -> {
            if (name.getText().isBlank() || roomBox.getValue() == null) { alert("Fill in all fields."); return; }
            try {
                Guest g = service.registerGuest(name.getText().trim(), phone.getText().trim(), email.getText().trim());
                Booking b = service.bookRoom(g.getGuestId(), roomBox.getValue().getRoomNumber(), checkIn.getValue(), checkOut.getValue());
                info("Booking #" + b.getBookingId() + " confirmed!  Total: $" + String.format("%.2f", b.getTotalAmount()));
                name.clear(); phone.clear(); email.clear(); roomBox.setValue(null); preview.setText("");
                roomBox.setItems(FXCollections.observableArrayList(service.getAvailableRooms()));
                refreshRoomTable(); refreshStats();
            } catch (Exception ex) { alert(ex.getMessage()); }
        });

        VBox pane = new VBox(9,
            bold("Guest"), row("Name:", name), row("Phone:", phone), row("Email:", email),
            new Separator(),
            bold("Room & Dates"), row("Room:", roomBox), row("Check-In:", checkIn), row("Check-Out:", checkOut),
            preview, new Separator(), btnBook
        );
        pane.setPadding(new Insets(14));
        pane.setMaxWidth(480);
        return pane;
    }

    // ── TAB 3: BOOKINGS ──────────────────────────────────────────────────────
    private BorderPane bookingsTab() {
        TableView<BookingRow> table = makeTable(bookingData,
            col("ID",     "bookingId",  60),
            col("Guest",  "guestName",  130),
            col("Room",   "roomNumber", 60),
            col("In",     "checkIn",    90),
            col("Out",    "checkOut",   90),
            col("Total",  "total",      80),
            col("Status", "status",     110)
        );
        refreshBookingTable();

        Button btnIn  = styledBtn("Check In",  "#28a745");
        Button btnOut = styledBtn("Check Out", "#e09b2d");
        Button btnCan = styledBtn("Cancel",    "#dc3545");

        btnIn.setOnAction(e -> withSelected(table, r -> {
            service.checkIn(r.getBookingId()); refreshAll(table); info("Checked in!");
        }));
        btnOut.setOnAction(e -> withSelected(table, r -> {
            service.checkOut(r.getBookingId()); refreshAll(table); info("Room is now free.");
        }));
        btnCan.setOnAction(e -> withSelected(table, r -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Cancel booking #" + r.getBookingId() + "?", ButtonType.YES, ButtonType.NO);
            c.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
                try { service.cancelBooking(r.getBookingId()); refreshAll(table); }
                catch (Exception ex) { alert(ex.getMessage()); }
            });
        }));

        BorderPane pane = new BorderPane(table);
        pane.setBottom(hbox(btnIn, btnOut, btnCan));
        pane.setPadding(new Insets(10));
        return pane;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────
    @SafeVarargs
    private <T> TableView<T> makeTable(ObservableList<T> data, TableColumn<T,?>... cols) {
        TableView<T> t = new TableView<>(data);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.getColumns().addAll(cols);
        return t;
    }

    private <T> TableColumn<T, ?> col(String title, String prop, double maxW) {
        TableColumn<T, Object> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        if (maxW < 999) c.setMaxWidth(maxW * 2);
        return c;
    }

    private HBox row(String label, javafx.scene.Node field) {
        Label l = new Label(label); l.setMinWidth(75);
        HBox h = new HBox(8, l, field); h.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS); return h;
    }

    private HBox hbox(javafx.scene.Node... nodes) {
        HBox h = new HBox(8, nodes); h.setPadding(new Insets(8, 0, 0, 0)); return h;
    }

    private Label bold(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-weight:bold;"); return l;
    }

    private Button styledBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;");
        return b;
    }

    private void showDialog(String title, VBox content, Runnable onConfirm) {
        Dialog<Boolean> d = new Dialog<>(); d.setTitle(title);
        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content);
        d.setResultConverter(b -> b == ok);
        d.showAndWait().ifPresent(res -> { if (res) onConfirm.run(); });
    }

    private void withSelected(TableView<BookingRow> table, ThrowingConsumer<BookingRow> action) {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null) { alert("Select a booking first."); return; }
        try { action.accept(r); } catch (Exception ex) { alert(ex.getMessage()); }
    }

    private void refreshAll(TableView<BookingRow> t) { refreshBookingTable(); refreshRoomTable(); refreshStats(); t.refresh(); }
    private void refreshRoomTable()    { roomData.setAll(service.getAllRooms().stream().map(RoomRow::new).toList()); }
    private void refreshBookingTable() { bookingData.setAll(service.getAllBookings().stream().map(BookingRow::new).toList()); }
    private void refreshStats()        { statsLabel.setText(String.format("Rooms: %d  |  Available: %d  |  Occupied: %d  |  Revenue: $%.0f",
            service.totalRooms(), service.availableRooms(), service.occupiedRooms(), service.totalRevenue())); }
    private void alert(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }
    private void info(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }

    @FunctionalInterface interface ThrowingConsumer<T> { void accept(T t) throws Exception; }

    // ── ROW WRAPPERS ─────────────────────────────────────────────────────────
    public static class RoomRow {
        private final Room r;
        public RoomRow(Room r) { this.r = r; }
        public int    getRoomNumber() { return r.getRoomNumber(); }
        public String getType()      { return r.getType().name(); }
        public String getPrice()     { return String.format("$%.2f", r.getPricePerNight()); }
        public String getStatus()    { return r.isAvailable() ? "Available" : "Occupied"; }
        public String getAmenities() { return r.getAmenities(); }
    }

    public static class BookingRow {
        private final Booking b;
        public BookingRow(Booking b) { this.b = b; }
        public int    getBookingId()  { return b.getBookingId(); }
        public String getGuestName()  { return b.getGuest().getName(); }
        public int    getRoomNumber() { return b.getRoom().getRoomNumber(); }
        public String getCheckIn()    { return b.getCheckIn().toString(); }
        public String getCheckOut()   { return b.getCheckOut().toString(); }
        public String getTotal()      { return String.format("$%.2f", b.getTotalAmount()); }
        public String getStatus()     { return b.getStatus().name(); }
    }
}
