import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class FirstPage extends Application {
    private final LibraryService service = SampleDataFactory.create();
    private final TableView<BookItem> catalogTable = new TableView<>();
    private final TableView<MemberAccount> memberTable = new TableView<>();
    private final TableView<BookLending> lendingTable = new TableView<>();
    private final TableView<BookReservation> reservationTable = new TableView<>();
    private final ListView<String> notificationList = new ListView<>();
    private final TableView<Fine> fineTable = new TableView<>();
    private final TextArea summaryArea = new TextArea();
    private final ComboBox<MemberAccount> memberPicker = new ComboBox<>();
    private final ComboBox<BookItem> itemPicker = new ComboBox<>();
    private final ComboBox<Book> bookPicker = new ComboBox<>();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildTabs());
        root.setBottom(buildSummaryPanel());

        refreshAll();

        Scene scene = new Scene(root, 1200, 760);
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildHeader() {
        Label title = new Label(service.getLibrary().getName());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitle = new Label("Search, manage books, register members, check out, reserve, renew, return, fine, and notify.");
        VBox box = new VBox(6, title, subtitle);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: linear-gradient(to right, #dae7f7, #f8fbff);");
        return box;
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Catalog", buildCatalogTab()),
                new Tab("Members", buildMemberTab()),
                new Tab("Circulation", buildCirculationTab()),
                new Tab("Notifications", buildNotificationTab())
        );
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        return tabs;
    }

    private VBox buildCatalogTab() {
        ComboBox<String> searchMode = new ComboBox<>(FXCollections.observableArrayList("Title", "Author", "Subject", "Publication Date"));
        searchMode.setValue("Title");
        TextField searchInput = new TextField();
        searchInput.setPromptText("Query or YYYY-MM-DD");
        Button searchButton = new Button("Search");
        Button resetButton = new Button("Show All");

        setupCatalogTable();

        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        TextField publisherField = new TextField();
        publisherField.setPromptText("Publisher");
        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        TextField dateField = new TextField();
        dateField.setPromptText("YYYY-MM-DD");
        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Barcode");
        TextField rackField = new TextField();
        rackField.setPromptText("Rack");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        Button addBookButton = new Button("Add Book Copy");
        Button removeBookButton = new Button("Remove Selected");

        searchButton.setOnAction(event -> {
            try {
                catalogTable.setItems(FXCollections.observableArrayList(service.search(searchMode.getValue(), searchInput.getText())));
                setSummary("Catalog searched by " + searchMode.getValue() + ".");
            } catch (Exception ex) {
                setSummary("Invalid search input. Use YYYY-MM-DD for publication date.");
            }
        });
        resetButton.setOnAction(event -> refreshCatalog());
        addBookButton.setOnAction(event -> {
            try {
                Book book = new Book(
                        isbnField.getText(),
                        titleField.getText(),
                        subjectField.getText(),
                        publisherField.getText(),
                        LocalDate.parse(dateField.getText()),
                        List.of(new Author("A-" + System.nanoTime(), authorField.getText()))
                );
                service.addBookItem(new BookItem(
                        barcodeField.getText(),
                        book,
                        new Rack(rackField.getText(), locationField.getText()),
                        BookStatus.AVAILABLE
                ));
                refreshAll();
                setSummary("Added book copy " + barcodeField.getText() + ".");
            } catch (Exception ex) {
                setSummary("Could not add book. Check the date and required fields.");
            }
        });
        removeBookButton.setOnAction(event -> {
            BookItem selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.removeBookItem(selected);
                refreshAll();
                setSummary("Removed book copy " + selected.getBarcode() + ".");
            }
        });

        HBox searchBar = new HBox(10, searchMode, searchInput, searchButton, resetButton);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, isbnField, titleField, subjectField);
        form.addRow(1, publisherField, authorField, dateField);
        form.addRow(2, barcodeField, rackField, locationField);
        form.addRow(3, addBookButton, removeBookButton);

        VBox content = new VBox(12, searchBar, catalogTable, new Label("Librarian Book Management"), form);
        content.setPadding(new Insets(16));
        VBox.setVgrow(catalogTable, Priority.ALWAYS);
        return content;
    }

    private VBox buildMemberTab() {
        setupMemberTable();

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        Button addMemberButton = new Button("Register Member");
        Button cancelMemberButton = new Button("Cancel Membership");
        Button listBooksButton = new Button("Show Checked Out Books");

        addMemberButton.setOnAction(event -> {
            if (!nameField.getText().isBlank() && !emailField.getText().isBlank()) {
                MemberAccount member = service.registerMember(nameField.getText(), emailField.getText());
                refreshAll();
                setSummary("Registered member " + member.getName() + ".");
            }
        });
        cancelMemberButton.setOnAction(event -> {
            MemberAccount selected = memberTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.cancelMembership(selected);
                refreshAll();
                setSummary("Canceled membership for " + selected.getName() + ".");
            }
        });
        listBooksButton.setOnAction(event -> {
            MemberAccount selected = memberTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                List<BookItem> books = service.booksCheckedOutBy(selected);
                if (books.isEmpty()) {
                    setSummary(selected.getName() + " has no checked out books.");
                } else {
                    String details = books.stream()
                            .map(item -> item.getBook().getTitle() + " [" + item.getBarcode() + "]")
                            .reduce((a, b) -> a + "\n" + b)
                            .orElse("");
                    setSummary("Books checked out by " + selected.getName() + ":\n" + details);
                }
            }
        });

        HBox form = new HBox(10, nameField, emailField, addMemberButton, cancelMemberButton, listBooksButton);
        VBox content = new VBox(12, memberTable, form);
        content.setPadding(new Insets(16));
        VBox.setVgrow(memberTable, Priority.ALWAYS);
        return content;
    }

    private SplitPane buildCirculationTab() {
        setupLendingTable();
        setupReservationTable();

        memberPicker.setConverter(memberConverter());
        itemPicker.setConverter(itemConverter());
        bookPicker.setConverter(bookConverter());

        Button checkoutButton = new Button("Check Out");
        Button renewButton = new Button("Renew");
        Button returnButton = new Button("Return");
        Button reserveButton = new Button("Reserve Book");
        Button overdueButton = new Button("Send Overdue Notices");
        Button whoBorrowedButton = new Button("Who Borrowed?");

        checkoutButton.setOnAction(event -> {
            if (memberPicker.getValue() != null && itemPicker.getValue() != null) {
                setSummary(service.checkoutBook(memberPicker.getValue(), itemPicker.getValue()));
                refreshAll();
            }
        });
        renewButton.setOnAction(event -> {
            if (memberPicker.getValue() != null && itemPicker.getValue() != null) {
                setSummary(service.renewBook(memberPicker.getValue(), itemPicker.getValue()));
                refreshAll();
            }
        });
        returnButton.setOnAction(event -> {
            if (memberPicker.getValue() != null && itemPicker.getValue() != null) {
                setSummary(service.returnBook(memberPicker.getValue(), itemPicker.getValue()));
                refreshAll();
            }
        });
        reserveButton.setOnAction(event -> {
            if (memberPicker.getValue() != null && bookPicker.getValue() != null) {
                setSummary(service.reserveBook(memberPicker.getValue(), bookPicker.getValue()));
                refreshAll();
            }
        });
        overdueButton.setOnAction(event -> {
            service.sendOverdueNotifications();
            refreshAll();
            setSummary("Overdue notifications sent.");
        });
        whoBorrowedButton.setOnAction(event -> {
            if (itemPicker.getValue() != null) {
                String borrower = service.whoBorrowed(itemPicker.getValue())
                        .map(MemberAccount::getName)
                        .orElse("No active borrower");
                setSummary("Borrower for " + itemPicker.getValue().getBarcode() + ": " + borrower);
            }
        });

        memberPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getMembers()));
        itemPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
        bookPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory().stream().map(BookItem::getBook).distinct().toList()));

        VBox left = new VBox(12,
                new Label("Member and Item Actions"),
                memberPicker,
                itemPicker,
                bookPicker,
                new HBox(10, checkoutButton, renewButton, returnButton),
                new HBox(10, reserveButton, overdueButton, whoBorrowedButton),
                new Label("Active Lendings"),
                lendingTable
        );
        left.setPadding(new Insets(16));
        VBox.setVgrow(lendingTable, Priority.ALWAYS);

        VBox right = new VBox(12, new Label("Reservations"), reservationTable);
        right.setPadding(new Insets(16));
        VBox.setVgrow(reservationTable, Priority.ALWAYS);

        SplitPane pane = new SplitPane(left, right);
        pane.setDividerPositions(0.65);
        return pane;
    }

    private VBox buildNotificationTab() {
        notificationList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Fine, String> memberColumn = new TableColumn<>("Member");
        memberColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().memberName()));
        TableColumn<Fine, String> bookColumn = new TableColumn<>("Book");
        bookColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().bookTitle()));
        TableColumn<Fine, String> daysColumn = new TableColumn<>("Late Days");
        daysColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().lateDays())));
        TableColumn<Fine, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper("$" + data.getValue().amount()));
        fineTable.getColumns().setAll(memberColumn, bookColumn, daysColumn, amountColumn);

        VBox content = new VBox(12, new Label("Notifications"), notificationList, new Label("Collected Fines"), fineTable);
        content.setPadding(new Insets(16));
        VBox.setVgrow(notificationList, Priority.ALWAYS);
        VBox.setVgrow(fineTable, Priority.ALWAYS);
        return content;
    }

    private VBox buildSummaryPanel() {
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(5);
        VBox box = new VBox(6, new Label("System Summary"), summaryArea);
        box.setPadding(new Insets(12, 16, 16, 16));
        return box;
    }

    private void setupCatalogTable() {
        TableColumn<BookItem, String> barcode = new TableColumn<>("Barcode");
        barcode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBarcode()));
        TableColumn<BookItem, String> title = new TableColumn<>("Title");
        title.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBook().getTitle()));
        TableColumn<BookItem, String> author = new TableColumn<>("Author");
        author.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBook().getAuthorNames()));
        TableColumn<BookItem, String> subject = new TableColumn<>("Subject");
        subject.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBook().getSubject()));
        TableColumn<BookItem, String> rack = new TableColumn<>("Rack");
        rack.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRack().toString()));
        TableColumn<BookItem, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        catalogTable.getColumns().setAll(barcode, title, author, subject, rack, status);
    }

    private void setupMemberTable() {
        TableColumn<MemberAccount, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        TableColumn<MemberAccount, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        TableColumn<MemberAccount, String> card = new TableColumn<>("Card Barcode");
        card.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLibraryCard().getBarcode()));
        TableColumn<MemberAccount, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        TableColumn<MemberAccount, String> fine = new TableColumn<>("Outstanding Fine");
        fine.setCellValueFactory(data -> new ReadOnlyStringWrapper("$" + data.getValue().getOutstandingFine()));
        memberTable.getColumns().setAll(name, email, card, status, fine);
    }

    private void setupLendingTable() {
        TableColumn<BookLending, String> member = new TableColumn<>("Member");
        member.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMember().getName()));
        TableColumn<BookLending, String> item = new TableColumn<>("Book Item");
        item.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBookItem().getBarcode()));
        TableColumn<BookLending, String> title = new TableColumn<>("Title");
        title.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBookItem().getBook().getTitle()));
        TableColumn<BookLending, String> due = new TableColumn<>("Due Date");
        due.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getDueDate())));
        TableColumn<BookLending, String> returned = new TableColumn<>("Returned");
        returned.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getReturnDate())));
        lendingTable.getColumns().setAll(member, item, title, due, returned);
    }

    private void setupReservationTable() {
        TableColumn<BookReservation, String> member = new TableColumn<>("Member");
        member.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMember().getName()));
        TableColumn<BookReservation, String> title = new TableColumn<>("Book");
        title.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getBook().getTitle()));
        TableColumn<BookReservation, String> created = new TableColumn<>("Created");
        created.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getCreatedOn())));
        TableColumn<BookReservation, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus().name()));
        reservationTable.getColumns().setAll(member, title, created, status);
    }

    private StringConverter<MemberAccount> memberConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(MemberAccount object) {
                return object == null ? "" : object.getName() + " [" + object.getLibraryCard().getBarcode() + "]";
            }

            @Override
            public MemberAccount fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<BookItem> itemConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(BookItem object) {
                return object == null ? "" : object.getBarcode() + " - " + object.getBook().getTitle();
            }

            @Override
            public BookItem fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<Book> bookConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Book object) {
                return object == null ? "" : object.getTitle();
            }

            @Override
            public Book fromString(String string) {
                return null;
            }
        };
    }

    private void refreshAll() {
        refreshCatalog();
        memberTable.setItems(FXCollections.observableArrayList(service.getLibrary().getMembers()));
        lendingTable.setItems(FXCollections.observableArrayList(service.getLendings()));
        reservationTable.setItems(FXCollections.observableArrayList(service.getReservations()));
        notificationList.setItems(FXCollections.observableArrayList(service.getNotifications().stream().map(Notification::toString).toList()));
        fineTable.setItems(FXCollections.observableArrayList(service.getFines()));
        memberPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getMembers()));
        itemPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
        bookPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory().stream().map(BookItem::getBook).distinct().toList()));
        setSummary("""
                Library: %s
                Inventory copies: %d
                Members: %d
                Active lendings: %d
                Reservations: %d
                Notifications: %d
                """.formatted(
                service.getLibrary().getName(),
                service.getLibrary().getInventory().size(),
                service.getLibrary().getMembers().size(),
                service.getLendings().stream().filter(BookLending::isActive).count(),
                service.getReservations().size(),
                service.getNotifications().size()
        ));
    }

    private void refreshCatalog() {
        catalogTable.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
    }

    private void setSummary(String text) {
        summaryArea.setText(text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
