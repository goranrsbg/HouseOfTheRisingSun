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
        locations = new ArrayList();
        streets = new HashMap<>();
        db = DBHandler.getInstance();
        showLocations = false;
    }
    
    public static LocationsHandler createInstance(Pane locationPane) {
        if(instance == null) {
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
            list = db.executeSelectLocations(settlementID, DBHandler.SELECTLOCATIONSBY_ID);
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
            list = db.executeSelectLocations(streetPAK, DBHandler.SELECTLOCATIONSBY_PAK);
            buildLocations(list);
        } catch (SQLException ex) {
            sendMessage("Selektovanje ulica nije uspelo.\nGreška: " + ex.getErrorCode(), true);
        }
        return showLocations;
    }
    
    private void memorizeStreets(List<StreetEntity> streetsList) {
        streetsList.forEach((s) -> {
            if(!streets.containsKey(s.getPak())) {
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
        icon.getStyleClass().add(DEFAULT_ICON_STYLE);
        return icon;
    }

    private FontAwesomeIconView createIconView(int pos, LocationEntity le) {
        FontAwesomeIconView location = locations.get(pos);
        location.setLayoutX(le.getX());
        location.setLayoutY(le.getY());
        Tooltip t = new Tooltip("Adresa:\n" + streets.get(le.getStreetPak()) + le.toTooltipString());
        t.setStyle(DEFAULT_TOOLTIP_STYLE);
        Tooltip.install(location, t);
        location.setUserData(le);
        location.setOnMouseClicked((e) -> {
            e.consume();
            FontAwesomeIconView source = (FontAwesomeIconView) e.getSource();
            System.out.println("Click # " + source.getUserData());
            Bounds lb = source.getLayoutBounds();
            System.out.println(lb.getWidth() + " " +lb.getHeight());
        });
        return location;
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

    public static final String DRAG_KEY_WORD = "dragged";
    public static final String TITLE = "Upravljač adresama.";
    public static final double ICON_WIDTH_HALF = 11.5;
    public static final double ICON_HEIGHT_HALF = 13.5;
    private static final String DEFAULT_ICON_STYLE = "location-icon";
    private static final String DEFAULT_TOOLTIP_STYLE = "-fx-font: normal normal 15px 'Oxygen'; -fx-base: #AE3522; -fx-text-fill: orange;";

    

}
