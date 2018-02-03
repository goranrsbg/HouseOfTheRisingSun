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
import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import java.util.List;
import javafx.scene.layout.Pane;

/**
 *
 * @author Goran
 */
public class LocationsHandler {

    private final DBConnector db;
    private final Pane locationsPane;

    private boolean onFlag;

    public LocationsHandler(Pane locationPane) {
        this.locationsPane = locationPane;
        db = DBConnector.getInstance();
        onFlag = false;
    }

    public boolean addLocationsByID(int settlementID) {
        List<LocationEntity> list = db.executeSelectLocations(settlementID, true);
        addLocationsToThePane(list);
        return onFlag;
    }

    public boolean addLocationsByPAK(int streetPAK) {
        List<LocationEntity> list = db.executeSelectLocations(streetPAK, false);
        addLocationsToThePane(list);
        return onFlag;
    }

    public void addLocation(LocationEntity address) {
        locationsPane.getChildren().add(address.updateLayout());
    }

    private void addLocationsToThePane(List<LocationEntity> list) {
        if (list.isEmpty()) {
            sentMessaage("Nema memorisanih lokacija.", false);
        } else {
            locationsPane.getChildren().addAll(list);
            sentMessaage("Učitano lokacija: (" + list.size() + ").", false);
            onFlag = true;
            printList(list);
        }
    }

    public LocationsHandler clearLocatons() {
        locationsPane.getChildren().clear();
        onFlag = false;
        return this;
    }

    public boolean isOnFlagOn() {
        return onFlag;
    }

    private void printList(List list) {
        list.forEach((a) -> {
            System.out.println(a);
        });
    }

    private void sentMessaage(String message, boolean type) {
        MainController.notifyWithMsg(TITLE, message, type);
    }

    public static final String TITLE = "Upravljač adresama.";

}
