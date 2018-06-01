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
import com.goranrsbg.houseoftherisingsun.ui.showlocation.ShowLocationController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
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

    private final DBHandler db;
    private static MainController instance;

    public enum MessageType {
        CONFIRM,
        ERROR,
        WARNING,
        INFORMATION;
    }

    public enum ButtonType {
        ADD_LOCATION,
        SHOW_LOCATION,
        SHOW_STREETS_TABLE;
    }

    private final Map<ButtonType, JFXButton> buttons;
    private final ArrayList<JFXButton> mapButtons;
    private final JFXToggleButton toggleLocationsButton;
    private static final Logger LOGGER = Logger.getLogger(LocatorApp.class.getName());
    private final Map<Integer, ShowLocationController> shownLocations;
    private final Pattern recipientPattern;
    private final ObservableList showRecipientsData;
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
    }

    private void initSearch() {
        TableColumn<SearchRecipient, String> lastName = new TableColumn<>("Prezime");
        lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        TableColumn<SearchRecipient, String> firstName = new TableColumn<>("Ime");
        firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<SearchRecipient, String> details = new TableColumn<>("Detalj");
        details.setCellValueFactory(new PropertyValueFactory<>("details"));
        TableColumn<SearchRecipient, String> locationAddress = new TableColumn<>("Adresa");
        locationAddress.setCellValueFactory(new PropertyValueFactory<>("locationAddress"));
        searchRecipientsTable.getColumns().addAll(lastName, firstName, details, locationAddress);
        searchRecipientsTable.setItems(showRecipientsData);
        searchRecipientsTable.setVisible(false);
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Search text: " + newValue + " (" + oldValue + ")");
            searchRunnable.setValueToSearchFor(newValue);
        });
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
        btn.setId(ButtonType.ADD_LOCATION.toString());
        buttons.put(ButtonType.ADD_LOCATION, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        Tooltip tooltip = new Tooltip("Prikaži / ukloni lokacije.");
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        toggleLocationsButton.setTooltip(tooltip);
        toggleLocationsButton.setText(null);
        toggleLocationsButton.setOnAction(this::toggleShowLocations);
        locationsJFXNodesList.addAnimatedNode(toggleLocationsButton);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("roadt-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Prikaži spisak ulica.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(ButtonType.SHOW_STREETS_TABLE.toString());
        buttons.put(ButtonType.SHOW_STREETS_TABLE, btn);
        streetsJFXNodesList.addAnimatedNode(btn);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("showlocation-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Prikaži primaoce na adresi.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(ButtonType.SHOW_LOCATION.toString());
        buttons.put(ButtonType.SHOW_LOCATION, btn);
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

    private JFXButton createButton(String text, Node graphic, String cssButtonSubClass, String tooltipText) {
        JFXButton btn = new JFXButton(text, graphic);
        btn.getStyleClass().addAll(DEFAULT_BUTTON_CSS_CLASS, cssButtonSubClass);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        btn.setTooltip(tooltip);
        return btn;
    }

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
            case "SHOW_LOCATION":
                try {
                    LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/showlocation/showlocation.fxml", btn, true, "Prikaži lokaciju.");
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
            default:
                showMessage(TITLE, "Nepoznat taster\nID: " + id, MessageType.ERROR);
        }
        event.consume();
    }

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
                        AddLocationController ac = ((AddLocationController) buttons.get(ButtonType.ADD_LOCATION).getUserData());
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

    public void updateLocationText(String id, String newLocationAddressNumberText) {
        FilteredList<Node> filtered = locationsPane.getChildren().filtered((t) -> {
            return t.getId().equals(id);
        });
        if (filtered.size() == 1) {
            ((Text) filtered.get(0)).setText(newLocationAddressNumberText);
        }
    }

    /**
     * Creates new Text element that represent location and adds it the
     * locationPane.
     *
     * @param no Location number, text of the Text element.
     * @param x Location position X on the Pane.
     * @param y Location position Y on the Pane .
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
     * Called on mouse left double click on any location Text element, to show
     * location recipients.
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
                    ((MainController) ud[0]).getShownLocations().remove((Integer) ud[1]);
                });
                stage.setUserData(new Object[]{this, locationID});
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                showMessage(TITLE, "Greška pri učitavanju showlocation.fxml fajla.\n" + ex.getMessage(), MessageType.ERROR);
            }
        }
    }

    public Map<Integer, ShowLocationController> getShownLocations() {
        return shownLocations;
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

    public void refreshRecipients(Integer locationId) {
        if (shownLocations.containsKey(locationId)) {
            shownLocations.get(locationId).loadRecipients();
        }
    }

    @FXML
    private void onSearchBoxAction(ActionEvent event) {
        searchRunnable.resume();
    }

    /**
     * Text element of the location.
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
     * `
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
     * Toggles between show/hide searchRecipientsTable.
     *
     * @param event
     */
    @FXML
    private void searchArrowOnAction(ActionEvent event) {
        searchRecipientsTable.setVisible(!searchRecipientsTable.isVisible());
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        if (isMapLoaded()) {
            MouseButton mb = event.getButton();
            final double x = event.getX();
            final double y = event.getY();
            if (mb == MouseButton.SECONDARY) {
                centerPointOnTheWindow(x, y);
            } else if (mb == MouseButton.PRIMARY) {
                AddLocationController ac = ((AddLocationController) buttons.get(ButtonType.ADD_LOCATION).getUserData());
                if (ac != null) {
                    ac.setLocationXY(x, y);
                }
            }
        }
        event.consume();
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
     * Shows notification message.
     *
     * @param title
     * @param message
     * @param type
     */
    public void showMessage(String title, String message, MessageType type) {
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
    }

    public static final String TITLE = "Glavni prikaz.";
    private final String PATH_TO_MAPS = "res/maps/";
    private final String DEFAULT_BUTTON_CSS_CLASS = "animated-option-button";
    private final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal bold 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";
    private final String DEFAULT_LOCATION_CSS_CLASS = "location-text";
}
