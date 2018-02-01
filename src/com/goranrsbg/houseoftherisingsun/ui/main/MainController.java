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
import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.addaddress.AddAddressController;
import com.goranrsbg.houseoftherisingsun.ui.showstreets.ShowStreetsController;
import com.goranrsbg.houseoftherisingsun.utility.AddressHandler;
import com.goranrsbg.houseoftherisingsun.utility.ButtonFactory;
import com.goranrsbg.houseoftherisingsun.utility.MapHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
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

    private AddAddressController addAddressController;

    private final DBConnector db;
    private MapHandler mapHandler;
    private AddressHandler addressHandler;

    private final Properties props;

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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addressHandler = new AddressHandler(locationsPane);
        db.setAddressHandler(addressHandler);
        mapHandler = new MapHandler(theMapImageView, db.executeSellectAllSettlements()).loadDefaultMap();
        initButtons();
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
        Iterator<Settlement> itr = mapHandler.getSettlementsIterator();
        while (itr.hasNext()) {
            Settlement s = itr.next();
            JFXButton button = factory.createNewJFXTextButton(s.getInitials(), s.getMapName()).getButton();
            button.setOnAction(this::btAddMapChangeOnActionEvent);
            button.setUserData(s.getId());
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
                if (mapHandler.isMapLoaded()) {
                    loadWindow("/com/goranrsbg/houseoftherisingsun/ui/addaddress/addaddress.fxml", AddAddressController.TITLE, userData);
                    buttonAddLocation.setDisable(true);
                } else {
                    notifyWithMsg(props.getProperty("title"), "Karta mora biti odabrana.", true);
                }
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
        int mapId = (int) ((JFXButton) event.getSource()).getUserData();
        if (mapHandler.loadMap(mapId).isMapLoaded()) {
            if (addressHandler.isOnFlagOn()) {
                boolean isAdded = addressHandler.clearLocatons().addLocationsByID(mapHandler.getCurrentMapId());
                setButtonShowAllLocatonsToShow(!isAdded);
            }
            if (buttonAddLocation.isDisabled()) {
                addAddressController.comboBoxAddStreets();
                addAddressController.clearLocationXY();
            }
        }
    }

    private void btShowAllLocationsOnActionEvent(ActionEvent event) {
        if (mapHandler.isMapLoaded()) {
            boolean flag = addressHandler.isOnFlagOn();
            if (flag) {
                setButtonShowAllLocatonsToShow(flag);
                addressHandler.clearLocatons();
            } else {
                boolean isAdded = addressHandler.clearLocatons().addLocationsByID(mapHandler.getCurrentMapId());
                setButtonShowAllLocatonsToShow(!isAdded);
            }
        } else {
            notifyWithMsg(props.getProperty("title"), "Karta nije odabrana.", false);
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
        final boolean isMapLoaded = mapHandler.isMapLoaded();
        if (mb == MouseButton.SECONDARY && isMapLoaded) {
            final double w = rootScrollPane.getWidth();
            final double h = rootScrollPane.getHeight();
            final double imgWidth = mapHandler.getMapWidth();
            final double imgHeight = mapHandler.getMapHeight();
            // move right click point to center of the screen if possible
            if (imgWidth > w) {
                rootScrollPane.setHvalue((x - w / 2) / (imgWidth - w));
            }
            if (imgHeight > h) {
                rootScrollPane.setVvalue((y - h / 2) / (imgHeight - h));
            }
        } else if (mb == MouseButton.PRIMARY && buttonAddLocation.isDisabled() && isMapLoaded) {
            addAddressController.setLocationXY(x, y);
        }
    }

    public static void notifyWithMsg(String title, String message, boolean error) {
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
