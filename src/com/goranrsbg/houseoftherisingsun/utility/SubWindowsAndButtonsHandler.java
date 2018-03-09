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
import java.util.Map;
import javafx.collections.ObservableList;
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
    private final Map<ButtonType, JFXButton> buttonsOfImportance;
    private JFXButton[] btnMaps;
    private final Map<ButtonType, Object> subWindowsControllers;

    public SubWindowsAndButtonsHandler(JFXNodesList rootButtonList, LocationsHandler locationsHandler, MapHandler mapHandler) {
        this.rootButtonList = rootButtonList;
        this.locationsHandler = locationsHandler;
        this.mapHandler = mapHandler;
        buttonsOfImportance = new HashMap<>();
        subWindowsControllers = new HashMap<>();
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
        btn.setUserData(ButtonType.ADD_LOCATION);
        buttonsOfImportance.put(ButtonType.ADD_LOCATION, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_THIRD_SUBCLASS, "roadt-icon", "Prikaži spisak ulica.").getButton();
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setUserData(ButtonType.SHOW_STREETS_TABLE);
        buttonsOfImportance.put(ButtonType.SHOW_STREETS_TABLE, btn);
        streetsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_SECOND_SUBCLASS, "streetview-icon", "Prikaži sve Lokacije.").getButton();
        btn.setOnAction(this::buttonClickActionEvent);
        btn.setUserData(ButtonType.SHOW_ALL_LOCATIONS);
        buttonsOfImportance.put(ButtonType.SHOW_ALL_LOCATIONS, btn);
        locationsJFXNodesList.addAnimatedNode(btn);

        btn = factory.createNewJFXGlyphButton(ButtonFactory.BUTTON_FIRST_SUBCLASS, "addrecip-icon", "Dodaj primaoca.").getButton();
        btn.setUserData(ButtonType.ADD_RECIPIENT);
        buttonsOfImportance.put(ButtonType.ADD_RECIPIENT, btn);
        recipientsJFXNodesList.addAnimatedNode(btn);

        settlementsMapChooserList.setRotate(90d);
        ObservableList<SettlementEntity> settlements = mapHandler.getSettlements();
        btnMaps = new JFXButton[settlements.size()];
        for (int i = 0; i < settlements.size(); i++) {
            SettlementEntity e = settlements.get(i);
            JFXButton button = factory.createNewJFXTextButton(e.getInitials(), e.getMapName()).getButton();
            button.setOnAction(this::buttonClickActionEvent);
            button.setId(e.getId() + "");
            button.setUserData(ButtonType.MAP);
            settlementsMapChooserList.addAnimatedNode(button);
            btnMaps[i] = button;
        }
        settlementsJFXNodesList.addAnimatedNode(settlementsMapChooserList);

        rootButtonList.addAnimatedNode(recipientsJFXNodesList);
        rootButtonList.addAnimatedNode(locationsJFXNodesList);
        rootButtonList.addAnimatedNode(streetsJFXNodesList);
        rootButtonList.addAnimatedNode(settlementsJFXNodesList);
    }

    private void buttonClickActionEvent(ActionEvent event) {
        event.consume();
        JFXButton btn = (JFXButton) event.getSource();
        ButtonType btn_type = (ButtonType) btn.getUserData();
        switch (btn_type) {
            case ADD_LOCATION:
                if (mapHandler.isMapLoaded()) {
                    loadWindow("/com/goranrsbg/houseoftherisingsun/ui/addlocation/addlocation.fxml", AddLocationController.TITLE, btn_type);
                    btn.setDisable(true);
                } else {
                    notifyWithMsg(MainController.TITLE, "Karta mora biti odabrana.", true);
                }
                break;
            case SHOW_STREETS_TABLE:
                loadWindow("/com/goranrsbg/houseoftherisingsun/ui/showstreets/showstreets.fxml", ShowStreetsController.TITLE, btn_type);
                btn.setDisable(true);
                break;
            case SHOW_ALL_LOCATIONS:
                if (mapHandler.isMapLoaded()) {
                    boolean flag = locationsHandler.isLocationsShown();
                    if (flag) {
                        toggelButtonShowAllLocationsTooltip(flag);
                        locationsHandler.clearLocatons();
                    } else {
                        boolean isAdded = locationsHandler.clearLocatons().addLocationsByID(mapHandler.getCurrentMapId());
                        toggelButtonShowAllLocationsTooltip(!isAdded);
                    }
                } else {
                    toggelButtonShowAllLocationsTooltip(true);
                    MainController.notifyWithMsg(TITLE, "Karta nije odabrana.", false);
                }
                break;
            case MAP:
                final String id = btn.getId();
                int mapId = Integer.parseInt(id);
                if (mapHandler.loadMap(mapId).isMapLoaded()) {
                    enableMapButton();
                    btn.setDisable(true);
                    locationsHandler.clearLocatons();
                    Object adc = subWindowsControllers.get(ButtonType.ADD_LOCATION);
                    if (adc != null) {
                        ((AddLocationController)adc).comboBoxAddStreets().clearLocationXY();
                    }
                }
                break;
        }
    }
    
    private void enableMapButton() {
        for (JFXButton btn : btnMaps) {
            if (btn.isDisabled()) {
                btn.setDisable(false);
            }
        }
    }

    private void toggelButtonShowAllLocationsTooltip(boolean show) {
        JFXButton btn = buttonsOfImportance.get(ButtonType.SHOW_ALL_LOCATIONS);
        if (show) {
            btn.getTooltip().setText("Prikaži sve lokacije.");
            btn.setStyle("-fx-border-color: white;");
        } else {
            btn.getTooltip().setText("Ukloni sve lokacije.");
            btn.setStyle("-fx-border-color: red;");
        }
    }

    private void loadWindow(String location, String title, ButtonType userData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(location));
            Parent parent = loader.load();
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.setScene(new Scene(parent));
            stage.setAlwaysOnTop(true);
            stage.setTitle(title);
            stage.setOnCloseRequest(this::addClosingEvent);
            stage.setUserData(userData);
            subWindowsControllers.put(userData, loader.getController());
            stage.show();
        } catch (IOException ex) {
            MainController.notifyWithMsg(MainController.TITLE, "Prikazivanje prozora nije uspelo.\nGreška: " + ex.getMessage(), true);
        }
    }

    private void addClosingEvent(WindowEvent event) {
        ButtonType btn_type = (ButtonType) ((Stage) event.getSource()).getUserData();
        buttonsOfImportance.get(btn_type).setDisable(false);
        subWindowsControllers.put(btn_type, null);
    }

    public boolean isWindowLoaded(ButtonType btn_type) {
        return buttonsOfImportance.get(btn_type).isDisable();
    }

    public Object getController(ButtonType btn_type) {
        return subWindowsControllers.get(btn_type);
    }

}