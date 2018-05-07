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
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    @FXML
    private ImageView theMapImageView;
    @FXML
    private JFXNodesList rootButtonList;
    @FXML
    private ScrollPane rootScrollPane;
    @FXML
    private Pane locationsPane;

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
        ADD_RECIPIENT,
        SHOW_STREETS_TABLE;
    }

    private final Map<ButtonType, JFXButton> buttons;
    private final ArrayList<JFXButton> mapButtons;
    private final JFXToggleButton toggleLocationsButton;
    private static final Logger LOGGER = Logger.getLogger(LocatorApp.class.getName());

    public MainController() {
        db = DBHandler.getInstance();
        mapButtons = new ArrayList<>();
        buttons = new HashMap<>();
        toggleLocationsButton = new JFXToggleButton();
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initButtons();
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

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("roadt-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 3, "Prikaži spisak ulica.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(ButtonType.SHOW_STREETS_TABLE.toString());
        buttons.put(ButtonType.SHOW_STREETS_TABLE, btn);
        streetsJFXNodesList.addAnimatedNode(btn);

        Tooltip tooltip = new Tooltip("Prikaži / ukloni lokacije.");
        tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
        toggleLocationsButton.setTooltip(tooltip);
        toggleLocationsButton.setText(null);
        toggleLocationsButton.setOnAction(this::toggleShowLocations);
        locationsJFXNodesList.addAnimatedNode(toggleLocationsButton);

        btn = createButton(null, new FontAwesomeIconView().setStyleClass("addrecip-icon"), DEFAULT_BUTTON_CSS_SUBCLASS + 1, "Dodaj primaoca.");
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(ButtonType.ADD_RECIPIENT.toString());
        buttons.put(ButtonType.ADD_RECIPIENT, btn);
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
                        LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/addlocation/addlocation.fxml", btn);
                        btn.setDisable(true);
                    } catch (IOException ex) {
                        showMessage(TITLE, "Dodaj lokaciju .fxml fajl nije pronađen.\n" + ex.getMessage(), MessageType.ERROR);
                    }
                } else {
                    showMessage(TITLE, "Mapa nije odabrana.", MessageType.INFORMATION);
                }
                break;
            case "ADD_RECIPIENT":
                break;
            case "SHOW_STREETS_TABLE":
                try {
                    LocatorApp.getInstance().LoadSubWindow("/com/goranrsbg/houseoftherisingsun/ui/showstreets/showstreets.fxml", btn);
                    btn.setDisable(true);
                } catch (IOException ex) {
                    showMessage(TITLE, "Prikaži ulice .fxml fajl nije pronađen.\n" + ex.getMessage(), MessageType.ERROR);
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

    public void addLocationToPane(String no, double x, double y, String id, String note) {
        Text text = new Text(no);
        text.setX(x);
        text.setY(y);
        text.setId(id);
        if (note != null && !note.isEmpty()) {
            Tooltip tooltip = new Tooltip(note);
            tooltip.setStyle(DEFAULT_TOOLTIP_STYLE);
            Tooltip.install(text, tooltip);
        }
        text.setOnMouseClicked(this::locationTextOnMouseClick);
        locationsPane.getChildren().add(text);
    }

    private void locationTextOnMouseClick(MouseEvent event) {
        MouseButton bt = event.getButton();
        Text t = (Text) event.getSource();
        if (bt == MouseButton.PRIMARY) {
            System.out.println("Click: " + t.getId());
            
            // TODO implement add/remove/update recipient on this location.
            
        }
        event.consume();
    }

    private boolean loadMap(String fileName, String name) {
        try {
            Image theMapImage = new Image(new FileInputStream(PATH_TO_MAPS + fileName));
            theMapImageView.setImage(theMapImage);
            LocatorApp.getInstance().setSubTitle(name);
            Platform.runLater(() -> {
                showMessage(name, "Karta je učitana.", MessageType.INFORMATION);
            });
            return true;
        } catch (FileNotFoundException e) {
            Platform.runLater(() -> {
                showMessage(name, "Karta nije dostupna.\nError: " + e.getMessage(), MessageType.ERROR);
            });
        }
        return false;
    }

    public boolean isMapLoaded() {
        return mapButtons.stream().anyMatch((b) -> ((int) b.getUserData() > 1 && b.isDisabled()));
    }

    public int getLoadedMapId() {
        for (int i = 0; i < mapButtons.size(); i++) {
            if (mapButtons.get(i).isDisabled()) {
                return (int) mapButtons.get(i).getUserData();
            }
        }
        return 0;
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        event.consume();
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
    }

    private void centerPointOnTheWindow(final double x, final double y) {
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
}
