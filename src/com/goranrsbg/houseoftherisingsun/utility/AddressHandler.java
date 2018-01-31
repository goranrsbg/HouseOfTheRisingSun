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

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import java.util.List;
import javafx.scene.layout.Pane;

/**
 *
 * @author Goran
 */
public class AddressHandler {

    public static final String TITLE = "AddressHandler";

    private static AddressHandler instance;
    private DBConnector db;
    private MainController mc;
    private Pane locationsPane;

    private boolean onFlag;

    private AddressHandler() {
        onFlag = false;
    }

    public static AddressHandler getInstance() {
        if (instance == null) {
            instance = new AddressHandler();
            instance.db = DBConnector.getInstance();
            instance.db.setAh(instance);
            instance.mc = MainController.getInstance();
            instance.locationsPane = instance.mc.getLocationsPane();
        }
        return instance;
    }

    public boolean addLocationsByID(int settlementID) {
        List<Address> list = db.execugeSelectLocationsByID(settlementID);
        addLocationsToThePane(list);
        return onFlag;
    }

    public boolean addLocationsByPAK(int streetPAK) {
        List<Address> list = db.executeSelectLocatonsByPak(streetPAK);
        addLocationsToThePane(list);
        return onFlag;
    }

    public void addLocation(Address address) {
        locationsPane.getChildren().add(address);
        mc.notifyWithMsg(TITLE, "Učitana " + address.getBr() + " lokacija.", false);
    }

    private void addLocationsToThePane(List<Address> list) {
        if (list.isEmpty()) {
            mc.notifyWithMsg(TITLE, "Nema memorisanih lokacija.", false);
        } else {
            locationsPane.getChildren().addAll(list);
            mc.notifyWithMsg(TITLE, "Učitano " + list.size() + " lokacija.", false);
            onFlag = true;
            printList(list);
        }
    }

    public AddressHandler clearLocatons() {
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

}
