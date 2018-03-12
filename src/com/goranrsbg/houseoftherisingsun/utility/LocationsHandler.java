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

import com.goranrsbg.houseoftherisingsun.utility.entity.LocationEntity;
import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.database.DBHandler.DBTypes;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.utility.entity.StreetEntity;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

/**
 *
 * @author Goran
 */
public class LocationsHandler {

    private static LocationsHandler instance;

    private final DBHandler db;
    private final Pane locationsPane;
    private final Map<Integer, String> streets;
    private final List<FontAwesomeIconView> locations;
    private int currentSize;

    private boolean showLocations;

    private LocationsHandler(Pane locationPane) {
        this.locationsPane = locationPane;
        locationPane.setOnDragOver(this::onDragOver);
        locationPane.setOnDragDropped(this::onDragDropped);
        locations = new ArrayList();
        streets = new HashMap<>();
        db = DBHandler.getInstance();
        showLocations = false;
    }

    public static LocationsHandler createInstance(Pane locationPane) {
        if (instance == null) {
            instance = new LocationsHandler(locationPane);
        }
        return instance;
    }

    public static LocationsHandler getInstance() {
        return instance;
    }

    public boolean addLocationsByID(int settlementID) {
        List<LocationEntity> list;
        List<StreetEntity> streetsList;
        try {
            list = db.executeSelectLocations(settlementID, DBTypes.SELECT_LOCATIONS_BY_SETTLEMENT_ID);
            streetsList = db.executeSelectStreetsById(settlementID);
            memorizeStreets(streetsList);
            buildLocations(list);
        } catch (SQLException ex) {
            sendMessage("Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        return showLocations;
    }

    public boolean addLocationsByPAK(int streetPAK) {
        List<LocationEntity> list;
        try {
            list = db.executeSelectLocations(streetPAK, DBTypes.SELECT_LOCATIONS_BY_PAK);
            buildLocations(list);
        } catch (SQLException ex) {
            sendMessage("Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        return showLocations;
    }

    private void memorizeStreets(List<StreetEntity> streetsList) {
        streetsList.forEach((s) -> {
            if (!streets.containsKey(s.getPak())) {
                streets.put(s.getPak(), s.getName());
            }
        });
    }

    public void addLocation(LocationEntity location) {
        ensureLocationsSize(currentSize + 1);
        locationsPane.getChildren().add(createIconView(currentSize - 1, location));
    }

    private void buildLocations(List<LocationEntity> list) {
        if (list.isEmpty()) {
            sendMessage("Nema memorisanih lokacija.", false);
            return;
        }
        ensureLocationsSize(list.size());
        for (int i = 0; i < list.size(); i++) {
            locationsPane.getChildren().add(createIconView(i, list.get(i)));
        }
        sendMessage("Učitano lokacija: (" + list.size() + ").", false);
        showLocations = true;
    }

    private FontAwesomeIconView createLocation() {
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setOnMouseClicked(this::onMouseClicked);
        icon.setOnDragDetected(this::onDragDetected);
        icon.getStyleClass().add(DEFAULT_ICON_STYLE);
        return icon;
    }

    private FontAwesomeIconView createIconView(int pos, LocationEntity le) {
        FontAwesomeIconView location = locations.get(pos);
        location.setX(le.getX());
        location.setY(le.getY());
        Tooltip t = new Tooltip("Adresa:\n" + streets.get(le.getStreetPak()) + le.toTooltipString());
        t.setStyle(DEFAULT_TOOLTIP_STYLE);
        Tooltip.install(location, t);
        location.setUserData(le);
        
        return location;
    }

    private void onMouseClicked(MouseEvent e) {
        e.consume();
        FontAwesomeIconView source = (FontAwesomeIconView)e.getSource();
        System.out.println("Click # " + source.getUserData());
        Bounds lb = source.getBoundsInParent();
        System.out.println(lb.getWidth() + " <-WH-> " + lb.getHeight());
        System.out.println(source.getX() + " <-XY-> " + source.getY());
    }

    private void onDragDetected(MouseEvent e) {
        FontAwesomeIconView location = (FontAwesomeIconView) e.getSource();
        LocationEntity le = (LocationEntity) location.getUserData();
        Dragboard dragBoard = location.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(le.getLocationId() + "");
        dragBoard.setContent(content);
        e.consume();
    }
    
    private void onDragOver(DragEvent e) {
        if(e.getDragboard().hasString()) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    }
    
    private void onDragDropped(DragEvent e) {
        FontAwesomeIconView loc = (FontAwesomeIconView)e.getGestureSource();
        LocationEntity locationEntity = (LocationEntity)loc.getUserData();
        double tempX = locationEntity.getX();
        double tempY = locationEntity.getY();
        locationEntity.setX(e.getX() - ICON_WIDTH_HALF);
        locationEntity.setY(e.getY() + ICON_HEIGHT_HALF);
        LocationEntity updatedLocation = db.executeInsertOrUpdateLocation(locationEntity);
        if(updatedLocation != null) {
            loc.setX(locationEntity.getX());
            loc.setY(locationEntity.getY());
        } else {
            locationEntity.setX(tempX);
            locationEntity.setY(tempY);
        }
        System.out.println(e.getSceneX() + " XdropY " + e.getSceneY());
    }

    private void ensureLocationsSize(int minSize) {
        int size = locations.size();
        if (size < minSize) {
            for (int i = 0; i < minSize - size; i++) {
                locations.add(createLocation());
            }
        }
        currentSize = minSize;
    }

    public LocationsHandler clearLocatons() {
        locationsPane.getChildren().clear();
        streets.clear();
        showLocations = false;
        return this;
    }

    public boolean isLocationsShown() {
        return showLocations;
    }

    private void sendMessage(String message, boolean type) {
        MainController.notifyWithMsg(TITLE, message, type);
    }

    public static final String TITLE = "Upravljač adresama.";
    public static final double ICON_WIDTH_HALF = 12.0;
    public static final double ICON_HEIGHT_HALF = 14.0;
    private static final String DEFAULT_ICON_STYLE = "location-icon";
    private static final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal normal 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";

}
