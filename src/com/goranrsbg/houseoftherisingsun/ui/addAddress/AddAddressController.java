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
package com.goranrsbg.houseoftherisingsun.ui.addAddress;

import com.goranrsbg.houseoftherisingsun.database.DBConnector;
import com.goranrsbg.houseoftherisingsun.ui.main.MainController;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextFormatter;

/**
 *
 * @author Goran
 */
public class AddAddressController implements Initializable {
    
    private static final String TITLE = "Dodaj adresu";
    
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
    private JFXComboBox<String> streetCombo;
    
    List<String> streets;
    
    public AddAddressController() {
        db = DBConnector.getInstance();
        streets = new ArrayList<>();
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
                    mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude realan broj.");
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
                    mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude realan broj.");
                }
            }
            return t;
        });
        
        xTextField.setTextFormatter(formatterX);
        yTextField.setTextFormatter(formatterY);

        TextFormatter<String> formatterBr = new TextFormatter<>((t) -> {
            if(t.getText().contains(" ")) {
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja ne sme da sadrži razmak.");
            } else if(t.getControlNewText().length() > 10){
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude do 10 znakova.");
            }
            return t;
        });
        TextFormatter<String> formatterNote = new TextFormatter<>((t) -> {
            if(t.getControlNewText().length() > 512){
                t = null;
                mc.notifyWithMsg(TITLE, "Vrednost polja mora da bude do 512 znakova.");
            }
            return t;
        });
        
        brTextField.setTextFormatter(formatterBr);
        noteAreaField.setTextFormatter(formatterNote);
        
        comboBoxAddStreets();
    }
    
    public void comboBoxAddStreets() {
        int mapId = mc.getCurrentMap().getID();
        ResultSet streetsRS = db.executeSelectStreets(mapId);
        streets.clear();
        try {
            while(streetsRS.next()) {
                streets.add(streetsRS.getInt(1) + " ~ " + streetsRS.getString(2));
            }
            streetsRS.close();
        } catch (SQLException ex) {
            mc.notifyWithMsg(TITLE, "Rezultat upita sa greškom: " + ex.getErrorCode());
        }
        if(!streets.isEmpty()) {
            streetCombo.setItems(FXCollections.observableList(streets));
        }
    }

    @FXML
    private void saveButtonAction(ActionEvent event) {
    }

}
