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
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @FXML
    private JFXTextField xTextField;
    @FXML
    private JFXTextField yTextField;
    @FXML
    private JFXComboBox<Street> streetCombo;
    @FXML
    private JFXTextField pathWayTextField;
    @FXML
    private JFXTextField brTextField;
    @FXML
    private JFXTextField postmanPathTextField;
    @FXML
    private JFXTextArea noteAreaField;

    private final DBHandler db;
    private final ObservableList<Street> streetsData;
    private final MainController mc;

    public AddLocationController() {
        db = DBHandler.getInstance();
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
                    sendMessage("Vrednost polja mora da bude realan broj.", MainController.MessageType.INFORMATION);
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
                    sendMessage("Vrednost polja mora da bude realan broj.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        TextFormatter<String> formatterNumber = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                if (textNew.contains(" ")) {
                    t = null;
                    sendMessage("Vrednost polja ne sme da sadrži razmak.", MainController.MessageType.INFORMATION);
                } else if (textNew.length() > 10) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude do 10 znakova.", MainController.MessageType.INFORMATION);
                } else if (!Character.isDigit(textNew.charAt(0))) {
                    t = null;
                    sendMessage("Broj mora da počne sa brojem.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        TextFormatter<String> formatterPostmanPath = new TextFormatter<>((t) -> {
            String textNew = t.getControlNewText();
            if (!textNew.isEmpty()) {
                try {
                    Integer.valueOf(textNew);
                } catch (NumberFormatException e) {
                    t = null;
                    sendMessage("Vrednost polja mora da bude ceo broj.", MainController.MessageType.INFORMATION);
                }
            }
            return t;
        });
        TextFormatter<String> formatterNote = new TextFormatter<>((t) -> {
            if (t.getControlNewText().length() > 512) {
                t = null;
                sendMessage("Vrednost polja mora da bude do 512 znakova.", MainController.MessageType.INFORMATION);
            }
            return t;
        });

        xTextField.setTextFormatter(formatterX);
        yTextField.setTextFormatter(formatterY);
        brTextField.setTextFormatter(formatterNumber);
        postmanPathTextField.setTextFormatter(formatterPostmanPath);
        noteAreaField.setTextFormatter(formatterNote);

        streetCombo.setItems(streetsData);
        streetCombo.setPromptText("Ulica:");
        comboBoxLoadStreets();
    }

    public void comboBoxLoadStreets() {
        streetsData.clear();
        if (mc.isMapLoaded()) {
            try {
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.SELECT_STREETS_WITH_SETTLEMENT_ID);
                ps.setInt(1, mc.getLoadedMapId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        streetsData.add(new Street(rs.getInt("STREET_ID"), rs.getString("STREET_NAME")));
                    }
                }
            } catch (SQLException ex) {
                sendMessage("Greška pri učitavanju ulica (id:" + mc.getLoadedMapId() + ")\n" + "Error: " + ex.getMessage(), MainController.MessageType.ERROR);
            }
        }
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
        final Street s = streetCombo.getValue();
        final String x = xTextField.getText();
        final String y = yTextField.getText();
        final String pathWay = pathWayTextField.getText().trim();
        final String houseNumber;
        if (pathWay.isEmpty()) {
            houseNumber = brTextField.getText().trim();
        } else {
            houseNumber = pathWay + '-' + brTextField.getText().trim();
        }
        final String note = noteAreaField.getText().trim();
        if (validateFields(s, x, y, houseNumber)) {
            try {
                PreparedStatement ps = db.getStatement(DBHandler.StatementType.INSERT_LOCATION);
                double pX = Double.parseDouble(x);
                double pY = Double.parseDouble(y);
                ps.setDouble(1, pX);
                ps.setDouble(2, pY);
                ps.setString(3, houseNumber);
                String ppText = postmanPathTextField.getText();
                ps.setInt(4, ppText.isEmpty()? Integer.MAX_VALUE: Integer.parseInt(ppText));
                ps.setString(5, note.isEmpty()? null : note);
                ps.setInt(6, s.getId());
                ps.executeUpdate();
                if(mc.isShowLocationsSelected()) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        int id = rs.getInt(1);
                        mc.addLocationToPane(houseNumber, pX, pY, id + "", note);
                    }
                }
                ps.clearParameters();
                sendMessage("Lokacija >> " + houseNumber + " << uspešno dodana.", MainController.MessageType.INFORMATION);
            } catch (SQLException ex) {
                sendMessage("Greška pri dodavanju lokacije >> " + houseNumber + " <<.\nError: " + ex.getMessage(), MainController.MessageType.ERROR);
            }
        }
    }

    private boolean validateFields(final Street s, final String x, final String y, final String houseNumber) {
        boolean valid;
        valid = true;
        if (s == null) {
            sendMessage("Ulica nije izabrana.", MainController.MessageType.ERROR);
            valid = false;
        }
        if (x.isEmpty() || y.isEmpty()) {
            sendMessage("Koordinate nisu definisane.", MainController.MessageType.ERROR);
            valid = false;
        }
        if (houseNumber.isEmpty()) {
            sendMessage("Broj nije odabran.", MainController.MessageType.ERROR);
            valid = false;
        }
        return valid;
    }

    private void sendMessage(final String message, MainController.MessageType type) {
        MainController.getInstance().showMessage(TITLE, message, type);
    }

    public static final String TITLE = "Dodaj adresu.";

}
