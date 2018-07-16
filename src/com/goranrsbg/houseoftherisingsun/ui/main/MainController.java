/*
 * Copyright 2017 Goran.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goranrsbg.houseoftherisingsun.ui.main;

import com.goranrsbg.houseoftherisingsun.LocatorApp;
import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.addlocation.AddLocationController;
import com.goranrsbg.houseoftherisingsun.ui.mailinglist.MailinglistController;
import com.goranrsbg.houseoftherisingsun.ui.showlocation.ShowLocationController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView theMapImageView;
    @FXML
    private JFXNodesList rootButtonList;
    @FXML
    private ScrollPane rootScrollPane;
    @FXML
    private Pane locationsPane;
    @FXML
    private JFXTextField searchBox;
    @FXML
    private JFXButton searchArrow;
    @FXML
    private VBox searchLine;
    @FXML
    private TableView<SearchRecipient> searchRecipientsTable;
    @FXML
    private TableColumn<SearchRecipient, Integer> numCol;
    @FXML
    private TableColumn<SearchRecipient, String> lastNameCol;
    @FXML
    private TableColumn<SearchRecipient, String> firstNameCol;
    @FXML
    private TableColumn<SearchRecipient, String> detailsCol;
    @FXML
    private TableColumn<SearchRecipient, String> addressCol;

    private final DBHandler db;
    private static MainController instance;

    public enum MessageType {
        CONFIRM,
        ERROR,
        WARNING,
        INFORMATION;
    }

    public enum DefaultButtonType {
        ADD_LOCATION,
        MAILING_LIST,
        SHOW_STREETS_TABLE,
        SHOW_RETIRE_TABLE,
        CREATE_USER;
    }

    private final Map<DefaultButtonType, JFXButton> buttons;
    private final ArrayList<JFXButton> mapButtons;
    private final JFXToggleButton toggleLocationsButton;
    private static final Logger LOGGER = Logger.getLogger(LocatorApp.class.getName());
    private final Map<Integer, ShowLocationController> shownLocations;
    private final Pattern recipientPattern;
    private final ObservableList<SearchRecipient> showRecipientsData;
    private SearchRunnable searchRunnable;

    public MainController() {
        db = DBHandler.getInstance();
        mapButtons = new ArrayList<>();
        buttons = new HashMap<>();
        toggleLocationsButton = new JFXToggleButton();
        shownLocations = new HashMap<>();
        recipientPattern = Pattern.compile("RID:\\d+,LID:\\d+");
        showRecipientsData = FXCollections.observableArrayList();
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initButtons();
        initSearch();
        searchRunnable = new SearchRunnable(showRecipientsData);
        searchRunnable.start();
        rootPane.setOnKeyTyped((event) -> {
            if (isMapLoaded() && searchBox.getText().isEmpty()) {
                searchBox.fireEvent(event);
                searchBox.requestFocus();
                searchBox.deselect();
                searchBox.positionCaret(1);
            }
        });
    }

    private void initSearch() {
        numCol.setCellValueFactory((param) -> {
            return new ReadOnlyObjectWrapper<>(searchRecipientsTable.getItems().indexOf(param.getValue()) + 1);
        });
        numCol.setStyle("-fx-alignment: CENTER;");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("locationAddress"));
        addressCol.setStyle("-fx-alignment: CENTER-LEFT;");
        searchRecipientsTable.setItems(showRecipientsData);
        searchRecipientsTable.setVisible(false);
        searchRecipientsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && oldValue != newValue) {
                selectLocation(oldValue.getLocationId() + "", false);
            }
            if (newValue != null) {
                selectLocation(newValue.getLocationId() + "", true);
            }
        });
        searchRecipientsTable.setOnKeyPressed((event) -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.ESCAPE) {
                searchRecipientsTable.getSelectionModel().clearSelection();
            } else if (key == KeyCode.UP && searchRecipientsTable.getSelectionModel().isSelected(0)) {
                searchBox.requestFocus();
            } else if (event.getCode() == KeyCode.ENTER) {
                SearchRecipient recipient = searchRecipientsTable.getSelectionModel().getSelectedItem();
                if (recipient != null) {
                    Object userData = buttons.get(DefaultButtonType.MAILING_LIST).getUserData();
                    if (userData != null) {
                        MailinglistController controller = (MailinglistController) userData;
                        controller.addRecipient(recipient);
                    }
                }
            }
        });
        searchRecipientsTable.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                searchRecipientsTable.getSelectionModel().clearSelection();
            }
        });
        searchRecipientsTable.setRowFactory((param) -> {
            TableRow<SearchRecipient> row = new TableRow<>();
            row.setOnMouseClicked((event) -> {
                if (event.getClickCount() == 2) {
                    SearchRecipient item = row.getItem();
                    centerLocationOnTheScreen(item.getLocationId() + "");
                }
            });
            row.itemProperty().addListener((obs, previous, current) -> {
                if (current != null) {
                    row.pseudoClassStateChanged(RETIRE_PSEUDO_CLASS, current.getIsRetire());
                } else if (previous != null) {
                    row.pseudoClassStateChanged(RETIRE_PSEUDO_CLASS, false);
                }
            });
            return row;
        });
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            searchRunnable.setValueToSearchFor(newValue);
        });
        searchBox.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.DOWN) {
                searchRecipientsTable.requestFocus();
            }
        });
    }

    /**
     * Select or deselect location of the selected recipient inside of the
     * serchTable.
     *
     * @param id Id of the location text element
     * @param ON toggle switch show/hide true/false
     */
    public void selectLocation(String id, boolean ON) {
        FilteredList<Node> filtered = locationsPane.getChildren().filtered((t) -> {
            return t.getId().equals(id);
        });
        if (filtered.size() == 1) {
            filtered.get(0).pseudoClassStateChanged(MARKED_PSEUDO_CLASS, ON);
        }
    }

    private void initButtons() {
        final String DEFAULT_BUTTON_CSS_SUBCLASS = "animated-option-sub-button";
        final String DEFAULT_TEXT_BUTTON_CSS_SUBCLASS = "settlement";
        JFXNodesList recipientsJFXNodesList = new JFXNodesList();
        JFXNodesList locationsJFXNodesList = new JFXNodesList();
        JFXNodesList streetsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsMapChooserList = new JFXNodesList();

        rootButtonList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("start-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 0, "Start"));
        recipientsJFXNodesList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("recip-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Primaoci:"));
        locationsJFXNodesList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("loc-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 2, "Lokacije:"));
        streetsJFXNodesList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("road-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Ulice:"));
        settlementsJFXNodesList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("map-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Naselja:"));
        settlementsMapChooserList.addAnimatedNode(createButton(null, new FontAwesomeIconView().setStyleClass("settchooser-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Izaberi mapu naselja."));

        JFXButton btn;
        btn = createButton(null, new FontAwesomeIconView().setStyleClass("marker-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 2, "Dadaj lokaciju.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(DefaultButtonType.ADD_LOCATION.toString());
        buttons.put(DefaultButtonType.ADD_LOCATION, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        Tooltip tooltip = new Tooltip("Prikaži / ukloni lokacije.");
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        toggleLocationsButton.setTooltip(tooltip);
        toggleLocationsButton.setText(null);
        toggleLocationsButton.setOnAction(this::toggleShowLocations);
        locationsJFXNodesList.addAnimatedNode(toggleLocationsButton);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("roadt-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Prikaži spisak ulica.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(DefaultButtonType.SHOW_STREETS_TABLE.toString());
        buttons.put(DefaultButtonType.SHOW_STREETS_TABLE, btn);
        streetsJFXNodesList.addAnimatedNode(btn);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("showmailinglist-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Prikaži listu primalaca.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(DefaultButtonType.MAILING_LIST.toString());
        buttons.put(DefaultButtonType.MAILING_LIST, btn);
        recipientsJFXNodesList.addAnimatedNode(btn);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("retire-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Prikaži pen/neg/pom korisnike.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(DefaultButtonType.SHOW_RETIRE_TABLE.toString());
        buttons.put(DefaultButtonType.SHOW_RETIRE_TABLE, btn);
        recipientsJFXNodesList.addAnimatedNode(btn);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("user-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Dodaj/Obriši korisnika.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(DefaultButtonType.CREATE_USER.toString());
        buttons.put(DefaultButtonType.CREATE_USER, btn);
        recipientsJFXNodesList.addAnimatedNode(btn);

        settlementsMapChooserList.setRotate(90d);
        try (Statement stmt = db.getConnection().createStatement(); ResultSet rs = stmt.executeQuery("SELECT SETTLEMENT_ID, SETTLEMENT_NAME, SETTLEMENT_INITIALS FROM SETTLEMENTS")) {
            while (rs.next()) {
                JFXButton button = createButton(rs.getString("SETTLEMENT_INITIALS"), null, DEFAULT_TEXT_BUTTON_CSS_SUBCLASS, rs.getString("SETTLEMENT_NAME"));
                button.setOnAction(this::buttonMapClickActionEvent);
                button.setUserData(rs.getInt("SETTLEMENT_ID"));
                mapButtons.add(button);
                settlementsMapChooserList.addAnimatedNode(button);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Failed to select settlements.\nError: {0}", ex.getMessage());
        }

        settlementsJFXNodesList.addAnimatedNode(settlementsMapChooserList);
        rootButtonList.addAnimatedNode(recipientsJFXNodesList);
        rootButtonList.addAnimatedNode(locationsJFXNodesList);
        rootButtonList.addAnimatedNode(streetsJFXNodesList);
        rootButtonList.addAnimatedNode(settlementsJFXNodesList);
        Platform.runLater(() -> {
            mapButtons.get(0).fire();
        });

        // search part
        searchBox.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        searchArrow.setText("");
    }

    /**
     * Creates button for the main screen.
     *
     * @param text
     * @param graphic
     * @param cssButtonSubClass
     * @param tooltipText
     * @return
     */
    private JFXButton createButton(String text, Node graphic, String cssButtonSubClass, String tooltipText) {
        JFXButton btn = new JFXButton(text, graphic);
        btn.getStyleClass().addAll(DEFAULT_BUTTON_CSS_CLASS, cssButtonSubClass);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        btn.setTooltip(tooltip);
        return btn;
    }

    /**
     * Method for the buttons fire event of the main screen. Button for changing
     * map are excluded.
     *
     * @param event
     */
    private void buttonClickActionEvent(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();
        String id = btn.getId();
        switch (id) {
            case "ADD_LOCATION":
                if (isMapLoaded()) {
                    try {
                        LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/addlocation/addlocation.fxml", btn, false, "Dadaj lokaciju.");
                        btn.setDisable(true);
                    } catch (IOException ex) {
                        showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MessageType.ERROR);
                    }
                } else {
                    showMessage(TITLE, "Mapa nije odabrana.", MessageType.INFORMATION);
                }
                break;
            case "MAILING_LIST":
                try {
                    LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/mailinglist/mailinglist.fxml", btn, true, "Lista primalaca.");
                    btn.setDisable(true);
                } catch (IOException ex) {
                    showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MessageType.ERROR);
                }
                break;
            case "SHOW_STREETS_TABLE":
                try {
                    LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/showstreets/showstreets.fxml", btn, true, "Prikaz ulica.");
                    btn.setDisable(true);
                } catch (IOException ex) {
                    showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MessageType.ERROR);
                }
                break;
            case "SHOW_RETIRE_TABLE":
                try {
                    LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/retireonly/retireonly.fxml", btn, true, "Prikaz pen/neg/pom.");
                    btn.setDisable(true);
                } catch (IOException ex) {
                    showMessage(TITLE, "Greška pri učitavanju .fxml faja.\n" + ex.getMessage(), MessageType.ERROR);
                }
                break;
            case "CREATE_USER":
                Dialog dialog = createDialog();
                Optional result = dialog.showAndWait();
                if (result.isPresent()) {
                    User user = (User) result.get();
                    try {
                        PreparedStatement ps;
                        ps = user.isForDeletion() ? db.getStatement(DBHandler.StatementType.DELETE_USER) : db.getStatement(DBHandler.StatementType.INSERT_USER);
                        ps.setString(1, user.getNAME());
                        ps.setString(2, user.getPASSWORD());
                        ps.clearParameters();
                        showMessage(TITLE, "Korisnik uspešno " + (user.isForDeletion() ? "obrisan" : "dodan") + ".", MessageType.INFORMATION);
                    } catch (SQLException ex) {
                        showMessage(TITLE, user.isForDeletion() ? "Greška prilikom brisanja korisnika.\n" : "Greška prilikom dodavanja korisnika.\n" + ex.getMessage(), MessageType.ERROR);
                    }
                }
                break;
            default:
                showMessage(TITLE, "Nepoznat taster\nID: " + id, MessageType.ERROR);
        }
        event.consume();
    }

    /**
     * Creates dialog with text field user name and password field for password.
     * It is used for creating or deleting user for the application.
     *
     * @return Fresh dialog.
     */
    private Dialog createDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Kreiraj korisnika.");
        dialog.initStyle(StageStyle.UTILITY);
        ButtonType create = new ButtonType("Kreiraj", ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Otkaži", ButtonData.CANCEL_CLOSE);
        ButtonType delete = new ButtonType("Obriši", ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(create, delete, cancel);
        GridPane grid = new GridPane();
        JFXTextField name = new JFXTextField();
        JFXPasswordField password = new JFXPasswordField();
        JFXPasswordField passwordReTyped = new JFXPasswordField();
        name.setLabelFloat(true);
        password.setLabelFloat(true);
        passwordReTyped.setLabelFloat(true);
        name.setPromptText("Ime");
        password.setPromptText("Lozinka");
        grid.add(new Label("Korisničko ime:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Korisnička lozinka:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Lozinka prekucana:"), 0, 2);
        grid.add(passwordReTyped, 1, 2);
        grid.setHgap(13d);
        grid.setVgap(13d);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter((param) -> {
            if (param == cancel || name.getText().isEmpty() || password.getText().isEmpty() || !password.getText().equals(passwordReTyped.getText())) {
                return null;
            }
            return new User(toSha256String(name.getText()), toSha256String(password.getText()), param == delete);
        });
        return dialog;
    }

    /**
     * Algorithm SHA-256 is used for hashing given text.
     *
     * @param text String to be digested.
     * @return Digested string.
     */
    private String toSha256String(String text) {
        MessageDigest generator;
        try {
            generator = MessageDigest.getInstance("SHA-256");
            byte[] digestedText = generator.digest(text.getBytes());
            StringBuilder sb = new StringBuilder(64);
            for (int i = 0; i < digestedText.length; i++) {
                String val = Integer.toHexString(digestedText[i] & 0xff);
                if (val.length() == 1) {
                    sb.append('0');
                }
                sb.append(val);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Method for fire event of the buttons that change the map.
     *
     * @param event
     */
    private void buttonMapClickActionEvent(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();
        int id = (int) btn.getUserData();
        try {
            PreparedStatement stmt = db.getStatement(DBHandler.StatementType.SELECT_MAP_WITH_ID);
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (loadMap(rs.getString("SETTLEMENT_FILE_NAME"), rs.getString("SETTLEMENT_NAME"))) {
                    mapButtons.forEach((e) -> {
                        if (e.isDisabled()) {
                            e.setDisable(false);
                        }
                    });
                    btn.setDisable(true);
                    showRecipientsData.clear();
                    Platform.runLater(() -> {
                        AddLocationController ac = ((AddLocationController) buttons.get(DefaultButtonType.ADD_LOCATION).getUserData());
                        if (ac != null) {
                            ac.comboBoxLoadStreets();
                            ac.clearLocationXY();
                        }
                        if (toggleLocationsButton.isSelected()) { // default map
                            toggleLocationsButton.fire();
                        }
                    });
                }
            }
            stmt.clearParameters();
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        event.consume();
    }

    /**
     * Toggle button fire event for changing show/hide locations.
     *
     * @param event
     */
    private void toggleShowLocations(ActionEvent event) {
        JFXToggleButton bt = (JFXToggleButton) event.getSource();
        if (bt.isSelected()) {
            if (isMapLoaded()) {
                try {
                    PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_LOCATONS_WITH_SETTLEMENT_ID);
                    ps.setInt(1, getLoadedMapId());
                    int n = 0;
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            addLocationToPane(rs.getString("LOCATION_ADDRESS_NO"), rs.getDouble("LOCATION_POINT_X"), rs.getDouble("LOCATION_POINT_Y"), rs.getInt("LOCATION_ID") + "", rs.getString("LOCATION_NOTE"));
                            n++;
                        }
                    }
                    showMessage(TITLE, "(" + n + ") lokacija prikazano.", MessageType.INFORMATION);
                } catch (SQLException ex) {
                    showMessage(TITLE, "Greška pri čitanju lokacija iz baze.\n" + ex.getMessage(), MessageType.ERROR);
                }
            } else {
                showMessage(TITLE, "Mapa nije odabrana.", MessageType.INFORMATION);
                bt.fire();
            }
        } else {
            locationsPane.getChildren().clear();
            showMessage(TITLE, "Prikaz lokacija ugašen.", MessageType.INFORMATION);
        }
        event.consume();
    }

    public boolean isShowLocationsSelected() {
        return toggleLocationsButton.isSelected();
    }

    /**
     * Updates location number, text of the Text element representing location
     * on the location pane, If text element with id id exists then text will be
     * changed.
     *
     * @param id id of the location
     * @param newLocationAddressNumberText Address number to be replaced with
     * @param note Location note
     */
    public void updateLocationText(String id, String newLocationAddressNumberText, String note) {
        Node lookup = locationsPane.lookup("#" + id);
        if (lookup != null) {
            ((Text) lookup).setText(newLocationAddressNumberText);
            Tooltip tooltip = new Tooltip(note);
            tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
            Tooltip.install(lookup, tooltip);
        }
    }

    /**
     * Creates new Text element that represent location and adds it the
     * locationPane.
     *
     * @param no Location number, text of the Text element.
     * @param x Location position X on the Pane.
     * @param y Location position Y on the Pane.
     * @param id Id of the location in database.
     * @param note Note of the location as tool tip.
     */
    public void addLocationToPane(String no, double x, double y, String id, String note) {
        Text text = new Text(no);
        text.setX(x);
        text.setY(y);
        text.setId(id);
        text.getStyleClass().add(DEFAULT_LOCATION_CSS_CLASS);
        if (note != null && !note.isEmpty()) {
            Tooltip tooltip = new Tooltip(note);
            tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
            Tooltip.install(text, tooltip);
        }
        text.setOnMouseClicked(this::locationTextOnMouseClick);
        text.setOnDragDetected(this::onLocationDragDetected);
        locationsPane.getChildren().add(text);
    }

    /**
     * Called on mouse left <b>double</b> click on any location Text element, to
     * show location recipients.
     *
     * @param event
     */
    private void locationTextOnMouseClick(MouseEvent event) {
        MouseButton bt = event.getButton();
        Text locationText = (Text) event.getSource();
        if (bt == MouseButton.PRIMARY && event.getClickCount() == 2) {
            showLocation(Integer.parseInt(locationText.getId()));
        }
        event.consume();
    }

    /**
     * Shows location user interface.
     *
     * @param locationID Database ID of the location to be shown.
     */
    private void showLocation(Integer locationID) {
        if (shownLocations.containsKey(locationID)) {
            shownLocations.get(locationID).requestFocus();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/goranrsbg/houseoftherisingsun/ui/showlocation/showlocation.fxml"));
                Parent parent = loader.load();
                ShowLocationController controller = (ShowLocationController) loader.getController();
                shownLocations.put(locationID, controller);
                controller.setPattern(recipientPattern);
                controller.setLocation(locationID);
                controller.loadRecipients();
                Scene scene = new Scene(parent);
                Stage stage = new Stage(StageStyle.UTILITY);
                stage.initOwner(locationsPane.getScene().getWindow());
                stage.setOnCloseRequest((event) -> {
                    Stage st = (Stage) event.getSource();
                    Object[] ud = (Object[]) st.getUserData();
                    ((MainController) ud[0]).getShownLocationController().remove((Integer) ud[1]);
                });
                stage.setUserData(new Object[]{this, locationID});
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                showMessage(TITLE, "Greška pri učitavanju showlocation.fxml fajla.\n" + ex.getMessage(), MessageType.ERROR);
            }
        }
    }

    /**
     * Map containing controllers of all open
     * <code>showLocationSubwindow</code>.
     *
     * @return
     */
    public Map<Integer, ShowLocationController> getShownLocationController() {
        return shownLocations;
    }

    /**
     * Removes text element from location pane.
     *
     * @param locationId Id of the text element.(location id)
     */
    public void clearLocationFromLocationPane(String locationId) {
        locationsPane.getChildren().removeIf((t) -> {
            return t.getId().equals(locationId);
        });
    }

    /**
     * If location with given id is present, method
     * <I>canterPointOnTheWindow</I> is called with found location on screen
     * position.
     *
     * @param locationId Id of the location.
     */
    public void centerLocationOnTheScreen(String locationId) {
        Optional<Node> location = locationsPane.getChildren().stream().filter((t) -> {
            return t.getId().equals(locationId);
        }).findFirst();
        if (location.isPresent()) {
            Text loc = (Text) location.get();
            centerPointOnTheWindow(loc.getX(), loc.getY());
        }
    }

    /**
     * Reads the map file from PATH_TO_MAPS and adds it to the MapImageView.
     *
     * @param fileName Name of the file, map file.
     * @param name Name as part of subtile of the window to indicate witch map
     * image is loaded.
     * @return
     */
    private boolean loadMap(String fileName, String name) {
        try {
            Image theMapImage = new Image(new FileInputStream(PATH_TO_MAPS + fileName));
            theMapImageView.setImage(theMapImage);
            LocatorApp.getInstance().setSubTitle(name);
            Platform.runLater(() -> {
                showMessage(name, "Karta je učitana.", MessageType.INFORMATION);
            });
            searchLine.setVisible(!name.equals("default"));
            return true;
        } catch (FileNotFoundException e) {
            Platform.runLater(() -> {
                showMessage(name, "Karta nije dostupna.\nError: " + e.getMessage(), MessageType.ERROR);
            });
        }
        return false;
    }

    /**
     * Checks weather any map is shown except default map.
     *
     * @return
     */
    public boolean isMapLoaded() {
        return mapButtons.stream().anyMatch((b) -> ((int) b.getUserData() > 1 && b.isDisabled()));
    }

    /**
     * ID of the map from database.
     *
     * @return ID of the map.
     */
    public int getLoadedMapId() {
        for (int i = 0; i < mapButtons.size(); i++) {
            if (mapButtons.get(i).isDisabled()) {
                return (int) mapButtons.get(i).getUserData();
            }
        }
        return 0;
    }

    /**
     * Handles on drag detected on Text element which represent location.
     *
     * @param event
     */
    private void onLocationDragDetected(MouseEvent event) {
        Text location = (Text) event.getSource();
        Dragboard dragboard = location.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(location.getId());
        dragboard.setContent(clipboardContent);
        dragboard.setDragView(location.snapshot(null, null), location.getLayoutBounds().getWidth() / 2, location.getLayoutBounds().getHeight() / 2);
        event.consume();
    }

    /**
     * Reload all recipients of the location.
     *
     * @param locationId
     */
    public void refreshRecipients(Integer locationId) {
        if (shownLocations.containsKey(locationId)) {
            shownLocations.get(locationId).loadRecipients();
        }
    }

    /**
     * On return key typed in <code>searchBox<code>. Resume searchRunnable
     * and immediately query database.
     *
     * @param event
     */
    @FXML
    private void onSearchBoxAction(ActionEvent event) {
        searchRunnable.setValueToSearchFor(searchBox.getText());
        searchRunnable.resume();
    }

    /**
     * Text element of the location drag over locations pane.
     *
     * @param event
     */
    @FXML
    private void onTextDragOver(DragEvent event) {
        if (event.getGestureSource() instanceof Text && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    /**
     *
     * Handles location on drag dropped on locationsPane. Location is moved only
     * after successful database update.
     *
     * @param event
     */
    @FXML
    private void onTextDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;
        if (dragboard.hasString()) {
            try {
                String id = dragboard.getString();
                Text locationNode = (Text) locationsPane.lookup('#' + dragboard.getString());
                double x = event.getX() - locationNode.getLayoutBounds().getWidth() / 2;
                double y = event.getY() + locationNode.getLayoutBounds().getHeight() / 4;
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.UPDATE_LOCATON_XY);
                ps.setDouble(1, x);
                ps.setDouble(2, y);
                ps.setInt(3, Integer.parseInt(id));
                ps.executeUpdate();
                ps.clearParameters();
                locationNode.setX(x);
                locationNode.setY(y);
                showMessage(TITLE, "Lokacija na broju " + locationNode.getText() + " je uspešno premeštena.", MessageType.INFORMATION);
                success = true;
            } catch (SQLException ex) {
                showMessage(TITLE, "Neuspelo premeštanje lokacije.\nGreška: " + ex.getMessage(), MessageType.ERROR);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Toggles between show/hide searchRecipientsTable table.
     *
     * @param event
     */
    @FXML
    private void searchArrowOnAction(ActionEvent event) {
        searchRecipientsTable.setVisible(!searchRecipientsTable.isVisible());
    }

    /**
     * Default mouse click event of the main window.
     *
     * @param event
     */
    @FXML
    private void mouseClicked(MouseEvent event) {
        if (isMapLoaded()) {
            MouseButton mb = event.getButton();
            final double x = event.getX();
            final double y = event.getY();
            if (mb == MouseButton.SECONDARY) {
                centerPointOnTheWindow(x, y);
                event.consume();
            } else if (mb == MouseButton.PRIMARY) {
                AddLocationController ac = ((AddLocationController) buttons.get(DefaultButtonType.ADD_LOCATION).getUserData());
                if (ac != null) {
                    ac.setLocationXY(x, y);
                    event.consume();
                }
            }
        }
    }

    /**
     * Puts point (x,y) to the center of the window.
     *
     * @param x
     * @param y
     */
    private void centerPointOnTheWindow(double x, double y) {
        final double w = rootScrollPane.getWidth();
        final double h = rootScrollPane.getHeight();
        final double imgWidth = theMapImageView.getImage().getWidth();
        final double imgHeight = theMapImageView.getImage().getHeight();
        if (imgWidth > w) {
            rootScrollPane.setHvalue((x - w / 2) / (imgWidth - w));
        }
        if (imgHeight > h) {
            rootScrollPane.setVvalue((y - h / 2) / (imgHeight - h));
        }
    }

    /**
     * Shows message.
     *
     * @param title Title to set.
     * @param message Message to show.
     * @param type MessageType ERROR/WARNING/INFORMATION/CONFIRM
     */
    public void showMessage(String title, String message, MessageType type) {
        Platform.runLater(() -> {
            if (rootPane == null) {
                return;
            }
            Notifications notification = Notifications.create().title(title).text(message);
            switch (type) {
                case ERROR:
                    notification.showError();
                    break;
                case WARNING:
                    notification.showWarning();
                    break;
                case INFORMATION:
                    notification.showInformation();
                    break;
                case CONFIRM:
                    notification.showConfirm();
            }
        });
    }
    /**
     * Main window title.
     */
    public static final String TITLE = "Glavni prikaz.";
    /**
     * Relative path to all maps used by the application.
     */
    private final String PATH_TO_MAPS = "res/maps/";
    /**
     * Default cascade style sheet class for all buttons in the main window.
     */
    private final String DEFAULT_BUTTON_CSS_CLASS = "animated-option-button";
    /**
     * Tool tip style for all nodes.
     */
    private final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal bold 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";
    /**
     * Default cascade style sheet class for all text elements representing
     * location on the map.
     */
    private final String DEFAULT_LOCATION_CSS_CLASS = "location-text";
    /**
     * <i>Marked</i> location text element.
     */
    private final PseudoClass MARKED_PSEUDO_CLASS = PseudoClass.getPseudoClass("mark");
    /**
     * Pseudo class to mark table row containing recipient data that is in
     * retirement.
     */
    private final PseudoClass RETIRE_PSEUDO_CLASS = PseudoClass.getPseudoClass("retire");
}
