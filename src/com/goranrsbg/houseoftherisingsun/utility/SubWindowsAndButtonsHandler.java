/*
 * Copyright 2018 Goran.
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
package com.goranrsbg.houseoftherisingsun.utility;

import com.goranrsbg.houseoftherisingsun.ui.addlocation.AddLocationController;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import static com.goranrsbg.houseoftherisingsun.ui.main.MainController.TITLE;
import static com.goranrsbg.houseoftherisingsun.ui.main.MainController.notifyWithMsg;
import com.goranrsbg.houseoftherisingsun.ui.showstreets.ShowStreetsController;
import com.goranrsbg.houseoftherisingsun.utility.entity.SettlementEntity;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 *
 * @author Goran
 */
public class SubWindowsAndButtonsHandler {

    private final JFXNodesList rootButtonList;
    private final MapHandler mapHandler;
    private final LocationsHandler locationsHandler;
    private final Map<String, JFXButton> buttonsOfImportance;

    private AddLocationController addAddressController;

    public SubWindowsAndButtonsHandler(JFXNodesList rootButtonList, LocationsHandler locationsHandler) {
        this.rootButtonList = rootButtonList;
        this.locationsHandler = locationsHandler;
        mapHandler = MapHandler.getInstance();
        buttonsOfImportance = new HashMap<>();
        ADD_LOCATION_KEY = generateButtonKey(RANDOM_DEFAULT_STRING_LENGTH);
        ADD_RECIPIENT_KEY = generateButtonKey(RANDOM_DEFAULT_STRING_LENGTH);
        SHOW_ALL_LOCATIONS_KEY = generateButtonKey(RANDOM_DEFAULT_STRING_LENGTH);
        SHOW_STREETS_KEY = generateButtonKey(RANDOM_DEFAULT_STRING_LENGTH);
    }

    private String generateButtonKey(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        Random rand = new Random(System.nanoTime());
        for (int i = 0; i < digits; i++) {
            sb.append((char) (RANDOM_SEARCH_START + rand.nextInt(RANDOM_SEARCH_GAP)));
        }
        return sb.toString();
    }

    public void generateButtons() {
        final ButtonFactory factory = new ButtonFactory();
        JFXNodesList recipientsJFXNodesList = new JFXNodesList();
        JFXNodesList locationsJFXNodesList = new JFXNodesList();
        JFXNodesList streetsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsJFXNodesList = new JFXNodesList();
        JFXNodesList settlementsMapChooserList = new JFXNodesList();

        rootButtonList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_ZERO_SUBCLASS, "start-icon", "Start").getButton());
        recipientsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_FIRST_SUBCLASS, "recip-icon", "Primaoci:").getButton());
        locationsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "loc-icon", "Lokacije:").getButton());
        streetsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "road-icon", "Ulice:").getButton());
        settlementsJFXNodesList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "map-icon", "Naselja:").getButton());
        settlementsMapChooserList.addAnimatedNode(factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "settchooser-icon", "Izaberi mapu naselja.").getButton());

        JFXButton btn;
        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "marker-icon", "Dadaj lokaciju.").getButton();
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(ADD_LOCATION_KEY);
        buttonsOfImportance.put(ADD_LOCATION_KEY, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "roadt-icon", "Prikaži spisak ulica.").getButton();
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(SHOW_STREETS_KEY);
        buttonsOfImportance.put(SHOW_STREETS_KEY, btn);
        streetsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "streetview-icon", "Prikaži sve Lokacije.").getButton();
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setId(SHOW_ALL_LOCATIONS_KEY);
        buttonsOfImportance.put(SHOW_ALL_LOCATIONS_KEY, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_FIRST_SUBCLASS, "addrecip-icon", "Dodaj primaoca.").getButton();
        btn.setId(ADD_RECIPIENT_KEY);
        buttonsOfImportance.put(ADD_RECIPIENT_KEY, btn);
        recipientsJFXNodesList.addAnimatedNode(btn);

        settlementsMapChooserList.setRotate(90d);
        Iterator<SettlementEntity> itr = mapHandler.getSettlementsIterator();
        while (itr.hasNext()) {
            SettlementEntity s = itr.next();
            JFXButton button = factory.createNewJFXTextButton(s.getInitials(), s.getMapName()).getButton();
            button.setOnAction(this::buttonClickActionEvent);
            button.setId(s.getId() + "");
            settlementsMapChooserList.addAnimatedNode(button);
        }
        settlementsJFXNodesList.addAnimatedNode(settlementsMapChooserList);

        rootButtonList.addAnimatedNode(recipientsJFXNodesList);
        rootButtonList.addAnimatedNode(locationsJFXNodesList);
        rootButtonList.addAnimatedNode(streetsJFXNodesList);
        rootButtonList.addAnimatedNode(settlementsJFXNodesList);
    }

    private void buttonClickActionEvent(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();
        final String id = btn.getId();
        if (id.equals(ADD_LOCATION_KEY)) {
            if (mapHandler.isMapLoaded()) {
                loadWindow("/com/goranrsbg/houseoftherisingsun/ui/addlocation/addlocation.fxml", AddLocationController.TITLE, ADD_LOCATION_KEY);
                btn.setDisable(true);
            } else {
                notifyWithMsg(MainController.TITLE, "Karta mora biti odabrana.", true);
            }
        } else if (id.equals(SHOW_STREETS_KEY)) {
            loadWindow("/com/goranrsbg/houseoftherisingsun/ui/showstreets/showstreets.fxml", ShowStreetsController.TITLE, SHOW_STREETS_KEY);
            btn.setDisable(true);
        } else if (id.equals(SHOW_ALL_LOCATIONS_KEY)) {
            if (mapHandler.isMapLoaded()) {
                boolean flag = locationsHandler.isOnFlagOn();
                if (flag) {
                    changeButtonShowAllLocationsTooltip(flag);
                    locationsHandler.clearLocatons();
                } else {
                    boolean isAdded = locationsHandler.clearLocatons().addLocationsByID(mapHandler.getCurrentMapId());
                    changeButtonShowAllLocationsTooltip(!isAdded);
                }
            } else {
                changeButtonShowAllLocationsTooltip(true);
                MainController.notifyWithMsg(TITLE, "Karta nije odabrana.", false);
            }
        } else {
            // Map change buttons, must be last for simplicity.
            int mapId = Integer.parseInt(id);
            if (mapHandler.loadMap(mapId).isMapLoaded()) {
                if (locationsHandler.isOnFlagOn()) {
                    boolean isAdded = locationsHandler.clearLocatons().addLocationsByID(mapHandler.getCurrentMapId());
                    changeButtonShowAllLocationsTooltip(!isAdded);
                }
                if (addAddressController != null) {
                    addAddressController.comboBoxAddStreets();
                    addAddressController.clearLocationXY();
                }
            }
        }
    }

    private void changeButtonShowAllLocationsTooltip(boolean show) {
        JFXButton btn = buttonsOfImportance.get(SHOW_ALL_LOCATIONS_KEY);
        if (show) {
            btn.getTooltip().setText("Prikaži sve lokacije.");
            btn.setStyle("-fx-border-color: white;");
        } else {
            btn.getTooltip().setText("Ukloni sve lokacije.");
            btn.setStyle("-fx-border-color: red;");
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
            if (((String) userData).equals(ADD_LOCATION_KEY)) {
                addAddressController = (AddLocationController) loader.getController();
                stage.setResizable(false);
            }
            stage.show();
        } catch (IOException ex) {
            MainController.notifyWithMsg(MainController.TITLE, "Prikazivanje prozora nije uspelo.\nGreška: " + ex.getMessage(), true);
        }
    }

    private void addClosingEvent(WindowEvent event) {
        String key = (String) ((Stage) event.getSource()).getUserData();
        if (key.equals(ADD_LOCATION_KEY)) {
            buttonsOfImportance.get(key).setDisable(false);
            addAddressController = null;
        } else if (key.equals(SHOW_STREETS_KEY)) {
            buttonsOfImportance.get(SHOW_STREETS_KEY).setDisable(false);
        }
    }

    public boolean isAddAddressWindowShown() {
        return addAddressController != null;
    }

    public void setAddAddressWindow(double x, double y) {
        if (isAddAddressWindowShown()) {
            addAddressController.setLocationXY(x, y);
        }
    }

    private final int RANDOM_DEFAULT_STRING_LENGTH = 7;
    private final int RANDOM_SEARCH_START = 33;
    private final int RANDOM_SEARCH_GAP = 94;
    public final String ADD_LOCATION_KEY;
    public final String ADD_RECIPIENT_KEY;
    public final String SHOW_ALL_LOCATIONS_KEY;
    public final String SHOW_STREETS_KEY;

}
