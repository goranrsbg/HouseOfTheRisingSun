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
package com.goranrsbg.houseoftherisingsun.ui.addaddress;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.goranrsbg.houseoftherisingsun.utility.Street;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextFormatter;

/**
 *
 * @author Goran
 */
public class AddAddressController implements Initializable {

    public static final String TITLE = "Dodaj adresu";

    private final DBConnector db;

    private final MainController mc;

    @FXML
    private JFXTextField xTextField;
    @FXML
    private JFXTextField yTextField;
    @FXML
    private JFXTextField brTextField;
    @FXML
    private JFXTextArea noteAreaField;
    @FXML
    private JFXComboBox<Street> streetCombo;

    private final ObservableList<Street> streetsData;

    public AddAddressController() {
        db = DBConnector.getInstance();
        streetsData = FXCollections.observableArrayList();
        mc = MainController.getInstance();
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
                    mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude realan broj.", false);
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
                    mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude realan broj.", false);
                }
            }
            return t;
        });

        xTextField.setTextFormatter(formatterX);
        yTextField.setTextFormatter(formatterY);

        TextFormatter<String> formatterBr = new TextFormatter<>((t) -> {
            if (t.getText().contains(" ")) {
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja ne sme da sadrži razmak.", false);
            } else if (t.getControlNewText().length() > 10) {
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude do 10 znakova.", false);
            }
            return t;
        });
        TextFormatter<String> formatterNote = new TextFormatter<>((t) -> {
            if (t.getControlNewText().length() > 512) {
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude do 512 znakova.", false);
            }
            return t;
        });

        brTextField.setTextFormatter(formatterBr);
        noteAreaField.setTextFormatter(formatterNote);

        streetCombo.setItems(streetsData);

        comboBoxAddStreets();
    }

    public void comboBoxAddStreets() {
        int mapId = mc.getCurrentMap().getID();
        streetsData.clear();
        List<Street> list = db.executeSelectStreets(mapId);
        streetsData.addAll(list);
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
    private void saveButtonAction(ActionEvent event) {
        final Street s = streetCombo.getValue();
        String x = xTextField.getText().trim();
        String y = yTextField.getText().trim();
        String houseNumber = brTextField.getText().trim();
        String note = noteAreaField.getText().trim();
        if (validateFields(s, x, y, houseNumber)) {
            db.executeInsertLocation(Double.parseDouble(x) - WIDTH_HALF, Double.parseDouble(y) - HEIGHT_HALF, houseNumber, s, note);
        }
    }
    
    private boolean validateFields(final Street s, String x, String y, String houseNumber) {
        boolean valid;
        valid = true;
        if (s == null) {
            mc.notifyWithMsg(TITLE, "Ulica nije izabrana.", true);
            valid = false;
        }
        if (x.isEmpty() || y.isEmpty()) {
            mc.notifyWithMsg(TITLE, "Koordinate nisu definisane.", true);
            valid = false;
        }
        if (houseNumber.isEmpty()) {
            mc.notifyWithMsg(TITLE, "Broj nije odabran.", true);
            valid = false;
        }
        return valid;
    }

    private static final double WIDTH_HALF = 11.5714282989502;
    private static final double HEIGHT_HALF = 14.52099609375;

}
