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

import com.goranrsbg.houseoftherisingsun.utility.Settlement;
import com.goranrsbg.houseoftherisingsun.utility.StagesNames;
import com.goranrsbg.houseoftherisingsun.LocatorApp;
import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.addaddress.AddAddressController;
import com.goranrsbg.houseoftherisingsun.ui.showstreets.ShowStreetsController;
import com.goranrsbg.houseoftherisingsun.utility.AddressHandler;
import com.goranrsbg.houseoftherisingsun.utility.ButtonFactory;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    private static MainController instance;
    private AddAddressController addAddressController;

    private final DBConnector db;
    private AddressHandler ah;

    private final Properties props;

    private double imgHeight;
    private double imgWidth;
    private int currentMapIndex;

    private final List<Settlement> settlements;

    private JFXButton buttonAddRecipient;
    private JFXButton buttonAddLocation;
    private JFXButton buttonShowStreets;
    private JFXButton buttonShowLocation;
    private JFXButton buttonShowAllLocations;

    @FXML
    private ImageView theMapImageView;
    @FXML
    private JFXNodesList rootButtonList;
    @FXML
    private ScrollPane rootScrollPane;
    @FXML
    private Pane locationsPane;

    public MainController() {
        props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("main.properties"));
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        db = DBConnector.getInstance();
        settlements = new ArrayList<>();
        collectSettlements();
        instance = this;
    }

    private void collectSettlements() {
        final String query = "SELECT * FROM SETTLEMENTS";
        ResultSet rs = db.executeQuery(query);
        try {
            settlements.add(new Settlement(0, "default", null));
            while (rs.next()) {
                settlements.add(new Settlement(rs.getInt("ID"), rs.getString("NAME"), rs.getString("INITIAL")));
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static MainController getInstance() {
        return instance;
    }

    public Pane getLocationsPane() {
        return locationsPane;
    }

    public Settlement getCurrentMap() {
        return settlements.get(currentMapIndex);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db.setMc(instance);
        ah = AddressHandler.getInstance();
        initButtons();
        loadTheMap(0);
    }

    private void initButtons() {
        ButtonFactory factory = ButtonFactory.newInstance();
        JFXNodesList recipientsJFXNodesList = new JFXNodesList();
        JFXNodesList locationsJFXNodesList = new JFXNodesList();
        JFXNodesList streetsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsMapChooserList = new JFXNodesList();
        // first buttons that expand sub lists
        recipientsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_FIRST_SUBCLASS, "recip-icon", "Primaoci:").getButton());
        locationsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "loc-icon", "Lokacije:").getButton());
        streetsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "road-icon", "Ulice:").getButton());
        settlementsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "map-icon", "Naselja:").getButton());
        settlementsMapChooserList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "settchooser-icon", "Izaberi mapu naselja.").getButton());

        buttonAddLocation = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "marker-icon", "Dadaj lokaciju.").getButton();
        buttonAddLocation.setOnAction(this::addWindowLoadOnActionEvent);
        buttonAddLocation.setUserData(StagesNames.ADD_LOCATION);
        buttonShowAllLocations = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "streetview-icon", "Prikaži sve Lokacije.").getButton();
        buttonShowAllLocations.setOnAction(this::btShowAllLocationsOnActionEvent);
        buttonShowAllLocations.setUserData(new String[]{"Prikaži sve Lokacije.", "Ukloni prikazane lokacije."});

        buttonAddRecipient = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_FIRST_SUBCLASS, "addrecip-icon", "Dodaj primaoca.").getButton();

        buttonShowStreets = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "roadt-icon", "Prikaži spisak ulica.").getButton();
        buttonShowStreets.setOnAction(this::addWindowLoadOnActionEvent);
        buttonShowStreets.setUserData(StagesNames.SHOW_STREETS);

        recipientsJFXNodesList.addAnimatedNode(buttonAddRecipient);
        locationsJFXNodesList.addAnimatedNode(buttonAddLocation);
        locationsJFXNodesList.addAnimatedNode(buttonShowAllLocations);
        streetsJFXNodesList.addAnimatedNode(buttonShowStreets);

        settlementsMapChooserList.setRotate(90d);
        for (int i = 1; i < settlements.size(); i++) {
            Settlement s = settlements.get(i);
            JFXButton button = factory.createNewJFXTextButton(s.getINITIALS(), s.getMapName()).getButton();
            button.setOnAction(this::btAddMapChangeOnActionEvent);
            button.setUserData(i);
            settlementsMapChooserList.addAnimatedNode(button);
        }
        settlementsJFXNodesList.addAnimatedNode(settlementsMapChooserList);

        rootButtonList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_ZERO_SUBCLASS, "start-icon", "Start").getButton());
        rootButtonList.addAnimatedNode(recipientsJFXNodesList);
        rootButtonList.addAnimatedNode(locationsJFXNodesList);
        rootButtonList.addAnimatedNode(streetsJFXNodesList);
        rootButtonList.addAnimatedNode(settlementsJFXNodesList);
    }

    private void addWindowLoadOnActionEvent(ActionEvent event) {
        Object userData = ((JFXButton) event.getSource()).getUserData();
        switch ((StagesNames) ((JFXButton) event.getSource()).getUserData()) {
            case ADD_LOCATION:
                loadWindow("/com/goranrsbg/houseoftherisingsun/ui/addaddress/addaddress.fxml", AddAddressController.TITLE, userData);
                buttonAddLocation.setDisable(true);
                break;
            case SHOW_STREETS:
                loadWindow("/com/goranrsbg/houseoftherisingsun/ui/showstreets/showstreets.fxml", ShowStreetsController.TITLE, userData);
                buttonShowStreets.setDisable(true);
                break;
        }
    }

    private void loadWindow(String location, String title, Object userData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(location));
            Parent parent = loader.load();
            Stage stage = new Stage(StageStyle.UTILITY);
            Scene newScene = new Scene(parent);
            stage.setScene(newScene);
            stage.setAlwaysOnTop(true);
            stage.setTitle(title);
            stage.setOnCloseRequest(this::addClosingEvent);
            stage.setUserData(userData);
            if ((StagesNames) userData == StagesNames.ADD_LOCATION) {
                addAddressController = (AddAddressController) loader.getController();
                stage.setResizable(false);
            }
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addClosingEvent(WindowEvent event) {
        switch ((StagesNames) ((Stage) event.getSource()).getUserData()) {
            case ADD_LOCATION:
                buttonAddLocation.setDisable(false);
                addAddressController = null;
                break;
            case SHOW_STREETS:
                buttonShowStreets.setDisable(false);
                break;
        }
    }

    private void btAddMapChangeOnActionEvent(ActionEvent event) {
        int id = (int) ((JFXButton) event.getSource()).getUserData();
        if (currentMapIndex != id) {
            loadTheMap(id);
            if (ah.isOnFlagOn()) {
                boolean isAdded = ah.clearLocatons().addLocationsByID(getCurrentMap().getID());
                setButtonShowAllLocatonsToShow(!isAdded);
            }
            if (buttonAddLocation.isDisabled()) {
                addAddressController.comboBoxAddStreets();
                addAddressController.clearLocationXY();
            }
        }
    }

    private void btShowAllLocationsOnActionEvent(ActionEvent event) {
        if (currentMapIndex == 0) {
            notifyWithMsg(props.getProperty("title"), "Karta nije odabrana.", false);
        } else {
            boolean flag = ah.isOnFlagOn();
            if (flag) {
                setButtonShowAllLocatonsToShow(flag);
                ah.clearLocatons();
            } else {
                boolean isAdded = ah.clearLocatons().addLocationsByID(getCurrentMap().getID());
                setButtonShowAllLocatonsToShow(!isAdded);
            }
        }
    }

    private void setButtonShowAllLocatonsToShow(boolean show) {
        if (show) {
            buttonShowAllLocations.getTooltip().setText("Prikaži sve lokacije.");
            buttonShowAllLocations.setStyle("-fx-border-color: white;");
        } else {
            buttonShowAllLocations.getTooltip().setText("Ukloni sve lokacije.");
            buttonShowAllLocations.setStyle("-fx-border-color: red;");
        }
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        MouseButton mb = event.getButton();
        final double x = event.getX();
        final double y = event.getY();
        if (mb == MouseButton.SECONDARY) {
            final double w = rootScrollPane.getWidth();
            final double h = rootScrollPane.getHeight();
            // move right click point to center of the screen if possible
            if (imgWidth > w) {
                rootScrollPane.setHvalue((x - w / 2) / (imgWidth - w));
            }
            if (imgHeight > h) {
                rootScrollPane.setVvalue((y - h / 2) / (imgHeight - h));
            }
        } else if (mb == MouseButton.PRIMARY) {
            if (buttonAddLocation.isDisabled() && currentMapIndex != 0) {
                addAddressController.setLocationXY(x, y);
            }
        }
    }

    public void loadTheMap(int index) {
        currentMapIndex = index;
        String mapName = settlements.get(currentMapIndex).getMapName();
        String mapDir = props.getProperty("path.to.maps");
        String name;
        String message;
        boolean error = false;
        Image img;
        File f = new File(mapDir + mapName);
        if (!f.isFile()) {
            f = new File(mapDir + settlements.get(0).getMapName());
        }
        name = f.getName();
        message = "Karta je učitana.";
        try {
            img = new Image(new FileInputStream(f));
            imgHeight = img.getHeight();
            imgWidth = img.getWidth();
            theMapImageView.setImage(img);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            message = "Karta nije pronadjena.";
            error = true;
        } finally {
            final String message1 = message;
            final boolean error1 = error;
            Platform.runLater(() -> {
                LocatorApp.setSubTitle(name);
                notifyWithMsg(name, message1, error1);
            });
        }
    }

    public void notifyWithMsg(String title, String message, boolean error) {
        Notifications notification = Notifications.create()
                .title(title)
                .text(message);
        if (error) {
            notification.showError();
        } else {
            notification.showInformation();
        }
    }

}
