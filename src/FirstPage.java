import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstPage extends Application {

    private final LibraryService service = SampleDataFactory.create();

    private Stage primaryStage;
    private final Map<String, String> credentials = new HashMap<>();
    private StackPane formSwapper;
    private Node loginFormNode;
    private Node registerFormNode;
    private Node forgotFormNode;

    private String loggedInEmail;
    private String loggedInName;
    private boolean isLibrarian;

    private Label pageTitleLabel;
    private StackPane contentPane;
    private Button activeNavBtn;

    private Node catalogPanel;
    private Node membersPanel;
    private Node circulationPanel;
    private Node notificationsPanel;
    private Node myBooksPanel;

    private final TableView<BookItem>       catalogTable  = new TableView<>();
    private final TableView<MemberAccount>  memberTable   = new TableView<>();
    private final TableView<BookLending>    lendingTable  = new TableView<>();
    private final TableView<Fine>            fineTable         = new TableView<>();
    private final TableView<BookReservation> reservationTable  = new TableView<>();
    private final ListView<String>           notifList         = new ListView<>();

    private final TableView<BookItem>          memberCatalogTable   = new TableView<>();
    private final TableView<BookLending>       myCheckoutsTable     = new TableView<>();
    private final TableView<BookReservation>   myReservationsTable  = new TableView<>();
    private final ListView<String>             myNotifList          = new ListView<>();

    private final Label totalBooksLbl    = new Label("0");
    private final Label activeMembersLbl = new Label("0");
    private final Label lendingsLbl      = new Label("0");
    private final Label reservationsLbl  = new Label("0");

    private final ComboBox<MemberAccount> memberPicker = new ComboBox<>();
    private final ComboBox<BookItem>      itemPicker   = new ComboBox<>();
    private final ComboBox<Book>          bookPicker   = new ComboBox<>();
    private final TextArea                summaryArea  = new TextArea();

    private final ComboBox<Book> memberBookPicker = new ComboBox<>();

    private int librarianSeenNotifCount = 0;
    private int memberSeenNotifCount    = 0;
    private Button librarianNotifBtn;
    private Button memberNotifBtn;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        credentials.put("librarian@library.local", "admin123");
        credentials.put("alice@example.com", "alice123");
        credentials.put("bob@example.com",   "bob123");
        showLoginScreen();
    }


    private void showLoginScreen() {
        formSwapper      = new StackPane();
        loginFormNode    = buildLoginForm();
        registerFormNode = buildRegisterForm();
        forgotFormNode   = buildForgotForm();
        formSwapper.getChildren().setAll(loginFormNode);

        StackPane rightWrapper = new StackPane(formSwapper);
        rightWrapper.getStyleClass().add("auth-right");

        HBox root = new HBox(buildAuthLeft(), rightWrapper);
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);

        Scene scene = new Scene(root, 1000, 660);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Sign In — " + service.getLibrary().getName());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox buildAuthLeft() {
        VBox left = new VBox();
        left.getStyleClass().add("auth-left");
        left.setAlignment(Pos.CENTER);

        VBox content = new VBox(20);
        content.getStyleClass().add("auth-left-content");
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(300);

        StackPane logoIcon = new StackPane();
        logoIcon.getStyleClass().add("auth-logo-icon");

// load image (change file name later)
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));

        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(60);   // adjust size
        logoView.setFitHeight(60);
        logoView.setPreserveRatio(true);

        logoIcon.getChildren().add(logoView);
        Label libName = new Label(service.getLibrary().getName());
        libName.getStyleClass().add("auth-library-name");
        libName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        libName.setWrapText(true);

        Label tagline = new Label("Your complete library management solution. Manage books, members, and more.");
        tagline.getStyleClass().add("auth-tagline");
        tagline.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        tagline.setWrapText(true);


        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2A2D40;");

        VBox features = new VBox(10);
        features.setAlignment(Pos.CENTER_LEFT);
        for (String f : List.of("✓  Book catalog & inventory", "✓  Member management",
                "✓  Checkout & reservations", "✓  Fines & notifications")) {
            Label lbl = new Label(f);
            lbl.getStyleClass().add("auth-feature-item");
            features.getChildren().add(lbl);
        }

        content.getChildren().addAll(logoIcon, libName, tagline, sep, features);
        left.getChildren().add(content);
        return left;
    }


    private VBox buildLoginForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-form-card");

        Label title    = new Label("Welcome back");
        title.getStyleClass().add("auth-form-title");
        Label subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("auth-form-subtitle");

        VBox emailGroup = inputGroup("Email address");
        TextField emailField = authInput("you@example.com");
        emailGroup.getChildren().add(emailField);

        VBox passGroup = inputGroup("Password");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passField.getStyleClass().add("auth-input");
        passField.setMaxWidth(Double.MAX_VALUE);
        passGroup.getChildren().add(passField);

        Button forgotBtn = new Button("Forgot password?");
        forgotBtn.getStyleClass().add("auth-link-btn");
        forgotBtn.setOnAction(e -> formSwapper.getChildren().setAll(forgotFormNode));
        HBox forgotRow = new HBox(forgotBtn);
        forgotRow.setAlignment(Pos.CENTER_RIGHT);

        Label errorLbl = new Label();
        errorLbl.getStyleClass().add("auth-error");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);
        errorLbl.setMaxWidth(Double.MAX_VALUE);
        errorLbl.setWrapText(true);

        Button signInBtn = new Button("Sign In");
        signInBtn.getStyleClass().add("auth-btn");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setDefaultButton(true);
        signInBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            if (email.isBlank() || pass.isBlank()) {
                showMsg(errorLbl, "auth-error", "Please fill in all fields.");
            } else if (!credentials.containsKey(email) || !credentials.get(email).equals(pass)) {
                showMsg(errorLbl, "auth-error", "Invalid email or password.");
            } else {
                loginAs(email);
            }
        });

        HBox registerRow = new HBox(6);
        registerRow.setAlignment(Pos.CENTER);
        Label noAccount = new Label("Don't have an account?");
        noAccount.getStyleClass().add("auth-muted");
        Button registerLink = new Button("Create Account");
        registerLink.getStyleClass().add("auth-link-btn");
        registerLink.setOnAction(e -> formSwapper.getChildren().setAll(registerFormNode));
        registerRow.getChildren().addAll(noAccount, registerLink);

        card.getChildren().addAll(title, subtitle, emailGroup, passGroup, forgotRow, errorLbl, signInBtn, new Separator(), registerRow);
        return card;
    }

    private void loginAs(String email) {
        loggedInEmail = email;

        isLibrarian = service.getLibrary().getLibrarians().stream()
                .anyMatch(l -> l.getEmail().equals(email));

        if (isLibrarian) {
            loggedInName = service.getLibrary().getLibrarians().stream()
                    .filter(l -> l.getEmail().equals(email))
                    .map(LibrarianAccount::getName)
                    .findFirst().orElse(email);
        } else {
            loggedInName = service.getLibrary().getMembers().stream()
                    .filter(m -> m.getEmail().equals(email))
                    .map(MemberAccount::getName)
                    .findFirst().orElse(email);
        }

        showMainScreen();
    }


    private VBox buildRegisterForm() {
        VBox card = new VBox(14);
        card.getStyleClass().add("auth-form-card");

        Label title    = new Label("Create Account");
        title.getStyleClass().add("auth-form-title");
        Label subtitle = new Label("Join the library management system");
        subtitle.getStyleClass().add("auth-form-subtitle");

        VBox nameGroup = inputGroup("Full name");
        TextField nameField = authInput("e.g. John Smith");
        nameGroup.getChildren().add(nameField);

        VBox emailGroup = inputGroup("Email address");
        TextField emailField = authInput("you@example.com");
        emailGroup.getChildren().add(emailField);

        VBox passGroup = inputGroup("Password");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Min. 6 characters");
        passField.getStyleClass().add("auth-input");
        passField.setMaxWidth(Double.MAX_VALUE);
        passGroup.getChildren().add(passField);

        VBox confirmGroup = inputGroup("Confirm password");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Repeat password");
        confirmField.getStyleClass().add("auth-input");
        confirmField.setMaxWidth(Double.MAX_VALUE);
        confirmGroup.getChildren().add(confirmField);

        Label msgLbl = new Label();
        msgLbl.setVisible(false);
        msgLbl.setManaged(false);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setWrapText(true);

        Button createBtn = new Button("Create Account");
        createBtn.getStyleClass().add("auth-btn");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setDefaultButton(true);
        createBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String pass    = passField.getText();
            String confirm = confirmField.getText();

            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                showMsg(msgLbl, "auth-error", "Please fill in all fields.");
            } else if (pass.length() < 6) {
                showMsg(msgLbl, "auth-error", "Password must be at least 6 characters.");
            } else if (!pass.equals(confirm)) {
                showMsg(msgLbl, "auth-error", "Passwords do not match.");
            } else if (credentials.containsKey(email)) {
                showMsg(msgLbl, "auth-error", "An account with this email already exists.");
            } else {
                credentials.put(email, pass);
                service.registerMember(name, email);
                showMsg(msgLbl, "auth-success", "Account created! You can now sign in.");
                nameField.clear(); emailField.clear(); passField.clear(); confirmField.clear();
            }
        });

        HBox backRow = new HBox(6);
        backRow.setAlignment(Pos.CENTER);
        Label alreadyLbl = new Label("Already have an account?");
        alreadyLbl.getStyleClass().add("auth-muted");
        Button backBtn = new Button("Sign In");
        backBtn.getStyleClass().add("auth-link-btn");
        backBtn.setOnAction(e -> formSwapper.getChildren().setAll(loginFormNode));
        backRow.getChildren().addAll(alreadyLbl, backBtn);

        card.getChildren().addAll(title, subtitle, nameGroup, emailGroup, passGroup, confirmGroup, msgLbl, createBtn, new Separator(), backRow);
        return card;
    }


    private VBox buildForgotForm() {
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-form-card");

        Label title    = new Label("Reset Password");
        title.getStyleClass().add("auth-form-title");
        Label subtitle = new Label("Enter your email and we'll send you a reset link.");
        subtitle.getStyleClass().add("auth-form-subtitle");
        subtitle.setWrapText(true);

        VBox emailGroup = inputGroup("Email address");
        TextField emailField = authInput("you@example.com");
        emailGroup.getChildren().add(emailField);

        Label msgLbl = new Label();
        msgLbl.setVisible(false);
        msgLbl.setManaged(false);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setWrapText(true);

        Button sendBtn = new Button("Send Reset Link");
        sendBtn.getStyleClass().add("auth-btn");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setDefaultButton(true);
        sendBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isBlank()) {
                showMsg(msgLbl, "auth-error", "Please enter your email address.");
            } else if (!credentials.containsKey(email)) {
                showMsg(msgLbl, "auth-error", "No account found with that email address.");
            } else {
                showMsg(msgLbl, "auth-success", "Reset link sent to " + email + ". Check your inbox.");
                emailField.clear();
            }
        });

        HBox backRow = new HBox(6);
        backRow.setAlignment(Pos.CENTER);
        Label rememberLbl = new Label("Remember your password?");
        rememberLbl.getStyleClass().add("auth-muted");
        Button backBtn = new Button("Sign In");
        backBtn.getStyleClass().add("auth-link-btn");
        backBtn.setOnAction(e -> formSwapper.getChildren().setAll(loginFormNode));
        backRow.getChildren().addAll(rememberLbl, backBtn);

        card.getChildren().addAll(title, subtitle, emailGroup, msgLbl, sendBtn, new Separator(), backRow);
        return card;
    }

    private VBox inputGroup(String labelText) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        group.getChildren().add(lbl);
        return group;
    }

    private TextField authInput(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("auth-input");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private void showMsg(Label lbl, String cssClass, String msg) {
        lbl.getStyleClass().setAll(cssClass);
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }


    private void showMainScreen() {
        pageTitleLabel = new Label(isLibrarian ? "CATALOG" : "CATALOG");
        pageTitleLabel.getStyleClass().add("page-title");
        contentPane = new StackPane();
        contentPane.getStyleClass().add("content-area");

        if (isLibrarian) {
            buildLibrarianPanels();
        } else {
            buildMemberPanels();
        }

        VBox rightSide = new VBox(buildTopBar(), contentPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setLeft(isLibrarian ? buildLibrarianSidebar() : buildMemberSidebar());
        root.setCenter(rightSide);

        refreshAll();

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle(service.getLibrary().getName());
        primaryStage.setScene(scene);
    }


    private void buildLibrarianPanels() {
        catalogPanel       = buildCatalogPanel();
        membersPanel       = buildMembersPanel();
        circulationPanel   = buildCirculationPanel();
        notificationsPanel = buildNotificationsPanel();
        contentPane.getChildren().setAll(catalogPanel);
    }

    private VBox buildLibrarianSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        sidebar.getChildren().addAll(sidebarLogo(), new Separator(), buildLibrarianNav(), buildSidebarBottom());
        return sidebar;
    }

    private VBox buildLibrarianNav() {
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(8, 0, 0, 0));
        VBox.setVgrow(nav, Priority.NEVER);

        Button catalogBtn   = navBtn("📋  Catalog");
        Button membersBtn   = navBtn("👥  Members");
        Button circBtn      = navBtn("🔄  Circulation");
        Button notifBtn     = navBtn("🔔  Notifications");
        librarianNotifBtn   = notifBtn;

        setActive(catalogBtn);
        catalogBtn.setOnAction(e -> switchTo(catalogBtn,  catalogPanel,       "CATALOG"));
        membersBtn.setOnAction(e -> switchTo(membersBtn,  membersPanel,       "MEMBERS"));
        circBtn.setOnAction(e    -> switchTo(circBtn,     circulationPanel,   "CIRCULATION"));
        notifBtn.setOnAction(e   -> {
            librarianSeenNotifCount = service.getNotifications().size();
            updateNotifBadge(librarianNotifBtn, "🔔  Notifications", 0);
            switchTo(notifBtn, notificationsPanel, "NOTIFICATIONS");
        });

        nav.getChildren().addAll(catalogBtn, membersBtn, circBtn, notifBtn);
        return nav;
    }


    private void buildMemberPanels() {
        catalogPanel  = buildMemberCatalogPanel();
        myBooksPanel  = buildMyBooksPanel();
        notificationsPanel = buildMemberNotificationsPanel();
        contentPane.getChildren().setAll(catalogPanel);
    }

    private VBox buildMemberSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(8, 0, 0, 0));

        Button catalogBtn = navBtn("📋  Catalog");
        Button myBooksBtn = navBtn("📚  My Books");
        Button notifBtn   = navBtn("🔔  Notifications");
        memberNotifBtn    = notifBtn;

        setActive(catalogBtn);
        catalogBtn.setOnAction(e -> switchTo(catalogBtn, catalogPanel,       "CATALOG"));
        myBooksBtn.setOnAction(e -> switchTo(myBooksBtn, myBooksPanel,       "MY BOOKS"));
        notifBtn.setOnAction(e   -> {
            MemberAccount me = currentMember();
            memberSeenNotifCount = me == null ? 0 : (int) service.getNotifications().stream()
                    .filter(n -> n.recipient().equals(me.getName())).count();
            updateNotifBadge(memberNotifBtn, "🔔  Notifications", 0);
            switchTo(notifBtn, notificationsPanel, "NOTIFICATIONS");
        });

        nav.getChildren().addAll(catalogBtn, myBooksBtn, notifBtn);

        sidebar.getChildren().addAll(sidebarLogo(), new Separator(), nav, buildSidebarBottom());
        return sidebar;
    }


    private HBox sidebarLogo() {
        HBox logoArea = new HBox(12);
        logoArea.setPadding(new Insets(22, 16, 22, 16));
        logoArea.setAlignment(Pos.CENTER_LEFT);

        StackPane logoIcon = new StackPane();
        logoIcon.getStyleClass().add("logo-icon");
        Label logoLetter = new Label("L");
        logoLetter.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;");
        logoIcon.getChildren().add(logoLetter);

        VBox logoText = new VBox(2);
        Label libName = new Label(service.getLibrary().getName());
        libName.getStyleClass().add("logo-title");
        libName.setWrapText(true);
        Label libSubtitle = new Label("Library Management");
        libSubtitle.getStyleClass().add("logo-subtitle");
        logoText.getChildren().addAll(libName, libSubtitle);

        logoArea.getChildren().addAll(logoIcon, logoText);
        return logoArea;
    }

    private VBox buildSidebarBottom() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button signOutBtn = new Button("⬅  Sign Out");
        signOutBtn.getStyleClass().add("nav-item");
        signOutBtn.setMaxWidth(Double.MAX_VALUE);
        signOutBtn.setAlignment(Pos.CENTER_LEFT);
        signOutBtn.setOnAction(e -> showLoginScreen());

        Button newEntryBtn = new Button("+ New Entry");
        newEntryBtn.getStyleClass().add("new-entry-btn");
        newEntryBtn.setMaxWidth(Double.MAX_VALUE);
        newEntryBtn.setOnAction(e -> showAddBookDialog());
        VBox.setMargin(newEntryBtn, new Insets(4, 16, 24, 16));

        VBox bottom = new VBox(spacer, signOutBtn);
        if (isLibrarian) bottom.getChildren().add(newEntryBtn);
        return bottom;
    }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-item");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    private void setActive(Button btn) {
        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("active");
        activeNavBtn = btn;
        btn.getStyleClass().add("active");
    }

    private void switchTo(Button btn, Node panel, String title) {
        setActive(btn);
        contentPane.getChildren().setAll(panel);
        pageTitleLabel.setText(title);
        refreshAll();
    }


    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title, author, or barcode...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);

        ComboBox<String> searchMode = new ComboBox<>(
                FXCollections.observableArrayList("Title", "Author", "Subject"));
        searchMode.setValue("Title");
        searchMode.getStyleClass().add("search-mode-box");
        searchField.setOnAction(e -> {
            TableView<BookItem> target = isLibrarian ? catalogTable : memberCatalogTable;
            target.setItems(FXCollections.observableArrayList(
                    service.search(searchMode.getValue(), searchField.getText())));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String role = isLibrarian ? "HEAD LIBRARIAN" : "MEMBER";
        Label userLabel = new Label(loggedInName + "\n" + role);
        userLabel.getStyleClass().add("librarian-label");
        userLabel.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

        bar.getChildren().addAll(pageTitleLabel, searchField, searchMode, spacer, userLabel);
        return bar;
    }


    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        row.setPadding(new Insets(24, 24, 16, 24));

        VBox c1 = statCard(totalBooksLbl,    "TOTAL BOOKS",     "📚", "#3B82F6");
        VBox c2 = statCard(activeMembersLbl, "ACTIVE MEMBERS",  "👥", "#10B981");
        VBox c3 = statCard(lendingsLbl,      "ACTIVE LENDINGS", "🔄", "#F5A623");
        VBox c4 = statCard(reservationsLbl,  "RESERVATIONS",    "🔖", "#8B5CF6");

        for (Node c : List.of(c1, c2, c3, c4)) HBox.setHgrow(c, Priority.ALWAYS);
        row.getChildren().addAll(c1, c2, c3, c4);
        return row;
    }

    private VBox statCard(Label valueLbl, String title, String icon, String color) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("stat-icon-box");
        iconBox.setStyle("-fx-background-color: " + color + "22;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");
        iconBox.getChildren().add(iconLbl);

        valueLbl.getStyleClass().add("stat-value");
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-label");
        card.getChildren().addAll(iconBox, valueLbl, titleLbl);
        return card;
    }


    private VBox buildCatalogPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("content-area");

        VBox section = new VBox(14);
        section.setPadding(new Insets(0, 24, 24, 24));
        VBox.setVgrow(section, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Book Inventory");
        title.getStyleClass().add("section-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("btn-ghost");
        Button addBookBtn = new Button("+ Add Book");
        addBookBtn.getStyleClass().add("btn-primary");
        addBookBtn.setOnAction(e -> showAddBookDialog());
        header.getChildren().addAll(title, sp, exportBtn, addBookBtn);

        setupCatalogTable(catalogTable, true);
        VBox.setVgrow(catalogTable, Priority.ALWAYS);
        section.getChildren().addAll(header, catalogTable);

        panel.getChildren().addAll(buildStatsRow(), section);
        VBox.setVgrow(section, Priority.ALWAYS);
        return panel;
    }

    private VBox buildMembersPanel() {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24));
        VBox.setVgrow(panel, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Members");
        title.getStyleClass().add("section-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        TextField nameField  = new TextField();
        nameField.setPromptText("Full name");
        nameField.getStyleClass().add("form-field");
        nameField.setPrefWidth(150);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("form-field");
        emailField.setPrefWidth(180);

        Button addBtn = new Button("+ Register");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            if (!nameField.getText().isBlank() && !emailField.getText().isBlank()) {
                service.registerMember(nameField.getText(), emailField.getText());
                nameField.clear(); emailField.clear(); refreshAll();
            }
        });
        header.getChildren().addAll(title, sp, nameField, emailField, addBtn);

        setupMemberTable();
        VBox.setVgrow(memberTable, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel Membership");
        cancelBtn.getStyleClass().add("btn-danger");
        cancelBtn.setOnAction(e -> {
            MemberAccount sel = memberTable.getSelectionModel().getSelectedItem();
            if (sel != null) { service.cancelMembership(sel); refreshAll(); }
        });

        panel.getChildren().addAll(header, memberTable, cancelBtn);
        return panel;
    }

    private void setupMemberTable() {
        memberTable.getStyleClass().add("custom-table");

        TableColumn<MemberAccount, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setPrefWidth(110);
        statusCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getStatus().name()));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add("ACTIVE".equals(s) ? "badge-active" : "badge-canceled");
                setGraphic(badge); setText(null);
            }
        });

        memberTable.getColumns().setAll(
            col("NAME",  200, d -> new ReadOnlyStringWrapper(d.getValue().getName())),
            col("EMAIL", 220, d -> new ReadOnlyStringWrapper(d.getValue().getEmail())),
            col("CARD",  140, d -> new ReadOnlyStringWrapper(d.getValue().getLibraryCard().getBarcode())),
            statusCol,
            col("FINE",  100, d -> new ReadOnlyStringWrapper("$" + d.getValue().getOutstandingFine()))
        );
        memberTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        memberTable.setPlaceholder(new Label("No members registered."));
    }

    private VBox buildCirculationPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24));

        Label title = new Label("Circulation");
        title.getStyleClass().add("section-title");

        memberPicker.setConverter(conv(m -> m.getName()));
        itemPicker.setConverter(conv(b -> b.getBarcode() + " — " + b.getBook().getTitle()));
        bookPicker.setConverter(conv(b -> b.getTitle()));
        memberPicker.setMaxWidth(Double.MAX_VALUE);
        itemPicker.setMaxWidth(Double.MAX_VALUE);
        bookPicker.setMaxWidth(Double.MAX_VALUE);
        memberPicker.setPromptText("Select member...");
        itemPicker.setPromptText("Select book copy...");
        bookPicker.setPromptText("Select book (reserve)...");

        HBox pickers = new HBox(12, memberPicker, itemPicker, bookPicker);
        HBox.setHgrow(memberPicker, Priority.ALWAYS);
        HBox.setHgrow(itemPicker,   Priority.ALWAYS);
        HBox.setHgrow(bookPicker,   Priority.ALWAYS);

        Button checkoutBtn = new Button("Check Out");
        Button renewBtn    = new Button("Renew");
        Button returnBtn   = new Button("Return");
        Button reserveBtn  = new Button("Reserve");
        Button overdueBtn  = new Button("Send Overdue Notices");

        checkoutBtn.getStyleClass().add("btn-primary");
        renewBtn.getStyleClass().add("btn-ghost");
        returnBtn.getStyleClass().add("btn-ghost");
        reserveBtn.getStyleClass().add("btn-ghost");
        overdueBtn.getStyleClass().add("btn-ghost");

        checkoutBtn.setOnAction(e -> setSummary(service.checkoutBook(memberPicker.getValue(), itemPicker.getValue())));
        renewBtn.setOnAction(e    -> setSummary(service.renewBook(memberPicker.getValue(), itemPicker.getValue())));
        returnBtn.setOnAction(e   -> setSummary(service.returnBook(memberPicker.getValue(), itemPicker.getValue())));
        reserveBtn.setOnAction(e  -> setSummary(service.reserveBook(memberPicker.getValue(), bookPicker.getValue())));
        overdueBtn.setOnAction(e  -> { service.sendOverdueNotifications(); refreshAll(); setSummary("Overdue notices sent."); });

        HBox actions = new HBox(10, checkoutBtn, renewBtn, returnBtn, reserveBtn, overdueBtn);

        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(2);
        summaryArea.getStyleClass().add("summary-area");

        setupLendingTable();
        VBox.setVgrow(lendingTable, Priority.ALWAYS);

        Label lendLabel = new Label("Active Lendings");
        lendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        setupReservationTable();
        Label reservLabel = new Label("Pending Reservations");
        reservLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        panel.getChildren().addAll(title, pickers, actions, summaryArea, lendLabel, lendingTable, reservLabel, reservationTable);
        return panel;
    }

    private void setupLendingTable() {
        lendingTable.getStyleClass().add("custom-table");
        lendingTable.getColumns().setAll(
            col("MEMBER",   160, d -> new ReadOnlyStringWrapper(d.getValue().getMember().getName())),
            col("BARCODE",  110, d -> new ReadOnlyStringWrapper(d.getValue().getBookItem().getBarcode())),
            col("TITLE",    200, d -> new ReadOnlyStringWrapper(d.getValue().getBookItem().getBook().getTitle())),
            col("DUE DATE", 120, d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getDueDate()))),
            col("RETURNED", 120, d -> new ReadOnlyStringWrapper(
                    d.getValue().getReturnDate() == null ? "—" : String.valueOf(d.getValue().getReturnDate())))
        );
        lendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        lendingTable.setPlaceholder(new Label("No active lendings."));
    }

    private void setupReservationTable() {
        reservationTable.getStyleClass().add("custom-table");

        TableColumn<BookReservation, String> checkoutCol = new TableColumn<>("ACTION");
        checkoutCol.setPrefWidth(130);
        checkoutCol.setCellFactory(c -> new TableCell<>() {
            final Button btn = new Button("Approve Pick Up");
            { btn.getStyleClass().add("btn-primary");
              btn.setOnAction(e -> {
                  BookReservation r = getTableView().getItems().get(getIndex());
                  setSummary(service.checkoutReservation(r));
              }); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                BookReservation r = getTableView().getItems().get(getIndex());
                btn.setVisible(r.getStatus() == ReservationStatus.PENDING_PICKUP);
                setGraphic(btn); setText(null);
            }
        });

        reservationTable.getColumns().setAll(
            col("MEMBER",  160, d -> new ReadOnlyStringWrapper(d.getValue().getMember().getName())),
            col("TITLE",   200, d -> new ReadOnlyStringWrapper(d.getValue().getBook().getTitle())),
            col("STATUS",  130, d -> new ReadOnlyStringWrapper(d.getValue().getStatus().name())),
            col("DATE",    110, d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getCreatedOn()))),
            checkoutCol
        );
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reservationTable.setPlaceholder(new Label("No pending reservations."));
    }

    private VBox buildNotificationsPanel() {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24));

        Label title = new Label("Notifications & Fines");
        title.getStyleClass().add("section-title");

        notifList.getStyleClass().add("notif-list");
        notifList.setPlaceholder(new Label("No notifications yet."));
        VBox.setVgrow(notifList, Priority.ALWAYS);

        setupFineTable();
        Label fineLabel = new Label("Collected Fines");
        fineLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        panel.getChildren().addAll(title, notifList, fineLabel, fineTable);
        return panel;
    }

    private void setupFineTable() {
        fineTable.getStyleClass().add("custom-table");
        fineTable.getColumns().setAll(
            col("MEMBER",    180, d -> new ReadOnlyStringWrapper(d.getValue().memberName())),
            col("BOOK",      200, d -> new ReadOnlyStringWrapper(d.getValue().bookTitle())),
            col("LATE DAYS", 100, d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().lateDays()))),
            col("AMOUNT",    100, d -> new ReadOnlyStringWrapper("$" + d.getValue().amount()))
        );
        fineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fineTable.setPlaceholder(new Label("No fines recorded."));
    }


    private VBox buildMemberCatalogPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("content-area");

        VBox section = new VBox(14);
        section.setPadding(new Insets(0, 24, 24, 24));
        VBox.setVgrow(section, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Book Catalog");
        title.getStyleClass().add("section-title");
        header.getChildren().add(title);

        setupCatalogTable(memberCatalogTable, false);
        TableColumn<BookItem, String> reserveCol = new TableColumn<>("ACTION");
        reserveCol.setPrefWidth(100);
        reserveCol.setCellFactory(c -> new TableCell<>() {
            final Button btn = new Button("Reserve");
            { btn.getStyleClass().add("btn-primary");
              btn.setOnAction(e -> {
                  MemberAccount me = currentMember();
                  BookItem item = getTableView().getItems().get(getIndex());
                  if (me != null) {
                      new Alert(Alert.AlertType.INFORMATION, service.reserveBook(me, item.getBook())).showAndWait();
                      refreshAll();
                  }
              }); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btn); setText(null);
            }
        });
        memberCatalogTable.getColumns().add(reserveCol);
        VBox.setVgrow(memberCatalogTable, Priority.ALWAYS);
        section.getChildren().addAll(header, memberCatalogTable);

        panel.getChildren().addAll(buildStatsRow(), section);
        VBox.setVgrow(section, Priority.ALWAYS);
        return panel;
    }

    private VBox buildMyBooksPanel() {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24));

        Label title = new Label("My Books");
        title.getStyleClass().add("section-title");

        memberBookPicker.setConverter(conv(b -> b.getTitle()));
        memberBookPicker.setPromptText("Choose a book to reserve...");
        memberBookPicker.setMaxWidth(Double.MAX_VALUE);

        Button reserveBtn = new Button("Reserve");
        reserveBtn.getStyleClass().add("btn-primary");
        reserveBtn.setOnAction(e -> {
            MemberAccount me = currentMember();
            Book book = memberBookPicker.getValue();
            if (me != null && book != null) {
                new Alert(Alert.AlertType.INFORMATION, service.reserveBook(me, book)).showAndWait();
                refreshAll();
            }
        });

        HBox reserveRow = new HBox(12, memberBookPicker, reserveBtn);
        HBox.setHgrow(memberBookPicker, Priority.ALWAYS);

        Label checkoutsLabel = new Label("Currently Checked Out");
        checkoutsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        myCheckoutsTable.getStyleClass().add("custom-table");
        myCheckoutsTable.getColumns().setAll(
            col("TITLE",    220, d -> new ReadOnlyStringWrapper(d.getValue().getBookItem().getBook().getTitle())),
            col("BARCODE",  120, d -> new ReadOnlyStringWrapper(d.getValue().getBookItem().getBarcode())),
            col("DUE DATE", 130, d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getDueDate())))
        );
        myCheckoutsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        myCheckoutsTable.setPlaceholder(new Label("No books currently checked out."));
        VBox.setVgrow(myCheckoutsTable, Priority.ALWAYS);

        Label reservationsLabel = new Label("My Reservations");
        reservationsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        myReservationsTable.getStyleClass().add("custom-table");

        TableColumn<BookReservation, String> pickupCol = new TableColumn<>("ACTION");
        pickupCol.setPrefWidth(140);
        pickupCol.setCellFactory(c -> new TableCell<>() {
            final Button btn = new Button("Approve Pick Up");
            { btn.getStyleClass().add("btn-primary");
              btn.setOnAction(e -> {
                  BookReservation r = getTableView().getItems().get(getIndex());
                  new Alert(Alert.AlertType.INFORMATION, service.checkoutReservation(r)).showAndWait();
                  refreshAll();
              }); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                BookReservation r = getTableView().getItems().get(getIndex());
                btn.setVisible(r.getStatus() == ReservationStatus.PENDING_PICKUP);
                setGraphic(btn); setText(null);
            }
        });

        myReservationsTable.getColumns().setAll(
            col("TITLE",    180, d -> new ReadOnlyStringWrapper(d.getValue().getBook().getTitle())),
            col("STATUS",   130, d -> new ReadOnlyStringWrapper(d.getValue().getStatus().name())),
            col("DATE",     110, d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getCreatedOn()))),
            pickupCol
        );
        myReservationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        myReservationsTable.setPlaceholder(new Label("No reservations."));

        panel.getChildren().addAll(title, reserveRow, checkoutsLabel, myCheckoutsTable, reservationsLabel, myReservationsTable);
        return panel;
    }

    private VBox buildMemberNotificationsPanel() {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("content-area");
        panel.setPadding(new Insets(24));

        Label title = new Label("My Notifications");
        title.getStyleClass().add("section-title");

        myNotifList.getStyleClass().add("notif-list");
        myNotifList.setPlaceholder(new Label("No notifications yet."));
        VBox.setVgrow(myNotifList, Priority.ALWAYS);

        panel.getChildren().addAll(title, myNotifList);
        return panel;
    }


    private void setupCatalogTable(TableView<BookItem> table, boolean withActions) {
        table.getStyleClass().add("custom-table");

        TableColumn<BookItem, String> status = new TableColumn<>("STATUS");
        status.setPrefWidth(120);
        status.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getStatus().name()));
        status.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(switch (s) {
                    case "AVAILABLE" -> "badge-available";
                    case "LOANED"    -> "badge-loaned";
                    case "RESERVED"  -> "badge-reserved";
                    default          -> "badge-overdue";
                });
                setGraphic(badge); setText(null);
            }
        });

        List<TableColumn<BookItem, String>> cols = new java.util.ArrayList<>(List.of(
            col("BARCODE", 110, d -> new ReadOnlyStringWrapper(d.getValue().getBarcode())),
            col("TITLE",   190, d -> new ReadOnlyStringWrapper(d.getValue().getBook().getTitle())),
            col("AUTHOR",  160, d -> new ReadOnlyStringWrapper(d.getValue().getBook().getAuthorNames())),
            col("SUBJECT", 130, d -> new ReadOnlyStringWrapper(d.getValue().getBook().getSubject())),
            col("RACK",     70, d -> new ReadOnlyStringWrapper(d.getValue().getRack().toString())),
            status
        ));

        if (withActions) {
            TableColumn<BookItem, String> actions = new TableColumn<>("ACTIONS");
            actions.setPrefWidth(100);
            actions.setCellFactory(c -> new TableCell<>() {
                final Button btn = new Button("✕ Remove");
                { btn.getStyleClass().add("btn-danger");
                  btn.setOnAction(e -> { service.removeBookItem(getTableView().getItems().get(getIndex())); refreshAll(); }); }
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    setGraphic(empty ? null : btn); setText(null);
                }
            });
            cols.add(actions);
        }

        table.getColumns().setAll(cols);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No books in inventory."));
    }


    private void showAddBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter book details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField isbnField      = dialogField("ISBN");
        TextField titleField     = dialogField("Title");
        TextField subjectField   = dialogField("Subject");
        TextField publisherField = dialogField("Publisher");
        TextField authorField    = dialogField("Author");
        TextField dateField      = dialogField("YYYY-MM-DD");
        TextField barcodeField   = dialogField("Barcode");
        TextField rackField      = dialogField("Rack label");
        TextField locationField  = dialogField("Location");

        grid.addRow(0, new Label("ISBN:"),     isbnField,    new Label("Title:"),     titleField);
        grid.addRow(1, new Label("Subject:"),  subjectField, new Label("Publisher:"), publisherField);
        grid.addRow(2, new Label("Author:"),   authorField,  new Label("Pub. Date:"), dateField);
        grid.addRow(3, new Label("Barcode:"),  barcodeField, new Label("Rack:"),      rackField);
        grid.addRow(4, new Label("Location:"), locationField);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    Book book = new Book(isbnField.getText(), titleField.getText(),
                            subjectField.getText(), publisherField.getText(),
                            LocalDate.parse(dateField.getText()),
                            List.of(new Author(authorField.getText(), "")));
                    service.addBookItem(new BookItem(barcodeField.getText(), book,
                            new Rack(rackField.getText(), locationField.getText()), BookStatus.AVAILABLE));
                    refreshAll();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input. Check the date format (YYYY-MM-DD).").showAndWait();
                }
            }
        });
    }


    private void refreshAll() {
        long activeMembers  = service.getLibrary().getMembers().stream()
                .filter(m -> m.getStatus() == AccountStatus.ACTIVE).count();
        long activeLendings = service.getLendings().stream().filter(BookLending::isActive).count();

        totalBooksLbl.setText(String.valueOf(service.getLibrary().getInventory().size()));
        activeMembersLbl.setText(String.valueOf(activeMembers));
        lendingsLbl.setText(String.valueOf(activeLendings));
        reservationsLbl.setText(String.valueOf(service.getReservations().size()));

        if (isLibrarian) {
            int libUnread = service.getNotifications().size() - librarianSeenNotifCount;
            updateNotifBadge(librarianNotifBtn, "🔔  Notifications", libUnread);

            catalogTable.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
            memberTable.setItems(FXCollections.observableArrayList(
                    service.getLibrary().getMembers().stream()
                            .filter(m -> m.getStatus() == AccountStatus.ACTIVE)
                            .toList()));
            memberTable.refresh();
            lendingTable.setItems(FXCollections.observableArrayList(
                    service.getLendings().stream().filter(BookLending::isActive).toList()));
            notifList.setItems(FXCollections.observableArrayList(
                    service.getNotifications().stream().map(Notification::toString).toList()));
            fineTable.setItems(FXCollections.observableArrayList(service.getFines()));
            reservationTable.setItems(FXCollections.observableArrayList(
                    service.getReservations().stream()
                            .filter(r -> r.getStatus() != ReservationStatus.CANCELED && r.getStatus() != ReservationStatus.COMPLETED)
                            .toList()));
            memberPicker.setItems(FXCollections.observableArrayList(
                    service.getLibrary().getMembers().stream()
                            .filter(m -> m.getStatus() == AccountStatus.ACTIVE)
                            .toList()));
            itemPicker.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
            bookPicker.setItems(FXCollections.observableArrayList(
                    service.getLibrary().getInventory().stream().map(BookItem::getBook).distinct().toList()));
        } else {
            MemberAccount me = currentMember();
            if (me != null) {
                long memberTotal = service.getNotifications().stream()
                        .filter(n -> n.recipient().equals(me.getName())).count();
                int memberUnread = (int) memberTotal - memberSeenNotifCount;
                updateNotifBadge(memberNotifBtn, "🔔  Notifications", memberUnread);
            }
            memberCatalogTable.setItems(FXCollections.observableArrayList(service.getLibrary().getInventory()));
            memberBookPicker.setItems(FXCollections.observableArrayList(
                    service.getLibrary().getInventory().stream().map(BookItem::getBook).distinct().toList()));
            if (me != null) {
                myCheckoutsTable.setItems(FXCollections.observableArrayList(
                        service.getLendings().stream()
                                .filter(BookLending::isActive)
                                .filter(l -> l.getMember() == me)
                                .toList()));
                myReservationsTable.setItems(FXCollections.observableArrayList(
                        service.getReservations().stream()
                                .filter(r -> r.getMember() == me)
                                .toList()));
                myNotifList.setItems(FXCollections.observableArrayList(
                        service.getNotifications().stream()
                                .filter(n -> n.recipient().equals(me.getName()))
                                .map(Notification::toString)
                                .toList()));
            }
        }
    }

    private void setSummary(String text) {
        summaryArea.setText(text);
        refreshAll();
    }

    private MemberAccount currentMember() {
        return service.getLibrary().getMembers().stream()
                .filter(m -> m.getEmail().equals(loggedInEmail))
                .findFirst().orElse(null);
    }

    private void updateNotifBadge(Button btn, String baseText, int unread) {
        if (btn == null) return;
        btn.setText(baseText);
        if (unread > 0) {
            Label badge = new Label(String.valueOf(unread));
            badge.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; "
                    + "-fx-font-size: 10px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8px; -fx-padding: 1px 6px;");
            btn.setGraphic(badge);
            btn.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        } else {
            btn.setGraphic(null);
            btn.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        }
    }


    private <T> TableColumn<T, String> col(String header, double width,
            javafx.util.Callback<TableColumn.CellDataFeatures<T, String>,
            javafx.beans.value.ObservableValue<String>> factory) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setPrefWidth(width);
        c.setCellValueFactory(factory);
        return c;
    }

    private <T> StringConverter<T> conv(java.util.function.Function<T, String> toStr) {
        return new StringConverter<>() {
            @Override public String toString(T o)   { return o == null ? "" : toStr.apply(o); }
            @Override public T fromString(String s) { return null; }
        };
    }

    private TextField dialogField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(180);
        return f;
    }

    public static void main(String[] args) { launch(args); }
}
