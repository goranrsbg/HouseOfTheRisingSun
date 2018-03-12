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
package com.goranrsbg.houseoftherisingsun.ui.addlocation;

import com.goranrsbg.houseoftherisingsun.database.DBHandler;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.utility.LocationsHandler;
import com.goranrsbg.houseoftherisingsun.utility.entity.LocationEntity;
import com.goranrsbg.houseoftherisingsun.utility.MapHandler;
import com.goranrsbg.houseoftherisingsun.utility.entity.StreetEntity;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextFormatter;

/**
 *
 * @author Goran
 */
public class AddLocationController implements Initializable {
    
    public static final String TITLE = "Dodaj adresu";

    private final DBHandler db;
    private final MapHandler mapHandler;
    private final LocationsHandler locationsHandler;

    @FXML
    private JFXTextField xTextField;
    @FXML
    private JFXTextField yTextField;
    @FXML
    private JFXTextField brTextField;
    @FXML
    private JFXTextArea noteAreaField;
    @FXML
    private JFXComboBox<StreetEntity> streetCombo;

    private final ObservableList<StreetEntity> streetsData;

    public AddLocationController() {
        db = DBHandler.getInstance();
        mapHandler = MapHandler.getInstance();
        streetsData = FXCollections.observableArrayList();
        locationsHandler = LocationsHandler.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFormatter<String> formatterX = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                try {
                    Double.valueOf(textNew);
                } catch (NumberFormatException e) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude realan broj.", false);
                }
            }
            return t;
        });
        TextFormatter<String> formatterY = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                try {
                    Double.valueOf(textNew);
                } catch (NumberFormatException e) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude realan broj.", false);
                }
            }
            return t;
        });

        xTextField.setTextFormatter(formatterX);
        yTextField.setTextFormatter(formatterY);

        TextFormatter<String> formatterBr = new TextFormatter<>((t) -> {
            if (t.getText().contains(" ")) {
                t = null;
                sendMessage("Vrednost polja ne sme da sadrži razmak.", false);
            } else if (t.getControlNewText().length() > 10) {
                t = null;
                sendMessage("Vrednost polja mora da bude do 10 znakova.", false);
            }
            return t;
        });
        TextFormatter<String> formatterNote = new TextFormatter<>((t) -> {
            if (t.getControlNewText().length() > 512) {
                t = null;
                MainController.notifyWithMsg(TITLE, "Vrednost polja mora da bude do 512 znakova.", false);
            }
            return t;
        });

        brTextField.setTextFormatter(formatterBr);
        noteAreaField.setTextFormatter(formatterNote);

        streetCombo.setItems(streetsData);

        comboBoxAddStreets();
    }

    public AddLocationController comboBoxAddStreets() {
        streetsData.clear();
        List<StreetEntity> list = db.executeSelectStreetsById(mapHandler.getCurrentMapId());
        streetsData.addAll(list);
        return this;
    }

    public void clearLocationXY() {
        xTextField.clear();
        yTextField.clear();
    }

    public void setLocationXY(double x, double y) {
        xTextField.setText(Double.toString(x));
        yTextField.setText(Double.toString(y));
    }

    @FXML
    private void saveButtonAction() {
        final StreetEntity s = streetCombo.getValue();
        final String x = xTextField.getText().trim();
        final String y = yTextField.getText().trim();
        final String houseNumber = brTextField.getText().trim();
        final String note = noteAreaField.getText().trim();
        if (validateFields(s, x, y, houseNumber)) {
            LocationEntity loce = db.executeInsertOrUpdateLocation(new LocationEntity(
                    Double.parseDouble(x) - LocationsHandler.ICON_WIDTH_HALF, 
                    Double.parseDouble(y) + LocationsHandler.ICON_HEIGHT_HALF,
                    s.getPak(), houseNumber, 
                    note.isEmpty() ? null : note));
            if(loce != null && locationsHandler.isLocationsShown()) {
                locationsHandler.addLocation(loce);
            }
        }
    }

    private boolean validateFields(final StreetEntity s, final String x, final String y, final String houseNumber) {
        boolean valid;
        valid = true;
        if (s == null) {
            sendMessage("Ulica nije izabrana.", true);
            valid = false;
        }
        if (x.isEmpty() || y.isEmpty()) {
            sendMessage("Koordinate nisu definisane.", true);
            valid = false;
        }
        if (houseNumber.isEmpty()) {
            sendMessage("Broj nije odabran.", true);
            valid = false;
        }
        return valid;
    }

    private void sendMessage(final String message, final boolean type) {
        MainController.notifyWithMsg(TITLE, message, type);
    }
    
}
