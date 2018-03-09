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

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.addlocation.AddLocationController;
import com.goranrsbg.houseoftherisingsun.utility.ButtonType;
import com.goranrsbg.houseoftherisingsun.utility.LocationsHandler;
import com.goranrsbg.houseoftherisingsun.utility.SubWindowsAndButtonsHandler;
import com.goranrsbg.houseoftherisingsun.utility.MapHandler;
import com.jfoenix.controls.JFXNodesList;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Goran
 */
public class MainController implements Initializable {

    private final DBHandler db;
    private MapHandler mapHandler;
    private LocationsHandler locationsHandler;
    private SubWindowsAndButtonsHandler subWindowsHandler;

    @FXML
    private ImageView theMapImageView;
    @FXML
    private JFXNodesList rootButtonList;
    @FXML
    private ScrollPane rootScrollPane;
    @FXML
    private Pane locationsPane;

    public MainController() {
        db = DBHandler.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        locationsHandler = LocationsHandler.createInstance(locationsPane);
        mapHandler = MapHandler.createInstance(theMapImageView, db.executeSellectAllSettlements()).loadDefaultMap();
        subWindowsHandler = new SubWindowsAndButtonsHandler(rootButtonList, locationsHandler, mapHandler);
        subWindowsHandler.generateButtons();
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        MouseButton mb = event.getButton();
        if (mapHandler.isMapLoaded()) {
            final double x = event.getX();
            final double y = event.getY();
            if (mb == MouseButton.SECONDARY) {
                centerPointOnTheWindow(x, y);
            } else if (mb == MouseButton.PRIMARY && subWindowsHandler.isWindowLoaded(ButtonType.ADD_LOCATION)) {
                ((AddLocationController)subWindowsHandler.getController(ButtonType.ADD_LOCATION)).setLocationXY(x, y);
            }
        }
    }

    private void centerPointOnTheWindow(final double x, final double y) {
        final double w = rootScrollPane.getWidth();
        final double h = rootScrollPane.getHeight();
        final double imgWidth = mapHandler.getMapWidth();
        final double imgHeight = mapHandler.getMapHeight();
        if (imgWidth > w) {
            rootScrollPane.setHvalue((x - w / 2) / (imgWidth - w));
        }
        if (imgHeight > h) {
            rootScrollPane.setVvalue((y - h / 2) / (imgHeight - h));
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

    public static final String TITLE = "Glavni prikaz.";

}
